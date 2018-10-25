package labelingStudy.nctu.minuku.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.manager.SessionManager;

/**
 * Created by Lawrence on 2018/3/13.
 */

public class Annotation {

    private int mId=-1;

    //the offset. i.e. 300 means 300 seconds from the startTime of the session; -1 means the annotation applies to the whole session
    private long mStartTime=-1;
    private long mEndTime=-1;
    //by default an annotation applies to an entire session, unless being specified otherwise.
    private boolean mEntireSession = true;
    //the content of the annotation
    private String mContent="";
    //metadata of annotations
    private ArrayList<String> mTags;

    public Annotation(){

    }

    public void setId(int id){
        mId = id;
    }

    public long getId(){
        return mId;
    }

    public boolean isEntireSession() {
        return mEntireSession;
    }

    public void setEntireSession(boolean entireSession) {
        this.mEntireSession = entireSession;
    }

    public void setStartTime(long t){
        mStartTime = t;
    }

    public long getStartTime(){
        return mStartTime;
    }

    public void setEndTime(long t){
        mEndTime = t;
    }

    public long getEndTime(){
        return mEndTime;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }



    public JSONObject toJSONObject(){

        JSONObject obj = new JSONObject();

        try {
            //whether the annotation is for the entire session
            obj.put(SessionManager.ANNOTATION_PROPERTIES_IS_ENTIRE_SESSION, mEntireSession);

            //if the annotation is not for the entire session, there should be either starTime or endTime specified
            if (!mEntireSession){
                obj.put(SessionManager.ANNOTATION_PROPERTIES_START_TIME, mStartTime);
                obj.put(SessionManager.ANNOTATION_PROPERTIES_END_TIME, mEndTime);
            }
            if (mId!=-1)
                obj.put(SessionManager.ANNOTATION_PROPERTIES_ID, mId);

            //if the annotation has tags
            if (mTags.size()>0){

                JSONArray tags = new JSONArray();
                for (int i=0; i < mTags.size(); i++){
                    if (mTags.get(i)!=null && mTags.get(i).length()>0)
                        tags.put(mTags.get(i));
                }

                obj.put(SessionManager.ANNOTATION_PROPERTIES_TAG, (Object) tags);

            }

            //add content
            obj.put(SessionManager.ANNOTATION_PROPERTIES_CONTENT, mContent);


        }catch(JSONException e){

        }

        return obj;

    }

    public void addTag(String tag){

        if (mTags==null){
            mTags = new ArrayList<String>();
        }

        mTags.add(tag);
    }

    public ArrayList<String> getTags() {
        return mTags;
    }
}
