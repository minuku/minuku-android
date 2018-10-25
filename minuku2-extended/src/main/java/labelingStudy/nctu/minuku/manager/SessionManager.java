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
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.Utilities.Utils;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;

/**
 * Created by Lawrence on 2018/3/13.
 */

public class SessionManager {

    private final static String TAG = "SessionManager";

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

    private static SessionManager instance;

    private static ArrayList<Integer> mOngoingSessionIdList;
    private static ArrayList<Integer> mEmptyOngoingSessionIdList;

    public static boolean sessionIsWaiting;


    public SessionManager(Context context) {

        this.mContext = context;

        mOngoingSessionIdList = new ArrayList<Integer>();

        mEmptyOngoingSessionIdList = new ArrayList<Integer>();

        sessionIsWaiting = false;
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

    public static int getOngoingSessionId(){

        int session_id = -1;

        int countOfOngoingSession = mOngoingSessionIdList.size();

        //if there exists an ongoing session
        if (countOfOngoingSession > 0){
            session_id = mOngoingSessionIdList.get(0);
        }

        return session_id;
    }

    public static boolean isSessionOngoing(int sessionId, SharedPreferences sharedPrefs) {

        Log.d(TAG, " [test combine] tyring to see if the session is ongoing:" + sessionId);

//        for (int i = 0; i< mOngoingSessionIdList.size(); i++){
//            //       Log.d(LOG_TAG, " [getCurRecordingSession] looping to " + i + "th session of which the id is " + mCurRecordingSessions.get(i).getId());
//
//            if (mOngoingSessionIdList.get(i)==sessionId){
//                return true;
//            }
//        }

        int ongoingSessionid = sharedPrefs.getInt("ongoingSessionid", Constants.INVALID_INT_VALUE);

        if(ongoingSessionid == sessionId){

            return true;
        }

        Log.d(TAG, " [test combine] the session is not ongoing:" + sessionId);

        return false;
    }

    /**
     * In current structure, there would only be one ongoingSession in the list, while the ongoingSession could be considered as the current recording session
     * @return
     */
    public static ArrayList<Integer> getOngoingSessionIdList() {
        return mOngoingSessionIdList;
    }

    public static boolean isSessionEmptyOngoing(int sessionId, SharedPreferences sharedPrefs) {

        Log.d(TAG, " [test combine] tyring to see if the session is ongoing:" + sessionId);

//        for (int i = 0; i< mEmptyOngoingSessionIdList.size(); i++){
//
//            if (mEmptyOngoingSessionIdList.get(i)==sessionId){
//                return true;
//            }
//        }

        int ongoingSessionid = sharedPrefs.getInt("emptyOngoingSessionid", Constants.INVALID_INT_VALUE);

        if(ongoingSessionid == sessionId){

            return true;
        }

        Log.d(TAG, " [test combine] the session is not ongoing:" + sessionId);

        return false;
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
     * This function convert Session String retrieved from the DB to Object Session
     * @param sessionStr
     * @return
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

        if (!separated[DBHelper.COL_INDEX_SESSION_HIDEDORNOT].equals("null") && !separated[DBHelper.COL_INDEX_SESSION_HIDEDORNOT].equals("")) {

            session.setHidedOrNot(Integer.valueOf(separated[DBHelper.COL_INDEX_SESSION_HIDEDORNOT]));
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
            Log.e(TAG, "JSONException", e);
            e.printStackTrace();
        }

        //set annotationset if there is one
        if (annotateionSetJSONArray!=null){

            AnnotationSet annotationSet = toAnnorationSet(annotateionSetJSONArray);
            session.setAnnotationSet(annotationSet);
        }

        return session;
    }

    public static Session getSession (String id) {

        int sessionId = Integer.parseInt(id);
        ArrayList<String> res = DBHelper.querySession(sessionId);
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

    public static Session getSecondLastSession() {

        Session session = null;

        ArrayList<String> sessions = DBHelper.querySecondLastSessions();
        if(sessions.size() >= 2) {

            String sessionStr = sessions.get(1);
            Log.d(TAG, "test show trip lastsession " + sessionStr);
            session = convertStringToSession(sessionStr);
            Log.d(TAG, " test show trip  testgetdata id " + session.getId() + " startTime " + ScheduleAndSampleManager.getTimeString(session.getStartTime()) + " end time " + ScheduleAndSampleManager.getTimeString(session.getEndTime()) + " annotation " + session.getAnnotationsSet().toJSONObject().toString());
        }else{

            session = new Session(1);
        }

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

        Log.d(TAG, "sessionId : "+sessionId);
        Session session = null;
        try {

            String sessionStr = DBHelper.querySession(sessionId).get(0);
            Log.d(TAG, "query session from LocalDB is " + sessionStr);
            session = convertStringToSession(sessionStr);
            Log.d(TAG, " testgetdata id " + session.getId() + " startTime " + session.getStartTime() + " end time " + session.getEndTime() + " annotation " + session.getAnnotationsSet().toJSONObject().toString());
        }catch (IndexOutOfBoundsException e){
            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, "sessionId : "+sessionId+" no data in DB");
            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, Utils.getStackTrace(e));
            getSession(sessionId-1);
        }
        return session;
    }

