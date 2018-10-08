package labelingStudy.nctu.minuku.model;

import java.util.ArrayList;

/**
 * Created by jiangjiaen on 2017/9/3.
 */

public class Session {

    private long mCreatedTime = 0;
    private long mStartTime = 0;
    private long mEndTime = 0;
    private int mId;
    private int mTaskId;
    private boolean mPaused = false;
    private float mBatteryLife = -1;
    //we need to rememeber this number in order to cancel the ongoing notification when the current session is done.
    private int mOngoingNotificationId = -1;
    protected AnnotationSet mAnnotationSet;
    private boolean mIsUserPress;
    private boolean mIsModified;
    private boolean mIsHided;
    private int mIsSent;
    private String mType;

    ArrayList<String> mContextSourceNames;

    /**
     * @param sessionId Set session Id
     */
    public Session (int sessionId) {
        mId = sessionId;
        mAnnotationSet = new AnnotationSet();
    }

    /**
     * @param timestamp Set session StartTime and CreatedTime
     */
    public Session (long timestamp) {
        mStartTime = timestamp;
        mCreatedTime = timestamp;
        mAnnotationSet = new AnnotationSet();
    }

    /**
     *
     * @param timestamp Set session StartTime and CreatedTime
     * @param sessionId Set session Id
     */
    public Session (long timestamp, int sessionId) {
        mStartTime = timestamp;
        mCreatedTime = timestamp;
        mId = sessionId;
        mAnnotationSet = new AnnotationSet();
    }

    public ArrayList<String> getContextSourceNames() {
        return mContextSourceNames;
    }

    public void addContextSourceType(String sourceType) {
        if ( this.mContextSourceNames == null) {
            mContextSourceNames = new ArrayList<String>();
        }
        this.mContextSourceNames.add(sourceType);
    }

    public void setContextSourceTypes(ArrayList<String> sourceTypes) {
        this.mContextSourceNames = sourceTypes;
    }

    public void setContextSourceTypes(String[] contextSources) {
        for (int i = 0; i<contextSources.length; i++) {
            addContextSourceType(contextSources[i]);
        }
    }

    public boolean isUserPress() {
        return mIsUserPress;
    }

    public boolean isModified() {
        return mIsModified;
    }

    public void setIsUserPress(boolean isUserPress) {
        mIsUserPress = isUserPress;
    }

    public void setModified(boolean isModified) {
        mIsModified = isModified;
    }

    public boolean isHide() {
        return mIsHided;
    }

    public void setIsHided(boolean isHided) {
        mIsHided = isHided;
    }

    public void setIsSent(int isSent) {
        mIsSent = isSent;
    }

    public int getIsSent() {
        return mIsSent;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void setPaused(boolean paused) {
        mPaused = paused;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public int getOngoingNotificationId() {
        return mOngoingNotificationId;
    }

    public void setOngoingNotificationId(int ongoingNotificationId) {
        mOngoingNotificationId = ongoingNotificationId;
    }

    public void setTask(int taskId) {
        mTaskId = taskId;
    }

    public void setTaskId(int taskId) {
        mTaskId = taskId;
    }


    public int getTaskId() {
        return mTaskId;
    }


    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long endTime) {
        mEndTime = endTime;
    }

    public AnnotationSet getAnnotationsSet() {

        return mAnnotationSet;
    }

    public float getBatteryLife() {
        return mBatteryLife;
    }

    public void setBatteryLife(float batteryStatus) {
        mBatteryLife = batteryStatus;
    }

    public void setAnnotationSet(AnnotationSet annotationSet) {
        mAnnotationSet = annotationSet;
    }

    /**
     * Add annotation to annotation set
     * @param annotation
     */
    public void addAnnotation (Annotation annotation) {

        if (mAnnotationSet==null) {
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
}
