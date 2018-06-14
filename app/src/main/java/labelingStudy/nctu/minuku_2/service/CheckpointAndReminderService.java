package labelingStudy.nctu.minuku_2.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.R;

/**
 * Created by Lawrence on 2017/10/9.
 */

public class CheckpointAndReminderService extends Service {

    private final String TAG = "CheckpointAndReminderService";

//    private static final String PACKAGE_DIRECTORY_PATH="/Android/data/labelingStudy.nctu.minuku_2/";

    private Context mContext;

    private ScheduledExecutorService mScheduledExecutorService;
    private Future<?> future;

    private int car_notifyID = 3;
    private int check_notifyID = 1;

    private CSVWriter csv_writer = null;

    private final String logfile = "CheckAndRemindDataRecord";
    private String NotificationText = "";

    public final int REFRESH_FREQUENCY = 10; //1s, 1000ms
    public final int BACKGROUND_RECORDING_INITIAL_DELAY = 0;

    private String lastConfirmedActivityType = "NA";

    Handler handler;

    public static boolean CheckpointOrNot;

    public boolean futureRunningornot;

    static int count = 0;

    private int checkpointCount;
    private int checking;

    private SharedPreferences sharedPrefs;

    public CheckpointAndReminderService(){

    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "onCreate");

        CheckpointOrNot = false;

        mContext = this;

        handler = new Handler();

        mScheduledExecutorService = Executors.newScheduledThreadPool(REFRESH_FREQUENCY);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        checking = 0;

