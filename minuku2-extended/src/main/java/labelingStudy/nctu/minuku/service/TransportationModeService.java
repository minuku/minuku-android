package labelingStudy.nctu.minuku.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;

import static labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator.getLocalRecordPool;

/**
 * Created by Lawrence on 2017/7/19.
 */

public class TransportationModeService extends Service {

    public String TAG = "TransportationModeService";

    public static TransportationModeStreamGenerator transportationModeStreamGenerator;

    /**ContextSourceType**/
    public static final int CONTEXT_SOURCE_TRANSPORTATION = 0;
    public static final int CONTEXT_SOURCE_DETECTION_STATE = 1;

    public static final String STRING_CONTEXT_SOURCE_TRANSPORTATION = "Transportation";
    public static final String STRING_CONTEXT_SOURCE_DETECTION_STATE = "DetectionState";

    /**Table Name**/
    public static final String RECORD_TABLE_NAME_TRANSPORTATION = "Record_Table_Transportation";

    public static final int STATE_STATIC = 0;
    public static final int STATE_SUSPECTING_START = 1;
    public static final int STATE_CONFIRMED = 2;
    public static final int STATE_SUSPECTING_STOP = 3;

    //
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_IN_VEHICLE = (float) 0.6;
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_ON_FOOT = (float)0.6;
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_ON_BICYCLE =(float) 0.6;
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_IN_VEHICLE = (float)0.2;
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_FOOT = (float)0.1;
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_BICYCLE =(float) 0.2;

    /**label **/
    public static final String STRING_DETECTED_ACTIVITY_IN_VEHICLE = "in_vehicle";
    public static final String STRING_DETECTED_ACTIVITY_ON_FOOT = "on_foot";
    public static final String STRING_DETECTED_ACTIVITY_WALKING = "walking";
    public static final String STRING_DETECTED_ACTIVITY_RUNNING = "running";
    public static final String STRING_DETECTED_ACTIVITY_TILTING = "tilting";
    public static final String STRING_DETECTED_ACTIVITY_STILL = "still";
    public static final String STRING_DETECTED_ACTIVITY_ON_BICYCLE = "on_bicycle";
    public static final String STRING_DETECTED_ACTIVITY_UNKNOWN = "unknown";
    public static final String STRING_DETECTED_ACTIVITY_NA = "NA";

    public static final String TRANSPORTATION_MODE_NAME_IN_VEHICLE = STRING_DETECTED_ACTIVITY_IN_VEHICLE;
    public static final String TRANSPORTATION_MODE_NAME_ON_FOOT = STRING_DETECTED_ACTIVITY_ON_FOOT;
    public static final String TRANSPORTATION_MODE_NAME_ON_BICYCLE = STRING_DETECTED_ACTIVITY_ON_BICYCLE;
    public static final String TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION = "static";
    public static final String TRANSPORTATION_MODE_NAME_NA = "NA";

    private static final long WINDOW_LENGTH_START_ACTIVITY_DEFAULT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_START_ACTIVITY_IN_VEHICLE = 10 * Constants.MILLISECONDS_PER_SECOND; //TODO origin為20s
    private static final long WINDOW_LENGTH_START_ACTIVITY_ON_FOOT = 10 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_START_ACTIVITY_ON_BICYCLE = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_IN_VEHICLE = 150 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_ON_FOOT = 30 * Constants.MILLISECONDS_PER_SECOND; //TODO origin為60s
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_ON_BICYCLE = 90 * Constants.MILLISECONDS_PER_SECOND;

    private static final long WINDOW_LENGTH_TRANSITION_START_ACTIVITY_IN_VEHICLE = 5 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_TRANSITION_START_ACTIVITY_ON_FOOT = 10 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_TRANSITION_START_ACTIVITY_ON_BICYCLE = 10 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_IN_VEHICLE = 75 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_ON_FOOT = 10 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_ON_BICYCLE = 45 * Constants.MILLISECONDS_PER_SECOND;

    //the frequency of requesting google activity from the google play service
    public static int ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS = 5;

    public static long ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL =
            ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS * Constants.MILLISECONDS_PER_SECOND;

    private static long sActivityRecognitionUpdateIntervalInSeconds = ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS;

    private static long sActivityRecognitionUpdateIntervalInMilliseconds =
            sActivityRecognitionUpdateIntervalInSeconds * Constants.MILLISECONDS_PER_SECOND;

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_TRANSPORTATION = "Transportation";


    public static final int NO_ACTIVITY_TYPE = -1;
    public static final int IN_VEHICLE = DetectedActivity.IN_VEHICLE;
    public static final int ON_FOOT = DetectedActivity.ON_FOOT;
    public static final int ON_BICYCLE = DetectedActivity.ON_BICYCLE;
    public static final int UNKNOWN = DetectedActivity.UNKNOWN;
    public static final int STILL = DetectedActivity.STILL;
    public static final int TILTING = DetectedActivity.TILTING;

    private static ArrayList<ActivityRecognitionDataRecord> mActivityRecognitionRecords;


    /**Constant **/
    private static int mSuspectedStartActivityType = NO_ACTIVITY_TYPE;
    private static int mSuspectedStopActivityType = NO_ACTIVITY_TYPE;
    private static int mConfirmedActivityType = NO_ACTIVITY_TYPE;// the initial value of activity is STILL.
    private static long mSuspectTime = 0;
    private static int mCurrentState = STATE_STATIC;

    public ActivityRecognitionStreamGenerator activityRecognitionStreamGenerator;

    public static TransportationModeDataRecord toCheckFamiliarOrNotTransportationModeDataRecord;
    public static TransportationModeDataRecord transportationModeDataRecordFromService;


    private static final String PACKAGE_DIRECTORY_PATH="/Android/data/edu.ohio.minuku_2/";
    private CSVWriter csv_writer = null;

    private ActivityRecognitionDataRecord latest_activityRecognitionDataRecord;

    private static Context serviceInstance = null;
    private Context mContext;

    private ScheduledExecutorService mScheduledExecutorService;
    public static final int TransportationMode_REFRESH_FREQUENCY = 10; //1s, 1000ms
    public static final int BACKGROUND_RECORDING_INITIAL_DELAY = 0;

    private SharedPreferences sharedPrefs;

