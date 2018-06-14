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

    @ColumnInfo(name = "ConfirmedActivityType")
    public String ConfirmedActivityType; //


    public TransportationModeDataRecord(String ConfirmedActivityType){
        this.creationTime = new Date().getTime();
        this.ConfirmedActivityType = ConfirmedActivityType;
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
        return ConfirmedActivityType;
    }

    public void setConfirmedActivityType(String ConfirmedActivityType){
        this.ConfirmedActivityType=ConfirmedActivityType;
    }
}