        checkpointCount = sharedPrefs.getInt("checkpointCount", 0);

//        createCSV();

    }

    /*
    Runnable checkingRunnable = new Runnable() {
        @Override
        public void run() {
            checkInNoti();
            String currentConfirmedActivityType = TransportationModeStreamGenerator.toOtherClassDataRecord.getConfirmedActivityString();

            StoreToCSV(TransportationModeStreamGenerator.toOtherClassDataRecord.getCreationTime(),
                    lastConfirmedActivityType,
                    currentConfirmedActivityType,
                    checking,
                    CheckpointOrNot);
        }
    };*/


    Runnable waiting = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "count : "+ count);

            if(count>=60 || CheckpointOrNot==true){
                count = 0;
                handler.removeCallbacks(this);
            }
            count++;
            handler.postDelayed(this, 1000);
        }
    };

    private void waitingforUsersToCheckpoint(String currentConfirmedActivityType){

        //stop the runnable first
        future.cancel(true);

        count = 0;
        while(!(count>=60) && !CheckpointOrNot==true){
            Log.d(TAG, "count : "+ count);

            count++;

            try {
                Thread.sleep(1000); //TODO wait for the user press the button.

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        count = 0;

        checking = 1;

        checkpointCount++;
        sharedPrefs.edit().putInt("checkpointCount", checkpointCount).apply();

        if(CheckpointOrNot){
            lastConfirmedActivityType = "NA";
            CheckpointOrNot = false;
            checkpointCount--;
            sharedPrefs.edit().putInt("checkpointCount", checkpointCount).apply();

        }else{ //CheckpointOrNot = Not
            caReminder();
        }

//        StoreToCSV(TransportationModeStreamGenerator.toOtherClassDataRecord.getCreationTime(),
//                lastConfirmedActivityType,
//                currentConfirmedActivityType,
//                checking,
//                CheckpointOrNot);

        lastConfirmedActivityType = currentConfirmedActivityType;

        //continue the runnable
        future = mScheduledExecutorService.scheduleAtFixedRate(
                CARRunnable,
                BACKGROUND_RECORDING_INITIAL_DELAY,
                REFRESH_FREQUENCY,
                TimeUnit.SECONDS);

    }

    Runnable CARRunnable = new Runnable() {
        @Override
        public void run() {

            /*try{
//                String currentConfirmedActivityType = TransportationModeStreamGenerator.toOtherClassDataRecord.getConfirmedActivityString();
                String currentConfirmedActivityType = TransportationModeStreamGenerator.ConfirmedActvitiyString;

                Log.d(TAG, "currentConfirmedActivityType : "+ currentConfirmedActivityType);

                Log.d(TAG, "after waiting CARRunnable");

                if(!lastConfirmedActivityType.equals(currentConfirmedActivityType)) {
                    Log.d(TAG, "!lastConfirmedActivityType.equals(currentConfirmedActivityType)");

                    waitingforUsersToCheckpoint(currentConfirmedActivityType);
                }
            }catch (Exception e){
//                currentConfirmedActivityType = "NA";
                e.printStackTrace();
                Log.e(TAG, "exception", e);
            }

//            handler.postDelayed(this , 60 * 1000);

            //TODO comment it during testing.
//            checkInNoti();

            Log.d(TAG, "after checkInNoti");

            if(checking==1){
                checking = 0;
            }*/

        }
    };

    private void checkInNoti(){

        Log.d(TAG,"checkInNoti");

        try {

            String local_transportation = lastConfirmedActivityType;

//            Log.d(TAG,"sMostProbableActivity : "+ ActivityRecognitionStreamGenerator.getActivityNameFromType(ActivityRecognitionStreamGenerator.sMostProbableActivity.getType()));

            NotificationText = "Current Transportation Mode: " + local_transportation;
//                    + "\r\n" + "ActivityRecognition : "
//                    + ActivityRecognitionStreamGenerator.getActivityNameFromType(ActivityRecognitionStreamGenerator.sMostProbableActivity.getType());

            Log.d(TAG,"after NotificationText");

            NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);//Context.
            Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
            bigTextStyle.setBigContentTitle("CAR");
            bigTextStyle.bigText(NotificationText);

            Notification note = new Notification.Builder(mContext)
                    .setContentTitle(Constants.APP_NAME)
                    .setContentText(NotificationText)
//                .setContentIntent(pending)
                    .setStyle(bigTextStyle)
                    .setSmallIcon(R.drawable.self_reflection)
                    .setAutoCancel(true)
                    .build();

            mNotificationManager.notify(check_notifyID, note);
            note.flags = Notification.FLAG_AUTO_CANCEL;

        }catch(Exception e){
            e.printStackTrace();
        }

        /*
        StoreToCSV(TransportationModeStreamGenerator.toOtherClassDataRecord.getCreationTime(),
                lastConfirmedActivityType,
                lastConfirmedActivityType,
                checking,
                CheckpointOrNot);
                */

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Log.d(TAG, "future.isCancelled() : "+ future.isCancelled());

        future = mScheduledExecutorService.scheduleAtFixedRate(
                CARRunnable,
                BACKGROUND_RECORDING_INITIAL_DELAY,
                REFRESH_FREQUENCY,
                TimeUnit.SECONDS);

        /*mScheduledExecutorService.scheduleAtFixedRate(
                checkingRunnable,
                BACKGROUND_RECORDING_INITIAL_DELAY,
                REFRESH_FREQUENCY,
                TimeUnit.SECONDS);*/

        return START_REDELIVER_INTENT;
    }

    private void caReminder(){
        String notiText = "Please checkpoint.\t\t\t\t"+checkpointCount; //TODO change the format to custom view.

        Log.d(TAG,"caReminder");

        //TODO make a pendingIntent to record.xml
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction("open_timeline");
        PendingIntent pending = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);//Context.
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle("LS");
        bigTextStyle.bigText(notiText);

        Notification note = new Notification.Builder(mContext)
                .setContentTitle(Constants.APP_NAME)
                .setContentText(notiText)
                .setContentIntent(pending)
                .setStyle(bigTextStyle)
                .setSmallIcon(R.drawable.self_reflection)
                .setAutoCancel(true)
                .build();

        // using the same tag and Id causes the new notification to replace an existing one
        mNotificationManager.notify(car_notifyID, note); //String.valueOf(System.currentTimeMillis()),
        note.flags = Notification.FLAG_AUTO_CANCEL;
    }

    public void StoreToCSV(long timestamp, String lastConfirmedActivityType, String currentConfirmedActivityType, int checking, boolean CheckpointOrNot){

        Log.d(TAG,"StoreToCSV");

        String sFileName = logfile+".csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

//            data.add(new String[]{"timestamp","timeString","Latitude","Longitude","Accuracy"});
            String timeString = getTimeString(timestamp);

            data.add(new String[]{timeString,lastConfirmedActivityType,currentConfirmedActivityType,String.valueOf(checking),String.valueOf(CheckpointOrNot)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void createCSV(){
        String sFileName = logfile+".csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            data.add(new String[]{"timestamp","timeString","Latitude","Longitude","Accuracy"});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getTimeString(long time){
        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        String currentTimeString = sdf_now.format(time);

        return currentTimeString;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying service.");
    }

}
