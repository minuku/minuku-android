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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import labelingStudy.nctu.minuku.Data.DBHelper;
import labelingStudy.nctu.minuku.Data.DataHandler;
import labelingStudy.nctu.minuku.R;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.model.MinukuStreamSnapshot;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minukucore.event.IsDataExpectedEvent;
import labelingStudy.nctu.minukucore.event.NoDataChangeEvent;
import labelingStudy.nctu.minukucore.event.StateChangeEvent;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.manager.StreamManager;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.model.StreamSnapshot;
import labelingStudy.nctu.minukucore.stream.Stream;
import labelingStudy.nctu.minukucore.streamgenerator.StreamGenerator;

/**
 * Created by Neeraj Kumar on 7/17/16.
 *
 * The MinukuStreamManager class implements {@link StreamManager} and runs as a service within
 * the application context. It maintains a list of all the Streams and StreamGenerators registered
 * within the application and is responsible for trigerring the
 * {@link StreamGenerator#updateStream() updateStream} method of the StreamManager class after
 * every {@link StreamGenerator#getUpdateFrequency() updateFrequency}.
 *
 * This depends on a service to call it's updateStreamGenerators method.
 */
public class MinukuStreamManager implements StreamManager {

    private final String TAG = "MinukuStreamManager";

    protected Map<Class, Stream> mStreamMap;
    protected Map<Stream.StreamType, List<Stream<? extends DataRecord>>> mStreamTypeStreamMap;
    protected Map<Class, StreamGenerator> mRegisteredStreamGenerators;

    private ActivityRecognitionDataRecord mActivityRecognitionDataRecord;
    private TransportationModeDataRecord mTransportationModeDataRecord;
    private LocationDataRecord mLocationDataRecord;

    private static int sCounter = 0;

    private Handler mHandler = new Handler();

    private static MinukuStreamManager sInstance;

    private MinukuStreamManager() throws Exception {
        mStreamMap = new HashMap<>();
        mStreamTypeStreamMap = new HashMap<>();
        mRegisteredStreamGenerators = new HashMap<>();
    }

