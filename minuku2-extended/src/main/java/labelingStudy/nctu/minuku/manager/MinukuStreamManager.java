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

package labelingStudy.nctu.minuku.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import labelingStudy.nctu.minuku.Data.DBHelper;
import labelingStudy.nctu.minuku.Data.DataHandler;
import labelingStudy.nctu.minuku.NearbyPlaces.GetUrl;
import labelingStudy.nctu.minuku.R;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.Utilities.Utils;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.model.MinukuStreamSnapshot;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minukucore.event.IsDataExpectedEvent;
import labelingStudy.nctu.minukucore.event.NoDataChangeEvent;
import labelingStudy.nctu.minukucore.event.StateChangeEvent;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.manager.StreamManager;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.model.StreamSnapshot;
import labelingStudy.nctu.minukucore.stream.Stream;
import labelingStudy.nctu.minukucore.streamgenerator.StreamGenerator;

/**
 * Created by Neeraj Kumar on 7/17/16.
 *
 * The MinukuStreamManager class implements {@link StreamManager} and runs as a service within
 * the application context. It maintains a list of all the Streams and StreamGenerators registered
 * within the application and is responsible for trigerring the
 * {@link StreamGenerator#updateStream() updateStream} method of the StreamManager class after
 * every {@link StreamGenerator#getUpdateFrequency() updateFrequency}.
 *
 * This depends on a service to call it's updateStreamGenerators method.
 */
public class MinukuStreamManager implements StreamManager {

    private final String TAG = "MinukuStreamManager";

    protected Map<Class, Stream> mStreamMap;
    protected Map<Stream.StreamType, List<Stream<? extends DataRecord>>> mStreamTypeStreamMap;
    protected Map<Class, StreamGenerator> mRegisteredStreamGenerators;

    private ActivityRecognitionDataRecord activityRecognitionDataRecord;
    private TransportationModeDataRecord transportationModeDataRecord;
    private LocationDataRecord locationDataRecord;

    private static int counter = 0;

    private Handler handler = new Handler();

    private static MinukuStreamManager instance;

    private MinukuStreamManager() throws Exception {

        mStreamMap = new HashMap<>();
        mStreamTypeStreamMap = new HashMap<>();
        mRegisteredStreamGenerators = new HashMap<>();
    }

    public static MinukuStreamManager getInstance() {
        if(MinukuStreamManager.instance == null) {
            try {
                MinukuStreamManager.instance = new MinukuStreamManager();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return MinukuStreamManager.instance;
    }

    public void updateStreamGenerators() {
        for(StreamGenerator streamGenerator: mRegisteredStreamGenerators.values()) {
            Log.d(TAG, "Stream generator : " + streamGenerator.getClass() + " \n" +
                    "Update frequency: " + streamGenerator.getUpdateFrequency() + "\n" +
                    "Counter: " + counter);
            if(streamGenerator.getUpdateFrequency() == -1) {
                continue;
            }
            if(counter % streamGenerator.getUpdateFrequency() == 0) {
                Log.d(TAG, "Calling update stream generator for " + streamGenerator.getClass());
                try{

                    streamGenerator.updateStream();
                }catch (Exception e){

                    CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, "Class : "+streamGenerator.getClass().getName());
                    CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, Utils.getStackTrace(e));
                }
            }
        }
        counter++;
    }


    @Override
    public List<Stream> getAllStreams() {
        return new LinkedList<>(mStreamMap.values());
    }

    @Override
    public <T extends DataRecord> void register(Stream s,
                                                Class<T> clazz,
                                                StreamGenerator aStreamGenerator)
            throws StreamNotFoundException, StreamAlreadyExistsException {
        if(mStreamMap.containsKey(clazz)) {
            throw new StreamAlreadyExistsException();
        }
        for(Object dependsOnClass:s.dependsOnDataRecordType()) {
            if(!mStreamMap.containsKey(dependsOnClass)) {
                Log.e(TAG, "Stream not found : " + dependsOnClass.toString());
                throw new StreamNotFoundException();
            }
        }
        mStreamMap.put(clazz, s);
        mRegisteredStreamGenerators.put(clazz, aStreamGenerator);
        aStreamGenerator.onStreamRegistration();
        Log.d(TAG, "Registered a new stream generator for " + clazz);
    }

