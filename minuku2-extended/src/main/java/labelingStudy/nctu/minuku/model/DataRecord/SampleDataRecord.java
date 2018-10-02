package labelingStudy.nctu.minuku.model.DataRecord;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Marvin on 2018/09/26.
 * If a developer desires to collect data which is aside from the existent data record, the developer might have to program a new streamGenerator
 * to make a streamGenerator work properly, the relative stream and dataRecord are necessary, while sometimes need to program manager classes as well if the desired data is complicated
 * DataRecord objects would be stored through DAO into data base.
 * This class shows how developer adds a new data record that is desired to detected which requires this provided sample format
 * While the object values are usually set through constructor, the get / set for each column is used for DAO, thus please provide get / set function for each column.
 */

@Entity
public class SampleDataRecord implements DataRecord {

    // optional, with TAG, reading Log could be more efficient
    public String TAG = "SampleDataRecord";

    // autoGenerate id
    @PrimaryKey(autoGenerate = true)
    private long _id;

    // the necessary column
    @ColumnInfo(name = "creationTime")
    public long creationTime;

    // customized column, the data type developer desired to collect. Could be any basic type
    @ColumnInfo(name = "integerValue")
    public int integerValue;

    // customized column, the number of data that developer want to collect could be more. Just add another columnInfo if needed.
    // Of course it is also fine to collect only one datum
    @ColumnInfo(name = "stringValue")
    public String stringValue;

    /**
     * Constructor, in most cases the data record would be created in the function updateStream of relative streamGenerator, while the value will be set through the constructor
     * @param integerValue the desired data
     * @param stringValue the desired data
     */
    public SampleDataRecord(int integerValue, String stringValue) {

        // set the create time
        this.creationTime = new Date().getTime();
        //optional
        Log.d(TAG, "creationTime : "+creationTime);

        // set the value
        this.integerValue = integerValue;
        this.stringValue = stringValue;

        //optional
        Log.d(TAG,"integerValue : " + this.integerValue + " stringValue : " + this.stringValue);

    }
    public String getTAG() {return TAG;}

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

    public int getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(int value) {
        this.integerValue = value;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
}