    public static MinukuStreamManager getInstance() {
        if (MinukuStreamManager.sInstance == null) {
            try {
                MinukuStreamManager.sInstance = new MinukuStreamManager();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return MinukuStreamManager.sInstance;
    }

    /**
     * Maintaining all StreamGenerators and trigger their updateStream if it is time to update
     */
    public void updateStreamGenerators() {
        for (StreamGenerator streamGenerator : mRegisteredStreamGenerators.values()) {
            Log.d(TAG, "Stream generator : " + streamGenerator.getClass() + " \n" +
                    "Update frequency: " + streamGenerator.getUpdateFrequency() + "\n" +
                    "Counter: " + sCounter);
            if (streamGenerator.getUpdateFrequency() == -1) {
                continue;
            }
            if (sCounter % streamGenerator.getUpdateFrequency() == 0) {
                Log.d(TAG, "Calling update stream generator for " + streamGenerator.getClass());
                streamGenerator.updateStream();
            }
        }
        sCounter++;
    }


    @Override
    public List<Stream> getAllStreams() {
        return new LinkedList<>(mStreamMap.values());
    }

    /**
     * Register the given StreamGenerator so MinukuStreamManager can maintain it
     */
    @Override
    public <T extends DataRecord> void register(Stream stream,
                                                Class<T> clazz,
                                                StreamGenerator streamGenerator)
            throws StreamNotFoundException, StreamAlreadyExistsException {
        if (mStreamMap.containsKey(clazz)) {
            throw new StreamAlreadyExistsException();
        }
        for (Object dependsOnClass:stream.dependsOnDataRecordType()) {
            if (!mStreamMap.containsKey(dependsOnClass)) {
                Log.e(TAG, "Stream not found : " + dependsOnClass.toString());
                throw new StreamNotFoundException();
            }
        }
        mStreamMap.put(clazz, stream);
        mRegisteredStreamGenerators.put(clazz, streamGenerator);
        streamGenerator.onStreamRegistration();
        Log.d(TAG, "Registered a new stream generator for " + clazz);
    }

    /**
     * Remove  the required StreamGenerator from MinukuStreamManager
     */
    @Override
    public void unregister(Stream stream, StreamGenerator streamGenerator)
            throws StreamNotFoundException {
        Class classType = stream.getCurrentValue().getClass();
        if (!mStreamMap.containsKey(classType)) {
            throw new StreamNotFoundException();
        }
        mStreamMap.remove(stream);
        mRegisteredStreamGenerators.remove(streamGenerator);
    }

    /**
     * Get the corresponding Stream of the given dataRecord
     */
    @Override
    public <T extends DataRecord> Stream<T> getStreamFor(Class<T> clazz)
            throws StreamNotFoundException {
        if (mStreamMap.containsKey(clazz)) {
            return mStreamMap.get(clazz);
        } else {
            throw new StreamNotFoundException();
        }
    }

    @Override
    @Subscribe
    public void handleStateChangeEvent(StateChangeEvent aStateChangeEvent) {
        MinukuSituationManager.getInstance().onStateChange(getStreamSnapshot(),
                aStateChangeEvent);
    }

    @Override
    @Subscribe
    public void handleNoDataChangeEvent(NoDataChangeEvent aNoDataChangeEvent) {
        MinukuSituationManager.getInstance().onNoDataChange(getStreamSnapshot(),
                aNoDataChangeEvent);
    }

    @Override
    @Subscribe
    public void handleIsDataExpectedEvent(IsDataExpectedEvent isDataExpectedEvent) {
        MinukuSituationManager.getInstance().onIsDataExpected(getStreamSnapshot(),
                isDataExpectedEvent);
    }

    @Override
    public List<Stream<? extends DataRecord>> getStreams(Stream.StreamType streamType) {
        return mStreamTypeStreamMap.get(streamType);
    }

    @Override
    public <T extends DataRecord> StreamGenerator<T> getStreamGeneratorFor(Class<T> clazz)
            throws StreamNotFoundException {
        if (mRegisteredStreamGenerators.containsKey(clazz)) {
            return mRegisteredStreamGenerators.get(clazz);
        } else {
            throw new StreamNotFoundException();
        }
    }

    private StreamSnapshot getStreamSnapshot() {
        Map<Class<? extends DataRecord>, List<? extends DataRecord>> streamSnapshotData =
                new HashMap<>();
        for (Map.Entry<Class, Stream> entry: mStreamMap.entrySet()) {
            List list = createListOfType(entry.getKey());
            list.add(entry.getValue().getCurrentValue());
            list.add(entry.getValue().getPreviousValue());
            streamSnapshotData.put(entry.getKey(), list);
        }
        return new MinukuStreamSnapshot(streamSnapshotData);
    }

    private static <T extends DataRecord>  List<T> createListOfType(Class<T> type) {
        return new ArrayList<T>();
    }

    public void setActivityRecognitionDataRecord(ActivityRecognitionDataRecord activityRecognitionDataRecord) {
        this.mActivityRecognitionDataRecord = activityRecognitionDataRecord;
    }

    public ActivityRecognitionDataRecord getActivityRecognitionDataRecord() {
        return mActivityRecognitionDataRecord;
    }


    public void setTransportationModeDataRecord(TransportationModeDataRecord transportationModeDataRecord, final Context context, SharedPreferences sharedPrefs) {

        Log.d(TAG, "[test triggering] incoming transportation: " + transportationModeDataRecord.getConfirmedActivityString());

        Boolean addSessionFlag = false;

        if (mTransportationModeDataRecord == null) {
            //Init mTransportationModeDataRecord
            mTransportationModeDataRecord = new TransportationModeDataRecord(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA);
            Log.d(TAG, "[test triggering] test trip original null updated to " + this.mTransportationModeDataRecord.getConfirmedActivityString());

        } else {

            Log.d(TAG, "[test triggering] NEW: " + transportationModeDataRecord.getConfirmedActivityString() + " vs OLD:" + mTransportationModeDataRecord.getConfirmedActivityString());

            String currentWork = context.getResources().getString(R.string.current_task);

            Log.d(TAG,"in setTransportationModeDataRecord, currentWork is " +currentWork);

            // in PART mode, the Session is controlled by the user, so no need to detect transportation by the app
            if (!currentWork.equals("PART")) {

                // if the current activity is different from the latest one, should
                if (!mTransportationModeDataRecord.getConfirmedActivityString().equals(transportationModeDataRecord.getConfirmedActivityString())) {

                    Log.d(TAG, "[test triggering] test trip: the new acitivty is different from the previous!");

                    int sessionCount = SessionManager.getNumOfSession();

                    // if there is no session in the database and the new transportation is neither static nor NA, insert a session
                    if (sessionCount == 0
                                && !transportationModeDataRecord.getConfirmedActivityString().
                            equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA)) {

                        Log.d(TAG, "[test triggering] addSessionFlag = true there's no session in the db");
                        addSessionFlag = true;
                    } else if (sessionCount > 0) {  // there are sessions in the database, so the transportation given as the parameter isn't for the first session

                        // TODO: must test
                        // get the ongoingSession
                        Session ongoingSession = SessionManager.getOngoingSession();

                        int sessionIdOfLastSession = ongoingSession.getId();

                        Log.d(TAG, "[test triggering] session " + sessionIdOfLastSession +
                                " with annotation string " + ongoingSession.getAnnotationsSet().toString() +
                                " end time " + ongoingSession.getEndTime() +
                                " startTime " + ongoingSession.getStartTime());

                        if (!mTransportationModeDataRecord.getConfirmedActivityString().
                                equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA)) {

                            boolean isEmptyOngoingSession = SessionManager.isSessionEmptyOngoing(sessionIdOfLastSession);
                            CSVHelper.storeToCSV(CSVHelper.CSV_ESM, "not emptySessionOn ? " + (!isEmptyOngoingSession));

                            // if the ongoingSession isn't a emptyOngoingSession, stop the ongoingSession
                            if (!isEmptyOngoingSession) {
                                addSessionFlag = true;
                                sharedPrefs.edit().putInt("ongoingSessionid", -1).apply();
                                stopOngoingSession(ongoingSession, transportationModeDataRecord.getConfirmedActivityString());
                            }
                        }
                    }

                    Log.d(TAG, "[test triggering] addSessionFlag : " + addSessionFlag);
                    Log.d(TAG, "[test triggering] is NA ? : " + !transportationModeDataRecord.getConfirmedActivityString().
                            equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA));

                    CSVHelper.storeToCSV(CSVHelper.CSV_ESM, " addSessionFlag ? " + addSessionFlag);
                    CSVHelper.storeToCSV(CSVHelper.CSV_ESM, " tranp not NA ? " + (!transportationModeDataRecord.getConfirmedActivityString().
                            equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA)));

                    // if we need to add a session
                    if (addSessionFlag) {

                        Log.d(TAG, "[test triggering] we should add session " + ((int) sessionCount + 1));

                        Session lastSession = SessionManager.getLastSession();

                        int sessionIdOfLastSession = lastSession.getId();
                        boolean isEmptyOngoingSession = SessionManager.isSessionEmptyOngoing(sessionIdOfLastSession);

                        Log.d(TAG, "[test triggering] is CAR ? " + (currentWork.equals("CAR")));
                        Log.d(TAG, "[test triggering] is emptySessionOn ? " + isEmptyOngoingSession);
                        Log.d(TAG, "[test triggering] is CAR & emptySessionOn ? " + (currentWork.equals("CAR") && isEmptyOngoingSession));

                        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, " is CAR ? " +(currentWork.equals("CAR")));
                        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, " is emptySessionOn ? " +isEmptyOngoingSession);
                        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, " is CAR & emptySessionOn ? " +(currentWork.equals("CAR") && isEmptyOngoingSession));

