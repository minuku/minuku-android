package labelingStudy.nctu.minuku.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.Session;


/**
 * Created by Lawrence on 2017/6/5.
 */

/**
 * DBHelper manages database-related operation such as storing and accessing data.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper sInstance = null;

    public static final String ID = "_id";
    public static final String TAG = "DBHelper";

    public static final String HOME_COL = "home";
    public static final String NEIGHBOR_COL = "neighbor";
    public static final String OUTSIDE_COL = "outside";
    public static final String HOME_OR_FARAWAY = "homeorfaraway";
    public static final String STATIC_OR_NOT = "staticornot";
    public static final String DEVICE = "device_id";
    public static final String USERID = "user_id";
    public static final String TIME = "time"; //timeToSQLite

//    public static final String TaskDayCount = "TaskDayCount";
//    public static final String HOUR = "hour";

    //checkFamiliarOrNot link list
    public static final String LINK_COL = "link";
    public static final String CLICKORNOT_COL = "clickornot";

    //Location and Trip
    public static final String SESSION_ID_COL = "sessionid";
    public static final String LATITUDE_COL = "latitude";
    public static final String LONGITUDE_COL = "longitude";
    public static final String ACCURACY_COL = "Accuracy";
    public static final String ALTITUDE_COL = "Altitude";
    public static final String SPEED_COL = "Speed";
    public static final String BEARING_COL = "Bearing";
    public static final String PROVIDER_COL = "Provider";
    public static final String TRIP_TRANSPORTATION_COL = "trip_transportation";
    public static final String TRIP_SITE_COL = "trip_site";
    public static final String USER_PRESS_OR_NOT_COL = "userPressOrNot";

    //ActivityRecognition
    public static final String MOST_PROBABLE_ACTIVITY_COL = "MostProbableActivity";
    public static final String PROBABLE_ACTIVITIES_COL = "ProbableActivities";

    public static final String TRIP_COL = "Trip";

    //Custom site
    public static final String CUSTOM_SITE_NAME_COL = "sitename";
    public static final String CUSTOM_SITE_LATITUDE_COL = "latitude";
    public static final String CUSTOM_SITE_LONGITUDE_COL = "longitude";

    //Convenient site
    public static final String CONVENIENT_SITE_COL = "sitename";
    public static final String CONVENIENT_SITE_LATITUDE_COL = "latitude";
    public static final String CONVENIENT_SITE_LONGITUDE_COL = "longitude";

    //Transportation
    public static final String CONFIRM_TRANSPORTATION_COL = "Transportation";
    public static final String SUSPECTED_TRANSPORTATION_TIME_COL = "suspectedTransportation_Time";
    public static final String SUSPECTED_START_TRANSPORTATION_COL = "suspectedStartTransportation";
    public static final String SUSPECTED_STOP_TRANSPORTATION_COL = "suspectedStopTransportation";
    public static final int COL_INDEX_CONFIRM_TRANSPORTATION_TIME = 2;
    public static final int COL_INDEX_SUSPECTED_TRANSPORTATION_TIME = 3;
    public static final int COL_INDEX_SUSPECTED_START_TRANSPORTATION = 4;
    public static final int COL_INDEX_SUSPECTED_STOP_TRANSPORTATION = 5;

    //ringer
    public static final String RINGER_MODE_COL = "RingerMode";
    public static final String AUDIO_MODE_COL = "AudioMode";
    public static final String STREAM_VOLUME_MUSIC_COL = "StreamVolumeMusic";
    public static final String STREAM_VOLUME_NOTIFICATION_COL = "StreamVolumeNotification";
    public static final String STREAM_VOLUME_RING_COL = "StreamVolumeRing";
    public static final String STREAM_VOLUME_VOICE_CALL_COL = "StreamVolumeVoicecall";
    public static final String STREAM_VOLUME_SYSTEM_COL = "StreamVolumeSystem";

    //battery
    public static final String BATTERY_LEVEL_COL = "BatteryLevel";
    public static final String BATTERY_PERCENTAGE_COL = "BatteryPercentage";
    public static final String BATTERY_CHARGING_STATE_COL = "BatteryChargingState";
    public static final String IS_CHARGING_COL = "isCharging";

    //connectivity
    public static final String NETWORK_TYPE_COL = "NetworkType";
    public static final String IS_NETWORK_AVAILABLE_COL = "IsNetworkAvailable";
    public static final String IS_CONNECTED_COL = "IsConnected";
    public static final String IS_WIFI_AVAILABLE_COL = "IsWifiAvailable";
    public static final String IS_MOBILE_AVAILABLE_COL = "IsMobileAvailable";
    public static final String IS_WIFI_CONNECTED_COL = "IsWifiConnected";
    public static final String IS_MOBILE_CONNECTED_COL = "IsMobileConnected";

    //AppUsage
    public static final String SCREEN_STATUS_COL = "ScreenStatus";
    public static final String LATEST_USED_APP_COL = "Latest_Used_App";
    public static final String LATEST_FOREGROUND_ACTIVITY_COL = "Latest_Foreground_Activity";

    //UserInteraction
    public static final String PRESENT_COL = "Present";
    public static final String UNLOCK_COL = "Unlock";
    public static final String BACKGROUND_COL = "Background";
    public static final String FOREGROUND_COL = "Foreground";

    //telephony
    public static final String NETWORK_OPERATOR_NAME_COL = "NetworkOperatorName";
    public static final String CALL_STATE_COL = "CallState";
    public static final String PHONE_SIGNAL_TYPE_COL = "PhoneSignalType";
    public static final String GSM_SIGNAL_STRENGTH_COL = "GsmSignalStrength";
    public static final String LTE_SIGNAL_STRENGTH_COL = "LTESignalStrength";
    //public static final String CdmaSignalStrength_col = "CdmaSignalStrength";
    public static final String CDMA_SIGNAL_STRENGTH_LEVEL_COL = "CdmaSignalStrengthLevel";

    //accessibility
    public static final String PACK_COL = "pack";
    public static final String TEXT_COL = "text";
    public static final String TYPE_COL = "type";
    public static final String EXTRA_COL = "extra";

    //sensor
    public static final String ACCELEROMETER_COL = "ACCELEROMETER";
    public static final String GYROSCOPE_COL = "GYROSCOPE";
    public static final String GRAVITY_COL = "GRAVITY";
    public static final String LINEAR_ACCELERATION_COL = "LINEAR_ACCELERATION";
    public static final String ROTATION_VECTOR_COL = "ROTATION_VECTOR";
    public static final String PROXIMITY_COL = "PROXIMITY";
    public static final String MAGNETIC_FIELD_COL = "MAGNETIC_FIELD";
    public static final String LIGHT_COL = "LIGHT";
    public static final String PRESSURE_COL = "PRESSURE";
    public static final String RELATIVE_HUMIDITY_COL = "RELATIVE_HUMIDITY";
    public static final String AMBIENT_TEMPERATURE_COL = "AMBIENT_TEMPERATURE";

    //session
//    public static final String Sessionid_col = "Sessionid";

    //records
    public static final String COL_DATA = "data";
    public static final int COL_INDEX_RECORD_ID = 0;
    public static final int COL_INDEX_RECORD_SESSION_ID = 1;
    public static final int COL_INDEX_RECORD_DATA = 2;
    public static final int COL_INDEX_RECORD_TIMESTAMP_STRING = 3;
    public static final int COL_INDEX_RECORD_TIMESTAMP_LONG = 4;

    //session
    public static final String COL_ID = "_id";
    public static final String COL_SESSION_MODIFIED_FLAG = "session_modified_flag";
    public static final String COL_SESSION_START_TIME = "session_start_time";
    public static final String COL_SESSION_END_TIME = "session_end_time";
    public static final String COL_SESSION_USER_PRESS_OR_NOT_FLAG = "userPressOrNot";
    public static final String COL_SESSION_SENT_OR_NOT_FLAG = "sentOrNot";
    public static final String COL_SESSION_TYPE = "type";
    public static final String COL_SESSION_ID = "session_id";
    public static final String COL_SESSION_CREATED_TIME = "session_created_time";
    public static final String COL_SESSION_ANNOTATION_SET = "session_annotation_set";
    public static final int COL_INDEX_SESSION_ID = 0;
    public static final int COL_INDEX_SESSION_CREATED_TIME = 1;
    public static final int COL_INDEX_SESSION_START_TIME = 2;
    public static final int COL_INDEX_SESSION_END_TIME= 3;
    public static final int COL_INDEX_SESSION_ANNOTATION_SET= 4;
    public static final int COL_INDEX_SESSION_MODIFIED_FLAG= 5;
    public static final int COL_INDEX_SESSION_USER_PRESS_OR_NOT_FLAG = 6;
    public static final int COL_INDEX_SESSION_SENT_OR_NOT_FLAG = 7;
    public static final int COL_INDEX_SESSION_TYPE = 8;

    //Annotation
    public static final String START_TIME_COL = "StartTime";
    public static final String END_TIME_COL = "EndTime";
    public static final String START_TIME_STRING_COL = "StartTimeString";
    public static final String END_TIME_STRING_COL = "EndTimeString";
    public static final String ACTIVITY_COL = "Activity";
    public static final String ANNOTATION_GOAL_COL = "Annotate_Goal";
    public static final String ANNOTATION_SPECIAL_EVENT_COL = "Annotate_SpecialEvent";
    public static final String SITE_NAME_COL = "SiteName";
    public static final String UPLOADED_COL = "uploaded";

    //table name
    public static final String LOCATION_TABLE = "Location";
    public static final String ACTIVITY_RECOGNITION_TABLE = "ActivityRecognition";
    public static final String TRANSPORTATION_MODE_TABLE = "TransportationMode";
    public static final String ANNOTATE_TABLE = "Annotate";
    public static final String TRIP_TABLE = "Trip";
    public static final String RINGER_TABLE = "Ringer";
    public static final String BATTERY_TABLE = "Battery";
    public static final String CONNECTIVITY_TABLE = "Connectivity";
    public static final String APP_USAGE_TABLE = "AppUsage";
    public static final String USER_INTERACTION_TABLE = "UserInteraction";
    public static final String CUSTOM_SITE_TABLE = "Customsite";
    public static final String CONVENIENT_SITE_TABLE = "Convenientsite";
    public static final String TELEPHONY_TABLE = "Telephony";
    public static final String ACCESSIBILITY_TABLE = "Accessibility";
    public static final String SENSOR_TABLE = "Sensor";
    public static final String SESSION_TABLE_NAME = "Session_Table";


    public static final String DATABASE_NAME = "MySQLite.db";
    public static int DATABASE_VERSION = 1;

    private SQLiteDatabase db;

    /**
     * Initialize database
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        initiateDBManager();
    }

    public static DBHelper getInstance(Context applicationContext) {
        if (sInstance == null) {
            sInstance = new DBHelper(applicationContext);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("db","oncreate");

        createConvenientSiteTable(db);
        createCustomSiteTable(db);
        createSessionTable(db);
        createTransportationModeTable(db);
        createARTable(db);
        createLocationTable(db);
        createTripTable(db);
        createRingerTable(db);
        createBatteryTable(db);
        createUserInteractionTable(db);
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

    /**
     * Create convenient site table
     */
    public void createConvenientSiteTable(SQLiteDatabase db) {
        Log.d(TAG,"create CustomSite table");

        String cmd = "CREATE TABLE " +
                CONVENIENT_SITE_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                CONVENIENT_SITE_COL + " TEXT, " +
                CONVENIENT_SITE_LATITUDE_COL + " FLOAT, " +
                CONVENIENT_SITE_LONGITUDE_COL + " FLOAT " +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create custom site table
     */
    public void createCustomSiteTable(SQLiteDatabase db) {
        Log.d(TAG,"create CustomSite table");

        String cmd = "CREATE TABLE " +
                CUSTOM_SITE_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                CUSTOM_SITE_NAME_COL + " TEXT, " +
                CUSTOM_SITE_LATITUDE_COL + " FLOAT, " +
                CUSTOM_SITE_LONGITUDE_COL + " FLOAT " +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create annotation table
     * {@link labelingStudy.nctu.minuku.model.Annotation}
     */
    public void createAnnotationTable(SQLiteDatabase db) {
        Log.d(TAG,"create annotation table");

        String cmd = "CREATE TABLE " +
                ANNOTATE_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL," +
                START_TIME_COL + " TEXT NOT NULL," +
                END_TIME_COL + " TEXT NOT NULL," +
                START_TIME_STRING_COL + " TEXT NOT NULL," +
                END_TIME_STRING_COL + " TEXT NOT NULL," +
                SESSION_ID_COL + " TEXT," +
                ACTIVITY_COL + " TEXT," +
                ANNOTATION_GOAL_COL + " TEXT," +
                ANNOTATION_SPECIAL_EVENT_COL + " TEXT," +
                SITE_NAME_COL + " TEXT," +
                UPLOADED_COL + " BOOLEAN" +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create accessibility table
     * {@link labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord}
     */
    public void createAccessibilityTable(SQLiteDatabase db) {

        Log.d(TAG, "create accessibility table");

        String cmd = "CREATE TABLE " +
                ACCESSIBILITY_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL," +//"ID integer PRIMARY KEY AUTOINCREMENT," +
                TIME + " TEXT NOT NULL," +
                PACK_COL + " TEXT," +
                TEXT_COL + " TEXT," +
                TYPE_COL + " TEXT," +
                EXTRA_COL + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create activity recognition table
     * {@link labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord}
     */
    public void createARTable(SQLiteDatabase db) {
        Log.d(TAG,"create AR table");

        String cmd = "CREATE TABLE " +
                ACTIVITY_RECOGNITION_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                MOST_PROBABLE_ACTIVITY_COL + " TEXT," +
                PROBABLE_ACTIVITIES_COL + " TEXT " +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create app usage table
     * {@link labelingStudy.nctu.minuku.model.DataRecord.AppUsageDataRecord}
     */
    public void createAppUsageTable(SQLiteDatabase db) {
        Log.d(TAG,"create AppUsage table");

        String cmd = "CREATE TABLE " +
                APP_USAGE_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                SCREEN_STATUS_COL + " TEXT," +
                LATEST_USED_APP_COL + " TEXT," +
                LATEST_FOREGROUND_ACTIVITY_COL + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create battery table
     * {@link labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord}
     */
    public void createBatteryTable(SQLiteDatabase db) {
        Log.d(TAG,"create Battery table");

        String cmd = "CREATE TABLE " +
                BATTERY_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                BATTERY_LEVEL_COL + " INTEGER," +
                BATTERY_PERCENTAGE_COL + " FLOAT," +
                BATTERY_CHARGING_STATE_COL + " TEXT," +
                IS_CHARGING_COL + " BOOLEAN" +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create connectivity table
     * {@link labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord}
     */
    public void createConnectivityTable(SQLiteDatabase db) {
        Log.d(TAG,"create Connectivity table");

        String cmd = "CREATE TABLE " +
                CONNECTIVITY_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                NETWORK_TYPE_COL + " TEXT," +
                IS_NETWORK_AVAILABLE_COL + " BOOLEAN," +
                IS_CONNECTED_COL + " BOOLEAN," +
                IS_WIFI_AVAILABLE_COL + " BOOLEAN," +
                IS_MOBILE_AVAILABLE_COL + " BOOLEAN," +
                IS_WIFI_CONNECTED_COL + " BOOLEAN," +
                IS_MOBILE_CONNECTED_COL + " BOOLEAN" +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create location table
     * {@link labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord}
     */
    public void createLocationTable(SQLiteDatabase db) {
        Log.d(TAG,"create location table");

        String cmd = "CREATE TABLE " +
                LOCATION_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                LATITUDE_COL + " FLOAT," +
                LONGITUDE_COL + " FLOAT, " +
                ACCURACY_COL + " FLOAT, " +
                ALTITUDE_COL + " FLOAT," +
                SPEED_COL + " FLOAT," +
                BEARING_COL + " FLOAT," +
                PROVIDER_COL + " TEXT," +
                COL_SESSION_ID + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create ringer table
     * {@link labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord}
     */
    public void createRingerTable(SQLiteDatabase db) {
        Log.d(TAG,"create Ringer table");

        String cmd = "CREATE TABLE " +
                RINGER_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                RINGER_MODE_COL + " TEXT," +
                AUDIO_MODE_COL + " TEXT," +
                STREAM_VOLUME_MUSIC_COL + " INTEGER," +
                STREAM_VOLUME_NOTIFICATION_COL + " INTEGER," +
                STREAM_VOLUME_RING_COL + " INTEGER," +
                STREAM_VOLUME_VOICE_CALL_COL + " INTEGER," +
                STREAM_VOLUME_SYSTEM_COL + " INTEGER" +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create sensor table
     * {@link labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord}
     */
    private void createSensorTable(SQLiteDatabase db) {

        Log.d(TAG, "create sensor table");

        String cmd = "CREATE TABLE " +
                SENSOR_TABLE + "(" +
                ID + "ID integer PRIMARY KEY AUTOINCREMENT," +
                TIME + " TEXT NOT NULL," +
                ACCELEROMETER_COL + " TEXT," +
                GYROSCOPE_COL + " TEXT," +
                GRAVITY_COL + " TEXT," +
                LINEAR_ACCELERATION_COL + " TEXT," +
                ROTATION_VECTOR_COL + " TEXT," +
                PROXIMITY_COL + " TEXT," +
                MAGNETIC_FIELD_COL + " TEXT," +
                LIGHT_COL + " TEXT," +
                PRESSURE_COL + " TEXT," +
                RELATIVE_HUMIDITY_COL + " TEXT," +
                AMBIENT_TEMPERATURE_COL + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create trip table
     */
    public void createTripTable(SQLiteDatabase db) {

        Log.d(TAG,"create trip table");

        String cmd = "CREATE TABLE " +
                TRIP_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                SESSION_ID_COL + " TEXT," +
                LATITUDE_COL + " FLOAT," +
                LONGITUDE_COL + " FLOAT, " +
                ACCURACY_COL + " FLOAT," +
                TRIP_TRANSPORTATION_COL + " TEXT, " +
                TRIP_SITE_COL + " TEXT, " +
                USER_PRESS_OR_NOT_COL + " TEXT" +
                ");";

        db.execSQL(cmd);
    }
    
    /**
     * Create telephony table {@link labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord}
     */
    public void createTelephonyTable(SQLiteDatabase db) {

        Log.d(TAG,"create telephony table");

        String cmd = "CREATE TABLE " +
                TELEPHONY_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL," +
                TIME + " TEXT NOT NULL," +
                NETWORK_OPERATOR_NAME_COL + " TEXT," +
                CALL_STATE_COL + " INT," +
                PHONE_SIGNAL_TYPE_COL + " INT," +
                GSM_SIGNAL_STRENGTH_COL + " INT," +
                LTE_SIGNAL_STRENGTH_COL + " INT," +
                CDMA_SIGNAL_STRENGTH_LEVEL_COL + " INT" +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create transportation mode table
     */
    public void createTransportationModeTable(SQLiteDatabase db) {
        Log.d(TAG,"create TransportationMode table");

        String cmd = "CREATE TABLE " +
                TRANSPORTATION_MODE_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                CONFIRM_TRANSPORTATION_COL + " TEXT, " +
                SUSPECTED_TRANSPORTATION_TIME_COL + " TEXT, " +
                SUSPECTED_START_TRANSPORTATION_COL + " TEXT, " +
                SUSPECTED_STOP_TRANSPORTATION_COL + " TEXT " +
                ");";

        db.execSQL(cmd);
    }
    
    public void createUserInteractionTable(SQLiteDatabase db) {
        Log.d(TAG,"create UserInteraction table");

        String cmd = "CREATE TABLE " +
                USER_INTERACTION_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL, " +
                PRESENT_COL + " TEXT, " +
                UNLOCK_COL + " TEXT, " +
                BACKGROUND_COL + " TEXT, " +
                FOREGROUND_COL + " TEXT " +
                ");";

        db.execSQL(cmd);
    }

    /**
     * Create session table
     * {@link Session}
     */
    public void createSessionTable(SQLiteDatabase db) {

        Log.d(TAG, "createSessionTable");

        String cmd = "CREATE TABLE" + " " +
                SESSION_TABLE_NAME + " ( " +
                COL_ID + " "  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SESSION_CREATED_TIME + " INTEGER NOT NULL, " +
                COL_SESSION_START_TIME + " INTEGER NOT NULL, " +
                COL_SESSION_END_TIME + " INTEGER, " +
                COL_SESSION_ANNOTATION_SET + " TEXT, " +
                COL_SESSION_MODIFIED_FLAG + " INTEGER, " +
                COL_SESSION_USER_PRESS_OR_NOT_FLAG + " INTEGER, " +
                COL_SESSION_SENT_OR_NOT_FLAG + " INTEGER, " +
                COL_SESSION_TYPE + " TEXT " +
                ");" ;

        db.execSQL(cmd);
    }

    /**
     * Insert a site name with location to the ConvenientSiteTable
     */
    public static void insertConvenientSiteTable(String siteName, LatLng markerLocation) {

        ContentValues values = new ContentValues();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            values.put(DBHelper.CONVENIENT_SITE_COL, siteName);
            values.put(DBHelper.CONVENIENT_SITE_LATITUDE_COL, markerLocation.latitude);
            values.put(DBHelper.CONVENIENT_SITE_LONGITUDE_COL, markerLocation.longitude);

            db.insert(DBHelper.CONVENIENT_SITE_TABLE, null, values);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            values.clear();
            DBManager.getInstance().closeDatabase(); // Closing database connection
        }
    }

    /**
     * Insert a site name with location to the CustomizedSiteTable
     */
    public static void insertCustomizedSiteTable(String siteName, LatLng markerLocation) {

        ContentValues values = new ContentValues();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            values.put(DBHelper.CUSTOM_SITE_NAME_COL, siteName);
            values.put(DBHelper.CUSTOM_SITE_LATITUDE_COL, markerLocation.latitude);
            values.put(DBHelper.CUSTOM_SITE_LONGITUDE_COL, markerLocation.longitude);

            db.insert(DBHelper.CUSTOM_SITE_TABLE, null, values);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            values.clear();
            DBManager.getInstance().closeDatabase(); // Closing database connection
        }
    }

    /**
     * Get all stored CustomizedSites from database
     */
    public static ArrayList<String> queryCustomizedSites() {

        ArrayList<String> result = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            String sql = "SELECT *" + " FROM " + CUSTOM_SITE_TABLE;

            Log.d(TAG, "[test show trip querySession] the query statement is " + sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {

                String curRow = "";
                for (int i = 0; i < columnCount; i++) {

                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                result.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();


        } catch (Exception e) {

        }

        return result;
    }

    /**
     * Insert a session to database
     */
    public static long insertSessionTable(Session session) {

        Log.d(TAG, "test trip put session " + session.getId() + " to table " + SESSION_TABLE_NAME);

        long rowId;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

//            values.put(COL_TASK_ID, session.getTaskId());
            values.put(COL_SESSION_CREATED_TIME, session.getCreatedTime());
            values.put(COL_SESSION_START_TIME, session.getStartTime());
            values.put(COL_SESSION_ANNOTATION_SET, session.getAnnotationsSet().toJSONObject().toString());

            if (session.getEndTime() != 0) {
                values.put(COL_SESSION_END_TIME, session.getEndTime());
            }

            int sessionIsUserPress = 0;
            if (session.isUserPress()) {
                sessionIsUserPress = 1;
            }

            int sessionIsModified = 0;
            if (session.isModified()) {
                sessionIsModified = 1;
            }

            int sessionIsSent = session.getIsSent();
            
            values.put(COL_SESSION_USER_PRESS_OR_NOT_FLAG, sessionIsUserPress);
            values.put(COL_SESSION_MODIFIED_FLAG, sessionIsModified);
            values.put(COL_SESSION_SENT_OR_NOT_FLAG, sessionIsSent);
            values.put(COL_SESSION_TYPE, session.getType());

            //get row number after the insertion
            Log.d(TAG, "[test combine] insert session: " + values.toString());

            rowId = db.insert(SESSION_TABLE_NAME, null, values);

        } catch (Exception e) {
            e.printStackTrace();
            rowId = Constants.DATABASE_INVALID_VALUE;
        }

        DBManager.getInstance().closeDatabase();

        return rowId;
    }

    /**
     * Remove session with given id from database
     */
    public static void deleteSession(int sessionId) {

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            db.delete(SESSION_TABLE_NAME, COL_ID + " = " + sessionId, null);
        } catch (Exception e) {

        }

    }

    /**
     * Get next data of the datum with id, that is, get the datum with id + 1 from the required table
     * @param tableName the required table
     * @param id the id of the datum that is in front of desired one
     * @return the datum that index = id from the table
     */
    public static ArrayList<String> queryNextData(String tableName, int id) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            String sql = "SELECT *"  + " FROM " + tableName +
                    " where " + COL_ID + " = " + (id + 1);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {

                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }

        return rows;
    }

    /**
     * Get the session from database with given sessionId
     * @param sessionId id of the desired session
     * @return the desired session
     */
    public static ArrayList<String> querySession(int sessionId) {

        Log.d(TAG, "[test show trip]query session in DBHelper with session id" + sessionId);

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            String sql = "SELECT *"  + " FROM " + SESSION_TABLE_NAME +
                    //condition with session id
                    " where " + COL_ID + " = " + sessionId;

            Log.d(TAG, "[test show trip querySession] the query statement is " + sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {

                String curRow = "";
                for (int i = 0; i < columnCount; i++) {

                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();


        } catch (Exception e) {

        }

        Log.d(TAG, "[test show trip] the session is " + rows);

        return rows;
    }

    public static ArrayList<String> querySessionsBetweenTimes(long startTime, long endTime, String order) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + SESSION_TABLE_NAME +

//                    " where " + COL_SESSION_START_TIME + " > " + startTime + " and " +
//                    COL_SESSION_START_TIME + " < " + endTime +
                    " order by " + COL_SESSION_START_TIME + " " + order;

            Log.d(TAG, "[test show trip querySessionsBetweenTimes] test order the query statement is " + sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }


        return rows;

    }

    /**
     * Get sessions which started between startTime and endTime
     * @return sessions that fit the requirement in a ArrayList
     */
    public static ArrayList<String> querySessionsBetweenTimes(long startTime, long endTime) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + SESSION_TABLE_NAME +
                    " where "
                    /* + COL_SESSION_USER_PRESS_OR_NOT_FLAG + " = 1" + " and "*/
                    + COL_SESSION_START_TIME + " > " + startTime + " and " +
                    COL_SESSION_START_TIME + " < " + endTime +
                    " order by " + COL_SESSION_START_TIME + " DESC ";

            Log.d(TAG, "test combine [querySessionsBetweenTimes] the query statement is " + sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {

                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }


        return rows;

    }

    /**
     * Get sessions which started between startTime and endTime with either ascendant order or descendant order
     * @return sessions that fit the requirement in a ArrayList with order
     */
    public static ArrayList<String> querySessionsBetweenTimesAndOrder(long startTime, long endTime, String order) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            String sql = "SELECT *"  + " FROM " + SESSION_TABLE_NAME +
                    " where "
                    /* + COL_SESSION_USER_PRESS_OR_NOT_FLAG + " = 1" + " and "*/
                    + COL_SESSION_START_TIME + " > " + startTime + " and " +
                    COL_SESSION_START_TIME + " < " + endTime +
                    " order by " + COL_SESSION_START_TIME + " " + order + ", " + COL_SESSION_END_TIME + " " + order;

            Log.d(TAG, "test combine [querySessionsBetweenTimes] the query statement is " + sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {

                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();
//            Log.d(TAG,"cursor.getCount : " + cursor.getCount());

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }


        return rows;

    }

    //query task table
    public static ArrayList<String> queryModifiedSessions () {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + SESSION_TABLE_NAME + " where " +
                    COL_SESSION_MODIFIED_FLAG + " = 1";

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }


        return rows;

    }


    //query task table

    /**
     * Get all sessions in the database
     * @return
     */
    public static ArrayList<String> querySessions () {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + DBHelper.SESSION_TABLE_NAME +
                    " order by " + COL_SESSION_START_TIME + " DESC ";

            Log.d(TAG, "[queryLastRecord] the query statement is " + sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }
        Log.d(TAG, "[test show trip] the sessions are" + " " + rows);

        return rows;
    }

    /**
     * Get all sessions from database with the required order
     * @param order requiring order
     * @return
     */
    public static ArrayList<String> querySessions (String order) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + DBHelper.SESSION_TABLE_NAME +
                    " order by " + COL_SESSION_START_TIME + " " +order;

            Log.d(TAG, "[queryLastRecord] the query statement is " + sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }
        Log.d(TAG, "[test show trip] the sessions are" + " " + rows);

        return rows;
    }

    public static ArrayList<String> queryUnSentSessions() {

        Log.d(TAG, "[test show trip] queryUnSentSessions");

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + DBHelper.SESSION_TABLE_NAME +
                    " WHERE " + DBHelper.COL_SESSION_SENT_OR_NOT_FLAG + " = " + Constants.SESSION_SHOULD_BE_SENT_FLAG +
                    " order by " + COL_SESSION_START_TIME + " " + "ASC";

            Log.d(TAG, "[queryLastRecord] the query statement is " + sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

            Log.e(TAG, "exception", e);
        }

        Log.d(TAG, "[test show trip] the sessions are" + " " + rows);

        return rows;
    }

    /**
     * Get all sessions that started before 24 hours ago
     * @param time24HrAgo
     * @return
     */
    public static ArrayList<String> querySessions(long time24HrAgo) {

        Log.d(TAG, "[test show trip] querySessions");

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + DBHelper.SESSION_TABLE_NAME +
                    " WHERE " + DBHelper.COL_SESSION_START_TIME + " < " + time24HrAgo +
                    " AND " + DBHelper.COL_SESSION_SENT_OR_NOT_FLAG + " <> " + Constants.SESSION_IS_ALREADY_SENT_FLAG +
                    " order by " + DBHelper.COL_SESSION_START_TIME + " " + "ASC";

            Log.d(TAG, "[queryLastRecord] the query statement is " + sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

            Log.e(TAG, "exception", e);
        }

        Log.d(TAG, "[test show trip] the sessions are" + " " + rows);

        return rows;
    }

    /**
     * Get the total count of sessions
     * @return
     */
    public static long querySessionCount () {


        long count = Constants.DATABASE_INVALID_VALUE;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT * "  + " FROM " + SESSION_TABLE_NAME ;
            Cursor cursor = db.rawQuery(sql, null);
            count = cursor.getCount();

            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }
        return count;

    }

    /**
     * Get the last session in the database
     * @param tableName
     * @param sessionId
     * @return
     */
    public static ArrayList<String> queryLastRecord(String tableName, int sessionId) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + tableName +
                    " where " + COL_SESSION_ID + " = " + sessionId +
                    " order by " + COL_ID + " DESC LIMIT 1";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                Log.d(TAG, "[queryLastRecord] get result row " + curRow);

                rows.add(curRow);
            }
            cursor.close();


            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }


        return rows;
    }

    /**
     * Get the last session in the database
     * @return
     */
    public static ArrayList<String> queryLastSession() {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + SESSION_TABLE_NAME  +
                    " order by " + COL_ID + " DESC LIMIT 1";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                Log.d(TAG, "[test combine queryLastRecord] get result row " + curRow);

                rows.add(curRow);
            }
            cursor.close();


            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }


        return rows;

    }

    /**
     * Get last two sessions in database
     * @return
     */
    public static ArrayList<String> queryLastTwoSessions() {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + SESSION_TABLE_NAME  +
                    " order by " + COL_ID + " DESC LIMIT 2";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }
                Log.d(TAG, "[test combine queryLastRecord] get result row " + curRow);

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }

        return rows;

    }

    /**
     * Get data that time between startTime and endTime from required table
     * @return
     */
    public static ArrayList<String> queryRecordsBetweenTimes(String tableName, long startTime, long endTime) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + tableName  +
                    " where " +  TIME + " > " + startTime + " and " +
                    TIME + " < " + endTime  +
                    " order by " + TIME;

            Log.d(TAG, "[test sampling] the query statement is " + sql);

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
//                    Log.d(TAG, "[queryRecordsInSession][testgetdata] column " + i + " content: " + cursor.getString(i));
                    curRow += cursor.getString(i) + Constants.DELIMITER;

                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();


        } catch (Exception e) {

        }

        Log.d(TAG, "[test sampling] the rsult is " + rows);
        return rows;
    }

    /**
     * Get the last transportation which suspected start transportation fit the required transportation.
     * In this version of Minuku, we use this function with a confirmActivity as the parameter.
     * In this case, we could access the transportation datum which was created ,while the given confirmActivity was in the suspected-start phase, that is, it was still a suspected  start activity,
     * and we can get the information at that moment.
     *
     * Briefly to say, the function help us to access the suspected-start time of the given transportation, that is, the confirmActivity,
     * and set the actual start time of the session that is corresponding to confirmActivity
     * @param transportation confirmActivity that needs to get its actual start time
     * @return
     */
    public static ArrayList<String> queryLastTransportationWithSuspectedStartTransportation(String transportation) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + TRANSPORTATION_MODE_TABLE +
                    " where " + SUSPECTED_START_TRANSPORTATION_COL + " <> " + transportation +
                    " order by " + TIME + " DESC LIMIT 1 ";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }

        return rows;
    }

    /**
     * Get the last transportation which suspected start transportation fit the required transportation.
     * In this version of Minuku, we use this function with a confirmActivity as the parameter.
     * In this case, we could access the transportation datum which was created ,while the given confirmActivity was in the suspected-stop phase, that is, it was a suspected stop activity,
     * and we can get the information at that moment.
     *
     * Briefly to say, the function help us to access the suspected-stop time of the given transportation, that is, the confirmActivity,
     * and set the actual start time of the session that is corresponding to confirmActivity
     * @param transportation confirmActivity that needs to get its end time
     * @return
     */
    public static ArrayList<String> queryTransportationWithSuspectedStopTransportation(String transportation) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + TRANSPORTATION_MODE_TABLE +
                    " where " + SUSPECTED_STOP_TRANSPORTATION_COL + " <> " + transportation +
                    " order by " + TIME + " DESC LIMIT 1 ";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }

        return rows;
    }

    /**
     * Get data of the session which id = sessionId from required table while fit the time interval
     * @return
     */
    public static ArrayList<String> queryRecordsInSession(String tableName, int sessionId, long startTime, long endTime) {

        ArrayList<String> rows = new ArrayList<String>();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + tableName  +
                    " where " + COL_SESSION_ID + " = " + sessionId + " and " +
                    TIME + " > " + startTime + " and " +
                    TIME + " < " + endTime  +
                    " order by " + TIME;

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;

                }

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();


        } catch (Exception e) {

        }

        return rows;
    }

    /**
     * Get data of the session which id = sessionId from required table
     * @return
     */
    public static ArrayList<String> queryRecordsInSession(String tableName, int sessionId) {

        ArrayList<String> rows = new ArrayList<String>();

        Log.d(TAG, "[test show trip] queryRecordsInSession ");
        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  + " FROM " + tableName  +
                    " where " + COL_SESSION_ID + " = " + sessionId +
                    " order by " + TIME;


            Log.d(TAG, "[test show trip] the query statement is " + sql);

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String curRow = "";
                for (int i = 0; i < columnCount; i++) {
                    curRow += cursor.getString(i) + Constants.DELIMITER;
                }

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }


        return rows;


    }

    /**
     * Update the startTime and endTime of the session
     * @param sessionId id of the updating session
     * @param startTime
     * @param endTime
     */
    public static void updateSessionTable(int sessionId, long startTime, long endTime) {

        String where = COL_ID + " = " +  sessionId;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_START_TIME, startTime);
            values.put(COL_SESSION_END_TIME, endTime);

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

        }
    }

    /**
     * Update the information of the session about ending a session.
     * @param sessionId id of the updating session
     * @param endTime
     * @param sessionUserPressOrNot the labeling mode
     */
    public static void updateSessionTable(int sessionId, long endTime, boolean sessionUserPressOrNot) {

        String where = COL_ID + " = " +  sessionId;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            //TODO get the col name after complete the annotate part.

            values.put(COL_SESSION_END_TIME, endTime);
            values.put(COL_SESSION_USER_PRESS_OR_NOT_FLAG, sessionUserPressOrNot);

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

            Log.d(TAG, "test combine: completing updating end time for sesssion" + ID);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the information of the session about ending a session.
     */
    public static void updateSessionTable(int sessionId, long endTime, int sessionUserPressOrNot, int modifiedOrNot) {

        String where = COL_ID + " = " + sessionId;

        Log.d(TAG, "[test triggering] sessionUserPressOrNot : " + sessionUserPressOrNot);
        Log.d(TAG, "[test triggering] modifiedOrNot : " + modifiedOrNot);

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_END_TIME, endTime);
            values.put(COL_SESSION_USER_PRESS_OR_NOT_FLAG, sessionUserPressOrNot);
            values.put(COL_SESSION_MODIFIED_FLAG, modifiedOrNot);

            db.update(SESSION_TABLE_NAME, values, where, null);

            Log.d(TAG, "[test triggering] completing updating end time for session : " + sessionId );

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "[test triggering] updating fail" );
        }

        DBManager.getInstance().closeDatabase();
    }

    /**
     * Update the end time of the session
     * @param sessionId
     * @param endTime
     */
    public static void updateSessionTable(int sessionId, long endTime) {

        String where = COL_ID + " = " +  sessionId;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            //TODO get the col name after complete the annotate part.

            /**first check if the endtime is intentionally invalid**/

            //if not
            if (endTime!=Constants.INVALID_TIME_VALUE) {
                values.put(COL_SESSION_END_TIME, endTime);
            }
            else{
                values.put(COL_SESSION_END_TIME, "");
            }

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "test trip: completing updating end time for sesssion" + ID);

    }

    /**
     * Update session information with time, annotation and flag.
     * @param sessionId
     * @param startTime
     * @param endTime
     * @param annotationSet
     * @param toBeSent
     */
    public static void updateSessionTable(int sessionId, long startTime,long endTime, AnnotationSet annotationSet, int toBeSent) {

        String where = COL_ID + " = " +  sessionId;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_START_TIME, startTime);
            values.put(COL_SESSION_END_TIME, endTime);
            //because only one data(annotation) exist.
            values.put(COL_SESSION_ANNOTATION_SET, annotationSet.toString());
            values.put(COL_SESSION_SENT_OR_NOT_FLAG, toBeSent);

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Update the flag of the session
     * @param sessionId
     * @param toBeSent
     */
    public static void updateSessionTable(int sessionId, int toBeSent) {

        String where = COL_ID + " = " +  sessionId;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_SENT_OR_NOT_FLAG, toBeSent);

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Update the annotationSet of the session
     * @param sessionId
     * @param annotationSet
     */
    public static void updateSessionTable(int sessionId, AnnotationSet annotationSet) {

        String where = COL_ID + " = " +  sessionId;

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            //because only one data(annotation) exist.
            values.put(COL_SESSION_ANNOTATION_SET, annotationSet.toString());

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

            Log.d(TAG, "[storing sitename] store successfully");

        } catch (Exception e) {

            Log.d(TAG, "[storing sitename] Exception");

            e.printStackTrace();

            Log.e(TAG, "[storing sitename]", e);

        }

    }

    public static void updateSessionTable(Session session, long endTime) {

        String where = COL_ID + " = " +  session.getId();

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            //TODO get the col name after complete the annotate part.

            /**first check if the endtime is intentionally invalid**/
            //if not
            if (endTime!=Constants.INVALID_TIME_VALUE) {
                values.put(COL_SESSION_END_TIME, endTime);
            }
            else{
                values.put(COL_SESSION_END_TIME, "");
            }

            values.put(COL_SESSION_USER_PRESS_OR_NOT_FLAG, session.isUserPress());

            values.put(COL_SESSION_MODIFIED_FLAG, 1);


            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "test trip: completing updating end time for sesssion" + ID);

    }

    /**
     * Update session information when splitting a session
     * @param tableName
     * @param splittingTime
     * @param currentSessionId
     * @param newSessionId
     */
    public static void updateRecordsInSession(String tableName, long splittingTime, int currentSessionId, int newSessionId) {

//        String where = COL_SESSION_ID + " = " + currentSessionId;

        //get the exact session id in the delimiters
        String querySessionIdInDelimiters = "( '" + Constants.SESSION_DELIMITER + "' || RTRIM(" +COL_SESSION_ID + ") || '" + Constants.SESSION_DELIMITER + "' )"
                + "LIKE ('%" +Constants.SESSION_DELIMITER + "' || " + currentSessionId + " || '" + Constants.SESSION_DELIMITER + "%')";

        String querySessionid = COL_SESSION_ID + " = " + currentSessionId;

        String afterSplitting = TIME + " > " + splittingTime;
        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_ID, newSessionId);

            db.update(tableName, values, querySessionIdInDelimiters
//                            + " or " + querySessionidBetweenSpaceAndDelimiter
//                            + " or " + querySessionid
                            + " and " + afterSplitting
                    , null);

            DBManager.getInstance().closeDatabase();

        } catch (Exception e) {

            Log.e(TAG, "SessionConcat exception", e);
        }
    }

}
