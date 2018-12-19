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

package labelingStudy.nctu.minuku.streamgenerator;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.SensorDataRecordDAO;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;
import labelingStudy.nctu.minuku.stream.SensorStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static android.content.Context.SENSOR_SERVICE;
import static labelingStudy.nctu.minuku.config.Constants.CONTEXT_SOURCE_INVALID_VALUE_FLOAT;

/**
 * Created by neerajkumar on 7/18/16.
 */

public class SensorStreamGenerator extends AndroidStreamGenerator<SensorDataRecord> implements
        SensorEventListener {

    private SensorStream mStream;
    private String TAG = "SensorStreamGenerator";
    private Sensor sensor;
    public static SensorDataRecord sensorDataRecord;
    /** Tag for logging. */
    private static final String LOG_TAG = "PhoneSensorMnger";
    public static String CONTEXT_SOURCE_PHONE_SENSOR = "PhoneSensor";
    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_NAME = "SensorValues";
    /**system components**/
    private static Context mContext;
    private SensorDataRecordDAO sensorDataRecordDAO;
    private static SensorManager mSensorManager;
    private static List<Sensor> SensorList;

    public static final String STRING_PHONE_SENSOR_ACCELEROMETER = "Sensor-Accelerometer";
    public static final String STRING_PHONE_SENSOR_LINEAR_ACCELERATION = "Sensor-LinearAcceleration";
    public static final String STRING_PHONE_SENSOR_ROTATION_VECTOR = "Sensor-RotationVector";
    public static final String STRING_PHONE_SENSOR_GRAVITY = "Sensor-Gravity";
    public static final String STRING_PHONE_SENSOR_GYROSCOPE = "Sensor-Gyroscope";
    public static final String STRING_PHONE_SENSOR_LIGHT = "Sensor-Light";
    public static final String STRING_PHONE_SENSOR_MAGNETIC_FIELD = "Sensor-MagneticField";
    public static final String STRING_PHONE_SENSOR_PRESSURE = "Sensor-Pressure";
    public static final String STRING_PHONE_SENSOR_PROXIMITY = "Sensor-Proximity";
    public static final String STRING_PHONE_SENSOR_AMBIENT_TEMPERATURE = "Sensor-AmbientTemperature";
    public static final String STRING_PHONE_SENSOR_RELATIVE_HUMIDITY = "Sensor-RelativeHumidity";
    public static final String STRING_PHONE_SENSOR_STEP_COUNTER = "Sensor-StepCounter";
    public static final String STRING_PHONE_SENSOR_STEP_DETECTOR = "Sensor-StepDetector";
    public static final String STRING_PHONE_SENSOR_HEART_RATE = "Sensor-HeartRate";

    public static final int PHONE_SENSOR_ACCELEROMETER = 0;
    public static final int PHONE_SENSOR_LINEAR_ACCELERATION = 1;
    public static final int PHONE_SENSOR_ROTATION_VECTOR = 2;
    public static final int PHONE_SENSOR_GRAVITY = 3;
    public static final int PHONE_SENSOR_GYROSCOPE = 4;
    public static final int PHONE_SENSOR_LIGHT = 5;
    public static final int PHONE_SENSOR_MAGNETIC_FIELD = 6;
    public static final int PHONE_SENSOR_PRESSURE = 7;
    public static final int PHONE_SENSOR_PROXIMITY = 8;
    public static final int PHONE_SENSOR_AMBIENT_TEMPERATURE = 9;
    public static final int PHONE_SENSOR_RELATIVE_HUMIDITY = 10;
    public static final int PHONE_SENSOR_STEP_COUNTER = 11;
    public static final int PHONE_SENSOR_STEP_DETECTOR = 12;
    public static final int PHONE_SENSOR_HEART_RATE = 13;

    /**Motion Sensors**/
    private static float mAccele_x, mAccele_y, mAccele_z;
    private static float mGyroscope_x, mGyroscope_y, mGyroscope_z;
    private static float mGravity_x, mGravity_y, mGravity_z;
    private static float mLinearAcceleration_x, mLinearAcceleration_y, mLinearAcceleration_z;
    private static float mRotationVector_x_sin, mRotationVector_y_sin, mRotationVector_z_sin, mRotationVector_cos;
    private static float mHeartRate, mStepCount, mStepDetect;

    /**Position Sensors**/
    private static float mProximity ;
    private static float mMagneticField_x, mMagneticField_y, mMagneticField_z;

    private float mLight, mPressure, mRelativeHumidity, mAmbientTemperature ;

    ///// String to save each sensor-name and values
    String mAccele_str, mGyroscope_str, mGravity_str, mLinearAcceleration_str, mRotationVector_str,
            mProximity_str, mMagneticField_str, mLight_str, mPressure_str, mRelativeHumidity_str,  mAmbientTemperature_str;

    private SharedPreferences sharedPrefs;

    /** handle stream **/
    /**sensorStreamGenerator**/
    public SensorStreamGenerator(Context applicationContext) {
        super(applicationContext);
        this.mStream = new SensorStream(Constants.SENSOR_QUEUE_SIZE);

        mContext = applicationContext;
        sensorDataRecordDAO = appDatabase.getDatabase(applicationContext).sensorDataRecordDao();
        //call sensor manager from the service
        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);

        //initiate values of sensors
        mAccele_x = mAccele_y = mAccele_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT; //-9999
        mGyroscope_x = mGyroscope_y = mGyroscope_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mGravity_x = mGravity_y = mGravity_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mMagneticField_x = mMagneticField_y = mMagneticField_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mLinearAcceleration_x = mLinearAcceleration_y = mLinearAcceleration_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mRotationVector_x_sin = mRotationVector_y_sin =  mRotationVector_z_sin = mRotationVector_cos = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mHeartRate = mStepCount = mStepDetect = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mLight = mPressure = mRelativeHumidity = mProximity = mAmbientTemperature = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;

        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString,Context.MODE_PRIVATE);

        //initiate registered sensor list
        RegisterAvailableSensors();
        this.register();  // stream
    }
    /**onStreamRegistration**/


    @Override
    public void offer(SensorDataRecord dataRecord) {

    }

    /**register**/
    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, SensorDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which SensorDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides SensorDataRecord is already registered.");
        }
    }

    @Override
    public Stream<SensorDataRecord> generateNewStream() {
        return null;
    }


    @Override
    public boolean updateStream() {
        Log.d(TAG, "updateStream called");
//        int session_id = SessionManager.getOngoingSessionId();
        int session_id = sharedPrefs.getInt("ongoingSessionid", Constants.INVALID_INT_VALUE);

        Log.d(TAG,"mAccele_str = "+mAccele_str+" mGyroscope_str = "+mGyroscope_str+" mGravity_str = "+mGravity_str+" mLinearAcceleration_str = "+mLinearAcceleration_str);
        Log.d(TAG,"mRotationVector_str = "+mRotationVector_str+" mProximity_str = "+mProximity_str+" mMagneticField_str = "+mMagneticField_str+" mLight_str = "+mLight_str);
        Log.d(TAG,"mPressure_str = "+mPressure_str+" mRelativeHumidity_str = "+mRelativeHumidity_str+" mAmbientTemperature_str = "+mAmbientTemperature_str+" session_id = "+session_id);

        SensorDataRecord sensorDataRecord = new SensorDataRecord(mAccele_str, mGyroscope_str, mGravity_str, mLinearAcceleration_str,
                mRotationVector_str, mProximity_str, mMagneticField_str, mLight_str, mPressure_str, mRelativeHumidity_str, mAmbientTemperature_str);
        mStream.add(sensorDataRecord);
        Log.d(TAG, "Sensor to be sent to event bus" + sensorDataRecord);

        //post an event
        EventBus.getDefault().post(sensorDataRecord);
        try {
//            appDatabase db;
//            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
//                    .allowMainThreadQueries()
//                    .build();
            sensorDataRecordDAO.insertAll(sensorDataRecord);
            List<SensorDataRecord> sensorDataRecords = sensorDataRecordDAO.getAll();

            for (SensorDataRecord s : sensorDataRecords) {
                Log.e(TAG," Accele: "+ s.getmAccele_str());
                Log.e(TAG," AmbientTemperature: "+ s.getmAmbientTemperature_str());
                Log.e(TAG," Gravity: "+ s.getmGravity_str());

                Log.e(TAG," Gyroscope: "+ s.getmGyroscope_str());
                Log.e(TAG," Light: "+ s.getmLight_str());
                Log.e(TAG," LinearAcceleration: "+ s.getmLinearAcceleration_str());
                Log.e(TAG," MagneticField: "+ s.getmMagneticField_str());
                Log.e(TAG," Pressure: "+ s.getmPressure_str());
                Log.e(TAG," Proximity: "+ s.getmProximity_str());
                Log.e(TAG," RelativeHumidity: "+ s.getmRelativeHumidity_str());
                Log.e(TAG," RotationVector: "+ s.getmRotationVector_str());

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
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

    /** handle sensor **/
    /**register sensor - Not sure**/
    protected void RegisterAvailableSensors(){
        mSensorManager=(SensorManager) mApplicationContext.getSystemService(SENSOR_SERVICE);
        SensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor s : SensorList){
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(s.getType()), SensorManager.SENSOR_DELAY_NORMAL);
        }
        Log.d(LOG_TAG, "in register all available sensors" );
    }
    /** get sensor values **/
    @Override
    public void onSensorChanged(SensorEvent event) {

        /**Motion Sensor**/
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mAccele_str = saveRecordToStream(STRING_PHONE_SENSOR_ACCELEROMETER, event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            mGyroscope_str = saveRecordToStream(STRING_PHONE_SENSOR_GYROSCOPE, event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
            mGravity_str = saveRecordToStream(STRING_PHONE_SENSOR_GRAVITY, event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            mLinearAcceleration_str = saveRecordToStream(STRING_PHONE_SENSOR_LINEAR_ACCELERATION, event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            mRotationVector_str = saveRecordToStream(STRING_PHONE_SENSOR_ROTATION_VECTOR, event.values);
        }

        /**Position Sensor**/
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            //Log.d(LOG_TAG, "in [onSensorChange] Proximity: " +  event.values[0] );
            mProximity_str = saveRecordToStream(STRING_PHONE_SENSOR_PROXIMITY, event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            //Log.d(LOG_TAG, "in [onSensorChange] Proximity: " +  event.values[0] );
            mMagneticField_str = saveRecordToStream(STRING_PHONE_SENSOR_MAGNETIC_FIELD, event.values);
        }
        /*if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){

        }*/

        /**Environment Sensor**/
        if (event.sensor.getType() == Sensor.TYPE_LIGHT){
            mLight_str = saveRecordToStream(STRING_PHONE_SENSOR_LIGHT, event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
            mAmbientTemperature_str = saveRecordToStream(STRING_PHONE_SENSOR_AMBIENT_TEMPERATURE, event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE){
            mPressure_str = saveRecordToStream(STRING_PHONE_SENSOR_PRESSURE, event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY){
            mRelativeHumidity_str = saveRecordToStream(STRING_PHONE_SENSOR_RELATIVE_HUMIDITY, event.values);
        }

        /**health related**/
        /*if (event.sensor.getType() == Sensor.TYPE_HEART_RATE){
            getHeartRate (event);
        }
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            getStepCounter(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            getStepDetector(event);
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * In PhoneSensorManager, all the values are float numbers
     */
    protected String saveRecordToStream (String sourceName, float[] values) {

        /** store values into a Record so that we can store them in the local database **/
        //SensorDataRecord newSensorDataRecord = new SensorDataRecord( );
        //newSensorDataRecord.setSource(sourceName);

        /** create data in a JSON Object. Each CotnextSource will have different formats.
         * So we need each ContextSourceMAnager to implement this part**/
        /*JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();*/
        String data = "";

        for (int i=0; i< values.length; i++) {
            data = data + values[i];
            if (i==values.length-1)
                break;
            else
                data = data + ", ";
        }

        data = sourceName+": "+data;
//        Log.d(TAG, "data  "+ data);

        /*** Set data to SensorDataRecord **/
        //newSensorDataRecord.setData(data);
        //Log.d(LOG_TAG, "in SaveRecordtostream " +  newSensorDataRecord.getSource() + newSensorDataRecord.getData() );

        return data;
        /** Save to stream**/
        // mLocalRecordPool.add(record);
        //updateStream(ChangeJsonForm(newSensorDataRecord));
    }

    /**get Accelerometer values**/
    private void getAccelerometer(SensorEvent event) {
        Log.d(LOG_TAG, "getting accelerometer:" + mAccele_x + " : " +  mAccele_y +  " : " + mAccele_y);

        mAccele_x = event.values[0];    // Acceleration force along the x axis (including gravity). m/s2
        mAccele_y = event.values[1];    // Acceleration force along the y axis (including gravity). m/s2
        mAccele_z = event.values[2];    // Acceleration force along the z axis (including gravity). m/s2

        saveRecordToStream(STRING_PHONE_SENSOR_ACCELEROMETER, event.values);
    }


    /**get Gyroscope values**/
    private void getGyroscope(SensorEvent event) {
        mGyroscope_x = event.values[0]; // Rate of rotation around the x axis. rad/s
        mGyroscope_y = event.values[1]; // Rate of rotation around the y axis. rad/s
        mGyroscope_z = event.values[2]; // Rate of rotation around the z axis. rad/s

        saveRecordToStream(STRING_PHONE_SENSOR_GYROSCOPE, event.values);

    }


    /**get gravity values**/
    private void getGravity(SensorEvent event) {
        mGravity_x = event.values[0];   // Force of gravity along the x axis m/s2
        mGravity_y = event.values[1];   // Force of gravity along the y axis m/s2
        mGravity_z = event.values[2];   // Force of gravity along the z axis m/s2

        saveRecordToStream(STRING_PHONE_SENSOR_GRAVITY, event.values);
    }
    /**get linear acceleration values**/
    private void getLinearAcceleration(SensorEvent event) {
        mLinearAcceleration_x = event.values[0];    //Acceleration force along the x axis (excluding gravity).  m/s2
        mLinearAcceleration_y = event.values[1];    //Acceleration force along the y axis (excluding gravity).  m/s2
        mLinearAcceleration_z = event.values[2];    //Acceleration force along the z axis (excluding gravity).  m/s2

        saveRecordToStream(STRING_PHONE_SENSOR_LINEAR_ACCELERATION, event.values);
    }

    /**get rotation vector values**/
    private void getRotationVector(SensorEvent event) {
        mRotationVector_x_sin = event.values[0];    // Rotation vector component along the x axis (x * sin(�c/2))  Unitless
        mRotationVector_y_sin = event.values[1];    // Rotation vector component along the y axis (y * sin(�c/2)). Unitless
        mRotationVector_z_sin = event.values[2];    //  Rotation vector component along the z axis (z * sin(�c/2)). Unitless
        mRotationVector_cos = event.values[3];      // Scalar component of the rotation vector ((cos(�c/2)).1 Unitless

        saveRecordToStream(STRING_PHONE_SENSOR_ROTATION_VECTOR, event.values);
    }



    /**get magnetic field values**/
    private void getMagneticField(SensorEvent event){
        mMagneticField_x = event.values[0]; // Geomagnetic field strength along the x axis.
        mMagneticField_y = event.values[1]; // Geomagnetic field strength along the y axis.
        mMagneticField_z = event.values[2]; // Geomagnetic field strength along the z axis.

        saveRecordToStream(STRING_PHONE_SENSOR_MAGNETIC_FIELD, event.values);
    }

    /**get proximity values**/
    private void getProximity(SensorEvent event){

//        Log.d(LOG_TAG, "getting proximity" + mProximity);

        mProximity = event.values[0];

        saveRecordToStream(STRING_PHONE_SENSOR_PROXIMITY, event.values);
    }

    private void getAmbientTemperature(SensorEvent event){
        /* Environment Sensors */
        mAmbientTemperature = event.values[0];

        saveRecordToStream(STRING_PHONE_SENSOR_AMBIENT_TEMPERATURE, event.values);

    }

    private void getLight(SensorEvent event){

        Log.d(LOG_TAG, "getting light" + mLight);

        mLight = event.values[0];

        saveRecordToStream(STRING_PHONE_SENSOR_LIGHT, event.values);
    }

    private void getPressure(SensorEvent event){
        mPressure = event.values[0];

        saveRecordToStream(STRING_PHONE_SENSOR_PRESSURE, event.values);
    }

    private void getRelativeHumidity(SensorEvent event){
        mRelativeHumidity = event.values[0];

        saveRecordToStream(STRING_PHONE_SENSOR_RELATIVE_HUMIDITY, event.values);
    }

    private void getHeartRate (SensorEvent event) {
        mHeartRate = event.values[0];

        saveRecordToStream(STRING_PHONE_SENSOR_HEART_RATE, event.values);
    }

    private void getStepCounter (SensorEvent event) {
        mStepCount = event.values[0];

        saveRecordToStream(STRING_PHONE_SENSOR_STEP_COUNTER, event.values);

    }

    private void getStepDetector (SensorEvent event) {
        mStepDetect = event.values[0];

        saveRecordToStream(STRING_PHONE_SENSOR_STEP_DETECTOR, event.values);
    }



}