    @Override
    public void unregister(Stream s, StreamGenerator sg)
            throws StreamNotFoundException {
        Class classType = s.getCurrentValue().getClass();
        if(!mStreamMap.containsKey(classType)) {
            throw new StreamNotFoundException();
        }
        mStreamMap.remove(s);
        mRegisteredStreamGenerators.remove(sg);
    }

    @Override
    public <T extends DataRecord> Stream<T> getStreamFor(Class<T> clazz)
            throws StreamNotFoundException {
        if(mStreamMap.containsKey(clazz)) {
            return mStreamMap.get(clazz);
        } else {
            throw new StreamNotFoundException();
        }
    }

    @Override
    @Subscribe
    public void handleStateChangeEvent(StateChangeEvent aStateChangeEvent) {
        MinukuSituationManager.getInstance().onStateChange(getStreamSnapshot(),
                aStateChangeEvent);
    }

    @Override
    @Subscribe
    public void handleNoDataChangeEvent(NoDataChangeEvent aNoDataChangeEvent) {
        MinukuSituationManager.getInstance().onNoDataChange(getStreamSnapshot(),
                aNoDataChangeEvent);
    }

    @Override
    @Subscribe
    public void handleIsDataExpectedEvent(IsDataExpectedEvent isDataExpectedEvent) {
        MinukuSituationManager.getInstance().onIsDataExpected(getStreamSnapshot(),
                isDataExpectedEvent);
    }

    @Override
    public List<Stream<? extends DataRecord>> getStreams(Stream.StreamType streamType) {
        return mStreamTypeStreamMap.get(streamType);
    }

    @Override
    public <T extends DataRecord> StreamGenerator<T> getStreamGeneratorFor(Class<T> clazz)
            throws StreamNotFoundException {
        if(mRegisteredStreamGenerators.containsKey(clazz)) {
            return mRegisteredStreamGenerators.get(clazz);
        } else {
            throw new StreamNotFoundException();
        }
    }

    private StreamSnapshot getStreamSnapshot() {
        Map<Class<? extends DataRecord>, List<? extends DataRecord>> streamSnapshotData =
                new HashMap<>();
        for(Map.Entry<Class, Stream> entry: mStreamMap.entrySet()) {
            List list = createListOfType(entry.getKey());
            list.add(entry.getValue().getCurrentValue());
            list.add(entry.getValue().getPreviousValue());
            streamSnapshotData.put(entry.getKey(), list);
        }
        return new MinukuStreamSnapshot(streamSnapshotData);
    }

    private static <T extends DataRecord>  List<T> createListOfType(Class<T> type) {
        return new ArrayList<T>();
    }

    public void setActivityRecognitionDataRecord(ActivityRecognitionDataRecord activityRecognitionDataRecord){
        this.activityRecognitionDataRecord = activityRecognitionDataRecord;
    }

    public ActivityRecognitionDataRecord getActivityRecognitionDataRecord(){
        return activityRecognitionDataRecord;
    }

