package labelingStudy.nctu.minuku.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

@Entity
public class SensorDataRecord implements DataRecord {

    public String TAG = "sensorDataRecord";

    public SensorDataRecord(String mAccele_str, String mGyroscope_str, String mGravity_str, String mLinearAcceleration_str, String mRotationVector_str, String mProximity_str, String mMagneticField_str, String mLight_str, String mPressure_str, String mRelativeHumidity_str, String mAmbientTemperature_str) {
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

        Log.d(TAG, "mAccele_str " + mAccele_str + " mGyroscope_str " + mGyroscope_str + " mGravity_str " + mGravity_str
                + " mLinearAcceleration_str " + mLinearAcceleration_str + " mRotationVector_str " + mRotationVector_str + " mProximity_str " + mProximity_str
                + " mMagneticField_str " + mMagneticField_str + " mLight_str " + mLight_str + " mPressure_str " + mPressure_str
                + " mRelativeHumidity_str " + mRelativeHumidity_str + " mAmbientTemperature_str " + mAmbientTemperature_str);
    }

    @PrimaryKey(autoGenerate = true)
    private int _id;

//    public String getTAG() {
//        return TAG;
//    }


    @ColumnInfo(name = "creationTime")
    public long creationTime;


    @ColumnInfo(name = "acceleration")
    public String mAccele_str;

    @ColumnInfo(name = "gryroscope")
    public String mGyroscope_str;

    @ColumnInfo(name = "gravity")
    public String mGravity_str;

    @ColumnInfo(name = "linearAcceleration")
    public String mLinearAcceleration_str;

    @ColumnInfo(name = "rotationVector")
    public String mRotationVector_str;

    @ColumnInfo(name = "proximity")
    public String mProximity_str;

    @ColumnInfo(name = "magneticField")
    public String mMagneticField_str;

    @ColumnInfo(name = "light")
    public String mLight_str;

    @ColumnInfo(name = "pressure")
    public String mPressure_str;

    @ColumnInfo(name = "relativeHumidity")
    public String mRelativeHumidity_str;

    @ColumnInfo(name = "ambientTemperature")
    public String mAmbientTemperature_str;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getmAccele_str() {
        return mAccele_str;
    }

    public void setmAccele_str(String mAccele_str) {
        this.mAccele_str = mAccele_str;
    }

    public String getmGyroscope_str() {
        return mGyroscope_str;
    }

    public void setmGyroscope_str(String mGyroscope_str) {
        this.mGyroscope_str = mGyroscope_str;
    }

    public String getmGravity_str() {
        return mGravity_str;
    }

    public void setmGravity_str(String mGravity_str) {
        this.mGravity_str = mGravity_str;
    }

    public String getmLinearAcceleration_str() {
        return mLinearAcceleration_str;
    }

    public void setmLinearAcceleration_str(String mLinearAcceleration_str) {
        this.mLinearAcceleration_str = mLinearAcceleration_str;
    }

    public String getmRotationVector_str() {
        return mRotationVector_str;
    }

    public void setmRotationVector_str(String mRotationVector_str) {
        this.mRotationVector_str = mRotationVector_str;
    }

    public String getmProximity_str() {
        return mProximity_str;
    }

    public void setmProximity_str(String mProximity_str) {
        this.mProximity_str = mProximity_str;
    }

    public String getmMagneticField_str() {
        return mMagneticField_str;
    }

    public void setmMagneticField_str(String mMagneticField_str) {
        this.mMagneticField_str = mMagneticField_str;
    }

    public String getmLight_str() {
        return mLight_str;
    }

    public void setmLight_str(String mLight_str) {
        this.mLight_str = mLight_str;
    }

    public String getmPressure_str() {
        return mPressure_str;
    }

    public void setmPressure_str(String mPressure_str) {
        this.mPressure_str = mPressure_str;
    }

    public String getmRelativeHumidity_str() {
        return mRelativeHumidity_str;
    }

    public void setmRelativeHumidity_str(String mRelativeHumidity_str) {
        this.mRelativeHumidity_str = mRelativeHumidity_str;
    }

    public String getmAmbientTemperature_str() {
        return mAmbientTemperature_str;
    }

    public void setmAmbientTemperature_str(String mAmbientTemperature_str) {
        this.mAmbientTemperature_str = mAmbientTemperature_str;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }
}