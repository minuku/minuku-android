package labelingStudy.nctu.minuku.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.manager.SessionManager;

/**
 * Created by Lawrence on 2018/3/13.
 */

public class AnnotationSet {

    private String TAG = "AnnotationSet";

    //the id of the annotationSet for raw data is 0
    private int mId=0;
    private String mName="";
    private ArrayList<Annotation> mAnnotations;

    public AnnotationSet(){

    }

    public int getId(){
        return mId;
    }

    public void setId(int id){
        mId = id;
    }

    public String getName () {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public ArrayList<Annotation> getAnnotations() {
        return mAnnotations;
    }

    public void setAnnotations(ArrayList<Annotation> annotations) {

        mAnnotations = annotations;
    }

    public void addAnnotation(Annotation annotation){

        if (mAnnotations==null){
            mAnnotations = new ArrayList<Annotation>();
        }

        mAnnotations.add(annotation);

    }

    public ArrayList<Annotation> getAnnotationByTag(String tag){

//        Log.d("AnntationSet","[test show trip] searching " + tag + " inside annotationset");

        ArrayList<Annotation> annotations = new ArrayList<Annotation>();

        if (this.mAnnotations!=null){

            for (Annotation annotation : mAnnotations){

                Log.d(TAG,"[test show trip] now it's annotation " + annotation.toJSONObject().toString() );

                if (annotation.getTags().contains(tag)){
                    Log.d(TAG, "[test show trip] found annotation containing tag " + tag + " : " + annotation.toJSONObject().toString());
                    annotations.add(annotation);
                }

            }

        }

        return annotations;

    }


    public ArrayList<Annotation> getAnnotationByContent(String content){

//		Log.d("AnntationSet","[test combine] searching " + content + " inside annotationset" + this.mAnnotations.toString() );

        ArrayList<Annotation> annotations = new ArrayList<Annotation>();

        if (this.mAnnotations!=null){

            for (Annotation annotation : mAnnotations){

//				Log.d("AnntationSet","[test combine] now it's annotation " + annotation.toJSONObject().toString() );
//				Log.d("AnntationSet","[test combine] and it's content is " +annotation.getContent() );

                if (annotation.getContent().equals(content)){
                    Log.d(TAG, "[test combine] found annotation with the content" + annotation.toJSONObject().toString());
                    annotations.add(annotation);
                }

            }

        }

        return annotations;

    }

    public JSONObject toJSONObject(){

        JSONObject obj = new JSONObject();

        try{

            if (mAnnotations!=null && mAnnotations.size()>0){

                if (!mName.equals(""))
                    obj.put(SessionManager.ANNOTATION_PROPERTIES_NAME, mName);
                obj.put(SessionManager.ANNOTATION_PROPERTIES_ID, mId);
                obj.put(SessionManager.ANNOTATION_PROPERTIES_ANNOTATION, (Object) getAnnotationsInJSONArray());
            }

        }catch(JSONException e){

        }
        return obj;
    }

    public JSONArray getAnnotationsInJSONArray() {

        JSONArray array = new JSONArray() ;

        //get all the annotaitons and put their JSONObject format into the array
        for (int i=0; i<mAnnotations.size(); i++){
            array.put(mAnnotations.get(i).toJSONObject());
        }

        return array;
    }


    @Override
    public String toString(){
        return this.toJSONObject().toString();
    }

}
