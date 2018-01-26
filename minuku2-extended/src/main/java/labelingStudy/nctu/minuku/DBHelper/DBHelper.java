package labelingStudy.nctu.minuku.DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONObject;

import labelingStudy.nctu.minuku.manager.DBManager;


/**
 * Created by Lawrence on 2017/6/5.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper instance = null;

    public static final String id = "_id";
    public static final String TAG = "DBHelper";

    public static final String home_col = "home";
    public static final String neighbor_col = "neighbor";
    public static final String outside_col = "outside";
    public static final String homeorfaraway = "homeorfaraway";
    public static final String staticornot = "staticornot";
    public static final String DEVICE = "device_id";
    public static final String USERID = "user_id";
    public static final String TIME = "time"; //timeToSQLite

//    public static final String TaskDayCount = "TaskDayCount";
//    public static final String HOUR = "hour";

    //checkFamiliarOrNot link list
    public static final String link_col = "link";
    public static final String clickornot_col = "clickornot";

    //Location and Trip
    public static final String sessionid_col = "sessionid";
    public static final String latitude_col = "latitude";
    public static final String longitude_col = "longitude";
    public static final String Accuracy_col = "Accuracy";
    public static final String Altitude_col = "Altitude";
    public static final String Speed_col = "Speed";
    public static final String Bearing_col = "Bearing";
    public static final String Provider_col = "Provider";
    public static final String trip_transportation_col = "trip_transportation";
    public static final String trip_site_col = "trip_site";
    public static final String userPressOrNot_col = "userPressOrNot";

    //ActivityRecognition
    public static final String MostProbableActivity_col = "MostProbableActivity";
    public static final String ProbableActivities_col = "ProbableActivities";

    public static final String trip_col = "Trip";

    //Custom site
    public static final String customsitename_col = "sitename";

    //Transportation
    public static final String confirmTransportation_col = "Transportation";

    //Trip Annotate data
    public static final String Trip_updatedStatus = "UpdatedStatus";
    public static final String Trip_startTimeSecond = "Trip_startTimeSecond";
    public static final String Trip_startTime = "Trip_startTime";
    public static final String Trip_endTime = "Trip_endTime";
    public static final String Trip_id = "Trip_id";
    public static final String activityType = "ActivityType";
    public static final String tripType = "TripType";
    public static final String preplan = "Preplan";
    public static final String ans1 = "Ques1";
    public static final String ans2 = "Ques2";
    public static final String ans3 = "Ques3";
    public static final String ans4_1 = "Ques4_1";
    public static final String ans4_2 = "Ques4_2";
    public static final String lat = "latitude";
    public static final String lng = "longitude";

    public static JSONObject location_jsonObject;

    //ringer
    public static final String RingerMode_col = "RingerMode";
    public static final String AudioMode_col = "AudioMode";
    public static final String StreamVolumeMusic_col = "StreamVolumeMusic";
    public static final String StreamVolumeNotification_col = "StreamVolumeNotification";
    public static final String StreamVolumeRing_col = "StreamVolumeRing";
    public static final String StreamVolumeVoicecall_col = "StreamVolumeVoicecall";
    public static final String StreamVolumeSystem_col = "StreamVolumeSystem";

    //battery
    public static final String BatteryLevel_col = "BatteryLevel";
    public static final String BatteryPercentage_col = "BatteryPercentage";
    public static final String BatteryChargingState_col = "BatteryChargingState";
    public static final String isCharging_col = "isCharging";

    //connectivity
    public static final String NetworkType_col = "NetworkType";
    public static final String IsNetworkAvailable_col = "IsNetworkAvailable";
    public static final String IsConnected_col = "IsConnected";
    public static final String IsWifiAvailable_col = "IsWifiAvailable";
    public static final String IsMobileAvailable_col = "IsMobileAvailable";
    public static final String IsWifiConnected_col = "IsWifiConnected";
    public static final String IsMobileConnected_col = "IsMobileConnected";

    //AppUsage
    public static final String ScreenStatus_col = "ScreenStatus";
    public static final String Latest_Used_App_col = "Latest_Used_App";
    public static final String Latest_Foreground_Activity_col = "Latest_Foreground_Activity";

    //telephony
    public static final String NetworkOperatorName_col = "NetworkOperatorName";
    public static final String CallState_col = "CallState";
    public static final String PhoneSignalType_col = "PhoneSignalType";
    public static final String GsmSignalStrength_col = "GsmSignalStrength";
    public static final String LTESignalStrength_col = "LTESignalStrength";
    //public static final String CdmaSignalStrength_col = "CdmaSignalStrength";
    public static final String CdmaSignalStrengthLevel_col = "CdmaSignalStrengthLevel";

    //accessibility
    public static final String pack_col = "pack";
    public static final String text_col = "text";
    public static final String type_col = "type";
    public static final String extra_col = "extra";

    //sensor
    public static final String ACCELEROMETER_col = "ACCELEROMETER";
    public static final String GYROSCOPE_col = "GYROSCOPE";
    public static final String GRAVITY_col = "GRAVITY";
    public static final String LINEAR_ACCELERATION_col = "LINEAR_ACCELERATION";
    public static final String ROTATION_VECTOR_col = "ROTATION_VECTOR";
    public static final String PROXIMITY_col = "PROXIMITY";
    public static final String MAGNETIC_FIELD_col = "MAGNETIC_FIELD";
    public static final String LIGHT_col = "LIGHT";
    public static final String PRESSURE_col = "PRESSURE";
    public static final String RELATIVE_HUMIDITY_col = "RELATIVE_HUMIDITY";
    public static final String AMBIENT_TEMPERATURE_col = "AMBIENT_TEMPERATURE";

    //session
    public static final String Sessionid_col = "Sessionid";

    //Annotate
    public static final String StartTime_col = "StartTime";
    public static final String EndTime_col = "EndTime";
    public static final String StartTimeString_col = "StartTimeString";
    public static final String EndTimeString_col = "EndTimeString";
    public static final String Activity_col = "Activity";
    public static final String Annotation_Goal_col = "Annotate_Goal";
    public static final String Annotation_SpecialEvent_col = "Annotate_SpecialEvent";
    public static final String SiteName_col = "SiteName";
    public static final String uploaded_col = "uploaded";

    //table name
    public static final String location_table = "Location";
    public static final String activityRecognition_table = "ActivityRecognition";
    public static final String transportationMode_table = "TransportationMode";
    public static final String annotate_table = "Annotate";
    public static final String trip_table = "Trip";
    public static final String ringer_table = "Ringer";
    public static final String battery_table = "Battery";
    public static final String connectivity_table = "Connectivity";
    public static final String appUsage_table = "AppUsage";
    public static final String customsite_table = "Customsite";
    public static final String telephony_table = "Telephony";
    public static final String accessibility_table = "Accessibility";
    public static final String sensor_table = "Sensor";
    public static final String session_table = "Session";


    public static final String DATABASE_NAME = "MySQLite.db";
    public static int DATABASE_VERSION = 1;

    private SQLiteDatabase db;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        initiateDBManager();

    }

    public static DBHelper getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new DBHelper(applicationContext);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("db","oncreate");

        createCustomSiteTable(db);
        createSessionTable(db);
        createTransportationModeTable(db);
        createARTable(db);
        createLocationTable(db);
        createTripTable(db);
        createRingerTable(db);
        createBatteryTable(db);
        createConnectivityTable(db);
        createAppUsageTable(db);
        createSensorTable(db);
        createAccessibilityTable(db);
        createTelephonyTable(db);
        createAnnotationTable(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void initiateDBManager() {
        DBManager.initializeInstance(this);
    }

    public void createCustomSiteTable(SQLiteDatabase db){
        Log.d(TAG,"create CustomSite table");

        String cmd = "CREATE TABLE " +
                customsite_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                customsitename_col+" TEXT" +
                ");";

        db.execSQL(cmd);
    }

    public void createSessionTable(SQLiteDatabase db){
        Log.d(TAG,"create session table");

        String cmd = "CREATE TABLE " +
                session_table + "(" +
                id + " INTEGER PRIMARY KEY NOT NULL," +
                TIME + " TEXT NOT NULL," +
                sessionid_col + " TEXT" + //only one number not like "0, 2"
                ");";

        db.execSQL(cmd);
    }

    public void createAnnotationTable(SQLiteDatabase db){
        Log.d(TAG,"create annotation table");

        String cmd = "CREATE TABLE " +
                annotate_table + "(" +
                id + " INTEGER PRIMARY KEY NOT NULL," +
                StartTime_col + " TEXT NOT NULL," +
                EndTime_col + " TEXT NOT NULL," +
                StartTimeString_col + " TEXT NOT NULL," +
                EndTimeString_col + " TEXT NOT NULL," +
                sessionid_col + " TEXT," +
                Activity_col + " TEXT," +
                Annotation_Goal_col + " TEXT," +
                Annotation_SpecialEvent_col + " TEXT," +
                SiteName_col + " TEXT," +
                uploaded_col + " BOOLEAN" +
                ");";

        db.execSQL(cmd);
    }

    public void createTelephonyTable(SQLiteDatabase db){

        Log.d(TAG,"create telephony table");

        String cmd = "CREATE TABLE " +
                telephony_table + "(" +
                id + " INTEGER PRIMARY KEY NOT NULL," +
                TIME + " TEXT NOT NULL," +
                NetworkOperatorName_col + " TEXT," +
                CallState_col + " INT," +
                PhoneSignalType_col + " INT," +
                GsmSignalStrength_col + " INT," +
                LTESignalStrength_col + " INT," +
                CdmaSignalStrengthLevel_col + " INT" +
                ");";

        db.execSQL(cmd);
    }

    public void createTransportationModeTable(SQLiteDatabase db){
        Log.d(TAG,"create TransportationMode table");

        String cmd = "CREATE TABLE " +
                transportationMode_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                confirmTransportation_col+" TEXT" +
                ");";

        db.execSQL(cmd);

    }

    //TODO maybe need to expand
    public void createAppUsageTable(SQLiteDatabase db){
        Log.d(TAG,"create AppUsage table");

        String cmd = "CREATE TABLE " +
                appUsage_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                ScreenStatus_col+" TEXT," +
                Latest_Used_App_col+" TEXT," +
                Latest_Foreground_Activity_col+" TEXT" +
                ");";

        db.execSQL(cmd);
    }

    public void createConnectivityTable(SQLiteDatabase db){
        Log.d(TAG,"create Connectivity table");

        String cmd = "CREATE TABLE " +
                connectivity_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                NetworkType_col+" TEXT," +
                IsNetworkAvailable_col+" BOOLEAN," +
                IsConnected_col+" BOOLEAN," +
                IsWifiAvailable_col+" BOOLEAN," +
                IsMobileAvailable_col+" BOOLEAN," +
                IsWifiConnected_col+" BOOLEAN," +
                IsMobileConnected_col+" BOOLEAN" +
                ");";

        db.execSQL(cmd);
    }

    public void createBatteryTable(SQLiteDatabase db){
        Log.d(TAG,"create Battery table");

        String cmd = "CREATE TABLE " +
                battery_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                BatteryLevel_col+" INTEGER," +
                BatteryPercentage_col+" FLOAT," +
                BatteryChargingState_col+" TEXT," +
                isCharging_col+" BOOLEAN" +
                ");";

        db.execSQL(cmd);
    }

    public void createAccessibilityTable(SQLiteDatabase db){

        Log.d(TAG, "create accessibility table");

        String cmd = "CREATE TABLE " +
                accessibility_table + "(" +
                id + " INTEGER PRIMARY KEY NOT NULL," +//"ID integer PRIMARY KEY AUTOINCREMENT," +
                TIME + " TEXT NOT NULL," +
                pack_col + " TEXT," +
                text_col + " TEXT," +
                type_col + " TEXT," +
                extra_col + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    public void createRingerTable(SQLiteDatabase db){
        Log.d(TAG,"create Ringer table");

        String cmd = "CREATE TABLE " +
                ringer_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                RingerMode_col+" TEXT," +
                AudioMode_col+" TEXT," +
                StreamVolumeMusic_col+" INTEGER," +
                StreamVolumeNotification_col+" INTEGER," +
                StreamVolumeRing_col+" INTEGER," +
                StreamVolumeVoicecall_col+" INTEGER," +
                StreamVolumeSystem_col+" INTEGER" +
                ");";

        db.execSQL(cmd);
    }

    public void createARTable(SQLiteDatabase db){
        Log.d(TAG,"create AR table");

        String cmd = "CREATE TABLE " +
                activityRecognition_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                MostProbableActivity_col+" TEXT," +
                ProbableActivities_col +" TEXT " +
                ");";

        db.execSQL(cmd);

    }

    public void createLocationTable(SQLiteDatabase db){
        Log.d(TAG,"create location table");

        String cmd = "CREATE TABLE " +
                location_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                latitude_col+" FLOAT,"+
                longitude_col +" FLOAT, " +
                Accuracy_col + " FLOAT, " +
                Altitude_col +" FLOAT," +
                Speed_col +" FLOAT," +
                Bearing_col +" FLOAT," +
                Provider_col +" TEXT" +
                ");";

        db.execSQL(cmd);

    }

    public void createTripTable(SQLiteDatabase db){

        Log.d(TAG,"create trip table");

        String cmd = "CREATE TABLE " +
                trip_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                sessionid_col + " TEXT," +
                latitude_col+" FLOAT,"+
                longitude_col +" FLOAT, " +
                Accuracy_col + " FLOAT," +
                trip_transportation_col + " TEXT, " +
                trip_site_col + " TEXT, " +
                userPressOrNot_col + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    private void createSensorTable(SQLiteDatabase db) {

        Log.d(TAG, "create sensor table");

        String cmd = "CREATE TABLE " +
                sensor_table + "(" +
                id + "ID integer PRIMARY KEY AUTOINCREMENT," +
                TIME + " TEXT NOT NULL," +
                ACCELEROMETER_col + " TEXT," +
                GYROSCOPE_col + " TEXT," +
                GRAVITY_col + " TEXT," +
                LINEAR_ACCELERATION_col + " TEXT," +
                ROTATION_VECTOR_col + " TEXT," +
                PROXIMITY_col + " TEXT," +
                MAGNETIC_FIELD_col + " TEXT," +
                LIGHT_col + " TEXT," +
                PRESSURE_col + " TEXT," +
                RELATIVE_HUMIDITY_col + " TEXT," +
                AMBIENT_TEMPERATURE_col + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

}