    public static int getNumOfSession(){
        int num = 0;

        num = (int)DBHelper.querySessionCount();

        return num;
    }

    /**
     *
     * @param sessionId
     * @param endTime
     * @param userPressOrNot
     */
    public static void updateCurSessionEndInfoTo(int sessionId, long endTime, boolean userPressOrNot){

        DBHelper.updateSessionTable(sessionId, endTime, userPressOrNot);
    }

    public static void updateCurSession(int sessionId, long endTime, boolean userPressOrNot){

        DBHelper.updateSessionTable(sessionId, endTime, userPressOrNot);
    }

    public static void updateCurSession(int sessionId, long endTime, int userPressOrNot, int modifiedOrNot){

        DBHelper.updateSessionTable(sessionId, endTime, userPressOrNot, modifiedOrNot);
    }

    public static void updateCurSession(int sessionId, int hidedOrNot){

        //endtime is for preventing overload the function with toSentOrNot
        DBHelper.updateSessionTable(sessionId, 0, hidedOrNot);
    }

    /**
     *
     * @param session
     */
    public static void startNewSession(Session session) {

        //InstanceManager add ongoing session for the new activity
//        SessionManager.getInstance().addOngoingSessionid(session.getId());

        Log.d(TAG, "startNewSession id " + session.getId() + " startTime " + ScheduleAndSampleManager.getTimeString(session.getStartTime()) + " end time " + ScheduleAndSampleManager.getTimeString(session.getEndTime()) + " annotation " + session.getAnnotationsSet().toJSONObject().toString());

        DBHelper.insertSessionTable(session);

    }