                        //if there is an emptySession, update it to the next different system detected transportationMode
                        if (currentWork.equals("CAR") && isEmptyOngoingSession) {

                            Annotation annotation = new Annotation();
                            annotation.setContent(transportationModeDataRecord.getConfirmedActivityString());
                            annotation.addTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY);
                            lastSession.addAnnotation(annotation);

                            DataHandler.updateSession(lastSession.getId(), lastSession.getAnnotationsSet());

                            SessionManager.getEmptyOngoingSessionIdList().remove(Integer.valueOf(lastSession.getId()));

                            //to end a session (the previous is moving)
                            //we first need to check whether the previous is a transportation
                        } else {

                            //insert into the session table;
                            int sessionId = (int) sessionCount + 1;
                            Session session = new Session(sessionId);

                            session.setCreatedTime(ScheduleAndSampleManager.getCurrentTimeInMillis());

                            //set the current time for default
                            session.setStartTime(ScheduleAndSampleManager.getCurrentTimeInMillis());

                            //query suspect start time PREVIOUS id
                            ArrayList<String> firstDiffSuspectedStartTransportation = DBHelper.queryLastTransportationWithSuspectedStartTransportation(transportationModeDataRecord.getConfirmedActivityString());
                            if (firstDiffSuspectedStartTransportation.size() > 0) {

                                String diff1stSuspectedStartTransportation_ID = firstDiffSuspectedStartTransportation.get(DBHelper.COL_INDEX_RECORD_ID);

                                //query suspect start time id (the next one from the above data)
                                ArrayList<String> suspectedStartTransportation = DBHelper.queryNextData(DBHelper.TRANSPORTATION_MODE_TABLE, Integer.valueOf(diff1stSuspectedStartTransportation_ID));

                                if (suspectedStartTransportation.size() > 0) {

                                    String suspectTime = suspectedStartTransportation.get(DBHelper.COL_INDEX_SUSPECTED_TRANSPORTATION_TIME);

                                    session.setStartTime(Long.valueOf(suspectTime));
                                }
                            }

                            CSVHelper.storeToCSV(CSVHelper.CSV_ESM, " Session StartTime : " +ScheduleAndSampleManager.getTimeString(session.getStartTime()));

                            Annotation annotation = new Annotation();
                            annotation.setContent(transportationModeDataRecord.getConfirmedActivityString());
                            annotation.addTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY);
                            session.addAnnotation(annotation);
                            session.setUserPressOrNot(false);
                            session.setModified(false);
                            session.setIsSent(Constants.SESSION_SHOULDNT_BEEN_SENT_FLAG);
                            session.setType(Constants.SESSION_TYPE_DETECTED_BY_SYSTEM);

                            CSVHelper.storeToCSV(CSVHelper.CSV_ESM, " insert the session is with annotation : " +session.getAnnotationsSet().toJSONObject().toString());

                            Log.d(TAG, "[test triggering] insert the session is with annotation " + session.getAnnotationsSet().toJSONObject().toString());

                            String beforeSendESM = "Preparing to send ESM";
                            CSVHelper.storeToCSV(CSVHelper.CSV_ESM, beforeSendESM);

                            SessionManager.startNewSession(session);

                            CSVHelper.storeToCSV(CSVHelper.CSV_ESM, " after startNewSession ");

                            sharedPrefs.edit().putInt("ongoingSessionid", session.getId()).apply();
                        }

                        if (currentWork.equals("ESM")) {

                            //after starting a trip(session), send a notification to the user
                            sendNotification(context);

                            String afterSendESM = "After sending ESM";
                            CSVHelper.storeToCSV(CSVHelper.CSV_ESM, afterSendESM);

                            Log.d(TAG, "[test triggering] ESM Notification");

                        } else if (currentWork.equals("CAR")) {

                            //CAR need to be in the start of the session, after a threshold of the time, send the notification to remind the user

                            // set it earlier than other conditions due to the CountDownTimer
                            this.mTransportationModeDataRecord = transportationModeDataRecord;

                            SessionManager.sSessionIsWaiting = true;

                            //wait for a minute to the user
                            mHandler.postDelayed(new Runnable() {

                                @Override
                                public void run() {

                                    //detect the user has pressed the current trip(Session) or not.
                                    if (SessionManager.getOngoingSessionIdList().size() != 0) {

                                        int ongoingSessionid = SessionManager.getOngoingSessionIdList().get(0);

                                        Session ongoingSession = SessionManager.getSession(ongoingSessionid);

                                        //if the user hasn't pressed the current trip(Session); after ending a trip(session), send a notification to the user
                                        if (!ongoingSession.isUserPress()) {

                                            sendNotification(context);

                                            String afterSendCAR = "After sending CAR";
                                            CSVHelper.storeToCSV(CSVHelper.CSV_CAR, afterSendCAR);

                                            Log.d(TAG, "[test triggering] CAR Notification");
                                        } else {

                                            //recording
                                            String checkCAR = "the CAR record has been checkpointed by the user";
                                            CSVHelper.storeToCSV(CSVHelper.CSV_CAR, checkCAR);

                                            Log.d(TAG, "[test triggering] CAR check");
                                        }

                                        //the ongoing session might be removed because of the empty ongoing one.
                                    }else{

                                        String checkCAR = "the ongoing session is removed, assumed it was checkpoioned";
                                        CSVHelper.storeToCSV(CSVHelper.CSV_CAR, checkCAR);

                                        Log.d(TAG, "[test triggering] " + checkCAR);
                                    }

                                    SessionManager.sSessionIsWaiting = false;

                                }
                            }, Constants.MILLISECONDS_PER_MINUTE);
                        }
                    }
                }
            }

            mTransportationModeDataRecord = transportationModeDataRecord;
        }
    }

    private void sendNotification(Context context) {

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String notificationText = "您有新的移動紀錄";
        Notification notification = getNotification(notificationText, context);

        mNotificationManager.notify(MinukuNotificationManager.sReminderNotificationID, notification);
    }

    private Notification getNotification(String text, Context context) {

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle("LS");
        bigTextStyle.bigText(text);

        //launch the Timeline Page, check the action in the app's AndroidManifest.xml
        Intent resultIntent = new Intent("app.intent.action.Launch"); //MinukuNotificationManager.toTimeline;
        PendingIntent pending = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(context)
                .setContentTitle(Constants.APP_FULL_NAME)
                .setContentText(text)
                .setContentIntent(pending)
                .setStyle(bigTextStyle)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noti.setChannelId(Constants.SURVEY_CHANNEL_ID);
        }

        Notification note = noti.setSmallIcon(MinukuNotificationManager.getNotificationIcon(noti)).build();

        return note;
    }

    public TransportationModeDataRecord getTransportationModeDataRecord() {
        return mTransportationModeDataRecord;
    }

    public void setLocationDataRecord(LocationDataRecord locationDataRecord) {
        this.mLocationDataRecord = locationDataRecord;
    }

    public LocationDataRecord getLocationDataRecord() {
        return mLocationDataRecord;
    }

    /**
     * End the ongoing session by removing it from ongoingSessionList and set its end time accurately.
     * If its confirmedActivity has once been recorded with a suspect-stop time, set the end time to its suspect-stop time since it is when the ongoingSession actually ended
     * @param ongoingSession
     * @param confirmedActivity
     */
    public void stopOngoingSession(Session ongoingSession, String confirmedActivity) {
        // update end time of ongoingSession
        long endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
        ongoingSession.setEndTime(endTime);

        // set the end time of the session to the suspect stop time if necessary.
        ArrayList<String> firstDiffSuspectStopTransportation =
                DBHelper.queryTransportationWithSuspectedStopTransportation(confirmedActivity);
        if (firstDiffSuspectStopTransportation.size() > 0) {

            String diff1stSuspectedStopTransportationId = firstDiffSuspectStopTransportation.get(DBHelper.COL_INDEX_RECORD_ID);

            //query suspect start time id (the next one from the above data)
            ArrayList<String> suspectedStopTransportation = DBHelper.queryNextData(DBHelper.TRANSPORTATION_MODE_TABLE, Integer.valueOf(diff1stSuspectedStopTransportationId));

            if (suspectedStopTransportation.size() > 0) {

                String suspectTime = suspectedStopTransportation.get(DBHelper.COL_INDEX_SUSPECTED_TRANSPORTATION_TIME);

                ongoingSession.setEndTime(Long.valueOf(suspectTime));
            }
        }

        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, " lastSession EndTime : " + ScheduleAndSampleManager.getTimeString(ongoingSession.getEndTime()));

        //end the current session
        SessionManager.endCurSession(ongoingSession);
    }

}