    public void setTransportationModeDataRecord(TransportationModeDataRecord transportationModeDataRecord, final Context context, final SharedPreferences sharedPrefs){

        Log.d(TAG, "[test triggering] incoming transportation: " + transportationModeDataRecord.getConfirmedActivityString());

        Boolean addSessionFlag = false;

        //the first time we see incoming transportation mode data
        if (this.transportationModeDataRecord==null){

            this.transportationModeDataRecord = new TransportationModeDataRecord(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA);
            Log.d(TAG, "[test triggering] test trip original null updated to " + this.transportationModeDataRecord.getConfirmedActivityString());
        }
        else {

            Log.d(TAG, "[test triggering] NEW: " + transportationModeDataRecord.getConfirmedActivityString() + " vs OLD:" + this.transportationModeDataRecord.getConfirmedActivityString());

            /**
             *  check if the new activity label is different from the previous activity label. IF it is different, we should do something
             * **/

            String currentWork = context.getResources().getString(R.string.current_task);

            Log.d(TAG,"in setTransportationModeDataRecord, currentWork is "+currentWork);

            //in PART, the Session is controlled by the user, no need to detect by the app.
            if(!currentWork.equals(context.getResources().getString(R.string.task_PART))) {

                //if the current activity is different from the latest one
                if (!this.transportationModeDataRecord.getConfirmedActivityString().equals(transportationModeDataRecord.getConfirmedActivityString())) {

                    Log.d(TAG, "[test triggering] test trip: the new acitivty is different from the previous!");

                    /** we first see if the this is the first session **/
                    int sessionCount = SessionManager.getNumOfSession();

                    //if this is the first time seeing a session and the new transportation is neither static nor NA, we should just insert a session
                    if (sessionCount == 0
                                && !transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA)) {

                        Log.d(TAG, "[test triggering] addSessionFlag = true there's no session in the db");
                        CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, "addSessionFlag = true there's no session in the db");

                        addSessionFlag = true;
                    }
                    //there's exizstint sessions in the DB
                    else if (sessionCount > 0) {

                        //get the latest session (which should be the ongoing one)
                        Session lastSession = SessionManager.getLastSession();

                        int sessionIdOfLastSession = lastSession.getId();
                        AnnotationSet annotationSet = lastSession.getAnnotationsSet();
                        long endTimeOfLastSession = lastSession.getEndTime();
                        long startTimeOfLastSession = lastSession.getStartTime();

                        Log.d(TAG, "[test triggering] session " + sessionIdOfLastSession + " with annotation string " + annotationSet.toString() + " end time " + endTimeOfLastSession + " startTime " + startTimeOfLastSession);

                        CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, "session id : " + sessionIdOfLastSession +
                                " with annotation string : " + annotationSet.toString() + ", end time : " + ScheduleAndSampleManager.getTimeString(endTimeOfLastSession) +
                                ", startTime : " + ScheduleAndSampleManager.getTimeString(startTimeOfLastSession));

                        boolean emptySessionOn = SessionManager.isSessionEmptyOngoing(sessionIdOfLastSession, sharedPrefs);

                        if (!this.transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA)) {

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, "not emptySessionOn ? "+(!emptySessionOn));

                            //if there are no empty session, stop it
                            if(!emptySessionOn){

                                CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, "lastSession id : "+lastSession.getId() + " is not empty Session ");

                                //if we end the current session, we should update its time and set a long enough flag
                                long endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();

                                //check the suspect end time
                                //set the current time for default
                                lastSession.setEndTime(endTime);

                                //query suspect start time PREVIOUS id
                                ArrayList<String> firstDiffSuspectStopTransportation = DBHelper.queryTransportationSuspectedStopTimePreviousId(transportationModeDataRecord.getConfirmedActivityString());
                                if(firstDiffSuspectStopTransportation.size() > 0) {

                                    String diff1stSuspectedStopTransportation_ID = firstDiffSuspectStopTransportation.get(DBHelper.COL_INDEX_RECORD_ID);

                                    //query suspect start time id (the next one from the above data)
                                    ArrayList<String> suspectedStopTransportation = DBHelper.queryNextData(DBHelper.transportationMode_table, Integer.valueOf(diff1stSuspectedStopTransportation_ID));

                                    if(suspectedStopTransportation.size() > 0){

                                        String suspectTime = suspectedStopTransportation.get(DBHelper.COL_INDEX_Suspected_Transportation_TIME);

                                        lastSession.setEndTime(Long.valueOf(suspectTime));
                                    }
                                }

                                CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " lastSession EndTime : "+ScheduleAndSampleManager.getTimeString(lastSession.getEndTime()));

