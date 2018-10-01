package labelingStudy.nctu.minuku.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import labelingStudy.nctu.minuku.Data.DBHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.Session;

/**
 * Created by Lawrence on 2018/3/13.
 */

/**
 * SessionManager is the main class that handle session related operation
 */
public class SessionManager {

    private final static String TAG = "SessionManager";

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

    public static final long SESSION_MIN_INTERVAL_THRESHOLD_TRANSPORTATION = 2 * Constants.MILLISECONDS_PER_MINUTE;
    public static final long SESSION_MIN_DURATION_THRESHOLD_TRANSPORTATION = 3 * Constants.MILLISECONDS_PER_MINUTE;
    public static final long SESSION_MIN_DISTANCE_THRESHOLD_TRANSPORTATION = 100;  // meters;

    public static int SESSION_DISPLAY_RECENCY_THRESHOLD_HOUR = 24;


    private ArrayList<LocationDataRecord> LocationToTrip;

    private static Context mContext;

    /**
     * Store the SessionManager itself
     */
    private static SessionManager instance;

    /**
     * Store the ongoing session, that is, the current recording session. In most condition there would only be one ongoing session, so size of the list usually no greater than 1
     * In this edition of minuku there would always be only one ongoing session
     */
    private static ArrayList<Integer> mOngoingSessionIdList;

    private static ArrayList<Integer> mEmptyOngoingSessionIdList;

    private static boolean emptySessionOngoing;

    public static boolean sessionIsWaiting;

    private SharedPreferences sharedPrefs;
    private static SharedPreferences.Editor editor;

//    private int testing_count;

    /**
     * Initialize the SessionManager
     */
    public SessionManager(Context context) {

        this.mContext = context;

        sharedPrefs = context.getSharedPreferences("edu.umich.minuku_2",Context.MODE_PRIVATE);
        editor = context.getSharedPreferences("edu.umich.minuku_2", Context.MODE_PRIVATE).edit();

        sessionid = "0";

        mOngoingSessionIdList = new ArrayList<Integer>();

        mEmptyOngoingSessionIdList = new ArrayList<Integer>();

        emptySessionOngoing = false;

        sessionIsWaiting = false;

        transportation = "NA";

        lasttime_transportation = sharedPrefs.getString("","NA");

        lasttime_trip_transportation = sharedPrefs.getString("lasttime_trip_transportation","NA");
    }



    /**
     * Return the SessionManager itself
     */
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

    /**
     * Return the SessionManager itself. If SessionManager is not initialize yet, initialize SessionManager
     * @param context use to set context in SessionManager
     * @return SessionManager
     */
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

    /**
     * To identify if the given session is the ongoing session
     * @param sessionId the id of the session to be identified if it is ongoing
     * @return is the session ongoing or not
     */
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

    /**
     * Return the ongoing session, usually store at index(0) since there is only one ongoing session at most conditions
     * In this edition of minuku there would always be only one ongoing session
     * @return the ongoing session list
     */
    public static ArrayList<Integer> getOngoingSessionIdList() {
        return mOngoingSessionIdList;
    }

    public void setOngoingSessionIdList(ArrayList<Integer> ongoingSessionIdList) {
        mOngoingSessionIdList = ongoingSessionIdList;
    }

    /**
     * Update the ongoing session. In this edition of Minuku, there is always a removal of ongoing session id before calling addOngoingSessionId, thus the size of mOngoingSessionIdList keep at 1
     * @param id the id of the session that is set to be ongoing
     */
    public void addOngoingSessionid(int id) {
        Log.d(TAG, "test combine: adding ongonig session " + id );
        this.mOngoingSessionIdList.add(id);

    }

    public ArrayList<Integer> getOngoingSessionList () {
        return mOngoingSessionIdList;
    }

    /**
     * Remove the session given by id in the ongoingSessionList
     * @param id  id the id of the session that is set not to be ongoing anymore
     */
    public void removeOngoingSessionid(int id) {
        Log.d(TAG, "test replay: inside removeongogint session renove " + id );
        this.mOngoingSessionIdList.remove(id);
        Log.d(TAG, "test replay: inside removeongogint session the ongoiong list is  " + mOngoingSessionIdList.toString() );
    }

    /**
     * Store the empty ongoing session, use for CAR mode
     */
    public static boolean isSessionEmptyOngoing(int sessionId) {

        Log.d(TAG, " [test combine] tyring to see if the session is ongoing:" + sessionId);

        for (int i = 0; i< mEmptyOngoingSessionIdList.size(); i++){

            if (mEmptyOngoingSessionIdList.get(i)==sessionId){
                return true;
            }
        }
        Log.d(TAG, " [test combine] the session is not ongoing:" + sessionId);

        return false;
    }

    public static boolean isEmptySessionOngoing() {
        return emptySessionOngoing;
    }

