package labelingStudy.nctu.minuku.model.DataRecord;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

public class SensorDataRecord implements DataRecord {
    private final String TAG = "SensorDataRecord";

    public long creationTime;
    private String mAccele_str, mGyroscope_str, mGravity_str, mLinearAcceleration_str, mRotationVector_str,
            mProximity_str, mMagneticField_str, mLight_str, mPressure_str, mRelativeHumidity_str,  mAmbientTemperature_str;

    protected long _id;
    protected String _source;
    protected Session _session;
    protected ArrayList<Integer> mSavedBySessionIds;
    protected boolean isCopiedToPublicPool;
    protected JSONObject mData;
    protected String mTimestring;

    public SensorDataRecord(String mAccele_str, String mGyroscope_str, String mGravity_str, String mLinearAcceleration_str,
                            String mRotationVector_str, String mProximity_str, String mMagneticField_str, String mLight_str,
                            String mPressure_str, String mRelativeHumidity_str, String mAmbientTemperature_str){
        this.mAccele_str = mAccele_str;
        this.mGyroscope_str = mGyroscope_str;
        this.mGravity_str = mGravity_str;
        this.mLinearAcceleration_str = mLinearAcceleration_str;
        this.mRotationVector_str = mRotationVector_str;
        this.mProximity_str = mProximity_str;
        this.mMagneticField_str = mMagneticField_str;
        this.mLight_str = mLight_str;
        this.mPressure_str = mPressure_str;
        this.mRelativeHumidity_str = mRelativeHumidity_str;
        this.mAmbientTemperature_str = mAmbientTemperature_str;

        Log.d(TAG, "mAccele_str "+mAccele_str+" mGyroscope_str "+mGyroscope_str+" mGravity_str "+mGravity_str
                +" mLinearAcceleration_str "+mLinearAcceleration_str+" mRotationVector_str "+mRotationVector_str+" mProximity_str "+mProximity_str
                +" mMagneticField_str "+mMagneticField_str+" mLight_str "+mLight_str+" mPressure_str "+mPressure_str
                +" mRelativeHumidity_str "+mRelativeHumidity_str+" mAmbientTemperature_str "+mAmbientTemperature_str);

    }

    public ArrayList<Integer> getSavedSessionIds() {
        return mSavedBySessionIds;
    }

    public void addSavedBySessionId(int sessionId){

        mSavedBySessionIds.add(sessionId);

    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + _id +
                ", source='" + _source + '\'' +
                ", session=" + _session +
                ", savedBySessionIds=" + mSavedBySessionIds +
                ", data=" + mData +
                ", createTime='" + creationTime + '\'' +
                '}';
    }

    public boolean isCopiedToPublicPool() {
        return isCopiedToPublicPool;
    }

    public void setIsCopiedToPublicPool(boolean isCopiedToPublicPool) {
        this.isCopiedToPublicPool = isCopiedToPublicPool;
    }

    public void setID(long id){
        _id = id;
    }

    public long getID(){
        return _id;
    }


    public JSONObject getData() {
        return mData;
    }

    public void setData(JSONObject data) {
        this.mData = data;
    }


    public String getSource(){
        return _source;
    }

    public void setSource(String source){
        _source = source;
    }

    public Session getSession(){
        return _session;
    }

    public void setSession(Session s){
        _session = s;
    }
    @Override
    public long getCreationTime() {

        return creationTime;
    }

    public String getmAccele_str() {return mAccele_str;}

    public String getmGyroscope_str() {return mGyroscope_str;}

    public String getmGravity_str() {return mGravity_str;}

    public String getmLinearAcceleration_str() {return mLinearAcceleration_str;}

    public String getmRotationVector_str() {return mRotationVector_str;}

    public String getmProximity_str() {return mProximity_str;}

    public String getmMagneticField_str() {return mMagneticField_str;}

    public String getmLight_str() {return mLight_str;}

    public String getmPressure_str() {return mPressure_str;}

    public String getmRelativeHumidity_str() {return mRelativeHumidity_str;}

    public String getmAmbientTemperature_str() {return mAmbientTemperature_str;}
}
