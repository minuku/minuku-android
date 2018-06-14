package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.service.TransportationModeService;
import labelingStudy.nctu.minuku.stream.TransportationModeStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator.getLocalRecordPool;

/**
 * Created by Lawrence on 2017/5/22.
 */

public class TransportationModeStreamGenerator extends AndroidStreamGenerator<TransportationModeDataRecord> {

    public final String TAG = "TransportationModeStreamGenerator";

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

    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_IN_VEHICLE = (float) 0.6;
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_ON_FOOT = (float)0.6;
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_ON_BICYCLE =(float) 0.6;
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_IN_VEHICLE = (float)0.2;
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_FOOT = (float)0.2;
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
    public static final String TRANSPORTATION_MODE_NAME_IN_NO_TRANSPORTATION = "static";
    public static final String TRANSPORTATION_MODE_NAME_NA = "NA";

    private static final long WINDOW_LENGTH_START_ACTIVITY_DEFAULT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_START_ACTIVITY_IN_VEHICLE = 10 * Constants.MILLISECONDS_PER_SECOND; //TODO origin為20s
    private static final long WINDOW_LENGTH_START_ACTIVITY_ON_FOOT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_START_ACTIVITY_ON_BICYCLE = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_IN_VEHICLE = 150 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_ON_FOOT = 30 * Constants.MILLISECONDS_PER_SECOND; //TODO origin為60s
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_ON_BICYCLE = 90 * Constants.MILLISECONDS_PER_SECOND;

    private static final long WINDOW_LENGTH_TRANSITION_START_ACTIVITY_IN_VEHICLE = 5 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_TRANSITION_START_ACTIVITY_ON_FOOT = 10 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_TRANSITION_START_ACTIVITY_ON_BICYCLE = 10 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_IN_VEHICLE = 75 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_ON_FOOT = 15 * Constants.MILLISECONDS_PER_SECOND;
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

    /**Constant **/
    private static int mSuspectedStartActivityType = NO_ACTIVITY_TYPE;
    private static int mSuspectedStopActivityType = NO_ACTIVITY_TYPE;
    private static int mConfirmedActivityType = NO_ACTIVITY_TYPE;// the initial value of activity is STILL.
    private static long mSuspectTime = 0;
    private static int mCurrentState = STATE_STATIC;

    private Context mContext;
    private TransportationModeStream mStream;

    private final ScheduledExecutorService mScheduledExecutorService;
    public static final int TransportationMode_REFRESH_FREQUENCY = 10; //1s, 1000ms
    public static final int BACKGROUND_RECORDING_INITIAL_DELAY = 0;

    public ActivityRecognitionStreamGenerator activityRecognitionStreamGenerator;

    public static TransportationModeDataRecord toCheckFamiliarOrNotTransportationModeDataRecord;

    public String ConfirmedActvitiyString = "NA";

