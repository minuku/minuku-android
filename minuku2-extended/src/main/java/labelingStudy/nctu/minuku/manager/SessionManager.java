package labelingStudy.nctu.minuku.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.Session;

/**
 * Created by Lawrence on 2018/3/13.
 */

public class SessionManager {

    private final static String TAG = "SessionManager";

    public static int sessionid_unStatic;
    private static int trip_size;
    private long lastSessiontime;
    private long lastSessionIDtime;

    private String sessionid;
    private String transportation;
    private String lasttime_transportation;
    private String lasttime_trip_transportation;

    public static final String ANNOTATION_PROPERTIES_ANNOTATION = "Annotation";
    public static final String ANNOTATION_PROPERTIES_ID = "Id";
    public static final String ANNOTATION_PROPERTIES_NAME= "Name";
    public static final String ANNOTATION_PROPERTIES_START_TIME = "Start_time";
    public static final String ANNOTATION_PROPERTIES_END_TIME = "End_time";
    public static final String ANNOTATION_PROPERTIES_IS_ENTIRE_SESSION = "Entire_session";
    public static final String ANNOTATION_PROPERTIES_CONTENT = "Content";
    public static final String ANNOTATION_PROPERTIES_TAG = "Tag";

    public static final String SESSION_DISPLAY_ONGOING = "Ongoing...";
    public static final String SESSION_DISPLAY_NO_ANNOTATION = "No Label";

    public static final String SESSION_LONGENOUGH_THRESHOLD_DISTANCE = "distance";

    public static final long SESSION_MIN_INTERVAL_THRESHOLD_TRANSPORTATION = 3 * Constants.MILLISECONDS_PER_MINUTE;
    public static final long SESSION_MIN_DURATION_THRESHOLD_TRANSPORTATION = 3 * Constants.MILLISECONDS_PER_MINUTE;
    public static final long SESSION_MIN_DISTANCE_THRESHOLD_TRANSPORTATION = 100;  // meters;

    public static int SESSION_DISPLAY_RECENCY_THRESHOLD_HOUR = 24;


    private ArrayList<LocationDataRecord> LocationToTrip;

    private static Context mContext;
    private static final String PACKAGE_DIRECTORY_PATH="/Android/data/edu.ohio.minuku_2/";
    private CSVWriter csv_writer = null;
    private static CSVWriter csv_writer2 = null;

    private static SessionManager instance;

    private static ArrayList<Integer> mOngoingSessionIdList;

    private SharedPreferences sharedPrefs;
    private static SharedPreferences.Editor editor;

//    private int testing_count;

    public SessionManager(Context context) {

        this.mContext = context;

        sharedPrefs = context.getSharedPreferences("edu.umich.minuku_2",Context.MODE_PRIVATE);
        editor = context.getSharedPreferences("edu.umich.minuku_2", Context.MODE_PRIVATE).edit();

        sessionid = "0";

        mOngoingSessionIdList = new ArrayList<Integer>();


        sessionid_unStatic = sharedPrefs.getInt("sessionid_unStatic",0);
        trip_size = sharedPrefs.getInt("trip_size",0);

        transportation = "NA";

        lasttime_transportation = sharedPrefs.getString("","NA");

        lasttime_trip_transportation = sharedPrefs.getString("lasttime_trip_transportation","NA");

        lastSessiontime = sharedPrefs.getLong("lastSessiontime", -1);
//        testing_count = 0;
        lastSessionIDtime = sharedPrefs.getLong("lastSessionIDtime", -1);
    }

