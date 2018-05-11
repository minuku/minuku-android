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

package labelingStudy.nctu.minuku_2.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.manager.MobilityManager;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.Receiver.WifiReceiver;
import labelingStudy.nctu.minuku_2.Utils;
import labelingStudy.nctu.minuku_2.controller.Dispatch;
import labelingStudy.nctu.minuku_2.manager.InstanceManager;

public class BackgroundService extends Service {

    private static final String TAG = "BackgroundService";

    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    WifiReceiver mWifiReceiver;
    IntentFilter intentFilter;

    MinukuStreamManager streamManager;

    private ScheduledExecutorService mScheduledExecutorService;

    private int ongoingNotificationID = 42;
    private String ongoingNotificationText = Constants.RUNNING_APP_DECLARATION;

    NotificationManager mNotificationManager;

    public BackgroundService() {
        super();

        streamManager = MinukuStreamManager.getInstance();
        mScheduledExecutorService = Executors.newScheduledThreadPool(Constants.NOTIFICATION_UPDATE_THREAD_SIZE);

        //TODO recall it back
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        mWifiReceiver = new WifiReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String onStart = "BackGround, start service";
        CSVHelper.storeToCSV(CSVHelper.file_ESM, onStart);
        CSVHelper.storeToCSV(CSVHelper.file_CAR, onStart);

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        //TODO recall it back
        //make the WifiReceiver start sending data to the server.
        registerReceiver(mWifiReceiver, intentFilter);

        //building the ongoing notification to the foreground
        startForeground(ongoingNotificationID, getOngoingNotification(ongoingNotificationText));

        if(!InstanceManager.isInitialized()) {

            InstanceManager.getInstance(this);
            SessionManager.getInstance(this);
            MobilityManager.getInstance(this);

            updateNotificationAndStreamManagerThread();
        }

        // read test file
//        FileHelper fileHelper = FileHelper.getInstance(getApplicationContext());
//        FileHelper.readTestFile();

        return START_REDELIVER_INTENT; //START_STICKY_COMPATIBILITY;
    }

    private void updateNotificationAndStreamManagerThread(){

        mScheduledExecutorService.scheduleAtFixedRate(
                updateStreamManagerRunnable,
                10,
                Constants.STREAM_UPDATE_FREQUENCY,
                TimeUnit.SECONDS);
    }

    Runnable updateStreamManagerRunnable = new Runnable() {
        @Override
        public void run() {

            try {

                CSVHelper.storeToCSV(CSVHelper.file_CheckService_alive, "Background, service update, stream");

                streamManager.updateStreamGenerators();
            }catch (Exception e){

                CSVHelper.storeToCSV(CSVHelper.file_CheckService_alive, "Background, service update, stream, Exception");
                CSVHelper.storeToCSV(CSVHelper.file_CheckService_alive, Utils.getStackTrace(e));
            }
        }
    };

    private Notification getOngoingNotification(String text){

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(Constants.APP_NAME);
        bigTextStyle.bigText(text);

        Intent resultIntent = new Intent(this, Dispatch.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(this);

        return noti.setContentTitle(Constants.APP_NAME)
                .setContentText(text)
                .setStyle(bigTextStyle)
                .setSmallIcon(getNotificationIcon(noti))
                .setContentIntent(pending)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
    }

    private int getNotificationIcon(Notification.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            notificationBuilder.setColor(Color.TRANSPARENT);
            return R.drawable.muilab_icon_noti;

        }
        return R.drawable.muilab_icon;
    }

    @Override
    public void onDestroy() {

        String onDestroy = "BackGround, onDestroy";
        CSVHelper.storeToCSV(CSVHelper.file_ESM, onDestroy);
        CSVHelper.storeToCSV(CSVHelper.file_CAR, onDestroy);
        CSVHelper.storeToCSV(CSVHelper.file_CheckService_alive, onDestroy);

        Log.d(TAG, "Destroying service. Your state might be lost!");
    }

    @Override
    public void onTaskRemoved(Intent intent){
        super.onTaskRemoved(intent);

        String onTaskRemoved = "BackGround, onTaskRemoved";
        CSVHelper.storeToCSV(CSVHelper.file_CheckService_alive, onTaskRemoved);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