    public TransportationModeStreamGenerator(Context applicationContext) {
        super(applicationContext);
        this.mContext = applicationContext;
        this.mStream = new TransportationModeStream(Constants.LOCATION_QUEUE_SIZE);

        this.activityRecognitionStreamGenerator = ActivityRecognitionStreamGenerator.getInstance(applicationContext);

        mScheduledExecutorService = Executors.newScheduledThreadPool(TransportationMode_REFRESH_FREQUENCY);


        this.register();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, TransportationModeDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which LocationDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides LocationDataRecord is already registered.");
        }
    }
    @Override
    public Stream<TransportationModeDataRecord> generateNewStream() {
        return mStream;
    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean updateStream() {

        Log.d(TAG, "Update stream called.");

        TransportationModeDataRecord transportationModeDataRecord =
                new TransportationModeDataRecord(ConfirmedActvitiyString);

        Log.d(TAG,"updateStream transportationModeDataRecord : " + ConfirmedActvitiyString);

        toCheckFamiliarOrNotTransportationModeDataRecord = transportationModeDataRecord;
        Log.d(TAG, "transportationModeDataRecordFromService : " + toCheckFamiliarOrNotTransportationModeDataRecord.getConfirmedActivityString());

        mStream.add(transportationModeDataRecord);
        Log.d(TAG, "TransportationMode to be sent to event bus" + transportationModeDataRecord);

        MinukuStreamManager.getInstance().setTransportationModeDataRecord(transportationModeDataRecord,mContext);

        // also post an event.
        EventBus.getDefault().post(transportationModeDataRecord);
        try {
            appDatabase db;
            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();
            db.transportationModeDataRecordDao().insertAll(transportationModeDataRecord);

            List<TransportationModeDataRecord> transportationModeDataRecords = db.transportationModeDataRecordDao().getAll();
            for (TransportationModeDataRecord t : transportationModeDataRecords) {
                Log.d(TAG, t.getConfirmedActivityString());
            }

        } catch (NullPointerException e) { //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 1; //TODO check its efficiency.
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {

        Intent intent = new Intent(mContext, TransportationModeService.class);
        if(!TransportationModeService.isServiceRunning())
            mContext.startService(intent);

//        startTransportationModeMainThread();
    }

    @SuppressLint("LongLogTag")
    public void setTransportationModeDataRecord(String getConfirmedActvitiyString){

        ConfirmedActvitiyString = getConfirmedActvitiyString;

        Log.d(TAG, "ConfirmedActvitiyString : " + ConfirmedActvitiyString);

       /* TransportationModeDataRecord temp = new TransportationModeDataRecord(getConfirmedActvitiyString);

        transportationModeDataRecord = temp;*/
//        transportationModeDataRecord = new TransportationModeDataRecord(getConfirmedActvitiyString);

    }

    public void startTransportationModeMainThread() {
        mScheduledExecutorService.scheduleAtFixedRate(
                TransportationModeRunnable,
                BACKGROUND_RECORDING_INITIAL_DELAY,
                TransportationMode_REFRESH_FREQUENCY,
                TimeUnit.SECONDS);
    }

    Runnable TransportationModeRunnable = new Runnable() {
        @SuppressLint("LongLogTag")
        @Override
        public void run() {

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

                }
            }
            else
                Log.e(TAG, "ActicityRecogntion's Stream might not start working yet.");
        }
    };

    @Override
    public void offer(TransportationModeDataRecord dataRecord) {

    }

    public int examineTransportation(ActivityRecognitionDataRecord activityRecognitionDataRecord) {
        //** eat ActivityRecognition and Timestamp to get
        //List<DetectedActivity> probableActivities = record.getProbableActivities();
        //long detectionTime = record.getTimestamp();

        List<DetectedActivity> probableActivities = activityRecognitionDataRecord.getProbableActivities();
//        long detectionTime = activityRecognitionDataRecord.getTimestamp();

        //if in the static state, we try to suspect new activity
        if (getCurrentState()==STATE_STATIC) {

            //if the detected activity is vehicle, bike or on foot, then we suspect the activity from now
            if (probableActivities.get(0).getType()== DetectedActivity.ON_BICYCLE ||
                    probableActivities.get(0).getType()== DetectedActivity.IN_VEHICLE ||
                    probableActivities.get(0).getType()== DetectedActivity.ON_FOOT ) {

                //set current state to suspect stop
                setCurrentState(STATE_SUSPECTING_START);

                //set suspected Activity type
                setSuspectedStartActivityType(probableActivities.get(0).getType());

                //set suspect time
//                setSuspectTime(detectionTime);
/*
                LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                        LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                        "Suspect Start Transportation:\t" + getActivityNameFromType(getSuspectedStartActivityType()) + "\t" + "state:" + getStateName(getCurrentState()) );
*/
            }

        }
//        else if (getCurrentState()==STATE_SUSPECTING_START) {
//            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(), getWindowLengh(getSuspectedStartActivityType(), getCurrentState()) );

//            if (isTimeToConfirm) {
//
//                long startTime = detectionTime - getWindowLengh(getSuspectedStartActivityType(), getCurrentState());
//                long endTime = detectionTime;
//                boolean isNewTransportationModeConfirmed = confirmStartPossibleTransportation(getSuspectedStartActivityType(), getWindowData(startTime, endTime));
//
//                if (isNewTransportationModeConfirmed) {
//
//                    //change the state to Confirmed
//                    setCurrentState(STATE_CONFIRMED);
//                    //set confirmed activity type
//                    setConfirmedActivityType(getSuspectedStartActivityType());
//                    //no suspect
//                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);
//
//                    //set the suspect time so that other class can access it.(Trip_startTime is when we think the transportation starts)
//                    setSuspectTime(startTime);
///*
//                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
//                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
//                            "Confirm Transportation:\t" +  getActivityNameFromType(getConfirmedActivityString())  + "\t" + "state:" + getStateName(getCurrentState()) );
//*/
//                    return getConfirmedActivityType();
//                }
//                //if the suspection is wrong, back to the static state
//                else {
//
//                    //change the state to Confirmed
//                    setCurrentState(STATE_STATIC);
//                    //set confirmed activity type
//                    setConfirmedActivityType(NO_ACTIVITY_TYPE);
//
//                    setSuspectTime(0);
///*
//                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
//                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
//                            "Cancel Suspection:\t" + "state:" + getStateName(getCurrentState()) );
//*/
//                    return getConfirmedActivityType();
//
//                }
//            }
//        }
        //if in the confirmed state, we suspect whether users exit the activity
        else if (getCurrentState()==STATE_CONFIRMED) {
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
//                setSuspectTime(detectionTime);
/*
                LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                        LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                        "Suspect Stop Transportation:\t" +  getActivityNameFromType(getSuspectedStopActivityType())  + "\t" + "state:" + getStateName(getCurrentState()) );
*/
            }
        }
        else if (getCurrentState()==STATE_SUSPECTING_STOP) {

//            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(), getWindowLengh(getSuspectedStopActivityType(), getCurrentState()) );

//            if (isTimeToConfirm) {
//
//                long startTime =detectionTime - getWindowLengh(getSuspectedStartActivityType(), getCurrentState());
//                long endTime = detectionTime;
//                boolean isExitingTransportationMode = confirmStopPossibleTransportation(getSuspectedStopActivityType(), getWindowData(startTime, endTime));
//
//                if (isExitingTransportationMode) {
///*
//                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
//                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
//                            "Stop Transportation:\t" +  getActivityNameFromType(getSuspectedStopActivityType())  + "\t" + "state:" + getStateName(getCurrentState()) );
//*/
//                    //back to static
//                    setCurrentState(STATE_STATIC);
//
//                    setConfirmedActivityType(NO_ACTIVITY_TYPE);
//
//                    setSuspectedStopActivityType(NO_ACTIVITY_TYPE);
//
//                    //set the suspect time so that other class can access it.(Trip_startTime is when we think the transportation starts)
//                    setSuspectTime(startTime);
//
//                }
//
//                //not exiting the confirmed activity
//                else {
//                    //back to static, cancel the suspection
//                    setCurrentState(STATE_CONFIRMED);
//
//                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);
//
///*
//                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
//                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
//                            "Cancel Suspection:\t" +  "state:" + getStateName(getCurrentState()) );
//*/
//                }
//
//                setSuspectTime(0);
//            }



            //or directly enter suspecting activity: if the current record is other type of transportation mode
//            if (probableActivities.get(0).getType() != getSuspectedStopActivityType() &&
//                    probableActivities.get(0).getType()!=DetectedActivity.TILTING &&
//                    probableActivities.get(0).getType()!=DetectedActivity.STILL &&
//                    probableActivities.get(0).getType()!=DetectedActivity.UNKNOWN ) {
//
//                isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(
//                        detectionTime,
//                        getSuspectTime(),
//                        getWindowLengh(probableActivities.get(0).getType(),
//                                STATE_SUSPECTING_START) );
//
//                if (isTimeToConfirm) {
//
//                    long startTime = detectionTime - getWindowLengh(probableActivities.get(0).getType(), STATE_SUSPECTING_START) ;
//                    long endTime = detectionTime;
//                    boolean isActuallyStartingAnotherActivity = changeSuspectingTransportation(
//                            probableActivities.get(0).getType(),
//                            getWindowData(startTime, endTime));
//
//                    if (isActuallyStartingAnotherActivity) {
//
//                        //back to static
//                        setCurrentState(STATE_SUSPECTING_START);
//
//                        setSuspectedStopActivityType(NO_ACTIVITY_TYPE);
//
//                        setSuspectedStartActivityType(probableActivities.get(0).getType());
//
//                        //start suspecting new activity
//                        setSuspectTime(detectionTime);
//
///*
//                        LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
//                                LogManager.LOG_TAG_PROBE_TRANSPORTATION,
//                                "Suspect Start Transportation:\t" + getActivityNameFromType(getSuspectedStartActivityType()) + "\t" + "state:" + getStateName(getCurrentState()) );
//*/
//                    }
//                }
//            }
        }


        /** if transportation is requested, save transportation record **/
/*        boolean isRequested = checkRequestStatusOfContextSource(STRING_CONTEXT_SOURCE_TRANSPORTATION);

        if (isRequested){
            saveRecordToLocalRecordPool();
        }
*/
        return getConfirmedActivityType();


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

        if (lastestActivityTime - suspectTime > windowLenth)
            //wait for long enough
            return true;
        else
            //still need to wait
            return false;
    }

    public static long getTransitionWindowLength (int activityType, int state) {

//        WINDOW_LENGTH_TRANSITION_START_ACTIVITY_IN_VEHICLE = 5 * Constants.MILLISECONDS_PER_SECOND;
//        private static final long WINDOW_LENGTH_TRANSITION_START_ACTIVITY_ON_FOOT = 10 * Constants.MILLISECONDS_PER_SECOND;
//        private static final long WINDOW_LENGTH_TRANSITION_START_ACTIVITY_ON_BICYCLE = 10 * Constants.MILLISECONDS_PER_SECOND;
//        private static final long WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_IN_VEHICLE = 75 * Constants.MILLISECONDS_PER_SECOND;
//        private static final long WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_ON_FOOT = 15 * Constants.MILLISECONDS_PER_SECOND;
//        private static final long WINDOW_LENGTH_TRANSITION_STOP_ACTIVITY_ON_BICYCLE

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
            //if the percentage > threshold
            if ( threshold <= percentage || inRecentCount >= 2)
                return true;
            else
                return false;

        }
        else
            //if there's no data in the windowdata, we should not confirm the possible activity
            return false;
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
            if ( threshold >= percentage && inRecentCount <= 2)
                return true;
            else
                return false;

        }
        else
            //if there's no data in the windowdata, we should not confirm the possible activity
            return false;
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
                return TRANSPORTATION_MODE_NAME_IN_NO_TRANSPORTATION;
        }
        return TRANSPORTATION_MODE_NAME_IN_NO_TRANSPORTATION;
    }

}
