package labelingStudy.nctu.minuku.model.DataRecord;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/5/22.
 */

@Entity
public class TransportationModeDataRecord implements DataRecord {


    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "taskDayCount")
    private int taskDayCount;

    @ColumnInfo(name = "hour")
    private long hour;

    @ColumnInfo(name = "suspectedTime")
    private long suspectedTime;

    @ColumnInfo(name = "ConfirmedActivityString")
    public String ConfirmedActivityString; //

    @ColumnInfo(name = "suspectedStartActivityString")
    public String suspectedStartActivityString;

    @ColumnInfo(name = "suspectedStopActivityString")
    public String suspectedStopActivityString;

    public TransportationModeDataRecord(String confirmedActvitiyString, long suspectTime, String suspectedStartActivity, String suspectedEndActivity) {
        this.suspectedTime = suspectTime;
        this.ConfirmedActivityString = confirmedActvitiyString;
        this.suspectedStartActivityString = suspectedStartActivity;
        this.suspectedStopActivityString = suspectedEndActivity;
    }

    public int getTaskDayCount() {
        return taskDayCount;
    }

    public void setTaskDayCount(int taskDayCount) {
        this.taskDayCount = taskDayCount;
    }

    public long getHour() {
        return hour;
    }

    public void setHour(long hour) {
        this.hour = hour;
    }

    public long getSuspectedTime() {
        return suspectedTime;
    }

    public void setSuspectedTime(long suspectedTime) {
        this.suspectedTime = suspectedTime;
    }

    public String getSuspectedStartActivityString() {
        return suspectedStartActivityString;
    }

    public void setSuspectedStartActivityString(String suspectedStartActivityString) {
        this.suspectedStartActivityString = suspectedStartActivityString;
    }

    public String getSuspectedStopActivityString() {
        return suspectedStopActivityString;
    }

    public void setSuspectedStopActivityString(String suspectedStopActivityString) {
        this.suspectedStopActivityString = suspectedStopActivityString;
    }

    public TransportationModeDataRecord(String ConfirmedActivityString){
        this.creationTime = new Date().getTime();
        this.ConfirmedActivityString = ConfirmedActivityString;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getConfirmedActivityString(){
        return ConfirmedActivityString;
    }

    public void setConfirmedActivityType(String ConfirmedActivityString){
        this.ConfirmedActivityString=ConfirmedActivityString;
    }
}