    /**
     * Combine sessions if
     * newSession and lastSecond are same
     * or
     * newSession and lastSecond are same while last is static
     */
    public static boolean examineSessionCombinationByActivityAndTime(String newSessionActivityType, long newSessionStartTime){

        boolean combine = false;

        Log.d(TAG, "EXAMINE : " + newSessionActivityType);
        if (newSessionActivityType == TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION) {
            CSVHelper.storeToCSV(CSVHelper.CSV_EXAMINE_COMBINE_SESSION,
                    newSessionActivityType,
                    String.valueOf(combine));
            return false;
        }
        Session lastSession = getLastSession();
        Session secondLastSession = getSecondLastSession();
        //get annotaitons that has the transportation mode tag

        //check if the last session has endtime. It is possible that it ends unexpectedly

        //if the previous session does not have any annotation of which transportation is of the same tag, we should not combine
        Log.d(TAG, "[test combine] addSessionFlag = true  the last session is not the same activity");
        String staticActivity = "static";
        if (lastSession.getAnnotationsSet().getAnnotationByContent(staticActivity).size() == 0) {
            combine = false;
        } else {
            if (secondLastSession.getAnnotationsSet().getAnnotationByContent(newSessionActivityType).size() == 0) {
                combine = false;
            } else {
                // TODO: identify if the threshold should double since this is the case that merging three sessions
                int twiceIntervalThreshHoldFactor = 1;
                // the current activity is the same TM with the previous session mode, we check its time difference
                Log.d(TAG, "[test combine] we found the third last session with the same activity");
                //check its interval to see if it's within 5 minutes

                Log.d(TAG, "[test combine] the third last session ends at " +  secondLastSession.getEndTime() + " and the current activity starts at " + newSessionStartTime  +
                        " the difference is " + (newSessionStartTime - secondLastSession.getEndTime()) / Constants.MILLISECONDS_PER_MINUTE + " minutes");

                //if the current session is too close from the previous one in terms of time, we should combine
                if (newSessionStartTime - secondLastSession.getEndTime() <=
                        (SessionManager.SESSION_MIN_INTERVAL_THRESHOLD_TRANSPORTATION * twiceIntervalThreshHoldFactor)) {

                    Log.d(TAG, "[test combine] the current activity is too close from the previous trip, continue the last session! the difference is "
                            + (newSessionStartTime - secondLastSession.getEndTime()) / Constants.MILLISECONDS_PER_MINUTE + " minutes");

                    combine = true;
                } else {
                    //the session is far from the previous one, it should be a new session. we should not combine
                    Log.d(TAG, "[test combine] addSessionFlag = true the current truip is far from the previous trip");
                    combine = false;
                }
            }
        }

        // new start, last start, threshold
        CSVHelper.storeToCSV(CSVHelper.CSV_EXAMINE_COMBINE_SESSION,
                ScheduleAndSampleManager.getTimeString(newSessionStartTime, new SimpleDateFormat(Constants.DATE_FORMAT_HOUR_MIN_SECOND)),
                ScheduleAndSampleManager.getTimeString(secondLastSession.getStartTime(), new SimpleDateFormat(Constants.DATE_FORMAT_HOUR_MIN_SECOND)),
                String.valueOf((newSessionStartTime - secondLastSession.getEndTime())),
                newSessionActivityType,
                (secondLastSession.getAnnotationsSet().getAnnotationByContent(newSessionActivityType).size() == 0) ? "Same" : "Different",
                (lastSession.getAnnotationsSet().getAnnotationByContent(staticActivity).size() == 0)  ? "Static" : "NonStatic",
                String.valueOf(combine));
        //debug...
//        String lastSessionStr = DBHelper.queryLastSession().get(0);
//        Log.d(TAG, "test combine: the previous acitivty is movnig,after combine it the last session is: " +  lastSessionStr );
        return combine;
    }


    /**
     * Combine secondLast  session and new session
     */
    public static void continue2ndLastSession(SharedPreferences sharedPrefs) {

        //reset ongoingSession to last session
//        getOngoingSessionIdList().add(getLastSession().getId());
//        getOngoingSessionIdList().add(getSecondLastSession().getId());
        sharedPrefs.edit().putInt("ongoingSessionid", getSecondLastSession().getId()).apply();

        //update session with end time and long enough flag.
        updateCurSessionEndInfoTo(getSecondLastSession().getId(),0,true);

        //TODO set the lastSession which is static to be the flag representing do not show it
        updateCurSession(getLastSession().getId(), Constants.SESSION_IS_HIDED_FLAG);
    }

    /**
     *
     * @param session
     */
    public static void endCurSession(Session session) {

        Log.d(TAG, "test show trip: end cursession Id : " + session.getId());
        Log.d(TAG, "test show trip: end cursession EndTime : " + ScheduleAndSampleManager.getTimeString(session.getEndTime()));
        Log.d(TAG, "test show trip: end cursession isUserPress : " + session.isUserPress());

        //remove the ongoing session
//        mOngoingSessionIdList.remove(Integer.valueOf(session.getId()));

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

    public static Session combineSession(Session s1, Session s2) {
        Session sessionPrevious;
        Session sessionLater;

        if (s1.getEndTime() < s2.getEndTime()) {
            sessionPrevious = s1;
            sessionLater = s2;
        } else {
            sessionPrevious = s2;
            sessionLater = s1;
        }
        //TODO: merge two sessions, may note the order

        return sessionPrevious;
    }
}