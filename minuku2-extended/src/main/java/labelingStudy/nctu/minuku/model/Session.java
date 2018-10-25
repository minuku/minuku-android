package labelingStudy.nctu.minuku.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;

/**
 * Created by jiangjiaen on 2017/9/3.
 */

public class Session {

    private long mCreatedTime=0;
    private long mStartTime=0;
    private long mEndtime=0;
    private int mId;
    private int mTaskId;
    private boolean mPaused=false;
    private float mBatteryLife = -1;
    //we need to rememeber this number in order to cancel the ongoing notification when the current session is done.
    private int mOngoingNotificationId=-1;
    protected AnnotationSet mAnnotationSet;
    private boolean mUserPressOrNot;
    private boolean mIsModified;
    private int hidedOrNot;
    private int mIsSent;
    private String type;

    ArrayList<String> mContextSourceNames;

    public Session (int sessionId){
        mId = sessionId;
        mAnnotationSet = new AnnotationSet();
    }

    public Session (long timestamp){
        mStartTime = timestamp;
        mCreatedTime = timestamp;
        mAnnotationSet = new AnnotationSet();
    }

    public Session (long timestamp, int sessionId){
        mStartTime = timestamp;
        mCreatedTime = timestamp;
        mId = sessionId;
        mAnnotationSet = new AnnotationSet();
    }

    public Session (int id, long timestamp){
        mId = id;
        mStartTime = timestamp;
        mCreatedTime = timestamp;
        mAnnotationSet = new AnnotationSet();
    }

    public ArrayList<String> getContextSourceNames() {
        return mContextSourceNames;
    }

    public void addContextSourceType(String sourceType) {
        if ( this.mContextSourceNames==null){
            mContextSourceNames = new ArrayList<String>();
        }
        this.mContextSourceNames.add(sourceType);
    }

    public void setContextSourceTypes(ArrayList<String> sourceTypes) {
        this.mContextSourceNames = sourceTypes;
    }

    public void setContextSourceTypes(String[] contextsources) {
        for (int i=0; i<contextsources.length; i++){
            addContextSourceType(contextsources[i]);
        }
    }

    public boolean isUserPress() {
        return mUserPressOrNot;
    }

    public boolean isModified() {
        return mIsModified;
    }

    public void setUserPressOrNot(boolean userPressOrNot){
        mUserPressOrNot = userPressOrNot;
    }

    public void setModified(boolean isModified){
        mIsModified = isModified;
    }

    public int isHide(){
        return hidedOrNot;
    }

    public void setHidedOrNot(int hidedOrNot){
        this.hidedOrNot = hidedOrNot;
    }

    public void setIsSent(int isSent){
        mIsSent = isSent;
    }

    public int getIsSent(){
        return mIsSent;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void setPaused(boolean paused) {
        this.mPaused = paused;
    }

    public void setId(int id){
        mId = id;
    }

    public int getId(){
        return mId;
    }

    public int getOngoingNotificationId() {
        return mOngoingNotificationId;
    }

    public void setOngoingNotificationId(int ongoingNotificationId) {
        this.mOngoingNotificationId = ongoingNotificationId;
    }

    public void setTask(int taskId) {
        mTaskId = taskId;
    }

    public void setTaskId(int taskId) {
        this.mTaskId = taskId;
    }


    public int getTaskId(){
        return mTaskId;
    }


    public void setStartTime(long startTime){
        mStartTime = startTime;
    }

    public long getStartTime(){
        return mStartTime;
    }

    public long getEndTime() {
        return mEndtime;
    }

    public void setEndTime(long endtime) {
        this.mEndtime = endtime;
    }

    public AnnotationSet getAnnotationsSet(){

        return mAnnotationSet;
    }

    public float getBatteryLife() {
        return mBatteryLife;
    }

    public void setBatteryLife(float batteryStatus) {
        this.mBatteryLife = batteryStatus;
    }

    public void setAnnotationSet(AnnotationSet annotationSet){
        mAnnotationSet = annotationSet;
    }

    public void addAnnotation (Annotation annotation) {

        if (mAnnotationSet==null){
            mAnnotationSet = new AnnotationSet();
        }
        mAnnotationSet.addAnnotation(annotation);

    }

    public long getCreatedTime() {
        return mCreatedTime;
    }

    public void setCreatedTime(long mCreatedTime) {
        this.mCreatedTime = mCreatedTime;
    }
    public String getTransporationType() {
        ArrayList<Annotation> annotations_label = mAnnotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_Label);
        Annotation annotation_label = annotations_label.get(annotations_label.size() - 1);
        String label = annotation_label.getContent();
        String label_Transportation;
        JSONObject labelJson = new JSONObject();
        try {
            labelJson = new JSONObject(label);
            label_Transportation = labelJson.getString(Constants.ANNOTATION_Label_TRANSPORTATOIN);
            switch (label_Transportation) {
                case "走路":
                    return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_FOOT;
                case "自行車":
                    return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_BICYCLE;
                case "汽機車":
                    return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_IN_VEHICLE;
                case "定點":
                    return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION;
                case "此移動不存在":
                    return "此移動不存在";
                case "與上一個相同":
                    return "與上一個相同";
                default:
                    return "Unknown";
            }

        } catch (JSONException e) {

        } catch (IndexOutOfBoundsException e) {

        }
        if (!labelJson.has(Constants.ANNOTATION_Label_TRANSPORTATOIN)) {

            ArrayList<Annotation> annotations = mAnnotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY);

            String transportation;

            if (annotations.size() == 0)
                transportation = TransportationModeStreamGenerator.TRANSPORTATION_MODE_HASNT_DETECTED_FLAG;
            else {
                Annotation annotation = annotations.get(annotations.size() - 1);
                transportation = annotation.getContent();
                return transportation;
            }

        }
        return "";
    }
}
