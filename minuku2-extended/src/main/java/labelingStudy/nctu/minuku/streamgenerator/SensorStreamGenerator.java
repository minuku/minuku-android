package labelingStudy.nctu.minuku.streamgenerator;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;
import labelingStudy.nctu.minuku.stream.SensorStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static android.content.Context.SENSOR_SERVICE;
import static labelingStudy.nctu.minuku.config.Constants.CONTEXT_SOURCE_INVALID_VALUE_FLOAT;

/**
 * Created by chiaenchiang on 07/03/2018.
 */

public class SensorStreamGenerator extends AndroidStreamGenerator<SensorDataRecord> implements
        SensorEventListener {
    /**  variable declaration
     **/

    private SensorStream mStream;
    private String TAG = "SensorStreamGenerator";
    private Sensor sensor;
    //SensorDataRecordDAO mDAO;
    public static labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord sensorDataRecord;
    /** Tag for logging. */
    private static final String LOG_TAG = "PhoneSensorMnger";
    public static String CONTEXT_SOURCE_PHONE_SENSOR = "PhoneSensor";
    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_NAME = "SensorValues";
    /**system components**/
    private static Context mContext;
    private static SensorManager mSensorManager ;
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

    /** handle stream **/
    /**SensorStreamGenerator**/
    public SensorStreamGenerator(Context applicationContext) {
        super(applicationContext);
        this.mStream = new SensorStream(Constants.SENSOR_QUEUE_SIZE);
        //this.mDAO = MinukuDAOManager.getInstance().getDaoFor(labelingStudy.nctu.minuku.model.DataRecord.sensorDataRecord.class);

        mContext = applicationContext;
        //call sensor manager from the service
        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);

        //initiate values of sensors
        mAccele_x = mAccele_y = mAccele_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT; //-9999
        mGyroscope_x = mGyroscope_y = mGyroscope_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mGravity_x = mGravity_y = mGravity_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mMagneticField_x = mMagneticField_y = mMagneticField_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mLinearAcceleration_x = mLinearAcceleration_y = mLinearAcceleration_z = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mRotationVector_x_sin = mRotationVector_y_sin =  mRotationVector_z_sin = mRotationVector_cos = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mHeartRate = mStepCount = mStepDetect =CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mLight = mPressure = mRelativeHumidity = mProximity = mAmbientTemperature = CONTEXT_SOURCE_INVALID_VALUE_FLOAT;

        //initiate registered sensor list
        RegisterAvailableSensors();
        this.register();  // stream
    }
    /**onStreamRegistration**/


    @Override
    public void offer(labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord dataRecord) {

    }

    /**register**/

    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which sensorDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides sensorDataRecord is already registered.");
        }
    }

    @Override
    public Stream<SensorDataRecord> generateNewStream() {
        return null;
    }


    @Override
    public boolean updateStream() {
        Log.d(TAG, "updateStream called");

        labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord sensorDataRecord = new SensorDataRecord(mAccele_str, mGyroscope_str, mGravity_str, mLinearAcceleration_str,
                mRotationVector_str, mProximity_str, mMagneticField_str, mLight_str, mPressure_str, mRelativeHumidity_str, mAmbientTemperature_str);
        mStream.add(sensorDataRecord);
        Log.d(TAG, "Sensor to be sent to event bus" + sensorDataRecord);

        //post an event
        EventBus.getDefault().post(sensorDataRecord);
        try {
            appDatabase db;
            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();
            db.sensorDataRecordDao().insertAll(sensorDataRecord);
            List<SensorDataRecord> sensorDataRecords = db.sensorDataRecordDao().getAll();

            for (SensorDataRecord s : sensorDataRecords) {
                Log.d(TAG, s.getmAccele_str());
            }


        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }



    @Override
    public long getUpdateFrequency() {
        return 1; // 1 minutes
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {

    }

    /**handle different json form convertion**/



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

        return data;
    }


}

