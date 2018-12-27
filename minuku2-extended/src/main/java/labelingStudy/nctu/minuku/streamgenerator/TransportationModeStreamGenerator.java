package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.opencsv.CSVWriter;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.Utilities.Utils;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.TransportationModeDataRecordDAO;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.stream.TransportationModeStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator.getLocalRecordPool;

/**
 * Created by Lawrence on 2017/5/22.
 */

public class TransportationModeStreamGenerator extends AndroidStreamGenerator<TransportationModeDataRecord> {

    public final String TAG = "TransportationModeStreamGenerator";

    private TransportationModeStream mStream;
    private TransportationModeDataRecordDAO transportationModeDataRecordDAO;

    private String ConfirmedActvitiyString = "NA";

    /**ContextSourceType**/
    public static final int CONTEXT_SOURCE_TRANSPORTATION = 0;
    public static final int CONTEXT_SOURCE_DETECTION_STATE = 1;

    public static final String STRING_CONTEXT_SOURCE_TRANSPORTATION = "Transportation";
    public static final String STRING_CONTEXT_SOURCE_DETECTION_STATE = "DetectionState";

    /**Table Name**/
    public static final String RECORD_TABLE_NAME_TRANSPORTATION = "Record_Table_Transportation";

    public static final int STATE_INITIAL = -1;
    public static final int STATE_STATIC = 0;
    public static final int STATE_SUSPECTING_START = 1;
    public static final int STATE_CONFIRMED = 2;
    public static final int STATE_SUSPECTING_STOP = 3;

    //
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_IN_VEHICLE = (float) 0.8; //TODO origin為 0.6
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_ON_FOOT = (float)0.6;
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_ON_BICYCLE =(float) 0.6;
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_IN_VEHICLE = (float)0.3; //0.2
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_FOOT = (float)0.3; //0.1
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_BICYCLE =(float) 0.3; //0.2

    public static final int CONFIRM_START_ACTIVITY_Needed_Confidence = 40;
    public static final int CONFIRM_STOP_ACTIVITY_Needed_Confidence = 40;
    public static final int CANCEL_SUSPECT_Threshold = 95;
    public static final int SWTICH_TO_NEW_ACTIVITY_Threshold = 95;

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

    public static final String TRANSPORTATION_MODE_HASNT_DETECTED_FLAG = "未知";

    private static final long WINDOW_LENGTH_START_ACTIVITY_DEFAULT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_START_ACTIVITY_IN_VEHICLE = 60 * Constants.MILLISECONDS_PER_SECOND; //TODO origin為20s
    private static final long WINDOW_LENGTH_START_ACTIVITY_ON_FOOT = 10 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_START_ACTIVITY_ON_BICYCLE = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_IN_VEHICLE = 150 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_ON_FOOT = 30 * Constants.MILLISECONDS_PER_SECOND;
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
    public static int mSuspectedStartActivityType = NO_ACTIVITY_TYPE;
    public static int mSuspectedStopActivityType = NO_ACTIVITY_TYPE;
    public static int mConfirmedActivityType = NO_ACTIVITY_TYPE;// the initial value of activity is STILL.
    public static long mSuspectTime = 0;
    public static int mCurrentState = STATE_STATIC;


    private static final String PACKAGE_DIRECTORY_PATH="/Android/data/labelingStudy.nctu.minuku_2/";
    private CSVWriter csv_writer = null;

    private ActivityRecognitionDataRecord latest_activityRecognitionDataRecord;

    private static Context serviceInstance = null;
    private Context mContext;

    public static ScheduledExecutorService mScheduledExecutorService;
    public static final int TransportationMode_REFRESH_FREQUENCY = 5; //1s, 1000ms
    public static final int BACKGROUND_RECORDING_INITIAL_DELAY = 0;
    private final int TransportationMode_ThreadSize = 1;

    private SharedPreferences sharedPrefs;

    public TransportationModeStreamGenerator(Context applicationContext) {
        super(applicationContext);
        this.mContext = applicationContext;
        this.mStream = new TransportationModeStream(Constants.LOCATION_QUEUE_SIZE);
        transportationModeDataRecordDAO = appDatabase.getDatabase(applicationContext).transportationModeDataRecordDao();

        mScheduledExecutorService = Executors.newScheduledThreadPool(TransportationMode_ThreadSize);

        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString, mContext.MODE_PRIVATE);

        mCurrentState = sharedPrefs.getInt("CurrentState", STATE_STATIC);
        mConfirmedActivityType = sharedPrefs.getInt("ConfirmedActivityType", NO_ACTIVITY_TYPE);

        CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_TRANSPORTATION,
                "transportationCreationTime",
                "transportationConfirmedActivity",
                "transportationSuspectedTime",
                "transportationSuspectedStartActivity",
                "transportationSuspectedStopActivity",
                "activityRecognitionMostProbableActivity",
                "activityRecognitionProbableActivity");

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

        //TODO deprecated, move to activityRecognition to update every 5 second
        //detecting the Transportation from the activityRecognition
        /*if(MinukuStreamManager.getInstance().getActivityRecognitionDataRecord()!=null){

            ActivityRecognitionDataRecord record = MinukuStreamManager.getInstance().getActivityRecognitionDataRecord();

            if (record!=null) {

                //getting latest Transportation based on the incoming record
                examineTransportation(record);

                //Log.d(TAG, "[test replay] test trip: after examine transportation the current activity is  is " + getConfirmedActivityString() + " the status is " + getCurrentState());
            }

            sharedPrefs.edit().putInt("CurrentState", mCurrentState).apply();
            sharedPrefs.edit().putInt("ConfirmedActivityType", mConfirmedActivityType).apply();

            if(record.getMostProbableActivity().getConfidence()!=999){ //conf == 999 means it didn't receive anything from AR

                latest_activityRecognitionDataRecord = record;
            }
        }*/

//        int session_id = SessionManager.getOngoingSessionId();

        int session_id = sharedPrefs.getInt("ongoingSessionid", Constants.INVALID_INT_VALUE);

        String suspectedStartActivity = getActivityNameFromType(getSuspectedStartActivityType());
        String suspectedEndActivity = getActivityNameFromType(getSuspectedStopActivityType());

        TransportationModeDataRecord transportationModeDataRecord =
                new TransportationModeDataRecord(getConfirmedActivityString(), getSuspectTime(), suspectedStartActivity, suspectedEndActivity);

        Log.d(TAG,"updateStream transportationModeDataRecord : " + getConfirmedActivityString());

        mStream.add(transportationModeDataRecord);
        Log.d(TAG, "TransportationMode to be sent to event bus" + transportationModeDataRecord);

        //TODO move to AR after examine the transportation
