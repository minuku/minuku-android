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

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import labelingStudy.nctu.minuku.R;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.model.MinukuStreamSnapshot;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.service.TransportationModeService;
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
                streamGenerator.updateStream();
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

    public void setTransportationModeDataRecord(TransportationModeDataRecord transportationModeDataRecord, Context context){

        Log.d(TAG, "[test triggering] incoming transportation: " + transportationModeDataRecord.getConfirmedActivityString());

        Boolean addSessionFlag = false;

        //the first time we see incoming transportation mode data
        if (this.transportationModeDataRecord==null){
            this.transportationModeDataRecord = transportationModeDataRecord;
            Log.d(TAG, "[test triggering] test trip original null updated to " + this.transportationModeDataRecord.getConfirmedActivityString());
        }
        else {

            Log.d(TAG, "[test triggering] NEW: " + transportationModeDataRecord.getConfirmedActivityString() + " vs OLD:" + this.transportationModeDataRecord.getConfirmedActivityString());

            /**
             * 1. check if the new activity label is different from the previous activity label. IF it is different, we should do something
             * **/

            String currentWork = context.getResources().getString(R.string.current_task);

            Log.d(TAG,"in setTransportationModeDataRecord, currentWork is "+currentWork);
//            String currentWork = "PART";

            //in PART, the Session is controlled by the user, no need to detect by the app.
            if(!currentWork.equals("PART")) {

                if (!this.transportationModeDataRecord.getConfirmedActivityString().equals(transportationModeDataRecord.getConfirmedActivityString())) {

                    Log.d(TAG, "[test triggering] test trip: the new acitivty is different from the previous!");

                    /** we first see if the this is the first session **/
                    int sessionCount = SessionManager.getNumOfSession();

                    //if this is the first time seeing a session and the new transportation is neither static nor NA, we should just insert a session
                    if (sessionCount == 0
                            && !transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeService.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION)
                            && !transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeService.TRANSPORTATION_MODE_NAME_NA)) {

                        Log.d(TAG, "[test triggering] addSessionFlag = true there's no session in the db");
                        addSessionFlag = true;
                    }
                    //there's exizstint sessions in the DB
                    else if (sessionCount > 0) {

                        //get the latest session (Which should be the ongoing one)
                        Session lastSession = SessionManager.getLastSession();
                        int sessionIdOfLastSession = lastSession.getId();
                        AnnotationSet annotationSet = lastSession.getAnnotationsSet();
                        long endTimeOfLastSession = lastSession.getEndTime();
                        long startTimeOfLastSession = lastSession.getStartTime();

                        Log.d(TAG, "[test triggering] session " + sessionIdOfLastSession + " with annotation string " + annotationSet.toString() + " end time " + endTimeOfLastSession + " startTime " + startTimeOfLastSession);

                        /**
                         * 2 if the new activity is moving, we will first determine whether this is continuing the previous activity or a new activity. If it is a continuous one we will not add a new session but let the previous activity in the ongoing
                         * **/

                        //TODO might no need to combine the trips, we focus on "Transportation" not "Trip"
                        /*if (!transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION)
                                && !transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA)) {

                            // check if the new activity should be combine: if the new transportation mode is the same as the mode of the previous session and the time is 5 minutes

                            boolean shouldCombineWithLastSession = false;
                            long now = ScheduleAndSampleManager.getCurrentTimeInMillis();
                            shouldCombineWithLastSession = SessionManager.examineSessionCombinationByActivityAndTime(lastSession, transportationModeDataRecord.getConfirmedActivityString(), now);

                            if (shouldCombineWithLastSession) {
                                Log.d(TAG, "test triggering we should combine ");

                                //modify the endTime of the previous session to empty (because we extend it!). we should also make the notlongenough field to be true.
                                SessionManager.continueLastSession(lastSession);
                            }

                            //if the new moving should not combine, we should end the previous session, and a new session
                            else {
                                //1. end the previous session if the previous transportation is moving
                                addSessionFlag = true;
                                Log.d(TAG, "test triggering: we should not combine the new transportation activity with the last session ");

                            }
                        }*/

                        //to end a session (the previous is moving)
                        //we first need to check whether the previous is a transportation
                        if (!this.transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeService.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION)
                                && !this.transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeService.TRANSPORTATION_MODE_NAME_NA)) {

                            //TODO convert the LongEnough into UserPressOrNot
                            //first get the last session id, which is the same as the count of the session in the database (it should
//                            Log.d(TAG, "test triggering: the previous activity is moving, we're going to end the last session id " + sessionIdOfLastSession);
//                            boolean isSessionLongEnough = SessionManager.isSessionLongEnough(SessionManager.SESSION_LONGENOUGH_THRESHOLD_DISTANCE, sessionIdOfLastSession);

                            //if we end the current session, we should update its time and set a long enough flag
                            long endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
                            lastSession.setEndTime(endTime);

                            //TODO CAR need to be in the start of the session, after a threshold of the time, send the notification to remind the user
                            if(currentWork.equals("CAR")){

                                //TODO detect the user has pressed the current trip(Session) or not.

                                //TODO if the user hasn't pressed the current trip(Session); after ending a trip(session), send a notification to the user

                            }
//                            lastSession.setUserPressOrNot(isSessionLongEnough);

                            //end the current session
                            SessionManager.endCurSession(lastSession);
                            Log.d(TAG, "test triggering: after remove, now the session manager session list has  " + SessionManager.getInstance().getOngoingSessionList());


                        }

                    }

                    Log.d(TAG, "[test triggering] addSessionFlag : "+addSessionFlag);
                    Log.d(TAG, "[test triggering] is static ? : "+!transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeService.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION));
                    Log.d(TAG, "[test triggering] is NA ? : "+!transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeService.TRANSPORTATION_MODE_NAME_NA));

                    //if we need to add a session
                    if (!transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeService.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION)
                            && !transportationModeDataRecord.getConfirmedActivityString().equals(TransportationModeService.TRANSPORTATION_MODE_NAME_NA)) {
                        Log.d(TAG, "[test triggering] we should add session " + ((int) sessionCount + 1));

                        //insert into the session table;
                        int sessionId = (int) sessionCount + 1;
                        Session session = new Session(sessionId);
                        session.setStartTime(ScheduleAndSampleManager.getCurrentTimeInMillis());
                        Annotation annotation = new Annotation();
                        annotation.setContent(transportationModeDataRecord.getConfirmedActivityString());
                        annotation.addTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATOIN_ACTIVITY);
                        session.addAnnotation(annotation);

                        Log.d(TAG, "[test triggering] insert the session is with annotation " + session.getAnnotationsSet().toJSONObject().toString());


                        SessionManager.startNewSession(session);

                        if(currentWork.equals("ESM")){

                            //TODO after starting a trip(session), send a notification to the user
                            sendNotification(context);

                            Log.d(TAG, "[test triggering] Esm Notification");
                        }
                    }
                }
            }
        }

        this.transportationModeDataRecord = transportationModeDataRecord;
    }

    private void sendNotification(Context context){

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String notificationText = "您有新的移動紀錄";
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

        Notification.Builder noti = new Notification.Builder(context);

        return noti.setContentTitle(Constants.APP_FULL_NAME)
                .setContentText(text)
                .setStyle(bigTextStyle)
                .setSmallIcon(MinukuNotificationManager.getNotificationIcon(noti))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build();
    }



    public TransportationModeDataRecord getTransportationModeDataRecord(){
        return transportationModeDataRecord;
    }

    public void setLocationDataRecord(LocationDataRecord locationDataRecord){
        this.locationDataRecord = locationDataRecord;
    }

    public LocationDataRecord getLocationDataRecord(){
        return locationDataRecord;
    }


}