    public TransportationModeService(){}

    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "onCreate");

        serviceInstance = this;

        mContext = this;

        mScheduledExecutorService = Executors.newScheduledThreadPool(TransportationMode_REFRESH_FREQUENCY);

        sharedPrefs = getSharedPreferences("edu.umich.minuku_2", MODE_PRIVATE);

    }

    public static boolean isServiceRunning() {
        return serviceInstance != null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("CheckFamiliarOrNotService", "[test service running] going to start the probe service, isServiceRunning:  " + isServiceRunning());

        startService();

//        return START_STICKY;
        return START_REDELIVER_INTENT;
    }

    public void startService(){

        runMainThread();

    }

    private void runMainThread(){

        mScheduledExecutorService.scheduleAtFixedRate(
                TransportationModeRunnable,
                BACKGROUND_RECORDING_INITIAL_DELAY,
                TransportationMode_REFRESH_FREQUENCY,
                TimeUnit.SECONDS);
    }

    /*Runnable TransportationModeRunnable = new Runnable() {
        @Override
        public void run() {

            try {
                transportationModeStreamGenerator = (TransportationModeStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(TransportationModeDataRecord.class);
            }catch(StreamNotFoundException e){
                Log.e(TAG,"transportationModeStreamGenerator haven't created yet.");
            }

            //Log.e(TAG, String.valueOf(activityRecognitionStreamGenerator.getLastSavedRecord()));
            if(MinukuStreamManager.getInstance().getActivityRecognitionDataRecord()!=null){
                //if (activityRecognitionStreamGenerator.getLastSavedRecord()!=null) { //maybe need to judge Location's record even "getLastSavedRecord()!=null" ?

                ActivityRecognitionDataRecord recordPool = MinukuStreamManager.getInstance().getActivityRecognitionDataRecord();//activityRecognitionStreamGenerator.getLastSavedRecord();
//                Log.e(TAG,"getID : "+recordPool.getID());
                Log.e(TAG,"CreateTime:" + recordPool.getCreationTime()+ " MostProbableActivity:"+recordPool.getMostProbableActivity());

                if (recordPool!=null) {
                    examineTransportation(recordPool);
                    Log.e(TAG, "[testactivitylog] examine" +
                            " transportation: " + examineTransportation(recordPool));
                    Log.e(TAG, "[testactivitylog] transportation: " + getConfirmedActvitiyString());

                    //transportationModeDataRecordFromService =
                    //       new TransportationModeDataRecord(getActivityNameFromType(examineTransportation(recordPool)));//;getConfirmedActvitiyString()

                    try {
                        transportationModeStreamGenerator.setTransportationModeDataRecord(getConfirmedActvitiyString());

                        TransportationModeDataRecord transportationModeDataRecord = new TransportationModeDataRecord(getConfirmedActvitiyString());

                        MinukuStreamManager.getInstance().setTransportationModeDataRecord(transportationModeDataRecord);
//                    transportationModeDataRecordFromService = new TransportationModeDataRecord(getConfirmedActvitiyString());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                StoreToCSV(new Date().getTime(), getConfirmedActvitiyString(), recordPool.getCreationTime(), recordPool.getMostProbableActivity(), recordPool.getProbableActivities());

            }
            else
                Log.e(TAG, "ActivityRecognition's Stream might not start working yet.");

        }
    };
*/

    Runnable TransportationModeRunnable = new Runnable() {
        @Override
        public void run() {


//            Log.d(TAG, "test replay] examining transportation runnable ");
            try {
                transportationModeStreamGenerator = (TransportationModeStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(TransportationModeDataRecord.class);
            }catch(StreamNotFoundException e){
                Log.e(TAG,"transportationModeStreamGenerator haven't created yet.");
            }

            //Log.e(TAG, String.valueOf(activityRecognitionStreamGenerator.getLastSavedRecord()));
            if(MinukuStreamManager.getInstance().getActivityRecognitionDataRecord()!=null){
                //if (activityRecognitionStreamGenerator.getLastSavedRecord()!=null) { //maybe need to judge Location's record even "getLastSavedRecord()!=null" ?

                ActivityRecognitionDataRecord record = MinukuStreamManager.getInstance().getActivityRecognitionDataRecord();//activityRecognitionStreamGenerator.getLastSavedRecord();

                if (record!=null) {

//                    Log.d("TMService", "[test replay] inside if null the AR record to examine is " +  record.getDetectedtime() + " : " +  record.getProbableActivities().toString());


                    //getting latest Transportation based on the incoming record
                    examineTransportation(record);

                    Log.d("ARService", "[test replay] test trip: after examine transportation the current activity is  is " + getConfirmedActvitiyString() + " the status is " + getCurrentState());


                   // showTransportation(getConfirmedActvitiyString());

//                    Log.d("ARService", "[test replay] after show transportation");

                    try {
                        transportationModeStreamGenerator.setTransportationModeDataRecord(getConfirmedActvitiyString());

//                        TransportationModeDataRecord transportationModeDataRecord = new TransportationModeDataRecord(getConfirmedActvitiyString());
//
//                        MinukuStreamManager.getInstance().setTransportationModeDataRecord(transportationModeDataRecord);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                try {
                    Log.d(TAG, "record conf : " + record.getMostProbableActivity().getConfidence());
                    Log.d(TAG, "latest conf : " + latest_activityRecognitionDataRecord.getMostProbableActivity().getConfidence());
                    StoreToCSV(new Date().getTime(), record.getMostProbableActivity().getConfidence(), latest_activityRecognitionDataRecord.getMostProbableActivity().getConfidence());
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d(TAG, "record conf : " + record.getMostProbableActivity().getConfidence());
                    StoreToCSV(new Date().getTime(), record.getMostProbableActivity().getConfidence(), 0);
                }

                //write transportation mode record
                StoreToCSV(new Date().getTime(), latest_activityRecognitionDataRecord, getConfirmedActvitiyString(), mCurrentState);

                if(record.getMostProbableActivity().getConfidence()!=999){ //conf == 999 means it didn't receive anything from AR
//                    latest_activityRecognitionDataRecord = "";
                    latest_activityRecognitionDataRecord = record;

                }
            }
            else
                Log.e(TAG, "TransportationMode's Stream might not start working yet.");


//            Log.d("ARService", "[test replay] end of runnable");

        }
    };

    public void StoreToCSV(long timestamp, String transportation, long activityTime, DetectedActivity mostProbableActivity, List<DetectedActivity> probableActivities, int currentstate){

        Log.d(TAG,"StoreToCSV");

        String sFileName = "TransportationMode.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+ Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

//            data.add(new String[]{"timestamp","timeString","Latitude","Longitude","Accuracy"});
            String timeString = getTimeString(timestamp);

            String state = "";

            if (mCurrentState == 0)
                state = "STATE_STATIC";
            else if (mCurrentState == 1){
                state = "STATE_SUSPECTING_START";
            }
            else if (mCurrentState == 2){
                state = "STATE_CONFIRMED";
            }
            else if (mCurrentState == 3){
                state = "STATE_SUSPECTING_STOP";
            }


            String latest_AR_String = "";

            if (probableActivities!=null){

                for (int i=0; i<probableActivities.size(); i++){

                    if (i!=0){
                        latest_AR_String+= Constants.ACTIVITY_DELIMITER;
                    }
                    DetectedActivity activity =  probableActivities.get(i);
                    latest_AR_String += ActivityRecognitionStreamGenerator.getActivityNameFromType(activity.getType());
                    latest_AR_String += Constants.ACTIVITY_CONFIDENCE_CONNECTOR;
                    latest_AR_String += activity.getConfidence();

                }

//                Log.d("TMService", "[test replay] StoreToCSV writing receive AR CSV " +  latest_AR_String);
            }

            data.add(new String[]{String.valueOf(timestamp), timeString, transportation, "", state, String.valueOf(activityTime), String.valueOf(mostProbableActivity), latest_AR_String});

            csv_writer.writeAll(data);

//            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getTimeString(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_SLASH);
        String currentTimeString = sdf_now.format(time);

        return currentTimeString;
    }

    private void showTransportation(String transportation){

        Log.e(TAG,"showTransportation");

        String inhomeornot="not working yet";

        String local_transportation = transportation;

        String NotificationText = "Current Transportation Mode: " + local_transportation;


        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);//Context.
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle("DMS");
        bigTextStyle.bigText(NotificationText);

        Notification note = null;

        note = new Notification.Builder(mContext)
                .setContentTitle(Constants.APP_NAME)
                .setContentText(NotificationText)
//                .setContentIntent(pending)
                .setStyle(bigTextStyle)
//                .setSmallIcon(R.drawable.self_reflection)
                .setAutoCancel(true)
                .build();

        //note.flags |= Notification.FLAG_NO_CLEAR;
        //startForeground( 42, note );

        // using the same tag and Id causes the new notification to replace an existing one
        mNotificationManager.notify(7, note); //String.valueOf(System.currentTimeMillis()),
        note.flags = Notification.FLAG_AUTO_CANCEL;

        //note.setContentText(NotificationText);
        //mNotificationManager.notify(String.valueOf(System.currentTimeMillis()), 1, note.build());

    }

    public void StoreToCSV(long timestamp,int rec_conf, int latest_conf){
        Log.d(TAG,"StoreToCSV");

        String sFileName = "receive_latest.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+ Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp), timeString, String.valueOf(rec_conf), String.valueOf(latest_conf)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void StoreToCSV(long timestamp, ActivityRecognitionDataRecord latest_AR, String transportation, int currentstate){
        Log.d(TAG,"StoreToCSV");

        String sFileName = "TransportationMode.csv";


        //get location record
        float lat=0;
        float lng = 0;
        float accuracy = 0;


        if (MinukuStreamManager.getInstance().getLocationDataRecord()!=null) {
            lat = MinukuStreamManager.getInstance().getLocationDataRecord().getLatitude();
            lng = MinukuStreamManager.getInstance().getLocationDataRecord().getLongitude();
            accuracy = MinukuStreamManager.getInstance().getLocationDataRecord().getAccuracy();
        }


        Boolean TransportationModefirstOrNot = sharedPrefs.getBoolean("TransportationModefirstOrNot", true);

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+ Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = getTimeString(timestamp);

            String state = "";

            if (mCurrentState == 0)
                state = "STATE_STATIC";
            else if (mCurrentState == 1){
                state = "STATE_SUSPECTING_START";
            }
            else if (mCurrentState == 2){
                state = "STATE_CONFIRMED";
            }
            else if (mCurrentState == 3){
                state = "STATE_SUSPECTING_STOP";
            }

//            String rec_AR_String = rec_AR.getMostProbableActivity().toString();
            String rec_AR_String = "";
            String latest_AR_String = "";

            if (latest_AR!=null){
                for (int i=0; i<latest_AR.getProbableActivities().size(); i++){

                    if (i!=0){
                        latest_AR_String+= Constants.ACTIVITY_DELIMITER;
                    }
                    DetectedActivity activity =  latest_AR.getProbableActivities().get(i);
                    latest_AR_String += ActivityRecognitionStreamGenerator.getActivityNameFromType(activity.getType());
                    latest_AR_String += Constants.ACTIVITY_CONFIDENCE_CONNECTOR;
                    latest_AR_String += activity.getConfidence();

                }
//                Log.d("TMService", "[test replay] StoreToCSV writing latest AR data to CSV " + latest_AR_String);
            }

            if(TransportationModefirstOrNot) {
                data.add(new String[]{"timestamp", "timeString", "received_AR", "latest_AR", "transportation", "state", "lat", "lng", "accuracy"});
                sharedPrefs.edit().putBoolean("TransportationModefirstOrNot", false).apply();
            }

            //write transportation mode
            data.add(new String[]{String.valueOf(timestamp), timeString, rec_AR_String, latest_AR_String, transportation, state, String.valueOf(lat), String.valueOf(lng), String.valueOf(accuracy)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "exception", e);
        }
    }

    public void StoreToCSV(long timestamp,String state, String activitySofar, long detecttime, int currentstate){
        Log.d(TAG,"StoreToCSV");

        String sFileName = "Static.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+ Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp), timeString, state, String.valueOf(activitySofar), String.valueOf(detecttime), String.valueOf(currentstate)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

   /* //TODO eat ActivityRecognition and Timestamp to get
    public int examineTransportation(ActivityRecognitionDataRecord record) {

        //     Log.d(LOG_TAG, "[examineTransportation] enter" );
//        Log.d(LOG_TAG, "[examineTransportation] enter" + ActivityRecognitionManager.getProbableActivities() );

        List<DetectedActivity> probableActivities = record.getProbableActivities();
        //TODO eat ActivityRecognition and Timestamp to get
        long detectionTime = record.getTimestamp();


//        Log.d(LOG_TAG, "[examineTransportation] " + ScheduleAndSampleManager.getTimeString(detectionTime) + " the acitivty is: " +
//                probableActivities.toString());


        //if in the static state, we try to suspect new activity
        if (getCurrentState()==STATE_STATIC) {
            //getLatestActivityRecognitionRecord();

//            Log.d (LOG_TAG, " examineTransportation at STATE_STATIC " +
//                    getActivityNameFromType(getSuspectedStartActivityType()) );


            //if the detected activity is vehicle, bike or on foot, then we suspect the activity from now
            if (probableActivities.get(0).getType()== DetectedActivity.ON_BICYCLE ||
                    probableActivities.get(0).getType()== DetectedActivity.IN_VEHICLE ||
                    probableActivities.get(0).getType()== DetectedActivity.ON_FOOT ) {

                //set current state to suspect stop
                setCurrentState(STATE_SUSPECTING_START);

                //set suspected Activity type
                setSuspectedStartActivityType(probableActivities.get(0).getType());

                //set suspect time
                setSuspectTime(detectionTime);

//                Log.d (LOG_TAG, " examineTransportation [detected start possible activity] " + getActivityNameFromType(getSuspectedStartActivityType()) + " entering state " + getStateName(getCurrentState()));

                StoreToCSV(new Date().getTime(), "STATE_SUSPECTING_START", String.valueOf(probableActivities.get(0).getType()), detectionTime, getCurrentState());

            }

        }
        else if (getCurrentState()==STATE_SUSPECTING_START) {
            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(), getWindowLengh(getSuspectedStartActivityType(), getCurrentState()) );

            if (isTimeToConfirm) {

                long startTime = detectionTime - getWindowLengh(getSuspectedStartActivityType(), getCurrentState());
                long endTime = detectionTime;
                boolean isNewTransportationModeConfirmed = confirmStartPossibleTransportation(getSuspectedStartActivityType(), getWindowData(startTime, endTime));

                if (isNewTransportationModeConfirmed) {

                    //change the state to Confirmed
                    setCurrentState(STATE_CONFIRMED);
                    //set confirmed activity type
                    setConfirmedActivityType(getSuspectedStartActivityType());
                    //no suspect
                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

                    //set the suspect time so that other class can access it.(startTime is when we think the transportation starts)
                    setSuspectTime(startTime);

//                    Log.d (LOG_TAG, " examineTransportation [confiremd start activity]  " + getActivityNameFromType(getConfirmedActivityString()) + " entering state " + getStateName(getCurrentState()));

                    StoreToCSV(new Date().getTime(), "STATE_CONFIRMED", String.valueOf(probableActivities.get(0).getType()), startTime, getCurrentState());

                    return getConfirmedActivityString();
                }
                //if the suspection is wrong, back to the static state
                else {

                    //change the state to Confirmed
                    setCurrentState(STATE_STATIC);
                    //set confirmed activity type
                    setConfirmedActivityType(NO_ACTIVITY_TYPE);

                    setSuspectTime(0);

//                    Log.d (LOG_TAG, " examineTransportation [cancel activity suspection], back to state " + getStateName(getCurrentState()));

                    StoreToCSV(new Date().getTime(), "STATE_STATIC", "NO_ACTIVITY_TYPE", 0, getCurrentState());

                    return getConfirmedActivityString();

                }
            }
        }
        //if in the confirmed state, we suspect whether users exit the activity
        else if (getCurrentState()==STATE_CONFIRMED) {
            *//** if the detected activity is vehicle, bike or on foot, then we suspect the activity from now**//*

            //if the latest activity is not the currently confirmed activity nor tilting nor unkown
            if (probableActivities.get(0).getType() != getConfirmedActivityString() &&
                    probableActivities.get(0).getType() != DetectedActivity.TILTING &&
                    probableActivities.get(0).getType() != DetectedActivity.UNKNOWN) {

                //set current state to suspect stop
                setCurrentState(STATE_SUSPECTING_STOP);
                //set suspected Activity type to the confirmed activity type
                setSuspectedStopActivityType(getConfirmedActivityString());
                //set suspect time
                setSuspectTime(detectionTime);

//                Log.d (LOG_TAG, " examineTransportation [detected stop possible activity] " + getActivityNameFromType(getSuspectedStopActivityType()) + " entering state " + getStateName(getCurrentState()));

                StoreToCSV(new Date().getTime(), "STATE_SUSPECTING_STOP", getConfirmedActvitiyString(), detectionTime, getCurrentState());

            }
        }
        else if (getCurrentState()==STATE_SUSPECTING_STOP) {

            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(), getWindowLengh(getSuspectedStopActivityType(), getCurrentState()) );

            if (isTimeToConfirm) {

                long startTime =detectionTime - getWindowLengh(getSuspectedStartActivityType(), getCurrentState());
                long endTime = detectionTime;
                boolean isExitingTransportationMode = confirmStopPossibleTransportation(getSuspectedStopActivityType(), getWindowData(startTime, endTime));

                if (isExitingTransportationMode) {

                    //back to static
                    setCurrentState(STATE_STATIC);

                    setConfirmedActivityType(NO_ACTIVITY_TYPE);

                    setSuspectedStopActivityType(NO_ACTIVITY_TYPE);

                    //set the suspect time so that other class can access it.(startTime is when we think the transportation starts)
                    setSuspectTime(startTime);

//                    Log.d (LOG_TAG, " examineTransportation [stop activity], entering state" + getStateName(getCurrentState()));
                    StoreToCSV(new Date().getTime(), "STATE_STATIC", "NO_ACTIVITY_TYPE", startTime, getCurrentState());


                }

                //not exiting the confirmed activity
                else {
                    //back to static, cancel the suspection
                    setCurrentState(STATE_CONFIRMED);

                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

//                    Log.d (LOG_TAG, " examineTransportation [still maintain confirmed]" + getActivityNameFromType(getConfirmedActivityString()) + " still in the state " + getStateName(getCurrentState()));

                    StoreToCSV(new Date().getTime(), "STATE_CONFIRMED", "NO_ACTIVITY_TYPE", -999, getCurrentState());

                }

                setSuspectTime(0);
            }



            //or directly enter suspecting activity: if the current record is other type of transportation mode
            if (probableActivities.get(0).getType() != getSuspectedStopActivityType() &&
                    probableActivities.get(0).getType()!=DetectedActivity.TILTING &&
                    probableActivities.get(0).getType()!=DetectedActivity.STILL &&
                    probableActivities.get(0).getType()!=DetectedActivity.UNKNOWN ) {


                // Log.d (LOG_TAG, " examineTransportation by the way, check if we can suspect start activity " + getActivityNameFromType(record.getProbableActivities().get(0).getType()));
                isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(
                        detectionTime,
                        getSuspectTime(),
                        getWindowLengh(probableActivities.get(0).getType(),
                                STATE_SUSPECTING_START) );

                if (isTimeToConfirm) {

//                    Log.d (LOG_TAG, " examineTransportation yes it's good time to confirm whether we can change the suspection for "
//                            + getActivityNameFromType(probableActivities.get(0).getType()));

                    long startTime = detectionTime - getWindowLengh(probableActivities.get(0).getType(), STATE_SUSPECTING_START) ;
                    long endTime = detectionTime;
                    boolean isActuallyStartingAnotherActivity = changeSuspectingTransportation(
                            probableActivities.get(0).getType(),
                            getWindowData(startTime, endTime));

                    if (isActuallyStartingAnotherActivity) {

//                        Log.d (LOG_TAG, " examineTransportation [interrupt suspecting stop activity] " + getActivityNameFromType(getSuspectedStopActivityType()));



                        //back to static
                        setCurrentState(STATE_SUSPECTING_START);

                        //
                        // setConfirmedActivityType(NO_ACTIVITY_TYPE);

                        setSuspectedStopActivityType(NO_ACTIVITY_TYPE);

                        setSuspectedStartActivityType(probableActivities.get(0).getType());

//                        Log.d (LOG_TAG, " examineTransportation [detected start possible activity] " + getActivityNameFromType(getSuspectedStartActivityType()) + " entering state " + getStateName(getCurrentState()));

                        //start suspecting new activity
                        setSuspectTime(detectionTime);

                        StoreToCSV(new Date().getTime(), "STATE_SUSPECTING_START", "NO_ACTIVITY_TYPE", detectionTime, getCurrentState());

                    }
                }
            }
        }


        *//** if transportation is requested, save transportation record **//*

//        Log.d(LOG_TAG, STRING_CONTEXT_SOURCE_TRANSPORTATION + "examineTransportation isRequested: " + isRequested);

        return getConfirmedActivityString();
    }*/

    public int examineTransportation(ActivityRecognitionDataRecord activityRecognitionDataRecord){


        List<DetectedActivity> probableActivities = activityRecognitionDataRecord.getProbableActivities();

        Log.d(TAG, "[test replay] examine the incoming record.....for transportation " + activityRecognitionDataRecord.getDetectedtime()  +" : "+ activityRecognitionDataRecord.getProbableActivities().toString());

        long detectionTime = activityRecognitionDataRecord.getCreationTime();

        //if in the static state, we try to suspect new activity
        if (getCurrentState()==STATE_STATIC) {

//            Log.d(TAG,"[test replay] in Static");

            //if the detected activity is vehicle, bike or on foot, then we suspect the activity from now
            if (probableActivities.get(0).getType()== DetectedActivity.ON_BICYCLE ||
                    probableActivities.get(0).getType()== DetectedActivity.IN_VEHICLE ||
                    probableActivities.get(0).getType()== DetectedActivity.ON_FOOT) {


//                Log.d(TAG,"[test replay] change to suspect");

                //set current state to suspect stop
                setCurrentState(STATE_SUSPECTING_START);

                //set suspected Activity type
                setSuspectedStartActivityType(probableActivities.get(0).getType());

                //set suspect time
                setSuspectTime(detectionTime);
/*
                LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                        LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                        "Suspect Start Transportation:\t" + getActivityNameFromType(getSuspectedStartActivityType()) + "\t" + "state:" + getStateName(getCurrentState()) );
*/

                StoreToCSV(new Date().getTime(), "STATE_SUSPECTING_START", String.valueOf(probableActivities.get(0).getType()), detectionTime, getCurrentState());
            }

        }
        else if (getCurrentState()==STATE_SUSPECTING_START) {

            Log.d(TAG,"[test replay] in Suspect start, the suspected AR is " +getActivityNameFromType(getSuspectedStartActivityType()) );
            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(), getWindowLengh(getSuspectedStartActivityType(), getCurrentState()) );

            StoreToCSV(isTimeToConfirm, detectionTime);

            if (isTimeToConfirm) {

                long startTime = detectionTime - getWindowLengh(getSuspectedStartActivityType(), getCurrentState());
                long endTime = detectionTime;
                boolean isNewTransportationModeConfirmed = confirmStartPossibleTransportation(getSuspectedStartActivityType(), getWindowData(startTime, endTime));

                if (isNewTransportationModeConfirmed) {

                    //change the state to Confirmed
                    setCurrentState(STATE_CONFIRMED);
                    //set confirmed activity type
                    setConfirmedActivityType(getSuspectedStartActivityType());
                    //no suspect
                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

                    //set the suspect time so that other class can access it.(Trip_startTime is when we think the transportation starts)
                    setSuspectTime(startTime);
/*
                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                            "Confirm Transportation:\t" +  getActivityNameFromType(getConfirmedActivityString())  + "\t" + "state:" + getStateName(getCurrentState()) );
*/

                    StoreToCSV(new Date().getTime(), "STATE_CONFIRMED", String.valueOf(probableActivities.get(0).getType()), startTime, getCurrentState());

                    //

                    return getConfirmedActivityType();
                }
                //if the suspection is wrong, back to the static state
                else {

                    //change the state to Confirmed
                    setCurrentState(STATE_STATIC);
                    //set confirmed activity type
                    setConfirmedActivityType(NO_ACTIVITY_TYPE);

                    setSuspectTime(0);
/*
                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                            "Cancel Suspection:\t" + "state:" + getStateName(getCurrentState()) );
*/

                    StoreToCSV(new Date().getTime(), "STATE_STATIC", "NO_ACTIVITY_TYPE", 0, getCurrentState());

                    return getConfirmedActivityType();

                }
            }
        }
        //if in the confirmed state, we suspect whether users exit the activity
        else if (getCurrentState()==STATE_CONFIRMED) {

            Log.d(TAG,"[test replay] in confirm, the confirm AR is " + getActivityNameFromType(getConfirmedActivityType()));
            /** if the detected activity is vehicle, bike or on foot, then we suspect the activity from now**/

            //if the latest activity is not the currently confirmed activity nor tilting nor unkown
            if (probableActivities.get(0).getType() != getConfirmedActivityType() &&
                    probableActivities.get(0).getType() != DetectedActivity.TILTING &&
                    probableActivities.get(0).getType() != DetectedActivity.UNKNOWN) {

                //set current state to suspect stop
                setCurrentState(STATE_SUSPECTING_STOP);
                //set suspected Activity type to the confirmed activity type
                setSuspectedStopActivityType(getConfirmedActivityType());
                //set suspect time
                setSuspectTime(detectionTime);
/*
                LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                        LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                        "Suspect Stop Transportation:\t" +  getActivityNameFromType(getSuspectedStopActivityType())  + "\t" + "state:" + getStateName(getCurrentState()) );
*/

                StoreToCSV(new Date().getTime(), "STATE_SUSPECTING_STOP", getConfirmedActvitiyString(), detectionTime, getCurrentState());

            }
        }
        else if (getCurrentState()==STATE_SUSPECTING_STOP) {

//            Log.d(TAG,"[test replay] in suspect stop, the suspect stop activiti is " + getActivityNameFromType(getSuspectedStopActivityType()));
            //TODO change to the new constants.
            //TODO for "getTransitionWindowLength"
            //TODO If it is changing from unstatic to unstatic.
            //TODO If it is changing from unstatic to static, it will choose the original constant.

//            long WindowLength = getWindowLengh(getSuspectedStopActivityType(), getCurrentState());

            /*if(getSuspectedStopActivityType()==DetectedActivity.STILL){
                WindowLength = getWindowLengh(getSuspectedStopActivityType(), getCurrentState());
            }else{
                WindowLength = getTransitionWindowLength(getSuspectedStopActivityType(), getCurrentState());
            }*/

            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(),
                    getWindowLengh(getSuspectedStopActivityType(),
//                    getTransitionWindowLength(getSuspectedStopActivityType(),
                            getCurrentState())
//                    WindowLength
            );

            if (isTimeToConfirm) {
                //TODO change to the new constants
                long startTime =detectionTime -
                        getWindowLengh(getSuspectedStartActivityType(),
//                        getTransitionWindowLength(getSuspectedStartActivityType(),
                                getCurrentState())
//                        WindowLength
                        ;
                long endTime = detectionTime;
                boolean isExitingTransportationMode = confirmStopPossibleTransportation(getSuspectedStopActivityType(), getWindowData(startTime, endTime));

                if (isExitingTransportationMode) {
/*
                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                            "Stop Transportation:\t" +  getActivityNameFromType(getSuspectedStopActivityType())  + "\t" + "state:" + getStateName(getCurrentState()) );
*/
                    //back to static
                    setCurrentState(STATE_STATIC);

                    setConfirmedActivityType(NO_ACTIVITY_TYPE);

                    setSuspectedStopActivityType(NO_ACTIVITY_TYPE);

                    //set the suspect time so that other class can access it.(Trip_startTime is when we think the transportation starts)
                    setSuspectTime(startTime);

                    StoreToCSV(new Date().getTime(), "STATE_STATIC", "NO_ACTIVITY_TYPE", startTime, getCurrentState());

                }

                //not exiting the confirmed activity
                else {
                    //back to static, cancel the suspection
                    setCurrentState(STATE_CONFIRMED);

                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

/*
                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                            "Cancel Suspection:\t" +  "state:" + getStateName(getCurrentState()) );
*///
                    StoreToCSV(new Date().getTime(), "STATE_CONFIRMED", "NO_ACTIVITY_TYPE", -999, getCurrentState());

                }

                setSuspectTime(0);
            }

            //or directly enter suspecting activity: if the current record is other type of transportation mode
            if (probableActivities.get(0).getType() != getSuspectedStopActivityType() &&
                    probableActivities.get(0).getType()!= DetectedActivity.TILTING &&
                    probableActivities.get(0).getType()!= DetectedActivity.STILL &&
                    probableActivities.get(0).getType()!= DetectedActivity.UNKNOWN &&
                    //NA = 9
                    probableActivities.get(0).getType()!=9) {

                isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(
                        detectionTime,
                        getSuspectTime(),
                        //TODO change to the new constants
                        getWindowLengh(probableActivities.get(0).getType(),
//                        getTransitionWindowLength(probableActivities.get(0).getType(),
                                STATE_SUSPECTING_START) );

                if (isTimeToConfirm) {
                    //TODO change to the new constants
                    long startTime = detectionTime -
                            getWindowLengh(probableActivities.get(0).getType(),
//                            getTransitionWindowLength(probableActivities.get(0).getType(),
                                    STATE_SUSPECTING_START) ;
                    long endTime = detectionTime;
                    boolean isActuallyStartingAnotherActivity = changeSuspectingTransportation(
                            probableActivities.get(0).getType(),
                            getWindowData(startTime, endTime));

                    if (isActuallyStartingAnotherActivity) {

                        //back to static
                        setCurrentState(STATE_SUSPECTING_START);

                        setSuspectedStopActivityType(NO_ACTIVITY_TYPE);

                        setSuspectedStartActivityType(probableActivities.get(0).getType());

                        //start suspecting new activity
                        setSuspectTime(detectionTime);

/*
                        LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                                LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                               "Suspect Start Transportation:\t" + getActivityNameFromType(getSuspectedStartActivityType()) + "\t" + "state:" + getStateName(getCurrentState()) );
*/
                        StoreToCSV(new Date().getTime(), "STATE_SUSPECTING_START", "NO_ACTIVITY_TYPE", detectionTime, getCurrentState());

                    }
                }
            }
        }


        /** if transportation is requested, save transportation record **/
/*        boolean isRequested = checkRequestStatusOfContextSource(STRING_CONTEXT_SOURCE_TRANSPORTATION);

        if (isRequested){
            saveRecordToLocalRecordPool();
        }
*/
        return getConfirmedActivityType();


    }

    public void StoreToCSV(boolean isTimeToConfirm, long detectionTime){

        String sFileName = "isTimeToConfirm.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            CSVWriter csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = getTimeString(new Date().getTime());

            data.add(new String[]{String.valueOf(new Date().getTime()), timeString, String.valueOf(isTimeToConfirm), String.valueOf(detectionTime)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static int getConfirmedActivityType() {
        return mConfirmedActivityType;
    }

    public void setConfirmedActivityType(int confirmedActivityType) {
        mConfirmedActivityType = confirmedActivityType;
    }

    public static int getSuspectedStartActivityType() {
        return mSuspectedStartActivityType;
    }

    public static long getSuspectTime() {
        return mSuspectTime;
    }

    public static int getCurrentState() {
        return mCurrentState;
    }

    public static int getSuspectedStopActivityType() {
        return mSuspectedStopActivityType;
    }

    public static void setSuspectedStopActivityType(int suspectedStopActivityType) {
        mSuspectedStopActivityType = suspectedStopActivityType;
    }

    public static void setCurrentState(int state) {
        mCurrentState = state;
    }

    public static void setSuspectedStartActivityType(int suspectedStartActivityType) {
        mSuspectedStartActivityType = suspectedStartActivityType;
    }

    public static void setSuspectTime(long suspectTime) {
        mSuspectTime = suspectTime;
    }

    public static String getConfirmedActvitiyString() {
        return getActivityNameFromType(mConfirmedActivityType);
    }

    public static boolean checkTimeElapseOfLatestActivityFromSuspectPoint( long lastestActivityTime, long suspectTime, long windowLenth) {

        boolean flag = (lastestActivityTime - suspectTime > windowLenth); //(lastestActivityTime - suspectTime)*1000

        StoreToCSV(new Date().getTime(), lastestActivityTime, suspectTime, lastestActivityTime - suspectTime ,windowLenth, flag);

        if (flag)
            //wait for long enough
            return true;
        else
            //still need to wait
            return false;
    }

    public static void StoreToCSV(long timestamp, long lastestActivityTime, long suspectTime, long lastestActivityTime_suspectTime,long windowLenth, boolean flag){

        String sFileName = "checkTimeToConfirm.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            CSVWriter csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp), timeString, String.valueOf(lastestActivityTime), String.valueOf(suspectTime), String.valueOf(lastestActivityTime_suspectTime), String.valueOf(windowLenth), String.valueOf(flag)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static long getTransitionWindowLength (int activityType, int state) {

        if (state==STATE_SUSPECTING_START) {

            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    return WINDOW_LENGTH_TRANSITION_START_ACTIVITY_IN_VEHICLE;
                case DetectedActivity.ON_FOOT:
                    return WINDOW_LENGTH_TRANSITION_START_ACTIVITY_ON_FOOT;
                case DetectedActivity.ON_BICYCLE:
                    return WINDOW_LENGTH_TRANSITION_START_ACTIVITY_ON_BICYCLE;
                default:
                    return WINDOW_LENGTH_START_ACTIVITY_DEFAULT;

            }
        }
        else if (state==STATE_SUSPECTING_STOP) {

            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    return WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_IN_VEHICLE;
                case DetectedActivity.ON_FOOT:
                    return WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_ON_FOOT;
                case DetectedActivity.ON_BICYCLE:
                    return WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_ON_BICYCLE;
                default:
                    return WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT;

            }

        }else {
            return WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT;
        }

    }

    public static long getWindowLengh (int activityType, int state) {

        if (state==STATE_SUSPECTING_START) {

            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    return WINDOW_LENGTH_START_ACTIVITY_IN_VEHICLE;
                case DetectedActivity.ON_FOOT:
                    return WINDOW_LENGTH_START_ACTIVITY_ON_FOOT;
                case DetectedActivity.ON_BICYCLE:
                    return WINDOW_LENGTH_START_ACTIVITY_ON_BICYCLE;
                default:
                    return WINDOW_LENGTH_START_ACTIVITY_DEFAULT;

            }
        }
        else if (state==STATE_SUSPECTING_STOP) {

            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    return WINDOW_LENGTH_STOP_ACTIVITY_IN_VEHICLE;
                case DetectedActivity.ON_FOOT:
                    return WINDOW_LENGTH_STOP_ACTIVITY_ON_FOOT;
                case DetectedActivity.ON_BICYCLE:
                    return WINDOW_LENGTH_STOP_ACTIVITY_ON_BICYCLE;
                default:
                    return WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT;

            }

        }else {
            return WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT;
        }

    }


    private static boolean confirmStartPossibleTransportation(int activityType, ArrayList<ActivityRecognitionDataRecord> windowData) {

        float threshold = getConfirmStartThreshold(activityType);

        /** check if in the window data the number of the possible activity exceeds the threshold**/

        //get number of targeted data
        int count = 0;
        int inRecentCount = 0;

        for (int i=0; i<windowData.size(); i++) {

            List<DetectedActivity> detectedActivities = windowData.get(i).getProbableActivities();

            //in the recent 6 there are more than 3
            if (i >= windowData.size()-5) {

                if (detectedActivities.get(0).getType()==activityType ) {
                    inRecentCount +=1;
                }
            }

            if (detectedActivities.get(0).getType()==activityType ) {
                count +=1;
            }


        }

        if (windowData.size()!=0) {

            float percentage = (float)count/windowData.size();

            StoreToCSV(new Date().getTime(), String.valueOf(percentage), "start", windowData, threshold);

            //if the percentage > threshold
            if ( threshold <= percentage || inRecentCount >= 2)
                return true;
            else
                return false;

        }
        else{


            StoreToCSV(new Date().getTime(), "no float", "start",new ArrayList<ActivityRecognitionDataRecord>(), threshold);

            //if there's no data in the windowdata, we should not confirm the possible activity
            return false;
        }
    }

    public static void StoreToCSV(long timestamp, String percentage, String startstop, ArrayList<ActivityRecognitionDataRecord> windowData, float threshold){

        String sFileName = "windowdata.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            CSVWriter csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp), timeString, percentage, startstop,String.valueOf(windowData), String.valueOf(threshold)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static float getConfirmStartThreshold(int activityType) {

        //TODO: different activity has different threshold

        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return CONFIRM_START_ACTIVITY_THRESHOLD_IN_VEHICLE;
            case DetectedActivity.ON_FOOT:
                return CONFIRM_START_ACTIVITY_THRESHOLD_ON_FOOT;
            case DetectedActivity.ON_BICYCLE:
                return CONFIRM_START_ACTIVITY_THRESHOLD_ON_BICYCLE;
            default:
                return (float) 0.5;
        }
    }

    private ArrayList<ActivityRecognitionDataRecord> getWindowData(long startTime, long endTime) {

        ArrayList<ActivityRecognitionDataRecord> windowData = new ArrayList<ActivityRecognitionDataRecord>();

        //TODO: get activity records from the database
        //windowData = DataHandler.getActivityRecognitionRecordsBetweenTimes(Trip_startTime, Trip_endTime);

        ///for testing: get data from the testData

        ArrayList<ActivityRecognitionDataRecord> recordPool = getLocalRecordPool();

//        Log.d(LOG_TAG, " examineTransportation you find " + recordPool.size() + " records in the activity recognition pool");

        for (int i=0; i<recordPool.size(); i++) {

            ActivityRecognitionDataRecord record = (ActivityRecognitionDataRecord) recordPool.get(i);

            //       Log.d(LOG_TAG, " record.getTimestamp() " + record.getTimestamp() +
            //             " windwo Trip_startTime " + Trip_startTime + " windwo Trip_endTime " + Trip_endTime);


//            if (record.getTimestamp() >= startTime && record.getTimestamp() <= endTime)
//                windowData.add(record);
        }

        return windowData;
    }

    private static boolean confirmStopPossibleTransportation(int activityType, ArrayList<ActivityRecognitionDataRecord> windowData) {

        float threshold = getConfirmStopThreshold(activityType);

        /** check if in the window data the number of the possible activity exceeds the threshold**/

        //get number of targeted data
        int count = 0;
        int inRecentCount = 0;
        for (int i=0; i<windowData.size(); i++) {

            List<DetectedActivity> detectedActivities = windowData.get(i).getProbableActivities();

            //in the recent 6 there are more than 3
            if (i >= windowData.size()-5) {
                if (detectedActivities.get(0).getType()==activityType ) {
                    inRecentCount +=1;
                }
            }

            for (int activityIndex = 0; activityIndex<detectedActivities.size(); activityIndex++) {

                //if probable activities contain the target activity, we count! (not simply see the most probable one)
                if (detectedActivities.get(activityIndex).getType()==activityType ) {
                    count +=1;
                    break;
                }
            }
        }

        float percentage = (float)count/windowData.size();

        if (windowData.size()!=0) {
            //if the percentage > threshold
            StoreToCSV(new Date().getTime(), "stop", String.valueOf(percentage), windowData, threshold);

            if ( threshold >= percentage && inRecentCount <= 2)

                return true;
            else
                return false;

        }
        else{
            StoreToCSV(new Date().getTime(), "no float", "stop", new ArrayList<ActivityRecognitionDataRecord>(), threshold);

            //if there's no data in the windowdata, we should not confirm the possible activity
            return false;
        }
    }

    private static boolean changeSuspectingTransportation(int activityType, ArrayList<ActivityRecognitionDataRecord> windowData) {

        float threshold = getConfirmStartThreshold(activityType);

        /** check if in the window data the number of the possible activity exceeds the threshold**/

        int inRecentCount = 0;

        for (int i=windowData.size()-1; i>=0; i--) {

            List<DetectedActivity> detectedActivities = windowData.get(i).getProbableActivities();

            //in the recent 6 there are more than 3
            if (i >= windowData.size()-3) {
                if (detectedActivities.get(0).getType()==activityType ) {
                    inRecentCount +=1;
                }
            }


        }

        if (windowData.size()!=0) {

            //if the percentage > threshold
//            Log.d(LOG_TAG, "[changeSuspectingTransportation] examineTransportation changing transportation recentCount " +inRecentCount + " within " + windowData.size()  + "  data");


            if ( inRecentCount >= 2)
                return true;
            else
                return false;

        }
        else
            //if there's no data in the windowdata, we should not confirm the possible activity
            return false;

    }

    public static void addActivityRecognitionRecord(ActivityRecognitionDataRecord record) {
        getActivityRecognitionRecords().add(record);
    }

    public static ArrayList<ActivityRecognitionDataRecord> getActivityRecognitionRecords() {

        if (mActivityRecognitionRecords==null){
            mActivityRecognitionRecords = new ArrayList<ActivityRecognitionDataRecord>();
        }
        return mActivityRecognitionRecords;

    }


    private static float getConfirmStopThreshold(int activityType) {

        //TODO: different activity has different threshold

        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return CONFIRM_STOP_ACTIVITY_THRESHOLD_IN_VEHICLE;
            case DetectedActivity.ON_FOOT:
                return CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_FOOT;
            case DetectedActivity.ON_BICYCLE:
                return CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_BICYCLE;
            default:
                return (float) 0.5;

        }
    }

    /**
     * Map detected activity types to strings
     */
    public static String getActivityNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_IN_VEHICLE;
            case DetectedActivity.ON_BICYCLE:
                return ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_ON_BICYCLE;
            case DetectedActivity.ON_FOOT:
                return ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_ON_FOOT;
            case DetectedActivity.STILL:
                return ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_STILL;
            case DetectedActivity.RUNNING:
                return ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_RUNNING;
            case DetectedActivity.WALKING:
                return ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_WALKING;
            case DetectedActivity.UNKNOWN:
                return ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_UNKNOWN;
            case DetectedActivity.TILTING:
                return ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_TILTING;
            case NO_ACTIVITY_TYPE:
                return TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION;
        }
        return TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