//        MinukuStreamManager.getInstance().setTransportationModeDataRecord(transportationModeDataRecord, mContext, sharedPrefs);

        // also post an event.
        EventBus.getDefault().post(transportationModeDataRecord);

        try {

//            appDatabase db;
//            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
//                    .allowMainThreadQueries()
//                    .build();
            transportationModeDataRecordDAO.insertAll(transportationModeDataRecord);

            List<TransportationModeDataRecord> transportationModeDataRecords = transportationModeDataRecordDAO.getAll();
            for (TransportationModeDataRecord t : transportationModeDataRecords) {
                labelingStudy.nctu.minuku.logger.Log.e(TAG," ConfirmedActivity: "+ t.getConfirmedActivityString());
                labelingStudy.nctu.minuku.logger.Log.e(TAG, " SuspectedStartActivityString: "+t.getSuspectedStartActivityString());
                labelingStudy.nctu.minuku.logger.Log.e(TAG, " SuspectedStopActivityString: "+t.getSuspectedStopActivityString());
                labelingStudy.nctu.minuku.logger.Log.e(TAG, " SuspectedTime: "+String.valueOf(t.getSuspectedTime()));
                labelingStudy.nctu.minuku.logger.Log.e(TAG, "taskDayCount: "+t.getTaskDayCount());
                labelingStudy.nctu.minuku.logger.Log.e(TAG, "hour: "+t.getHour());

            }
        } catch (NullPointerException e) { //Sometimes no data is normal

            CSVHelper.storeToCSV(CSVHelper.CSV_ESM, "Transportation, update stream, NullPointerException");
            CSVHelper.storeToCSV(CSVHelper.CSV_ESM, Utils.getStackTrace(e));
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
    public void onStreamRegistration() {

    }

    @Override
    public void offer(TransportationModeDataRecord dataRecord) {

    }

    public int examineTransportation(ActivityRecognitionDataRecord activityRecognitionDataRecord){

        //if there's no existing activity type, we need to get activity from the shared preference
        if (mConfirmedActivityType == NO_ACTIVITY_TYPE){
            mConfirmedActivityType = sharedPrefs.getInt("ConfirmedActivityType", NO_ACTIVITY_TYPE);
        }

        if(mCurrentState == STATE_INITIAL){
            mCurrentState = sharedPrefs.getInt("CurrentState", STATE_INITIAL);
        }

        if(activityRecognitionDataRecord.getProbableActivities()==null || activityRecognitionDataRecord.getProbableActivities().isEmpty()){
            return -1;
        }

        List<DetectedActivity> probableActivities = activityRecognitionDataRecord.getProbableActivities();

        //Log.d(TAG, "[test replay] examine the incoming record.....for transportation " + activityRecognitionDataRecord.getDetectedtime()  +" : "+ activityRecognitionDataRecord.getProbableActivities().toString());

        long detectionTime = activityRecognitionDataRecord.getCreationTime();

        if ((probableActivities.get(0).getType() == DetectedActivity.ON_BICYCLE ||
                    probableActivities.get(0).getType() == DetectedActivity.IN_VEHICLE ||
                    probableActivities.get(0).getType() == DetectedActivity.ON_FOOT) &&
                    probableActivities.get(0).getConfidence() >= CANCEL_SUSPECT_Threshold) {

            //back to static, cancel the suspect
            setCurrentState(STATE_CONFIRMED);

            setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

            setConfirmedActivityType(probableActivities.get(0).getType());

            CSVHelper.TransportationState_StoreToCSV(new Date().getTime(), "STATE_CONFIRMED", getConfirmedActivityString());

            return getConfirmedActivityType();
        }

        //if in the static state or initial state, we try to suspect new activity
        if (getCurrentState()==STATE_STATIC || getCurrentState()==STATE_INITIAL) {

            //if the detected activity is vehicle, bike or on foot, then we suspect the activity from now
            if (probableActivities.get(0).getType()== DetectedActivity.ON_BICYCLE ||
                    probableActivities.get(0).getType()== DetectedActivity.IN_VEHICLE ||
                    probableActivities.get(0).getType()== DetectedActivity.ON_FOOT) {

                //set current state to suspect stop
                setCurrentState(STATE_SUSPECTING_START);

                //set suspected Activity type
                setSuspectedStartActivityType(probableActivities.get(0).getType());

                //set suspect time
                setSuspectTime(detectionTime);

                CSVHelper.TransportationState_StoreToCSV(new Date().getTime(), "STATE_SUSPECTING_START", getConfirmedActivityString());
            }

        }
        else if (getCurrentState()==STATE_SUSPECTING_START) {

            //Log.d(TAG,"[test replay] in Suspect start, the suspected AR is " +getActivityNameFromType(getSuspectedStartActivityType()) );
            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(), getWindowLengh(getSuspectedStartActivityType(), getCurrentState()) );

            StoreToCSV(isTimeToConfirm, detectionTime);

            if (isTimeToConfirm) {

                long startTime = detectionTime - getWindowLengh(getSuspectedStartActivityType(), getCurrentState());
                long endTime = detectionTime;
                boolean isNewTransportationModeConfirmed = confirmStartPossibleTransportation(getSuspectedStartActivityType(), getWindowData(startTime, endTime),
                        getWindowLengh(getSuspectedStartActivityType(), getCurrentState()));

                if (isNewTransportationModeConfirmed) {

                    //change the state to Confirmed
                    setCurrentState(STATE_CONFIRMED);
                    //set confirmed activity type
                    setConfirmedActivityType(getSuspectedStartActivityType());
                    //no suspect
                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

                    //set the suspect time so that other class can access it.(Trip_startTime is when we think the transportation starts)
                    setSuspectTime(startTime);

                    CSVHelper.TransportationState_StoreToCSV(new Date().getTime(), "STATE_CONFIRMED", getConfirmedActivityString());

                    return getConfirmedActivityType();
                }
                //if the suspection is wrong, back to the static state
                else {

                    //change the state to Confirmed
                    setCurrentState(STATE_STATIC);
                    //set confirmed activity type
                    setConfirmedActivityType(NO_ACTIVITY_TYPE);

                    setSuspectTime(0);

                    CSVHelper.TransportationState_StoreToCSV(new Date().getTime(), "STATE_STATIC", getConfirmedActivityString());

                    return getConfirmedActivityType();

                }
            }
        }
        //if in the confirmed state, we suspect whether users exit the activity
        else if (getCurrentState()==STATE_CONFIRMED) {

            //Log.d(TAG,"[test replay] in confirm, the confirm AR is " + getActivityNameFromType(getConfirmedActivityType()));
            /** if the detected activity is vehicle, bike or on foot, then we suspect the activity from now **/

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

                CSVHelper.TransportationState_StoreToCSV(new Date().getTime(), "STATE_SUSPECTING_STOP", getConfirmedActivityString());

            }
        }
        else if (getCurrentState()==STATE_SUSPECTING_STOP) {

            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(),
                    getWindowLengh(getSuspectedStopActivityType(),
                            getCurrentState())
            );

            if (isTimeToConfirm) {
                long startTime =detectionTime -
                        getWindowLengh(getSuspectedStartActivityType(),
                                getCurrentState())
                        ;
                long endTime = detectionTime;
                boolean isExitingTransportationMode =
                        confirmStopPossibleTransportation(getSuspectedStopActivityType(), getWindowData(startTime, endTime),
                                getWindowLengh(getSuspectedStartActivityType(), getCurrentState()));


                if (isExitingTransportationMode) {

                    //back to static
                    setCurrentState(STATE_STATIC);

                    setConfirmedActivityType(NO_ACTIVITY_TYPE);

                    setSuspectedStopActivityType(NO_ACTIVITY_TYPE);

                    //set the suspect time so that other class can access it.(Trip_startTime is when we think the transportation starts)
                    setSuspectTime(startTime);

                    CSVHelper.TransportationState_StoreToCSV(new Date().getTime(), "STATE_STATIC", getConfirmedActivityString());

                }
                //not exiting the confirmed activity
                else {
                    //back to static, cancel the suspection
                    setCurrentState(STATE_CONFIRMED);

                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

                    CSVHelper.TransportationState_StoreToCSV(new Date().getTime(), "STATE_CONFIRMED", getConfirmedActivityString());

                }

                setSuspectTime(0);
            }

            /**
             * switch activity
             */

            //or directly enter suspecting activity: if the current record is other type of transportation mode
            if (probableActivities.get(0).getType()!=getSuspectedStopActivityType() &&
                    probableActivities.get(0).getType()!=DetectedActivity.TILTING &&
                    probableActivities.get(0).getType()!=DetectedActivity.STILL &&
                    probableActivities.get(0).getType()!=DetectedActivity.UNKNOWN &&
                    //NA = 9
                    probableActivities.get(0).getType()!=9) {

                //if the new Activity having the confidence over 95, changing to state confirm
                if(probableActivities.get(0).getConfidence() > SWTICH_TO_NEW_ACTIVITY_Threshold){

                    //change the state to Confirmed
                    setCurrentState(STATE_CONFIRMED);
                    //set confirmed activity type
                    setConfirmedActivityType(probableActivities.get(0).getType());
                    //no suspect
                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

                    //set the suspect time so that other class can access it.(Trip_startTime is when we think the transportation starts)
                    setSuspectTime(detectionTime);

                    CSVHelper.TransportationState_StoreToCSV(new Date().getTime(), "STATE_CONFIRMED", getConfirmedActivityString());

                    return getConfirmedActivityType();
                }

                //no label with high confidence, we need to check labels in a window time
                else {

                    isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(
                            detectionTime,
                            getSuspectTime(),
                            getWindowLengh(probableActivities.get(0).getType(),
                                    STATE_SUSPECTING_START) );

                    if (isTimeToConfirm) {
                        long startTime = detectionTime -
                                getWindowLengh(probableActivities.get(0).getType(),
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

                            CSVHelper.TransportationState_StoreToCSV(new Date().getTime(), "STATE_SUSPECTING_START", getConfirmedActivityString());
                        }
                    }
                }

            }
        }

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

    public static String getConfirmedActivityString() {
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

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp), timeString, String.valueOf(lastestActivityTime), String.valueOf(suspectTime), String.valueOf(lastestActivityTime_suspectTime), String.valueOf(windowLenth), String.valueOf(flag)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }catch (Exception e){
            //e.printStackTrace();
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


    private boolean confirmStartPossibleTransportation(int activityType, ArrayList<ActivityRecognitionDataRecord> windowData, long windowLength) {

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

//            if (detectedActivities.get(0).getType()==activityType ) {
//                count +=1;
//            }

            for (int activityIndex = 0; activityIndex<detectedActivities.size(); activityIndex++) {

                //if probable activities contain the target activity, we count! (not simply see the most probable one)

                if (detectedActivities.get(activityIndex).getType()==activityType
                    //turned into getting the first two labels
                    //also, we only care about the label which is much confidence to
                    //prevent the low confidence ones would affect the result
//                        && detectedActivities.get(activityIndex).getConfidence() >= CONFIRM_START_ACTIVITY_Needed_Confidence
                        ){
                    count +=1;
                    break;
                }

                //only consider the first two labels
                if(activityIndex >= 1){
                    break;
                }
            }


        }

        if (windowData.size()!=0) {

            float percentage = (float)count/windowData.size();

            StoreToCSV(new Date().getTime(), String.valueOf(percentage), "start", windowData, threshold, windowLength);

            //if the percentage > threshold
            if ( threshold <= percentage || inRecentCount >= 2)
                return true;
            else
                return false;

        }
        else{


            StoreToCSV(new Date().getTime(), "no float", "start",new ArrayList<ActivityRecognitionDataRecord>(), threshold, windowLength);

            //if there's no data in the windowdata, we should not confirm the possible activity
            return false;
        }
    }

    public void StoreToCSV(long timestamp, String percentage, String startstop, ArrayList<ActivityRecognitionDataRecord> windowData, float threshold, long windowLength){

        String sFileName = "windowdata.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            CSVWriter csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+PACKAGE_DIRECTORY_PATH+sFileName,true));

            SharedPreferences sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString, mContext.MODE_PRIVATE);
            Boolean startwindowdataOrNot = sharedPrefs.getBoolean("startwindowdataOrNot", true);

            if(startwindowdataOrNot) {
                List<String[]> title = new ArrayList<String[]>();

                title.add(new String[]{"timestamp", "timeString", "percentage", "startstop", "windowData", "threshold", "windowLength"});

                csv_writer.writeAll(title);

                sharedPrefs.edit().putBoolean("startwindowdataOrNot", false).apply();

            }

            List<String[]> data = new ArrayList<String[]>();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp), timeString, percentage, startstop,String.valueOf(windowData), String.valueOf(threshold), String.valueOf(windowLength)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }catch (Exception e){
            //e.printStackTrace();
        }
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

            String timeString = ScheduleAndSampleManager.getTimeString(new Date().getTime());

            data.add(new String[]{String.valueOf(new Date().getTime()), timeString, String.valueOf(isTimeToConfirm), String.valueOf(detectionTime)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }catch (Exception e){
            //e.printStackTrace();
        }
    }

    private static float getConfirmStartThreshold(int activityType) {

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

//        //Log.d(LOG_TAG, " examineTransportation you find " + recordPool.size() + " records in the activity recognition pool");

        for (int i=0; i<recordPool.size(); i++) {

            ActivityRecognitionDataRecord record = (ActivityRecognitionDataRecord) recordPool.get(i);

            //       //Log.d(LOG_TAG, " record.getTimestamp() " + record.getTimestamp() +
            //             " windwo Trip_startTime " + Trip_startTime + " windwo Trip_endTime " + Trip_endTime);


            if (record.getCreationTime() >= startTime && record.getCreationTime() <= endTime)
                windowData.add(record);
        }

        return windowData;
    }

    private boolean confirmStopPossibleTransportation(int activityType, ArrayList<ActivityRecognitionDataRecord> windowData, long windowLength) {

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

                /*if (detectedActivities.get(activityIndex).getType()==activityType
                    //only consider the first two labels
                    //also, we only care about the label which is much confidence to
                    //prevent the low confidence ones would affect the result
                    //&& detectedActivities.get(activityIndex).getConfidence() >= CONFIRM_STOP_ACTIVITY_Needed_Confidence
                        ){
                    count +=1;
                    break;
                }

                //only consider the first two labels
                if(activityIndex > 1){
                    break;
                }*/

                if(activityIndex == 0){

                    if (detectedActivities.get(activityIndex).getType()==activityType){
                        count +=1;
                    }
                }else if(activityIndex == 1){

                    if (detectedActivities.get(activityIndex).getType()==activityType &&
                            detectedActivities.get(activityIndex).getConfidence() > 35){
                        count +=1;
                    }
                }else{

                    break;
                }

            }

        }

        float percentage = (float)count/windowData.size();

        if (windowData.size()!=0) {
            //if the percentage > threshold
            StoreToCSV(new Date().getTime(), "stop", String.valueOf(percentage), windowData, threshold, windowLength);

            if ( threshold >= percentage && inRecentCount <= 2)

                return true;
            else
                return false;

        }
        else{
            StoreToCSV(new Date().getTime(), "no float", "stop", new ArrayList<ActivityRecognitionDataRecord>(), threshold, windowLength);

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
//            //Log.d(LOG_TAG, "[changeSuspectingTransportation] examineTransportation changing transportation recentCount " +inRecentCount + " within " + windowData.size()  + "  data");


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
}