    public static void setEmptySessionOngoing(boolean emptySessionOngoing) {
        SessionManager.emptySessionOngoing = emptySessionOngoing;
    }

    public static void setEmptyOngoingSessionIdList(ArrayList<Integer> mEmptyOngoingSessionIdList) {
        SessionManager.mEmptyOngoingSessionIdList = mEmptyOngoingSessionIdList;
    }

    public static ArrayList<Integer> getEmptyOngoingSessionIdList() {
        return mEmptyOngoingSessionIdList;
    }

    public static void addEmptyOngoingSessionid(int id) {
        Log.d(TAG, "test combine: adding ongonig session " + id );
        mEmptyOngoingSessionIdList.add(id);

    }

    /**
     * Return time in text by the given time value
     * @param time the given time in long
     * @return date format of time in string
     */
    public static String getTimeString(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_SLASH);
        String currentTimeString = sdf_now.format(time);

        return currentTimeString;
    }

    /**
     * This function convert Session String retrieved from the database to Object Session
     * @param sessionStr session data in string object
     * @return session object get from sessionStr
     */
    public static Session convertStringToSession(String sessionStr) {

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

        long createdTime = Long.parseLong(separated[DBHelper.COL_INDEX_SESSION_CREATED_TIME]);
        session.setCreatedTime(createdTime);

        int isUserPress;
        int isModified;

        if (!separated[DBHelper.COL_INDEX_SESSION_USERPRESSORNOT_FLAG].equals("null") && !separated[DBHelper.COL_INDEX_SESSION_USERPRESSORNOT_FLAG].equals("")){

//            isUserPress = Boolean.parseBoolean(separated[DBHelper.COL_INDEX_SESSION_USERPRESSORNOT_FLAG]);
            isUserPress = Integer.parseInt(separated[DBHelper.COL_INDEX_SESSION_USERPRESSORNOT_FLAG]);

            Log.d(TAG, "[test show trip] testgetdata isUserPress is now:  " + isUserPress);

            if(isUserPress == 1){

                session.setUserPressOrNot(true);
            }else {

                session.setUserPressOrNot(false);
            }
        }

        if (!separated[DBHelper.COL_INDEX_SESSION_MODIFIED_FLAG].equals("null") && !separated[DBHelper.COL_INDEX_SESSION_MODIFIED_FLAG].equals("")){

//            isModified = Boolean.parseBoolean(separated[DBHelper.COL_INDEX_SESSION_MODIFIED_FLAG]);
            isModified = Integer.parseInt(separated[DBHelper.COL_INDEX_SESSION_MODIFIED_FLAG]);

            Log.d(TAG, "[test show trip] testgetdata isModified is now:  " + isModified);

            if(isModified == 1){

                session.setModified(true);
            }else {

                session.setModified(false);
            }
        }

        if (!separated[DBHelper.COL_INDEX_SESSION_SENTORNOT_FLAG].equals("null") && !separated[DBHelper.COL_INDEX_SESSION_SENTORNOT_FLAG].equals("")) {

            session.setIsSent(Integer.valueOf(separated[DBHelper.COL_INDEX_SESSION_SENTORNOT_FLAG]));
        }

        if (!separated[DBHelper.COL_INDEX_SESSION_TYPE].equals("null") && !separated[DBHelper.COL_INDEX_SESSION_TYPE].equals("")) {

            session.setType(separated[DBHelper.COL_INDEX_SESSION_TYPE]);
        }

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

    /**
     * Get session from database with id
     * @param id id of the requiring session
     * @return the session get from database
     */
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

    /**
     * Get last session from database. should avoid the discarded session
     * @return last recorded session
     */
    public static Session getLastSession() {

        Session session = null;

        ArrayList<String> sessions = DBHelper.queryLastSession();
        if(sessions.size()!=0) {
            String sessionStr = sessions.get(0);
            Log.d(TAG, "test show trip lastsession " + sessionStr);
            session = convertStringToSession(sessionStr);
//        Log.d(TAG, " test show trip  testgetdata id " + session.getId() + " startTime " + session.getStartTime() + " end time " + session.getEndTime() + " annotation " + session.getAnnotationsSet().toJSONObject().toString());
            Log.d(TAG, " test show trip  testgetdata id " + session.getId() + " startTime " + ScheduleAndSampleManager.getTimeString(session.getStartTime()) + " end time " + ScheduleAndSampleManager.getTimeString(session.getEndTime()) + " annotation " + session.getAnnotationsSet().toJSONObject().toString());
        }else{

            session = new Session(1);
        }

        return session;
    }

    /**
     * Get second last session from database, use for combine session.  Should avoid the discarded session
     */
    public static Session getLast2Session() {

        Session session = null;

        ArrayList<String> sessions = DBHelper.queryLast2Sessions();
        if(sessions.size() >= 2) { //sessions.size() != 0

            String sessionStr = sessions.get(1);
            Log.d(TAG, "test show trip lastsession " + sessionStr);
            session = convertStringToSession(sessionStr);
//        Log.d(TAG, " test show trip  testgetdata id " + session.getId() + " startTime " + session.getStartTime() + " end time " + session.getEndTime() + " annotation " + session.getAnnotationsSet().toJSONObject().toString());
            Log.d(TAG, " test show trip  testgetdata id " + session.getId() + " startTime " + ScheduleAndSampleManager.getTimeString(session.getStartTime()) + " end time " + ScheduleAndSampleManager.getTimeString(session.getEndTime()) + " annotation " + session.getAnnotationsSet().toJSONObject().toString());
        }else{

            session = new Session(1);
        }

        return session;
    }


    /**
     * Get the last time record of the session
     * Since the session contains a list of location record, LastRecordTime could be get by the timestamp of the last recorded location.
     * @param sessionId id of the requiring session
     * @return the last time record of the requiring session
     */
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

    /**
     * Get session from database by id
     * @param sessionId id of requiring session
     * @return the requiring session data in session object
     */
    public static Session getSession (int sessionId) {

        Log.d(TAG, "sessionId : "+sessionId);
        Session session = null;
        String sessionStr =  DBHelper.querySession(sessionId).get(0);
        Log.d(TAG, "query session from LocalDB is " + sessionStr);
        session = convertStringToSession(sessionStr);
        Log.d(TAG, " testgetdata id " + session.getId() + " startTime " + session.getStartTime() + " end time " + session.getEndTime() + " annotation " + session.getAnnotationsSet().toJSONObject().toString());

        return session;
    }

    /**
     * Get count of sessions in database
     * @return count of sessions in database
     */
    public static int getNumOfSession(){
        int num = 0;

        num = (int)DBHelper.querySessionCount();

        return num;
    }

    /**
     * Update the session end time while also set the labeling mode
     * @param sessionId id of the requiring update session
     * @param endTime the update end time
     * @param userPressOrNot the labeling mode
     */
    public static void updateCurSessionEndInfoTo(int sessionId, long endTime, boolean userPressOrNot){

        DBHelper.updateSessionTable(sessionId, endTime, userPressOrNot);
    }

    /**
     * Update the session end time while also set the labeling mode
     * @param sessionId id of the requiring update session
     * @param endTime the update end time
     * @param userPressOrNot the labeling mode
     */
    public static void updateCurSession(int sessionId, long endTime, boolean userPressOrNot){

        DBHelper.updateSessionTable(sessionId, endTime, userPressOrNot);
    }

    public static void updateCurSession(int sessionId, long endTime, int userPressOrNot, int modifiedOrNot){

        DBHelper.updateSessionTable(sessionId, endTime, userPressOrNot, modifiedOrNot);
    }

    /**
     * Start recording the session and set it to the ongoingSession, while also add it to database
     * @param session
     */
    public static void startNewSession(Session session) {

        //InstanceManager add ongoing session for the new activity
        SessionManager.getInstance().addOngoingSessionid(session.getId());

        Log.d(TAG, "startNewSession id " + session.getId() + " startTime " + ScheduleAndSampleManager.getTimeString(session.getStartTime()) + " end time " + ScheduleAndSampleManager.getTimeString(session.getEndTime()) + " annotation " + session.getAnnotationsSet().toJSONObject().toString());

        DBHelper.insertSessionTable(session);

    }

    public static boolean examineSessionCombinationByActivityAndTime(Session secondLastSession, String activity, long time){

        boolean combine = false;

        //get annotaitons that has the transportation mode tag
        ArrayList<Annotation> annotations = secondLastSession.getAnnotationsSet().getAnnotationByContent(activity);


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

            Log.d(TAG, "[test combine] the previous session ends at " +  secondLastSession.getEndTime() + " and the current activity starts at " + time  +
                    " the difference is " + (time - secondLastSession.getEndTime()) / Constants.MILLISECONDS_PER_MINUTE + " minutes");

            //if the current session is too close from the previous one in terms of time, we should combine
            if (time - secondLastSession.getEndTime() <= SessionManager.SESSION_MIN_INTERVAL_THRESHOLD_TRANSPORTATION) {

                Log.d(TAG, "[test combine] the current activity is too close from the previous trip, continue the last session! the difference is "
                        + (time - secondLastSession.getEndTime()) / Constants.MILLISECONDS_PER_MINUTE + " minutes");

                combine = true;

                //TODO delete the last session?

                Session lastSession = SessionManager.getLastSession();

                DBHelper.deleteSession(lastSession.getId());
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
    public static void continue2ndLastSession(Session session) {

        //remove the ongoing session
        getOngoingSessionIdList().add(session.getId());

        //update session with end time and long enough flag.
        updateCurSessionEndInfoTo(session.getId(),0,true);

    }

    /**
     * Stop recording the session, usually the ongoing session, and remove it from ongoing session list
     * @param session the ongoing session
     */
    public static void endCurSession(Session session) {

        Log.d(TAG, "test show trip: before ending the session the list sizr is " + getOngoingSessionIdList().size());

        Log.d(TAG, "test show trip: end cursession Id : " + session.getId());
        Log.d(TAG, "test show trip: end cursession EndTime : " + ScheduleAndSampleManager.getTimeString(session.getEndTime()));
        Log.d(TAG, "test show trip: end cursession isUserPress : " + session.isUserPress());

        Log.d(TAG, "test show trip: before remove the list at 0 is " + getOngoingSessionIdList().get(0));

        //remove the ongoing session
        // should use function to remove
        mOngoingSessionIdList.remove(Integer.valueOf(session.getId()));

        Log.d(TAG, "test show trip: after remove going the list is  " + getOngoingSessionIdList().toString());

        //update session with end time and long enough flag.
        updateCurSessionEndInfoTo(session.getId(),session.getEndTime(),session.isUserPress());
    }

    public static ArrayList<Session> getSessions() {

        Log.d(TAG, "[test show trip] getSessions");

        ArrayList<Session> sessions = new ArrayList<Session>();

        ArrayList<String> res =  DBHelper.querySessions();

        Log.d(TAG, "[test show trip] getRecentSessions get res: " +  res);

        //we start from 1 instead of 0 because the 1st session is the background recording. We will skip it.
        for (int i=0; i<res.size() ; i++) {

            Session session = convertStringToSession(res.get(i));
            Log.d(TAG, "[test show trip] session id : "+ session.getId());
            sessions.add(session);
        }

        return sessions;
    }

    public static ArrayList<Session> getSessionsByOrder(String order) {

        Log.d(TAG, "[test show trip] getSessionsByOrder");

        ArrayList<Session> sessions = new ArrayList<Session>();

        ArrayList<String> res =  DBHelper.querySessions(order);

        Log.d(TAG, "[test show trip] getRecentSessions get res: " +  res);

        //we start from 1 instead of 0 because the 1st session is the background recording. We will skip it.
        for (int i=0; i<res.size() ; i++) {

            Session session = convertStringToSession(res.get(i));
            Log.d(TAG, "[test show trip] session id : "+ session.getId());
            sessions.add(session);
        }

        return sessions;
    }

    public static ArrayList<Session> getSessionsBetweenTimesAndOrder(long startTime, long endTime, String order) {

        Log.d(TAG, "[test show trip] getSessionsByOrder");

        ArrayList<Session> sessions = new ArrayList<Session>();

        ArrayList<String> res =  DBHelper.querySessionsBetweenTimesAndOrder(startTime, endTime, order);

        Log.d(TAG, "[test show trip] getRecentSessions get res: " +  res);

        //we start from 1 instead of 0 because the 1st session is the background recording. We will skip it.
        for (int i=0; i<res.size() ; i++) {

            Session session = convertStringToSession(res.get(i));
            Log.d(TAG, "[test show trip] session id : "+ session.getId());
            sessions.add(session);
        }

        return sessions;
    }

    /**
     * Get recent stored sessions, while "recent" is defined by Config
     */
    public static ArrayList<Session> getRecentSessions() {

        Log.d(TAG, "[test show trip] getRecentSessions");

        ArrayList<Session> sessions = new ArrayList<Session>();

        long queryEndTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
        //start time = a specific hours ago
        long queryStartTime = ScheduleAndSampleManager.getCurrentTimeInMillis() - Constants.MILLISECONDS_PER_HOUR * SESSION_DISPLAY_RECENCY_THRESHOLD_HOUR;

        Log.d(TAG, " [test show trip] going to query session between " + ScheduleAndSampleManager.getTimeString(queryStartTime) + " and " + ScheduleAndSampleManager.getTimeString(queryEndTime) );


        //query get sessions between the starTime and endTime
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

    /**
     * Get the requiring session data from table of database
     * @param sessionId id of requiring session
     * @param tableName the table which stored requiring session
     * @return session data in ArrayList String format
     */
    public static ArrayList<String> getRecordsInSession(int sessionId, String tableName) {

        ArrayList<String> resultList = new ArrayList<String>();

        resultList = DBHelper.queryRecordsInSession(tableName, sessionId);
        Log.d(TAG, "[getRecordsInSession] test combine got " + resultList.size() + " of results from queryRecordsInSession");

        return resultList;
    }

    /**
     * ##Should move to date format related class
     */
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

    /**
     * Transfer annotationSet data from jSon to AnnotationSet format
     * @param annotationJSONArray
     * @return
     */
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