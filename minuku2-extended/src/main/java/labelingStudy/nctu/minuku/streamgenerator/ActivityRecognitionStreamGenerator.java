package labelingStudy.nctu.minuku.streamgenerator;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.ActivityRecognitionDataRecordDAO;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.service.ActivityRecognitionService;
import labelingStudy.nctu.minuku.stream.ActivityRecognitionStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2017/5/15.
 */

public class ActivityRecognitionStreamGenerator extends AndroidStreamGenerator<ActivityRecognitionDataRecord> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public final static String TAG = "ActivityRecognitionStreamGenerator";

    private PendingIntent mActivityRecognitionPendingIntent;
    private static GoogleApiClient mGoogleApiClient;

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
    public static final int NO_ACTIVITY_TYPE = -1;

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_NAME = "DetectedActivities";

    protected long recordCount;

    private Context mContext;
    private ActivityRecognitionStream mStream;
    ActivityRecognitionDataRecordDAO mDAO;

    private ActivityRecognitionDataRecord activityRecognitionDataRecord;

    /** KeepAlive **/
    protected int KEEPALIVE_MINUTE = 3;
    protected long sKeepalive;

    public static List<DetectedActivity> sProbableActivities;
    public static DetectedActivity sMostProbableActivity;
    private static long sLatestDetectionTime;

    public static int ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS = 5;
    public static long ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL =
            ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS * Constants.MILLISECONDS_PER_SECOND;

    public static ArrayList<ActivityRecognitionDataRecord> mLocalRecordPool;

    private static ActivityRecognitionStreamGenerator instance;

    public ActivityRecognitionStreamGenerator(Context applicationContext) { //,Context mContext
        super(applicationContext);
        //this.mContext = mMainServiceContext;
        this.mContext = applicationContext;
        this.mStream = new ActivityRecognitionStream(Constants.LOCATION_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(ActivityRecognitionDataRecord.class);

        mLocalRecordPool = new ArrayList<ActivityRecognitionDataRecord>();

        ActivityRecognitionStreamGenerator.instance = this;

        recordCount = 0;
        sKeepalive = KEEPALIVE_MINUTE * Constants.MILLISECONDS_PER_MINUTE;

        this.register();
    }
    /*
        public void setContext(Context Context){
            mContext = Context;
        }

        public static ActivityRecognitionStreamGenerator getInstance() {
            if(ActivityRecognitionStreamGenerator.instance == null) {
                try {
                    ActivityRecognitionStreamGenerator.instance = new ActivityRecognitionStreamGenerator();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ActivityRecognitionStreamGenerator.instance;
        }
    */

    public static ActivityRecognitionStreamGenerator getInstance(Context applicationContext) {
        if(ActivityRecognitionStreamGenerator.instance == null) {
            try {
                Log.e(TAG,"creating new ActivityRecognitionStreamGenerator.");
                ActivityRecognitionStreamGenerator.instance = new ActivityRecognitionStreamGenerator(applicationContext);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ActivityRecognitionStreamGenerator.instance;
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, ActivityRecognitionDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which LocationDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides LocationDataRecord is already registered.");
        }
    }

    @Override
    public void onStreamRegistration() {
        buildGoogleApiClient();
/*
        EventBus.getDefault().post(new IncrementLoadingProcessCountEvent());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Log.d(TAG, "Stream " + TAG + "initialized from previous state");
                    Future<List<ActivityRecognitionDataRecord>> listFuture =
                            mDAO.getLast(Constants.LOCATION_QUEUE_SIZE); //TODO Constants.LOCATION_QUEUE_SIZE must be replaced.
                    while(!listFuture.isDone()) {
                        Thread.sleep(1000);
                    }
                    Log.d(TAG, "Received data from Future for " + TAG);
                    mStream.addAll(new LinkedList<>(listFuture.get()));
                } catch (DAOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    EventBus.getDefault().post(new DecrementLoadingProcessCountEvent());
                }
            }
        });

        */
    }

    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient==null){

            mGoogleApiClient =
                    new GoogleApiClient.Builder(mApplicationContext) // "mApplicationContext" is inspired by LocationStreamGenerator,it might not wrong.
                            .addApi(com.google.android.gms.location.ActivityRecognition.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();

            mGoogleApiClient.connect();

        }

    }

    @Override
    public Stream<ActivityRecognitionDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.e(TAG, "Update stream called.");

//        ActivityRecognitionDataRecord activityRecognitionDataRecord
//               = new ActivityRecognitionDataRecord(sMostProbableActivity,sProbableActivities);

        MinukuStreamManager.getInstance().setActivityRecognitionDataRecord(activityRecognitionDataRecord);

        if(activityRecognitionDataRecord!=null) {
            mStream.add(activityRecognitionDataRecord);
            Log.e(TAG, "Location to be sent to event bus" + activityRecognitionDataRecord);

            EventBus.getDefault().post(activityRecognitionDataRecord);
            try {

                mDAO.add(activityRecognitionDataRecord);

                mDAO.query_counting();
            } catch (DAOException e) {
                e.printStackTrace();
                return false;
            }

        }
        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }



    @Override
    public void offer(ActivityRecognitionDataRecord dataRecord) {
        Log.e(TAG, "Offer for ActivityRecognition data record does nothing!");
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.e(TAG,"onConnected");

        startActivityRecognitionUpdates();
    }

    private void startActivityRecognitionUpdates() {

        Log.d(TAG, "[startActivityRecognitionUpdates]");

        mActivityRecognitionPendingIntent = createRequestPendingIntent();

        //request activity recognition update
        if (com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi!=null && !ActivityRecognitionService.isServiceRunning()){
            com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,                    //GoogleApiClient client
                    ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL,//detectionIntervalMillis
                    mActivityRecognitionPendingIntent);   //callbackIntent

            //Log.d(TAG, "[com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi] is running!!!");

        }

    }

    private PendingIntent createRequestPendingIntent() {
        Log.d(TAG, "createRequestPendingIntent");
        // If the PendingIntent already exists
        if (mActivityRecognitionPendingIntent != null) {
            return mActivityRecognitionPendingIntent;
            // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService

            Intent intent = new Intent(
                    mApplicationContext, ActivityRecognitionService.class);

            PendingIntent pendingIntent =
                    PendingIntent.getService(mApplicationContext, //mApplicationContext || mContext
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            mActivityRecognitionPendingIntent = pendingIntent;
            return pendingIntent;
        }
    }

    public void setActivitiesandDetectedtime (List<DetectedActivity> probableActivities, DetectedActivity mostProbableActivity, long detectedtime) {
        //set activities

        //set a list of probable activities
        setProbableActivities(probableActivities);
        //set the most probable activity
        setMostProbableActivity(mostProbableActivity);

        setDetectedtime(detectedtime);
        /*
        ActivityRecognitionRecord record = ContextManager.getActivityRecognitionRecord();

        record.setProbableActivities(sProbableActivities);
        record.setMostProbableActivity(sMostProbableActivity);
        record.setDetectedtime(sDetectedtime);

        setActivityRecord(record);
*/
        Log.e(TAG,detectedtime+"||"+ mostProbableActivity);


        // Assume isRequested.
        if(probableActivities!=null&&mostProbableActivity!=null)
            saveRecordToLocalRecordPool(mostProbableActivity,detectedtime);


    }

    public void saveRecordToLocalRecordPool(DetectedActivity MostProbableActivity,long Detectedtime) {
        /** create a Record to save timestamp, session it belongs to, and Data**/
        ActivityRecognitionDataRecord record = new ActivityRecognitionDataRecord(MostProbableActivity,Detectedtime);
        record.setProbableActivities(sProbableActivities);

        JSONObject data = new JSONObject();

        //also set data:
        JSONArray activitiesJSON = new JSONArray();

        //add all activities to JSONArray
        for (int i=0; i<sProbableActivities.size(); i++){
            DetectedActivity detectedActivity =  sProbableActivities.get(i);
            String activityAndConfidence = getActivityNameFromType(detectedActivity.getType()) + Constants.ACTIVITY_DELIMITER + detectedActivity.getConfidence();
            activitiesJSON.put(activityAndConfidence);
        }

        //add activityJSON Array to data
        try {
            data.put(RECORD_DATA_PROPERTY_NAME, activitiesJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**we set data in Record**/
        record.setData(data);
        record.setTimestamp(sLatestDetectionTime);

        Log.d(TAG, "testing saving records at " + record.getTimeString() + " data: " + record.getData());

        addRecord(record);

    }

    protected void addRecord(ActivityRecognitionDataRecord activityRecognitionDataRecord) {

        /**1. add record to the local pool **/
        long id = recordCount++;
        activityRecognitionDataRecord.setID(id);
        Log.e(TAG,"CreateTime:" + activityRecognitionDataRecord.getCreationTime()+ " MostProbableActivity:"+activityRecognitionDataRecord.getMostProbableActivity());

        Log.e("mLocalRecordPool ", String.valueOf(mLocalRecordPool));
        mLocalRecordPool.add(activityRecognitionDataRecord); //it's working.
        Log.e(TAG, "[test logging]add record " + "logged at " + activityRecognitionDataRecord.getTimeString() );
        //Log.e(TAG, String.valueOf(mLocalRecordPool.size()));

        /**2. check whether we should remove old record **/
        removeOutDatedRecord();
        //**** update the latest ActivityRecognitionDataRecord in mLocalRecordPool to MinukuStreamManager;
        mLocalRecordPool.get(mLocalRecordPool.size()-1).setID(999);
        Log.e(TAG,"size : "+mLocalRecordPool.size());
        MinukuStreamManager.getInstance().setActivityRecognitionDataRecord(mLocalRecordPool.get(mLocalRecordPool.size()-1));
        Log.e(TAG,"CreateTime:" + mLocalRecordPool.get(mLocalRecordPool.size()-1).getCreationTime()+ " MostProbableActivity:"+mLocalRecordPool.get(mLocalRecordPool.size()-1).getMostProbableActivity());


        this.activityRecognitionDataRecord = activityRecognitionDataRecord;

        //TODO check we need this function or not
//        updateStream();

    }

    /**
     * this function remove old record (depending on the maximum size of the local pool)
     */
    protected void removeOutDatedRecord() {

        for (int i=0; i<mLocalRecordPool.size(); i++) {

            ActivityRecognitionDataRecord record = mLocalRecordPool.get(i);

            //calculate time difference
            long diff =  getCurrentTimeInMillis() - mLocalRecordPool.get(i).getTimestamp();

            //remove outdated records.
            if (diff >= sKeepalive){
                mLocalRecordPool.remove(record);
                //Log.d(TAG, "[test logging]remove record " + record.getSource() + record.getID() + " logged at " + record.getTimeString() + " to " + this.getName());
                Log.e(TAG,"sKeepalive");
                i--;
            }
        }
    }

    //TODO might be useless
    public static ArrayList<ActivityRecognitionDataRecord> getLocalRecordPool(){
        return mLocalRecordPool;
    }

    public static ActivityRecognitionDataRecord getLastSavedRecord(){
        if(mLocalRecordPool==null){
            Log.e("getLastSavedRecord","null");
            return null;
        }
        if (mLocalRecordPool.size()>0)
            return mLocalRecordPool.get(mLocalRecordPool.size()-1);
        else{
            Log.e("getLastSavedRecord","mLocalRecordPool.size()<0");
            return null;
        }
    }

    /**get the current time in milliseconds**/
    public static long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }


    public void setProbableActivities(List<DetectedActivity> probableActivities) {
        sProbableActivities = probableActivities;

    }

    public void setMostProbableActivity(DetectedActivity mostProbableActivity) {
        sMostProbableActivity = mostProbableActivity;

    }

    public void setDetectedtime(long detectedtime){
        sLatestDetectionTime = detectedtime;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            // Log.d(LOG_TAG,"[onConnectionFailed] Conntection to Google Play services is failed");

        } else {
            Log.e(TAG, "[onConnectionFailed] No Google Play services is available, the error code is "
                    + connectionResult.getErrorCode());
        }
    }

    public static String getActivityNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return STRING_DETECTED_ACTIVITY_IN_VEHICLE;
            case DetectedActivity.ON_BICYCLE:
                return STRING_DETECTED_ACTIVITY_ON_BICYCLE;
            case DetectedActivity.ON_FOOT:
                return STRING_DETECTED_ACTIVITY_ON_FOOT;
            case DetectedActivity.STILL:
                return STRING_DETECTED_ACTIVITY_STILL;
            case DetectedActivity.RUNNING:
                return STRING_DETECTED_ACTIVITY_RUNNING;
            case DetectedActivity.WALKING:
                return STRING_DETECTED_ACTIVITY_WALKING;
            case DetectedActivity.UNKNOWN:
                return STRING_DETECTED_ACTIVITY_UNKNOWN;
            case DetectedActivity.TILTING:
                return STRING_DETECTED_ACTIVITY_TILTING;
            case NO_ACTIVITY_TYPE:
                return STRING_DETECTED_ACTIVITY_NA;
        }
        return "NA";
    }
}
