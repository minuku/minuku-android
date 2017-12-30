package labelingStudy.nctu.minuku.model;

import java.util.ArrayList;

/**
 * Created by jiangjiaen on 2017/9/3.
 */

public class Session {

    private long mStartTime=0;
    private long mEndtime=0;
    private int mId;
    private int mTaskId;
    private boolean mPaused=false;
    private float mBatteryLife = -1;
    //we need to rememeber this number in order to cancel the ongoing notification when the current session is done.
    private int mOngoingNotificationId=-1;
    //protected AnnotationSet mAnnotationSet;
    //TODO: this should be defined based on what's being activated.
    ArrayList<String> mContextSourceNames;

    public Session (int taskId){
        mTaskId = taskId;
        //mAnnotationSet = new AnnotationSet();
    }

    public Session (long timestamp){
        mStartTime = timestamp;
        // mAnnotationSet = new AnnotationSet();
    }

    public Session (long timestamp, int taskId){
        mStartTime = timestamp;
        mTaskId = taskId;
        //mAnnotationSet = new AnnotationSet();
    }

    public Session (int id, long timestamp, int taskId){
        mId = id;
        mStartTime = timestamp;
        mTaskId = taskId;
        // mAnnotationSet = new AnnotationSet();
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


    public boolean isPaused() {
        return mPaused;
    }

    public void setPaused(boolean paused) {
        this.mPaused = paused;
    }

    public void setId(int id){
        mId = id;
    }

    public long getId(){
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


    public void setStartTime(long t){
        mStartTime = t;
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

    /**  public AnnotationSet getAnnotationsSet(){

     return mAnnotationSet;
     }**/

    public float getBatteryLife() {
        return mBatteryLife;
    }

    public void setBatteryLife(float batteryStatus) {
        this.mBatteryLife = batteryStatus;
    }

    /**   public void setAnnotationSet(AnnotationSet annotationSet){

     mAnnotationSet = annotationSet;
     }

     public void addAnnotaiton (Annotation annotation) {

     if (mAnnotationSet==null){
     mAnnotationSet = new AnnotationSet();
     }
     mAnnotationSet.addAnnotation(annotation);

     }
     **/

}
