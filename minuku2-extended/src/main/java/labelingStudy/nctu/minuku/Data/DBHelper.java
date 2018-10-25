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
    public static final int COL_INDEX_LOC_LATITUDE = 2;
    public static final int COL_INDEX_LOC_LONGITUDE = 3;

    //ActivityRecognition
    public static final String MostProbableActivity_col = "MostProbableActivity";
    public static final String ProbableActivities_col = "ProbableActivities";

    public static final String trip_col = "Trip";

    //Custom site
    public static final String customsitename_col = "sitename";
    public static final String customsite_latitude_col = "latitude";
    public static final String customsite_longitude_col = "longitude";

    //Convenient site
    public static final String convenientsite_col = "sitename";
    public static final String convenientsite_latitude_col = "latitude";
    public static final String convenientsite_longitude_col = "longitude";

    //Transportation
    public static final String confirmTransportation_col = "Transportation";
    public static final String suspectedTransportation_Time_col = "suspectedTransportation_Time";
    public static final String suspectedStartTransportation_col = "suspectedStartTransportation";
    public static final String suspectedStopTransportation_col = "suspectedStopTransportation";
    public static final int COL_INDEX_CONFIRM_Transportation_TIME = 2;
    public static final int COL_INDEX_Suspected_Transportation_TIME= 3;
    public static final int COL_INDEX_Suspected_Start_Transportation= 4;
    public static final int COL_INDEX_Suspected_Stop_Transportation= 5;

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

    //UserInteraction
    public static final String Present_col = "Present";
    public static final String Unlock_col = "Unlock";
    public static final String Background_col = "Background";
    public static final String Foreground_col = "Foreground";

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
    public static final String COL_SESSION_USERPRESSORNOT_FLAG = "userPressOrNot";
    public static final String COL_SESSION_SENTORNOT_FLAG = "sentOrNot";
    public static final String COL_SESSION_TYPE = "type";
    public static final String COL_SESSION_HIDEDORNOT_FLAG = "hidedOrNot";
    public static final String COL_SESSION_ID = "session_id";
    public static final String COL_SESSION_CREATED_TIME = "session_created_time";
    public static final String COL_SESSION_ANNOTATION_SET = "session_annotation_set";
    public static final int COL_INDEX_SESSION_ID = 0;
    public static final int COL_INDEX_SESSION_CREATED_TIME = 1;
    public static final int COL_INDEX_SESSION_START_TIME = 2;
    public static final int COL_INDEX_SESSION_END_TIME= 3;
    public static final int COL_INDEX_SESSION_ANNOTATION_SET= 4;
    public static final int COL_INDEX_SESSION_MODIFIED_FLAG= 5;
    public static final int COL_INDEX_SESSION_USERPRESSORNOT_FLAG = 6;
    public static final int COL_INDEX_SESSION_SENTORNOT_FLAG = 7;
    public static final int COL_INDEX_SESSION_TYPE = 8;
    public static final int COL_INDEX_SESSION_HIDEDORNOT = 9;

    //ActionLog
    public static final String action_col = "Action";
    public static final String userUnlock_col = "UserUnlock";

    //table name
    public static final String location_table = "Location";
    public static final String activityRecognition_table = "ActivityRecognition";
    public static final String transportationMode_table = "TransportationMode";
    public static final String actionLog_table = "ActionLog";
    public static final String ringer_table = "Ringer";
    public static final String battery_table = "Battery";
    public static final String connectivity_table = "Connectivity";
    public static final String appUsage_table = "AppUsage";
    public static final String userInteraction_table = "UserInteraction";
    public static final String customsite_table = "Customsite";
    public static final String convenientsite_table = "Convenientsite";
    public static final String telephony_table = "Telephony";
    public static final String accessibility_table = "Accessibility";
    public static final String sensor_table = "Sensor";
    public static final String SESSION_TABLE_NAME = "Session_Table";


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

        createConvenientSiteTable(db);
        createCustomSiteTable(db);
        createSessionTable(db);

        createTransportationModeTable(db);
        createARTable(db);
        createLocationTable(db);
        createRingerTable(db);
        createBatteryTable(db);
        createConnectivityTable(db);
        createAppUsageTable(db);
        createSensorTable(db);
        createAccessibilityTable(db);
        createTelephonyTable(db);

        createActionLogTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void initiateDBManager() {
        DBManager.initializeInstance(this);
    }

    public void createConvenientSiteTable(SQLiteDatabase db){
        Log.d(TAG,"create CustomSite table");

        String cmd = "CREATE TABLE " +
                convenientsite_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                convenientsite_col+" TEXT, " +
                convenientsite_latitude_col+" TEXT, "+
                convenientsite_longitude_col +" TEXT " +
                ");";

        db.execSQL(cmd);
    }

    public void createCustomSiteTable(SQLiteDatabase db){
        Log.d(TAG,"create CustomSite table");

        String cmd = "CREATE TABLE " +
                customsite_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                customsitename_col+" TEXT, " +
                customsite_latitude_col+" TEXT, "+
                customsite_longitude_col +" TEXT " +
                ");";

        db.execSQL(cmd);
    }

    public void createActionLogTable(SQLiteDatabase db){

        //TODO add a col for inserting the user interaction into the ActionLog table
        //TODO might need to make action log insert a "" blank into the table to consistent the table size with sessionid

        String cmd = "CREATE TABLE " +
                actionLog_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                action_col+" TEXT, " +
                userUnlock_col + " TEXT "+
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
                CdmaSignalStrengthLevel_col + " INT," +
                COL_SESSION_ID + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    public void createTransportationModeTable(SQLiteDatabase db){
        Log.d(TAG,"create TransportationMode table");

        String cmd = "CREATE TABLE " +
                transportationMode_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                confirmTransportation_col + " TEXT, " +
                suspectedTransportation_Time_col + " TEXT, " +
                suspectedStartTransportation_col + " TEXT, " +
                suspectedStopTransportation_col + " TEXT, " +
                COL_SESSION_ID + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    public void createAppUsageTable(SQLiteDatabase db){
        Log.d(TAG,"create AppUsage table");

        String cmd = "CREATE TABLE " +
                appUsage_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                ScreenStatus_col+" TEXT," +
                Latest_Used_App_col+" TEXT," +
                Latest_Foreground_Activity_col+" TEXT," +
                COL_SESSION_ID + " TEXT" +
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
                IsMobileConnected_col+" BOOLEAN," +
                COL_SESSION_ID + " TEXT" +
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
                isCharging_col+" BOOLEAN," +
                COL_SESSION_ID + " TEXT" +
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
                extra_col + " TEXT," +
                COL_SESSION_ID + " TEXT" +
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
                StreamVolumeSystem_col+" INTEGER," +
                COL_SESSION_ID + " TEXT" +
        ");";

        db.execSQL(cmd);
    }

    public void createUserInteractionTable(SQLiteDatabase db){
        Log.d(TAG,"create UserInteraction table");

        String cmd = "CREATE TABLE " +
                userInteraction_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL, " +
                Present_col+" TEXT, " +
                Unlock_col+" TEXT, " +
                Background_col+" TEXT, " +
                Foreground_col+" TEXT " +
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
                ProbableActivities_col +" TEXT, " +
                COL_SESSION_ID + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    public void createLocationTable(SQLiteDatabase db){
        Log.d(TAG,"create location table");

        String cmd = "CREATE TABLE " +
                location_table + "(" +
                id+" INTEGER PRIMARY KEY NOT NULL, " +
                TIME + " TEXT NOT NULL," +
                latitude_col+" TEXT,"+
                longitude_col +" TEXT, " +
                Accuracy_col + " FLOAT, " +
                Altitude_col +" FLOAT," +
                Speed_col +" FLOAT," +
                Bearing_col +" FLOAT," +
                Provider_col +" TEXT," +
                COL_SESSION_ID + " TEXT" +
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
                AMBIENT_TEMPERATURE_col + " TEXT," +
                COL_SESSION_ID + " TEXT" +
                ");";

        db.execSQL(cmd);
    }

    public void createSessionTable(SQLiteDatabase db){

        Log.d(TAG, "createSessionTable");

        String cmd = "CREATE TABLE" + " " +
                SESSION_TABLE_NAME + " ( "+
                COL_ID + " "  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SESSION_CREATED_TIME + " INTEGER NOT NULL, " +
                COL_SESSION_START_TIME + " INTEGER NOT NULL, " +
                COL_SESSION_END_TIME + " INTEGER, " +
                COL_SESSION_ANNOTATION_SET + " TEXT, " +
                COL_SESSION_MODIFIED_FLAG + " INTEGER, " +
                COL_SESSION_USERPRESSORNOT_FLAG + " INTEGER, " +
                COL_SESSION_SENTORNOT_FLAG + " INTEGER, " +
                COL_SESSION_TYPE + " TEXT, "+
                COL_SESSION_HIDEDORNOT_FLAG + " INTEGER "+
                ");" ;

        db.execSQL(cmd);
    }

    public static long insertActionLogTable(long createdTime, String action){

        long rowId = 0;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(DBHelper.TIME, createdTime);
            values.put(DBHelper.action_col, action);
            values.put(DBHelper.userUnlock_col, "");

            rowId = db.insert(DBHelper.actionLog_table, null, values);
        }
        catch(NullPointerException e){
            //e.printStackTrace();
            rowId = -1;
        }

        return rowId;
    }

    public static long insertActionLogTable(long createdTime, String userpresent, String userunlock){

        long rowId = 0;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(DBHelper.TIME, createdTime);
            values.put(DBHelper.action_col, userpresent);
            values.put(DBHelper.userUnlock_col, userunlock);

            rowId = db.insert(DBHelper.actionLog_table, null, values);
        }
        catch(NullPointerException e){
            //e.printStackTrace();
            rowId = -1;
        }

        return rowId;
    }

    public static void insertConvenientSiteTable(String sitename, LatLng markerLocation){

        ContentValues values = new ContentValues();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            values.put(DBHelper.convenientsite_col, sitename);
            values.put(DBHelper.convenientsite_latitude_col , markerLocation.latitude);
            values.put(DBHelper.convenientsite_longitude_col , markerLocation.longitude);

            db.insert(DBHelper.convenientsite_table, null, values);
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            values.clear();
            DBManager.getInstance().closeDatabase(); // Closing database connection
        }
    }


    public static void insertCustomizedSiteTable(String sitename, LatLng markerLocation){

        ContentValues values = new ContentValues();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            Log.d(TAG,"Going to store in DB");
            Log.d(TAG,"lat : "+markerLocation.latitude+" lng : "+markerLocation.longitude);

            values.put(DBHelper.customsitename_col, sitename);
            values.put(DBHelper.customsite_latitude_col , markerLocation.latitude);
            values.put(DBHelper.customsite_longitude_col , markerLocation.longitude);

            db.insert(DBHelper.customsite_table, null, values);
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            values.clear();
            DBManager.getInstance().closeDatabase(); // Closing database connection
        }
    }

    public static ArrayList<String> queryCustomizedSites(){

        ArrayList<String> result = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            String sql = "SELECT *" +" FROM " + customsite_table;

            Log.d(TAG, "[test show trip querySession] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){

                String curRow = "";
                for (int i=0; i<columnCount; i++){

                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                result.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();


        }catch (Exception e){

        }

        return result;
    }

    public static long insertSessionTable(Session session){

        Log.d(TAG, "test trip put session " + session.getId() + " to table " + SESSION_TABLE_NAME);

        long rowId;

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

//            values.put(COL_TASK_ID, session.getTaskId());
            values.put(COL_SESSION_CREATED_TIME, session.getCreatedTime());
            values.put(COL_SESSION_START_TIME, session.getStartTime());
            values.put(COL_SESSION_ANNOTATION_SET, session.getAnnotationsSet().toJSONObject().toString());

            if(session.getEndTime() != 0){

                values.put(COL_SESSION_END_TIME, session.getEndTime());
            }

            int sessionIsUserPress = 0, sessionIsModified = 0, sessionIsSent = session.getIsSent();

            if(session.isUserPress())
                sessionIsUserPress = 1;

            if(session.isModified())
                sessionIsModified = 1;

            values.put(COL_SESSION_USERPRESSORNOT_FLAG, sessionIsUserPress);
            values.put(COL_SESSION_MODIFIED_FLAG, sessionIsModified);
            values.put(COL_SESSION_SENTORNOT_FLAG, sessionIsSent);
            values.put(COL_SESSION_TYPE, session.getType());
            values.put(COL_SESSION_HIDEDORNOT_FLAG, session.isHide());

            //get row number after the insertion
            Log.d(TAG, "[test combine] insert session: " + values.toString());

            rowId = db.insert(SESSION_TABLE_NAME, null, values);

//            Toast.makeText(mContext,"test trip inserting sessionid : "+ session.getId(),Toast.LENGTH_SHORT).show();

        }catch(Exception e){
            e.printStackTrace();
            rowId = -1;
        }

        DBManager.getInstance().closeDatabase();

        return rowId;
    }

    public static void deleteSession(int sessionId){

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            db.delete(SESSION_TABLE_NAME, COL_ID + " = "+ sessionId, null);
        }catch (Exception e){

        }

    }

    public static ArrayList<String> queryNextData(String tablename, int id){

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            String sql = "SELECT *"  +" FROM " + tablename +
                    " where " + COL_ID + " = " + (id + 1);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){

                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }

        return rows;
    }

    public static ArrayList<String> querySession(int sessionId){

        Log.d(TAG, "[test show trip] query session in DBHelper with session id" + sessionId);

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            String sql = "SELECT *"  +" FROM " + SESSION_TABLE_NAME +
                    " where " + COL_ID + " = " + sessionId;

            Log.d(TAG, "[test show trip querySession] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){

                String curRow = "";
                for (int i=0; i<columnCount; i++){

                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }

        Log.d(TAG, "[test show trip] the session is " +rows);

        return rows;
    }

    public static ArrayList<String> querySessionsBetweenTimes(long startTime, long endTime, String order) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + SESSION_TABLE_NAME +

//                    " where " + COL_SESSION_START_TIME + " > " + startTime + " and " +
//                    COL_SESSION_START_TIME + " < " + endTime +
                    " order by " + COL_SESSION_START_TIME + " " + order;

            Log.d(TAG, "[test show trip querySessionsBetweenTimes] test order the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;

    }

    public static ArrayList<String> querySessionsBetweenTimes(long startTime, long endTime) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + SESSION_TABLE_NAME +
                    " where "
                    /* + COL_SESSION_USERPRESSORNOT_FLAG + " = 1" + " and "*/
                    + COL_SESSION_START_TIME + " > " + startTime + " and " +
                    COL_SESSION_START_TIME + " < " + endTime +
                    " order by " + COL_SESSION_START_TIME + " DESC ";

            Log.d(TAG, "test combine [querySessionsBetweenTimes] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){

                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();
//            Log.d(TAG,"cursor.getCount : "+cursor.getCount());

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;

    }

    public static ArrayList<String> querySessionsBetweenTimesAndOrder(long startTime, long endTime, String order) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            String sql = "SELECT *"  +" FROM " + SESSION_TABLE_NAME +
                    " where "
                    /* + COL_SESSION_USERPRESSORNOT_FLAG + " = 1" + " and "*/
                    + COL_SESSION_START_TIME + " > " + startTime + " and " +
                    COL_SESSION_START_TIME + " < " + endTime +
                    " order by " + COL_SESSION_START_TIME + " " + order + ", " + COL_SESSION_END_TIME + " " + order;

            Log.d(TAG, "test combine [querySessionsBetweenTimes] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){

                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();
//            Log.d(TAG,"cursor.getCount : "+cursor.getCount());

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;

    }

    //query task table
    public static ArrayList<String> queryModifiedSessions (){

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + SESSION_TABLE_NAME + " where " +
                    COL_SESSION_MODIFIED_FLAG + " = 1";

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;

    }


    //query task table
    public static ArrayList<String> querySessions (){

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + DBHelper.SESSION_TABLE_NAME +
                    " order by " + COL_SESSION_START_TIME + " DESC ";

            Log.d(TAG, "[queryLastRecordBySession] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }
        Log.d(TAG, "[test show trip] the sessions are" + " " +rows);

        return rows;
    }

    public static ArrayList<String> querySessions (String order){

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + DBHelper.SESSION_TABLE_NAME +
                    " order by " + COL_SESSION_START_TIME + " "+order;

            Log.d(TAG, "[queryLastRecordBySession] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }
        Log.d(TAG, "[test show trip] the sessions are" + " " +rows);

        return rows;
    }

    public static ArrayList<String> queryUnSentSessions(){

        Log.d(TAG, "[test show trip] queryUnSentSessions");

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + DBHelper.SESSION_TABLE_NAME +
                    " WHERE " + DBHelper.COL_SESSION_SENTORNOT_FLAG + " = " + Constants.SESSION_SHOULD_BE_SENT_FLAG +
                    " order by " + COL_SESSION_START_TIME + " " + "ASC";

            Log.d(TAG, "[queryLastRecordBySession] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

            Log.e(TAG, "exception", e);
        }

        Log.d(TAG, "[test show trip] the sessions are" + " " +rows);

        return rows;
    }

    public static ArrayList<String> querySessions(long time24HrAgo){

        Log.d(TAG, "[test show trip] querySessions");

        ArrayList<String> rows = new ArrayList<>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + DBHelper.SESSION_TABLE_NAME +
                    " WHERE " + DBHelper.COL_SESSION_START_TIME + " < " + time24HrAgo +
                    " AND "+ DBHelper.COL_SESSION_SENTORNOT_FLAG + " <> " + Constants.SESSION_IS_ALREADY_SENT_FLAG +
                    " order by " + DBHelper.COL_SESSION_START_TIME + " " + "ASC";

            Log.d(TAG, "[queryLastRecordBySession] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

            Log.e(TAG, "exception", e);
        }

        Log.d(TAG, "[test show trip] the sessions are" + " " +rows);

        return rows;
    }

    //get the number of existing session
    public static long querySessionCount (){


        long count = 0;

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT * "  +" FROM " + SESSION_TABLE_NAME ;
            Cursor cursor = db.rawQuery(sql, null);
            count = cursor.getCount();

            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }
        return count;

    }

    public static ArrayList<String> queryLastRecord(String table_name) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + table_name  +
                    " order by " + COL_ID + " DESC LIMIT 1";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }

                rows.add(curRow);
            }
            cursor.close();


            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }

        return rows;
    }

    public static ArrayList<String> queryLastRecordBySession(String table_name, int sessionId) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + table_name  +
                    " where " + COL_SESSION_ID + " = " + sessionId +
                    " order by " + COL_ID + " DESC LIMIT 1";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                Log.d(TAG, "[queryLastRecordBySession] get result row " +curRow);

                rows.add(curRow);
            }
            cursor.close();


            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;
    }

    public static ArrayList<String> queryLastSession() {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + SESSION_TABLE_NAME  +
                    " WHERE "+ DBHelper.COL_SESSION_HIDEDORNOT_FLAG + " <> " + Constants.SESSION_IS_HIDED_FLAG +
                    " order by " + COL_ID + " DESC LIMIT 1";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                Log.d(TAG, "[test combine queryLastRecordBySession] get result row " +curRow);

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

            Log.e(TAG, "Exception", e);
        }

        return rows;
    }

    public static ArrayList<String> querySecondLastSessions() {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + SESSION_TABLE_NAME +
                    " WHERE "+ DBHelper.COL_SESSION_HIDEDORNOT_FLAG + " <> " + Constants.SESSION_IS_HIDED_FLAG +
                    " order by " + COL_ID + " DESC LIMIT 2";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                Log.d(TAG, "[test combine queryLastRecordBySession] get result row " +curRow);

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }
        return rows;
    }

    public static ArrayList<String> queryThirdLastSessions() {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + SESSION_TABLE_NAME  +
                    " order by " + COL_ID + " DESC LIMIT 3";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                Log.d(TAG, "[test combine queryLastRecordBySession] get result row " +curRow);

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }
        return rows;
    }

    public static ArrayList<String> queryRecordsBetweenTimes(String table_name, long startTime, long endTime) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + table_name  +
                    " where " +  TIME + " > " + startTime + " and " +
                    TIME + " < " + endTime  +
                    " order by " + TIME;

            Log.d(TAG, "[test sampling] the query statement is " +sql);

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
//                    Log.d(TAG, "[queryRecordsInSession][testgetdata] column " + i + " content: " + cursor.getString(i));
                    curRow += cursor.getString(i)+ Constants.DELIMITER;

                }
                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();


        }catch (Exception e){

        }


        Log.d(TAG, "[test sampling] the rsult is " +rows);
        return rows;


    }

    public static ArrayList<String> queryTransportationSuspectedStartTimePreviousId(String transportation){

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + transportationMode_table  +
                    " where " + suspectedStartTransportation_col + " <> "+ transportation +
                    " order by " + TIME + " DESC LIMIT 1 ";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }

        return rows;
    }

    public static ArrayList<String> queryTransportationSuspectedStopTimePreviousId(String transportation){

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + transportationMode_table  +
                    " where " + suspectedStopTransportation_col + " <> "+ transportation +
                    " order by " + TIME + " DESC LIMIT 1 ";

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }

        return rows;
    }

    public static ArrayList<String> queryRecordsInSession(String table_name, int sessionId, long startTime, long endTime) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + table_name  +
                    " where " + COL_SESSION_ID + " = " + sessionId + " and " +
                    TIME + " > " + startTime + " and " +
                    TIME + " < " + endTime  +
                    " order by " + TIME;

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;

                }

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();


        }catch (Exception e){

        }


        return rows;
    }

    public static ArrayList<String> queryRecordsInSession(String table_name, int sessionId) {

        ArrayList<String> rows = new ArrayList<String>();

        Log.d(TAG, "[test show trip] queryRecordsInSession ");
        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + table_name  +
                    " where " + COL_SESSION_ID + " = " + sessionId +
                    " order by " + TIME;


            Log.d(TAG, "[test show trip] the query statement is " +sql);

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }

                rows.add(curRow);
            }
            cursor.close();

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;


    }

    public static void updateSessionTable(int sessionId, long startTime, long endTime){

        String where = COL_ID + " = " +  sessionId;

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_START_TIME, startTime);
            values.put(COL_SESSION_END_TIME, endTime);

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        }catch(Exception e){

        }
    }

    /**
     * this is called usally when we want to end a session.
     * @param session_id
     * @param endTime
     * @param sessionUserPressOrNot
     */
    public static void updateSessionTable(int session_id, long endTime, boolean sessionUserPressOrNot){

        String where = COL_ID + " = " +  session_id;

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            //TODO get the col name after complete the annotate part.

            values.put(COL_SESSION_END_TIME, endTime);
            values.put(COL_SESSION_USERPRESSORNOT_FLAG, sessionUserPressOrNot);

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

            Log.d(TAG, "test combine: completing updating end time for sesssion" + id );

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void updateSessionTable(int session_id, long endTime, int sessionUserPressOrNot, int modifiedOrNot){

        String where = COL_ID + " = " + session_id;

        Log.d(TAG, "[test triggering] sessionUserPressOrNot : " + sessionUserPressOrNot);
        Log.d(TAG, "[test triggering] modifiedOrNot : " + modifiedOrNot);

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_END_TIME, endTime);
            values.put(COL_SESSION_USERPRESSORNOT_FLAG, sessionUserPressOrNot);
            values.put(COL_SESSION_MODIFIED_FLAG, modifiedOrNot);

            db.update(SESSION_TABLE_NAME, values, where, null);

            Log.d(TAG, "[test triggering] completing updating end time for session : " + session_id );

        }catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "[test triggering] updating fail" );
        }

        DBManager.getInstance().closeDatabase();
    }

    public static void updateSessionTable(int sessionId, long endTime){

        String where = COL_ID + " = " +  sessionId;

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            //TODO get the col name after complete the annotate part.

            /**first check if the endtime is intentionally invalid**/

            //if not
            if (endTime!=Constants.INVALID_TIME_VALUE){
                values.put(COL_SESSION_END_TIME, endTime);
            }
            else{
                values.put(COL_SESSION_END_TIME, "");
            }

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        }catch(Exception e){
            e.printStackTrace();
        }

        Log.d(TAG, "test trip: completing updating end time for sesssion" + id );

    }

    public static void updateSessionTable(int sessionId, long startTime,long endTime, AnnotationSet annotationSet, int toBeSent){

        String where = COL_ID + " = " +  sessionId;

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_START_TIME, startTime);
            values.put(COL_SESSION_END_TIME, endTime);
            //because only one data(annotation) exist.
            values.put(COL_SESSION_ANNOTATION_SET, annotationSet.toString());
            values.put(COL_SESSION_SENTORNOT_FLAG, toBeSent);

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void updateSessionTable(int sessionId, int toBeSent){

        String where = COL_ID + " = " +  sessionId;

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_SENTORNOT_FLAG, toBeSent);

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    //endTime is for preventing the overload conflict with the above function (toBeSent's one)
    public static void updateSessionTable(int sessionId, long endTime, int hidedOrNot){

        String where = COL_ID + " = " +  sessionId;

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_HIDEDORNOT_FLAG, hidedOrNot);

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void updateSessionTable(int sessionId, AnnotationSet annotationSet){

        String where = COL_ID + " = " +  sessionId;

        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            //because only one data(annotation) exist.
            values.put(COL_SESSION_ANNOTATION_SET, annotationSet.toString());

            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

            Log.d(TAG, "[storing sitename] store successfully");

        }catch(Exception e){

            Log.d(TAG, "[storing sitename] Exception");

            e.printStackTrace();

            Log.e(TAG, "[storing sitename]", e);

        }

    }

    public static void updateSessionTable(Session session, long endTime){

        String where = COL_ID + " = " +  session.getId();

        try{
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            //TODO get the col name after complete the annotate part.

            /**first check if the endtime is intentionally invalid**/
            //if not
            if (endTime!=Constants.INVALID_TIME_VALUE){
                values.put(COL_SESSION_END_TIME, endTime);
            }
            else{
                values.put(COL_SESSION_END_TIME, "");
            }

            values.put(COL_SESSION_USERPRESSORNOT_FLAG, session.isUserPress());

            values.put(COL_SESSION_MODIFIED_FLAG, 1);


            db.update(SESSION_TABLE_NAME, values, where, null);

            DBManager.getInstance().closeDatabase();

        }catch(Exception e){
            e.printStackTrace();
        }

        Log.d(TAG, "test trip: completing updating end time for sesssion" + id );

    }

    public static void updateRecordsInSession(String table_name, long splittingTime, int currentSessionId, int newSessionid) {

//        String where = COL_SESSION_ID + " = " + currentSessionId;

        //get the exact session id in the delimiters
        String querySessionidInDelimiters = "( '"+Constants.SESSION_DELIMITER + "' || RTRIM("+COL_SESSION_ID+") || '" + Constants.SESSION_DELIMITER+"' )"
                +"LIKE ('%" +Constants.SESSION_DELIMITER + "' || " + currentSessionId + " || '" + Constants.SESSION_DELIMITER + "%')";

        String querySessionid = COL_SESSION_ID + " = " + currentSessionId;

        String afterSplitting = TIME + " > " + splittingTime;
        try{

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_SESSION_ID, newSessionid);

            db.update(table_name, values, querySessionidInDelimiters
//                            + " or " + querySessionidBetweenSpaceAndDelimiter
//                            + " or " + querySessionid
                            +" and " + afterSplitting
                    , null);

            DBManager.getInstance().closeDatabase();

        }catch (Exception e){

            Log.e(TAG, "SessionConcat exception", e);
        }
    }

}