    public static SessionManager getInstance() {
        if(SessionManager.instance == null) {
            try {
//                SessionManager.instance = new SessionManager();
                Log.d(TAG,"getInstance without mContext.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return SessionManager.instance;
    }

    public static SessionManager getInstance(Context context) {
        if(SessionManager.instance == null) {
            try {
                SessionManager.instance = new SessionManager(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return SessionManager.instance;
    }

    public static boolean isSessionOngoing(int sessionId) {

        Log.d(TAG, " [test combine] tyring to see if the session is ongoing:" + sessionId);

        for (int i = 0; i< mOngoingSessionIdList.size(); i++){
            //       Log.d(LOG_TAG, " [getCurRecordingSession] looping to " + i + "th session of which the id is " + mCurRecordingSessions.get(i).getId());

            if (mOngoingSessionIdList.get(i)==sessionId){
                return true;
            }
        }
        Log.d(TAG, " [test combine] the session is not ongoing:" + sessionId);

        return false;
    }

    public static ArrayList<Integer> getOngoingSessionIdList() {
        return mOngoingSessionIdList;
    }

    public void setmOngoingSessionIdList(ArrayList<Integer> ongoingSessionIdList) {
        mOngoingSessionIdList = ongoingSessionIdList;
    }

    public void addOngoingSessionid(int id) {
        Log.d(TAG, "test combine: adding ongonig session " + id );
        this.mOngoingSessionIdList.add(id);

    }

    public ArrayList<Integer> getOngoingSessionList () {
        return mOngoingSessionIdList;
    }

    public void removeOngoingSessionid(int id) {
        Log.d(TAG, "test replay: inside removeongogint seesion renove " + id );
        this.mOngoingSessionIdList.remove(id);
        Log.d(TAG, "test replay: inside removeongogint seesion the ongoiong list is  " + mOngoingSessionIdList.toString() );
    }

    public static String getTimeString(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_SLASH);
        String currentTimeString = sdf_now.format(time);

        return currentTimeString;
    }


    /**
     * This function convert Session String retrieved from the DB to Object Session
     * @param sessionStr
     * @return
     */
    private static Session convertStringToSession(String sessionStr) {

        Session session = null;

        //split each row into columns
        String[] separated = sessionStr.split(Constants.DELIMITER);

        /** get properties of the session **/
        int id = Integer.parseInt(separated[DBHelper.COL_INDEX_SESSION_ID]);
        long startTime = Long.parseLong(separated[DBHelper.COL_INDEX_SESSION_START_TIME]);


        /** 1. create sessions from the properies obtained **/
        session = new Session(id, startTime);

        /**2. get end time (or time of the last record) of the sesison**/

        long endTime = 0;
        //the session could be still ongoing..so we need to check where's endTime
        Log.d(TAG, "[test show trip] separated[DBHelper.COL_INDEX_SESSION_END_TIME] " + separated[DBHelper.COL_INDEX_SESSION_END_TIME]);

        if (!separated[DBHelper.COL_INDEX_SESSION_END_TIME].equals("null") && !separated[DBHelper.COL_INDEX_SESSION_END_TIME].equals("")){
            endTime = Long.parseLong(separated[DBHelper.COL_INDEX_SESSION_END_TIME]);
        }
        //there 's no end time of the session, we take the time of the last record
        else {
            endTime = getLastRecordTimeinSession(session.getId());

        }
        Log.d(TAG, "[test show trip] testgetdata the end time is now:  " + ScheduleAndSampleManager.getTimeString(endTime));

        boolean islongEnough= true;
        boolean isModified = false;
        //longEnough flag
        if (!separated[DBHelper.COL_INDEX_SESSION_LONG_ENOUGH_FLAG].equals("null") && !separated[DBHelper.COL_INDEX_SESSION_LONG_ENOUGH_FLAG].equals("")){
            islongEnough = Boolean.parseBoolean(separated[DBHelper.COL_INDEX_SESSION_LONG_ENOUGH_FLAG]);
        }

        if (!separated[DBHelper.COL_INDEX_SESSION_MODIFIED_FLAG].equals("null") && !separated[DBHelper.COL_INDEX_SESSION_MODIFIED_FLAG].equals("")){
            isModified = Boolean.parseBoolean(separated[DBHelper.COL_INDEX_SESSION_MODIFIED_FLAG]);
        }

        session.setUserPressOrNot(islongEnough);
        session.setModified(isModified);

        //set end time
        session.setEndTime(endTime);

        /** 3. get annotaitons associated with the session **/
        JSONObject annotationSetJSON = null;
        JSONArray annotateionSetJSONArray = null;
        try {
            if (!separated[DBHelper.COL_INDEX_SESSION_ANNOTATION_SET].equals("null")){
                annotationSetJSON = new JSONObject(separated[DBHelper.COL_INDEX_SESSION_ANNOTATION_SET]);
                annotateionSetJSONArray = annotationSetJSON.getJSONArray(ANNOTATION_PROPERTIES_ANNOTATION);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //set annotationset if there is one
        if (annotateionSetJSONArray!=null){
            AnnotationSet annotationSet =  toAnnorationSet(annotateionSetJSONArray);
            session.setAnnotationSet(annotationSet);
        }

        return session;

    }

    public static Session getSession (String id) {

        int sessionId = Integer.parseInt(id);
        ArrayList<String> res =  DBHelper.querySession(sessionId);
        Log.d(TAG, "[test show trip]query session from LocalDB is " + res);
        Session session = null;

        for (int i=0; i<res.size() ; i++) {

            session = convertStringToSession(res.get(i));
            Log.d(TAG, " test show trip  testgetdata id " + session.getId() + " startTime " + session.getStartTime() + " end time " + session.getEndTime() + " annotation " + session.getAnnotationsSet().toJSONObject().toString());

        }


        return session;
    }

    public static Session getLastSession() {

        Session session = null;
        String sessionStr = DBHelper.queryLastSession().get(0);
        Log.d(TAG, "test show trip lastsession " + sessionStr );
        session = convertStringToSession(sessionStr);
//        Log.d(TAG, " test show trip  testgetdata id " + session.getId() + " startTime " + session.getStartTime() + " end time " + session.getEndTime() + " annotation " + session.getAnnotationsSet().toJSONObject().toString());
        Log.d(TAG, " test show trip  testgetdata id " + session.getId() + " startTime " + ScheduleAndSampleManager.getTimeString(session.getStartTime()) + " end time " + ScheduleAndSampleManager.getTimeString(session.getEndTime()) + " annotation " + session.getAnnotationsSet().toJSONObject().toString());

        return session;

    }

    public static long getLastRecordTimeinSession(int sessionId) {

        ArrayList<String> resultBySession = null;
        resultBySession = SessionManager.getRecordsInSession(sessionId, DBHelper.location_table);

        Log.d(TAG, "test combine: there are " + resultBySession.size() + " location records"  );

        //if there's no location points, it's not long enough
        if (resultBySession.size()==0){
            return 0;
        }

        //get the timestemp of the last record
        else {
            String[] separated = resultBySession.get(resultBySession.size()-1).split(Constants.DELIMITER);
            long time = Long.parseLong(separated[1]);
            Log.d(TAG, "test combine: the time of the last record is " + time );
            return time;
        }


    }

    public static Session getSession (int sessionId) {

        Log.d(TAG, "[test reEnter] sessionId : "+sessionId);
        Session session = null;
        String sessionStr =  DBHelper.querySession(sessionId).get(0);
        Log.d(TAG, "[test combine]query session from LocalDB is " + sessionStr);
        session = convertStringToSession(sessionStr);
        Log.d(TAG, " test combine  testgetdata id " + session.getId() + " startTime " + session.getStartTime() + " end time " + session.getEndTime() + " annotation " + session.getAnnotationsSet().toJSONObject().toString());

        return session;
    }


    /**
     *
     * @return
     */
    public static int getNumOfSession(){
        int num = 0;

        num = (int)DBHelper.querySessionCount();

        return num;
    }


    /**
     *
     * @param sessionId
     * @param endTime
     * @param isLongEnough
     */
    public static void updateCurSessionEndInfoTo(int sessionId, long endTime, boolean isLongEnough){

        DBHelper.updateSessionTable(sessionId, endTime, isLongEnough);
    }

    /**
     *
     * @param session
     */
    public static void startNewSession(Session session) {

        //InstanceManager add ongoing session for the new activity
        SessionManager.getInstance().addOngoingSessionid(session.getId());

        Log.d(TAG, "startNewSession id " + session.getId() + " startTime " + ScheduleAndSampleManager.getTimeString(session.getStartTime()) + " end time " + ScheduleAndSampleManager.getTimeString(session.getEndTime()) + " annotation " + session.getAnnotationsSet().toJSONObject().toString());

        DBHelper.insertSessionTable(session);

    }

    public static boolean examineSessionCombinationByActivityAndTime(Session lastSession, String activity, long time){

        boolean combine = false;

        //get annotaitons that has the transportation mode tag
        ArrayList<Annotation> annotations = lastSession.getAnnotationsSet().getAnnotationByContent(activity);


        //check if the last session has endtime. It is possible that it ends unexpectedly



        //if the previous session does not have any annotation of which transportation is of the same tag, we should not combine
        if (annotations.size() == 0) {
            Log.d(TAG, "[test combine] addSessionFlag = true  the last session is not the same activity");
            combine = false;
        }

        // the current activity is the same TM with the previous session mode, we check its time difference
        else {
            Log.d(TAG, "[test combine] we found the last session with the same activity");
            //check its interval to see if it's within 5 minutes

            Log.d(TAG, "[test combine] the previous session ends at " +  lastSession.getEndTime() + " and the current activity starts at " + time  +
                    " the difference is " + (time - lastSession.getEndTime()) / Constants.MILLISECONDS_PER_MINUTE + " minutes");

            //if the current session is too close from the previous one in terms of time, we should combine
            if (time - lastSession.getEndTime() <= SessionManager.SESSION_MIN_INTERVAL_THRESHOLD_TRANSPORTATION) {

                Log.d(TAG, "[test combine] the current activity is too close from the previous trip, continue the last session! the difference is "
                        + (time - lastSession.getEndTime()) / Constants.MILLISECONDS_PER_MINUTE + " minutes");

                combine = true;

            }
            //the session is far from the previous one, it should be a new session. we should not combine
            else {
                Log.d(TAG, "[test combine] addSessionFlag = true the current truip is far from the previous trip");
                combine = false;
            }
        }


        //debug...
        String lastSessionStr = DBHelper.queryLastSession().get(0);
        Log.d(TAG, "test combine: the previous acitivty is movnig,after combine it the last session is: " +  lastSessionStr );
        return combine;



    }


    /**
     *
     * @param session
     */
    public static void continueLastSession(Session session) {

        //remove the ongoing session
        getOngoingSessionIdList().add(session.getId());

        //update session with end time and long enough flag.
        updateCurSessionEndInfoTo(session.getId(),0,true);

    }

    /**
     *
     * @param session
     */
    public static void endCurSession(Session session) {

        Log.d(TAG, "test show trip: before ending the session the list sizr is " + getOngoingSessionIdList().size());

        Log.d(TAG, "test show trip: end cursession Id : " + session.getId());
        Log.d(TAG, "test show trip: end cursession EndTime : " + ScheduleAndSampleManager.getTimeString(session.getEndTime()));
        Log.d(TAG, "test show trip: end cursession isUserPress : " + session.isUserPress());

        Log.d(TAG, "test show trip: before remove the list at 0 is " + getOngoingSessionIdList().get(0));

        //remove the ongoing session
        mOngoingSessionIdList.remove(Integer.valueOf(session.getId()));

        Log.d(TAG, "test show trip: after remove going the list is  " + getOngoingSessionIdList().toString());

        //update session with end time and long enough flag.
        updateCurSessionEndInfoTo(session.getId(),session.getEndTime(),session.isUserPress());
    }

    public static boolean isSessionLongEnough(String thresholdType, int sessionId){


        boolean islongEnough = true;

        //we see if the session is long enough based on its distance
        if (thresholdType.equals(SESSION_LONGENOUGH_THRESHOLD_DISTANCE)){

            Log.d(TAG, "test combine: test threshold " + SESSION_LONGENOUGH_THRESHOLD_DISTANCE  + " for session " + sessionId);

            //get location records from the session
            ArrayList<String> resultBySession = null;
            resultBySession = SessionManager.getRecordsInSession(sessionId, DBHelper.location_table);

            Log.d(TAG, "test combine: there are " + resultBySession.size() + " location records"  );


            //if there's no location points, it's not long enough
            if (resultBySession.size()==0){
                islongEnough = false;
            }

            //there are location records, we need to examine its distance
            else {
                //create arraylist for storing the latlng
                ArrayList<LatLng> latLngs = new ArrayList<LatLng>();

                /** storing location records after we obain from the database*/
                for(int index = 0;index < resultBySession.size(); index++){
                    String[] separated = resultBySession.get(index).split(Constants.DELIMITER);
                    double lat = Double.valueOf(separated[2]);
                    double lng = Double.valueOf(separated[3]);

                    LatLng latLng = new LatLng(lat, lng);
                    latLngs.add(latLng);
                }

                //get distance between the last and the previous
                LatLng startLatLng = latLngs.get(0);
                LatLng endLatLng = latLngs.get(latLngs.size() - 1);

                Log.d(TAG, " test combine the first point is " + startLatLng + ", the end point is " + endLatLng );

                //calculate the distance between the first and the last
                float[] results = new float[1];
                Location.distanceBetween(startLatLng.latitude,startLatLng.longitude, endLatLng.latitude, endLatLng.longitude,results);
                float distance = results[0];

                Log.d(TAG, " test combine the distance between start and end is " + distance  + " now we comapre with the threshold " + SessionManager.SESSION_MIN_DISTANCE_THRESHOLD_TRANSPORTATION );

                //if the session is shorter than a threshold (now - start time < threshold), we should give it a flag, so that it wouldnot show up in the annotation list
                if (distance<SessionManager.SESSION_MIN_DISTANCE_THRESHOLD_TRANSPORTATION){
                    Log.d(TAG, " test combine the trip is too short  ");

                    islongEnough = false;
                }else {
                    Log.d(TAG, " test combine the trip is long enough ");
                }
            }



        }

        Log.d(TAG, " finally the session " + sessionId + " long enough is " + islongEnough);
        return islongEnough;

    }


    public static ArrayList<Session> getRecentSessions() {

        Log.d(TAG, "[test show trip] getRecentSessions");

        ArrayList<Session> sessions = new ArrayList<Session>();

        long queryEndTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
        //start time = a specific hours ago
        long queryStartTime = ScheduleAndSampleManager.getCurrentTimeInMillis() - Constants.MILLISECONDS_PER_HOUR * SESSION_DISPLAY_RECENCY_THRESHOLD_HOUR;

        Log.d(TAG, " [test show trip] going to query session between " + ScheduleAndSampleManager.getTimeString(queryStartTime) + " and " + ScheduleAndSampleManager.getTimeString(queryEndTime) );


        //query//get sessions between the starTime and endTime
        ArrayList<String> res =  DBHelper.querySessionsBetweenTimes(queryStartTime, queryEndTime);


        Log.d(TAG, "[test show trip] getRecentSessions get res: " +  res);

        //we start from 1 instead of 0 because the 1st session is the background recording. We will skip it.
        for (int i=0; i<res.size() ; i++) {

            Session session = convertStringToSession(res.get(i));
            Log.d(TAG, "[test show trip] session id : "+ session.getId());
            sessions.add(session);
        }

        return sessions;
    }

    public static ArrayList<String> getRecordsInSession(int sessionId, String tableName) {

        ArrayList<String> resultList = new ArrayList<String>();

        resultList = DBHelper.queryRecordsInSession(tableName, sessionId);
        Log.d(TAG, "[getRecordsInSession] test combine got " + resultList.size() + " of results from queryRecordsInSession");

        return resultList;
    }

    private static String addZero(int date){
        if(date<10)
            return String.valueOf("0"+date);
        else
            return String.valueOf(date);
    }

    public static String makingDataFormat(int year,int month,int date){
        String dataformat= "";

//        dataformat = addZero(year)+"-"+addZero(month)+"-"+addZero(date)+" "+addZero(hour)+":"+addZero(min)+":00";
        dataformat = addZero(year)+"/"+addZero(month)+"/"+addZero(date)+" "+"00:00:00";
        Log.d(TAG,"dataformat : " + dataformat);

        return dataformat;
    }


    public static AnnotationSet toAnnorationSet(JSONArray annotationJSONArray) {

        AnnotationSet annotationSet = new AnnotationSet();
        ArrayList<Annotation> annotations = new ArrayList<Annotation>();

        for (int i=0 ; i<annotationJSONArray.length(); i++){

            JSONObject annotationJSON = null;
            try {
                Annotation annotation = new Annotation();
                annotationJSON = annotationJSONArray.getJSONObject(i);

                String content = annotationJSON.getString(ANNOTATION_PROPERTIES_CONTENT);
                annotation.setContent(content);

                JSONArray tagsJSONArray = annotationJSON.getJSONArray(ANNOTATION_PROPERTIES_TAG);

                for (int j=0; j<tagsJSONArray.length(); j++){

                    String tag = tagsJSONArray.getString(j);
                    annotation.addTag(tag);
                    Log.d(TAG, "[toAnnorationSet] the content is " + content +  " tag " + tag);
                }

                annotations.add(annotation);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        annotationSet.setAnnotations(annotations);

        Log.d(TAG, "[toAnnorationSet] the annotationSet has  " + annotationSet.getAnnotations().size() + " annotations ");
        return annotationSet;

    }
    
}