                                //end the current session
                                SessionManager.endCurSession(lastSession);

                                CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " End lastSession which is not empty Session.");

                                sharedPrefs.edit().putInt("ongoingSessionid", Constants.INVALID_INT_VALUE).apply();

                                CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " Update ongoing session id with : -1(invalid id)");
                            }

                            addSessionFlag = true;

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " addSessionFlag : "+addSessionFlag);
                        }
                    }

                    Log.d(TAG, "[test triggering] addSessionFlag : "+addSessionFlag);
                    Log.d(TAG, "[test triggering] is NA ? : "+!transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA));

                    CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " addSessionFlag ? "+addSessionFlag);
                    CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " tranp not NA ? "+(!transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA)));
                    CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " tranp is : "+transportationModeDataRecord.getConfirmedActivityString());

                    //if we need to add a session
                    if (addSessionFlag && !transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA)) {

                        Log.d(TAG, "[test triggering] we should add session " + ((int) sessionCount + 1));

                        Session lastSession = SessionManager.getLastSession();

                        int sessionIdOfLastSession = lastSession.getId();
                        boolean emptySessionOn = SessionManager.isSessionEmptyOngoing(sessionIdOfLastSession, sharedPrefs);

                        Log.d(TAG, "[test triggering] is CAR ? " + (currentWork.equals(context.getResources().getString(R.string.task_CAR))));
                        Log.d(TAG, "[test triggering] is emptySessionOn ? " + emptySessionOn);
                        Log.d(TAG, "[test triggering] is CAR & emptySessionOn ? " + (currentWork.equals(context.getResources().getString(R.string.task_CAR)) && emptySessionOn));

                        CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " is CAR ? "+(currentWork.equals(context.getResources().getString(R.string.task_CAR))));
                        CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " is emptySessionOn ? "+emptySessionOn);
                        CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " is CAR & emptySessionOn ? "+(currentWork.equals(context.getResources().getString(R.string.task_CAR)) && emptySessionOn));

                        //if there is an emptySession, update it to the next different system detected transportationMode
                        if (currentWork.equals(context.getResources().getString(R.string.task_CAR)) && emptySessionOn) {

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " update the empty session");

                            Annotation annotation = new Annotation();
                            annotation.setContent(transportationModeDataRecord.getConfirmedActivityString());
                            annotation.addTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY);
                            lastSession.addAnnotation(annotation);

                            DataHandler.updateSession(lastSession.getId(), lastSession.getAnnotationsSet());

                            sharedPrefs.edit().putInt("emptyOngoingSessionid", Constants.INVALID_INT_VALUE).apply();
                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION,"update empty sessionid to "+Constants.INVALID_INT_VALUE);

