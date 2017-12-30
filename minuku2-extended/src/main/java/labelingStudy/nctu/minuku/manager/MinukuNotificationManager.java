/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku.manager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import labelingStudy.nctu.minuku.R;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.NotificationDAO;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.event.NotificationClickedEvent;
import labelingStudy.nctu.minukucore.event.ShowNotificationEvent;
import labelingStudy.nctu.minukucore.manager.NotificationManager;

/**
 * Created by neerajkumar on 8/3/16.
 */
public class MinukuNotificationManager extends Service implements NotificationManager {

    private static AtomicInteger CURRENT_NOTIFICATION_ID = new AtomicInteger(Integer.MIN_VALUE + 5);
    private Map<Integer, ShowNotificationEvent> registeredNotifications;
    private android.app.NotificationManager mNotificationManager;
    //private Map<String, ShowNotificationEvent> categorizedNotificationMap;
    private NotificationDAO mDAO;

    public MinukuNotificationManager() {
        Log.d(TAG, "Started minuku notification manager");
        registeredNotifications = new ConcurrentHashMap<>();
        //categorizedNotificationMap = new HashMap<>();
        mDAO = MinukuDAOManager.getInstance().getDaoFor(ShowNotificationEvent.class);
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "OnStartCommand");

        if(mNotificationManager == null) {
            mNotificationManager = (android.app.NotificationManager) getSystemService(
                    Service.NOTIFICATION_SERVICE);
        }

        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + Constants.PROMPT_SERVICE_REPEAT_MILLISECONDS,
                PendingIntent.getService(this, 0, new Intent(this, MinukuNotificationManager.class), 0)
        );

        Notification note  = new Notification.Builder(getBaseContext())
                .setContentTitle(Constants.APP_NAME)
                .setContentText(Constants.RUNNING_APP_DECLARATION)
                .setSmallIcon(R.drawable.self_reflection)
                .setAutoCancel(false)
                .build();
        note.flags |= Notification.FLAG_NO_CLEAR;
        startForeground( 42, note );

        checkRegisteredNotifications();


        return START_STICKY_COMPATIBILITY;
    }

    private void checkRegisteredNotifications() {

        Log.d(TAG, "Checking for registered notificaitons.");

        Log.d(TAG, "Number of registered notifications: " + registeredNotifications.size());
        for(Map.Entry<Integer, ShowNotificationEvent> entry: registeredNotifications.entrySet()) {
            ShowNotificationEvent notification = entry.getValue();
            Integer notificationID = entry.getKey();
            Integer counter = entry.getValue().counter;
            Log.d(TAG, "Counter : " + counter);
            Log.d(TAG, "Notification id " +  notificationID + "    " + notification.getExpirationTimeSeconds());
            if(counter == null) {
                Log.d(TAG, "The notification with " + notificationID + " is null.");
                /* TODO(neerajkumar): This is happening due to concurrent modification. Fix it */
                continue;
            }

            if(counter == notification.getExpirationTimeSeconds()/60) {
                Log.d(TAG, "Counter for " + notification.getTitle() + " is matching.");

                switch (notification.getExpirationAction()) {
                    case DISMISS:
                        Log.d(TAG, "Dismissing " + notification.getTitle());
                        mNotificationManager.cancel(entry.getKey());
                        unregisterNotification(notificationID);
                        Log.d(TAG, "Number of registered notifications after dismissing: " + registeredNotifications.size());
                        break;
                    case ALERT_AGAIN:
                        /**
                         * TODO(neerajkumar): Find a way to see if a notification was dismissed by
                         * the user and unregister it, if it was.
                         */
                        Log.d(TAG, "Alerting again " + notification.getTitle());
                        mNotificationManager.cancel(entry.getKey());
                        mNotificationManager.notify(notificationID,
                                buildNotificationForNotificationEvent(notification, entry.getKey()));
                        notification.incrementExpirationCount();
                        break;
                    case KEEP_SHOWING_WITHOUT_ALERT:
                        /**
                         * TODO(neerajkumar): Find a way to see if a notification was dismissed by
                         * the user and unregister it, if it was.
                         */
                        Log.d(TAG, "Ignoring " + notification.getTitle());
                        notification.incrementExpirationCount();
                        break;
                    default:
                        break;
                }
                counter = 0;
            }
            counter++;
            entry.getValue().counter = counter;
        }

    }

    private Notification buildNotificationForNotificationEvent(
            ShowNotificationEvent aShowNotificationEvent, Integer id) {

        if(aShowNotificationEvent.getCreationTimeMs() == 0) {
            aShowNotificationEvent.setCreationTimeMs(new Date().getTime());
        }

        Intent launchIntent = new Intent(this, aShowNotificationEvent.getViewToShow());
        for(Map.Entry<String, String> entry: aShowNotificationEvent.getParams().entrySet()) {
            launchIntent.putExtra(entry.getKey(), entry.getValue());
        }
        launchIntent.putExtra(Constants.TAPPED_NOTIFICATION_ID_KEY, id.toString());
        //adding extra stuff to signify from notification code
        //launchIntent.putExtra("STARTED_FROM", "notification");

        //keep this
        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(aShowNotificationEvent.getViewToShow());
        stackBuilder.addNextIntent(launchIntent);*/

        //keep this
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //keep this
        /*PendingIntent pIntent = TaskStackBuilder.create(this)
                .addParentStack(aShowNotificationEvent.getViewToShow())
                .addNextIntent(launchIntent)
                .getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);*/
//                PendingIntent.getActivity(this,
//                0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        Notification n  = new Notification.Builder(this)
                .setContentTitle(aShowNotificationEvent.getTitle())
                .setContentIntent(pIntent)
                .setContentText(aShowNotificationEvent.getMessage())
                .setSmallIcon(aShowNotificationEvent.getIconID())
                .setAutoCancel(true)
                .build();
        n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    private static final String TAG = "MinNotificationManager";


    @Subscribe
    @Override
    public void handleShowNotificationEvent(ShowNotificationEvent aShowNotificationEvent) {
        Log.d(TAG, "Handling notification event for notification from : " +
                aShowNotificationEvent.getParams().get(Constants.BUNDLE_KEY_FOR_NOTIFICATION_SOURCE));
        if(aShowNotificationEvent.getCategory() != null) {
            if(ifSameCategoryNotificationExists(aShowNotificationEvent)) {
                Log.d(TAG, "There is already a notification with the same category in map.");
                ShowNotificationEvent previousNotification = getSameCategoryNotification(aShowNotificationEvent);
                Log.d(TAG, "Old notification is: Category" + previousNotification.getCategory() +
                        "  Title:  " + previousNotification.getTitle() + " exp count: " +
                        previousNotification.getExpirationCount());
                if(previousNotification.getExpirationCount() > 0) {
                    Log.d(TAG, "The old notification already in the map seemed to have expired. " +
                            "Will clean it up. You should see a new notification.");
                    // If a previously existing notification has expired, then regardless of the
                    // expiration mechanism of such notification, remove it from the map, push
                    // that information to DAO and add the current notification to the map.
                    Integer id = null;
                    if((id = getIdForNotification(previousNotification)) != null) {
                        unregisterNotification(id);
                    }
                    /*try {
                        Log.d(TAG, "Adding previous notification information.");
                        mDAO.add(previousNotification);
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }*/
                } else {
                    Log.d(TAG, "The old notification already in the map has not  " +
                            " expired. New notification ignored..");
                    return;
                }
            }
        } else {
            Log.d(TAG, "The notification category was null, starting a basic notifiction.");
        }

        Integer notificationID = CURRENT_NOTIFICATION_ID.incrementAndGet();
        Log.d(TAG, "Getting new notification ID: " + notificationID + " with source: " +
                aShowNotificationEvent.getParams().get(Constants.BUNDLE_KEY_FOR_NOTIFICATION_SOURCE));
        Notification n = buildNotificationForNotificationEvent(aShowNotificationEvent,
                notificationID);
        registeredNotifications.put(notificationID, aShowNotificationEvent);
        mNotificationManager.notify(notificationID, n);
    }

    @Subscribe
    public void handleNotificationClickEvent(NotificationClickedEvent notificationClickedEvent) {
        Log.d(TAG, "Triggered notification click handler for notification ID"  +
                notificationClickedEvent.getNotificationId());
        Integer notificationId = getNotificationIdIfValid(
                notificationClickedEvent.getNotificationId());
        Log.d(TAG, "Valid notification id retrieved is " + notificationId);
        //unregister on click only when it is mood, else let the counter be
        if(registeredNotifications.get(notificationId).getCategory() == "MOOD_REPORT_NOTIF")
            unregisterNotification(notificationId);
    }


    private Integer getNotificationIdIfValid(String aNotificationId) {
        try {
            Integer notificationId = Integer.parseInt(aNotificationId);
            if(registeredNotifications.containsKey(notificationId)) {
                return notificationId;
            } else {
                return null;
            }
        } catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    /**
     *
     * @param aNotificaitonId
     * @return if the notification associated with the notificationID passed in was successfully
     *          unregistered.
     */
    private boolean unregisterNotification(Integer aNotificaitonId) {
        if(registeredNotifications.containsKey(aNotificaitonId)) {
            //get the NOTIFICATION using ID
            ShowNotificationEvent notification = registeredNotifications.get(aNotificaitonId);

            //Remove ID from registered notification map
            Log.d(TAG, "Removing notification: " + aNotificaitonId);
            registeredNotifications.remove(aNotificaitonId);
            Log.d(TAG, "Number of registered notifications in unreg method: " + registeredNotifications.size());

            //If notification is null, then return else you will get excepton when adding null to Firebase
            if(notification == null) {
                Log.d(TAG, "notification is null. Returning..");
                return true;
            }

            //set NOTIFICATION click time
            notification.setClickedTimeMs(new Date().getTime());
            try {
                //add NOTIFICATION to database
                Log.d(TAG, "adding the unregistered notification to database");
                if(mDAO == null) {
                    if (MinukuDAOManager.getInstance().getDaoFor(ShowNotificationEvent.class)
                            == null) {
                        MinukuDAOManager.getInstance().registerDaoFor(ShowNotificationEvent.class,
                                new NotificationDAO());
                    }
                    mDAO = MinukuDAOManager.getInstance().getDaoFor(ShowNotificationEvent.class);
                }
                mDAO.add(notification);
            } catch (DAOException e) {
                e.printStackTrace();
                Log.e(TAG, "Could not add notification info to DB");
            }

        }
        // Notification was already unregistered at some earlier time or was never registered.
        // This is not a failure case, hence we return true.
        return true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * (TODO:neerajkumar): Start using BiMaps
     */
    public Integer getIdForNotification(ShowNotificationEvent notification) {
        for(Map.Entry<Integer, ShowNotificationEvent> entry: registeredNotifications.entrySet()) {
            if(entry.getValue().equals(notification)) {
                return entry.getKey();
            }
        }
        Log.d(TAG, "Returning null object for notification");
        return null;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying service. Your state might be lost!");
    }

    private boolean ifSameCategoryNotificationExists(ShowNotificationEvent aShowNotificationEvent) {

        for(Map.Entry<Integer, ShowNotificationEvent> entry: registeredNotifications.entrySet()) {
            ShowNotificationEvent notificationEvent = entry.getValue();
            if(notificationEvent.getCategory().equals(aShowNotificationEvent.getCategory())) {
                return true;
            }
        }
        return false;
    }

    private ShowNotificationEvent getSameCategoryNotification(ShowNotificationEvent aShowNotificationEvent) {
        for(Map.Entry<Integer, ShowNotificationEvent> entry: registeredNotifications.entrySet()) {
            ShowNotificationEvent notificationEvent = entry.getValue();
            if(notificationEvent.getCategory().equals(aShowNotificationEvent.getCategory())) {
                return notificationEvent;
            }
        }
        return null;
    }
}
