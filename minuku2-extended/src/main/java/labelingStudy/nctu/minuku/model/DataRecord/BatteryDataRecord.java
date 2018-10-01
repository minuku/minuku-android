package labelingStudy.nctu.minuku.model.DataRecord;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

//import static labelingStudy.nctu.minuku.streamgenerator.BatteryStreamGenerator.TAG;

/**
 * Created by Lawrence on 2017/7/22.
 */

/**
 * BatteryDataRecord stores information about battery conditions and status
 */
@Entity
public class BatteryDataRecord implements DataRecord {

    public String TAG = "BatteryDataRecord";

    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "BatteryLevel")
    public int BatteryLevel;

    @ColumnInfo(name = "BatteryPercentage")
    public float BatteryPercentage;

    @ColumnInfo(name = "BatteryChargingState")
    public String BatteryChargingState = "NA";

    @ColumnInfo(name = "isCharging")
    public boolean isCharging;


    public BatteryDataRecord(int BatteryLevel, float BatteryPercentage, String BatteryChargingState, boolean isCharging){
        this.creationTime = new Date().getTime();
        Log.d(TAG, "creationTime : "+creationTime);

        this.BatteryLevel = BatteryLevel;
        this.BatteryPercentage = BatteryPercentage;
        this.BatteryChargingState = BatteryChargingState;
        this.isCharging = isCharging;

        Log.d(TAG,"BatteryLevel : "+ this.BatteryLevel+" BatteryPercentage : "+ this.BatteryPercentage
                +" BatteryChargingState : "+ this.BatteryChargingState +" isCharging : "+ this.isCharging);
    }

    public String getTAG() {
        return TAG;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public int getBatteryLevel() {
        return BatteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        BatteryLevel = batteryLevel;
    }

    public float getBatteryPercentage() {
        return BatteryPercentage;
    }

    public void setBatteryPercentage(float batteryPercentage) {
        BatteryPercentage = batteryPercentage;
    }

    public String getBatteryChargingState() {
        return BatteryChargingState;
    }

    public void setBatteryChargingState(String batteryChargingState) {
        BatteryChargingState = batteryChargingState;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
    }
}