//                            SessionManager.getEmptyOngoingSessionIdList().remove(Integer.valueOf(lastSession.getId()));

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION,
                                    " filled the transportation : "+transportationModeDataRecord.getConfirmedActivityString()+" in the emptysession : "+lastSession.getId());

                            //to end a session (the previous is moving)
                            //we first need to check whether the previous is a transportation
                        } else {

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " insert a new session");

                            //insert into the session table;
                            int sessionId = (int) sessionCount + 1;
                            Session session = new Session(sessionId);

                            session.setCreatedTime(ScheduleAndSampleManager.getCurrentTimeInMillis());

                            //set the current time for default
                            session.setStartTime(ScheduleAndSampleManager.getCurrentTimeInMillis());

                            //query suspect start time PREVIOUS id
                            ArrayList<String> firstDiffSuspectedStartTransportation = DBHelper.queryTransportationSuspectedStartTimePreviousId(transportationModeDataRecord.getConfirmedActivityString());
                            if(firstDiffSuspectedStartTransportation.size() > 0) {

                                String diff1stSuspectedStartTransportation_ID = firstDiffSuspectedStartTransportation.get(DBHelper.COL_INDEX_RECORD_ID);

                                //query suspect start time id (the next one from the above data)
                                ArrayList<String> suspectedStartTransportation = DBHelper.queryNextData(DBHelper.transportationMode_table, Integer.valueOf(diff1stSuspectedStartTransportation_ID));

                                if(suspectedStartTransportation.size() > 0){

                                    String suspectTime = suspectedStartTransportation.get(DBHelper.COL_INDEX_Suspected_Transportation_TIME);

                                    session.setStartTime(Long.valueOf(suspectTime));

                                    CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " set Session StartTime with the suspectStartTime : "+ScheduleAndSampleManager.getTimeString(session.getStartTime()));
                                }
                            }

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " Session StartTime : "+ScheduleAndSampleManager.getTimeString(session.getStartTime()));

                            Annotation annotationForTransportation = new Annotation();
                            annotationForTransportation.setContent(transportationModeDataRecord.getConfirmedActivityString());
                            annotationForTransportation.addTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY);
                            session.addAnnotation(annotationForTransportation);

                            //if transportationMode is static, add site by getUrl
                            if(transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION)){

                                Annotation annotationForSite = new Annotation();
                                Annotation annotationForSiteLoc = new Annotation();
                                //add site by getUrl
                                //get the latest location for searching site
                                ArrayList<String> locations = DBHelper.queryLastRecord(DBHelper.location_table);
                                if(locations.size() > 0){

                                    Log.d(TAG, "locations : "+locations);
                                    String location = locations.get(locations.size() - 1);
                                    String[] locationPieces = location.split(Constants.DELIMITER);
                                    double lat = Double.parseDouble(locationPieces[DBHelper.COL_INDEX_LOC_LATITUDE]);
                                    double lng = Double.parseDouble(locationPieces[DBHelper.COL_INDEX_LOC_LONGITUDE]);

                                    //check the customizedTable first
                                    String siteInform;
                                    boolean checkFromNet = true;

                                    ArrayList<String> customizedSite = DBHelper.queryCustomizedSites();

                                    float smallestDist = Float.MAX_VALUE;
                                    String closestSite = "", closestLat = "", closestLng = "";
                                    //check the distance between the session's first location and the customizedSite
                                    for(int index = 0; index < customizedSite.size(); index++){

                                        String eachData = customizedSite.get(index);

                                        String[] dataPieces = eachData.split(Constants.DELIMITER);

                                        Log.d(TAG, "check 精準度");
                                        Log.d(TAG, "sitename : "+dataPieces[1]+", siteLat : "+dataPieces[2]+", siteLng : "+dataPieces[3]);

                                        double siteLat = Double.parseDouble(dataPieces[2]);
                                        double siteLng = Double.parseDouble(dataPieces[3]);

                                        float[] results = new float[1];
                                        Location.distanceBetween(lat, lng, siteLat, siteLng, results);
                                        float distance = results[0];

                                        Log.d(TAG, "customizedSite");
                                        Log.d(TAG, "sitename : "+dataPieces[1]+", siteLat : "+siteLat+", siteLng : "+siteLng);

                                        if(smallestDist >= distance){

                                            smallestDist = distance;
                                            closestSite = dataPieces[1];
                                            closestLat = dataPieces[2];
                                            closestLng = dataPieces[3];
                                        }

                                    }

                                    //if still no site close enough then check from the net
                                    if(smallestDist <= Constants.siteRange){

                                        siteInform = closestSite+Constants.DELIMITER+"("+closestLat+","+closestLng+")";
                                    }else {

                                        siteInform = GetUrl.getSiteInformFromNet(lat, lng);
                                    }

                                    String siteName = siteInform.split(Constants.DELIMITER)[0];
                                    String siteLoc = siteInform.split(Constants.DELIMITER)[1];
                                    annotationForSite.setContent(siteName);
                                    annotationForSite.addTag(Constants.ANNOTATION_TAG_DETECTED_SITENAME);
                                    session.addAnnotation(annotationForSite);
                                    annotationForSiteLoc.setContent(siteLoc);
                                    annotationForSiteLoc.addTag(Constants.ANNOTATION_TAG_DETECTED_SITELOCATION);
                                    session.addAnnotation(annotationForSiteLoc);

                                    CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " Session detected site name : "+ siteName);

                                    Log.d(TAG, "[test triggering] detected siteName");
                                }
                            }

                            session.setUserPressOrNot(false);
                            session.setModified(false);
                            session.setIsSent(Constants.SESSION_SHOULDNT_BEEN_SENT_FLAG);
                            session.setType(Constants.SESSION_TYPE_DETECTED_BY_SYSTEM);
                            session.setHidedOrNot(Constants.SESSION_NEVER_GET_HIDED_FLAG);

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " insert the session with annotation : "+session.getAnnotationsSet().toJSONObject().toString());

                            Log.d(TAG, "[test triggering] insert the session is with annotation " + session.getAnnotationsSet().toJSONObject().toString());

                            String beforeSendESM = "Preparing to send ESM";
                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, beforeSendESM);

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " Examine if combine sessions, please check "+CSVHelper.CSV_EXAMINE_COMBINE_SESSION);

                            if (SessionManager.examineSessionCombinationByActivityAndTime(transportationModeDataRecord.getConfirmedActivityString(), session.getStartTime()) == true) {

                                CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " continue2ndLastSession ");

                                //Should combine
                                SessionManager.continue2ndLastSession(sharedPrefs);
                            } else {

                                CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " startNewSession ");

                                SessionManager.startNewSession(session);
                            }

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " after startNewSession ");

                            sharedPrefs.edit().putInt("ongoingSessionid", session.getId()).apply();

                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " update ongoingSessionid to : "+session.getId());
                        }

                        final Session sessionJustStart = SessionManager.getLastSession();

                        // set it earlier
                        this.transportationModeDataRecord = transportationModeDataRecord;

                        if (currentWork.equals(context.getResources().getString(R.string.task_ESM))) {

                            Log.d(TAG, "[test triggering] show session StartTime : "+sessionJustStart.getStartTime());
                            Log.d(TAG, "[test triggering] show session StartTime String : "+ScheduleAndSampleManager.getTimeString(sessionJustStart.getStartTime()));

                            String sessionTransportation = getTransportationNameOrSite(sessionJustStart, context);
                            sendNotification(context, sessionJustStart.getStartTime(), sessionTransportation);

                            String afterSendESM = "After sending ESM";
                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, afterSendESM);

                            Log.d(TAG, "[test triggering] ESM Notification");

                        } else if (currentWork.equals(context.getResources().getString(R.string.task_CAR))) {

                            //CAR need to be in the start of the session, after a threshold of the time, send the notification to remind the user

                            SessionManager.sessionIsWaiting = true;

                            //wait for a minute to the user
                            handler.postDelayed(new Runnable() {

                                @Override
                                public void run() {

                                    CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, "after the delay for a minute");

                                    int ongoingSessionid = sharedPrefs.getInt("ongoingSessionid", Constants.INVALID_INT_VALUE);

                                    //detect the user has pressed the current trip(Session) or not.
//                                    if(SessionManager.getOngoingSessionIdList().size() != 0){
                                    if(ongoingSessionid != Constants.INVALID_INT_VALUE){

//                                        int ongoingSessionid = SessionManager.getOngoingSessionIdList().get(0);
                                        Session ongoingSession = SessionManager.getSession(ongoingSessionid);

                                        //if the user hasn't pressed the current trip(Session); after ending a trip(session), send a notification to the user
                                        if (!ongoingSession.isUserPress()) {

                                            String sessionTransportation = getTransportationNameOrSite(sessionJustStart, context);
                                            sendNotification(context, sessionJustStart.getStartTime(), sessionTransportation);

                                            String afterSendCAR = "After sending CAR";
                                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, afterSendCAR);

                                            Log.d(TAG, "[test triggering] CAR Notification");
                                        } else {

                                            //recording
                                            String checkCAR = "the CAR record has been checkpointed by the user";
                                            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, checkCAR);

                                            Log.d(TAG, "[test triggering] CAR check");
                                        }

                                        //the ongoing session might be removed because of the empty ongoing one.
                                    }else{

                                        String checkCAR = "the ongoing session is removed, assumed it was checkpointed";
                                        CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, checkCAR);

                                        Log.d(TAG, "[test triggering] "+ checkCAR);
                                    }

                                    SessionManager.sessionIsWaiting = false;

                                }
                            }, Constants.MILLISECONDS_PER_MINUTE);
                        }
                    }
                }
            }

            this.transportationModeDataRecord = transportationModeDataRecord;
        }
    }

    private String getTransportationNameOrSite(Session sessionJustStart, Context context){

        ArrayList<Annotation> sessionTransportations = sessionJustStart.getAnnotationsSet().getAnnotationByTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY);

        String sessionTransportation = "";

        if(sessionTransportations.size() > 0)
            sessionTransportation = sessionTransportations.get(sessionTransportations.size()-1).getContent();

        if(sessionTransportation.equals("static")){

            ArrayList<Annotation> sessionSites = sessionJustStart.getAnnotationsSet().getAnnotationByTag(Constants.ANNOTATION_TAG_DETECTED_SITENAME);

            if(sessionSites.size() > 0)
                sessionTransportation = sessionSites.get(sessionSites.size()-1).getContent();
        }else{

            //TODO convert into Chinese name
            sessionTransportation = getTrafficInChinese(sessionTransportation, context);
        }

        return sessionTransportation;
    }

    private void sendNotification(Context context, long startTime, String sessionTransportation){

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        SimpleDateFormat sdf_ampm_hhmm = new SimpleDateFormat(Constants.DATE_FORMAT_AMPM_HOUR_MIN);
        String notificationText = "您有新的移動紀錄 : "+sessionTransportation+"\r\n"+"始於 "+ScheduleAndSampleManager.getTimeString(startTime, sdf_ampm_hhmm);
        Notification notification = getNotification(notificationText, context);

        mNotificationManager.notify(MinukuNotificationManager.reminderNotificationID, notification);
    }

    private Notification getNotification(String text, Context context){

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle("LS");
        bigTextStyle.bigText(text);

        //launch the Timeline Page, check the action in the app's AndroidManifest.xml
        Intent resultIntent = new Intent("app.intent.action.Launch"); //MinukuNotificationManager.toTimeline;
        PendingIntent pending = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(context)
                .setContentTitle(Constants.APP_FULL_NAME)
                .setContentText(text)
                .setContentIntent(pending)
                .setStyle(bigTextStyle)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noti.setChannelId(Constants.SURVEY_CHANNEL_ID);
        }

        Notification note = noti.setSmallIcon(MinukuNotificationManager.getNotificationIcon(noti)).build();

        return note;
    }

    public TransportationModeDataRecord getTransportationModeDataRecord(){
        return transportationModeDataRecord;
    }

    public void setLocationDataRecord(LocationDataRecord locationDataRecord){
        this.locationDataRecord = locationDataRecord;
    }

    private String getTrafficInChinese(String activity, Context context){

        Log.d(TAG, "getTrafficInChinese activity : "+ activity);

        switch (activity){

            case ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_ON_FOOT:
                return context.getResources().getString(R.string.walk_activity_type_in_chinese);
            case ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_ON_BICYCLE:
                return context.getResources().getString(R.string.bike_activity_type_in_chinese);
            case ActivityRecognitionStreamGenerator.STRING_DETECTED_ACTIVITY_IN_VEHICLE:
                return context.getResources().getString(R.string.car_activity_type_in_chinese);
        }

        return activity;
    }

    public LocationDataRecord getLocationDataRecord(){
        return locationDataRecord;
    }

}
