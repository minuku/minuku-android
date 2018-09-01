package labelingStudy.nctu.minuku.model.DataRecord;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/9/6.
 */
@Entity
public class AccessibilityDataRecord implements DataRecord {

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "pack")
    public String pack;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "extra")
    public String extra;

    @PrimaryKey(autoGenerate = true)
    private int _id;

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }
//    public AccessibilityDataRecord(){
//        this.creationTime = new Date().getTime();
//    }

    public AccessibilityDataRecord(String pack, String text, String type, String extra){
        this.creationTime = new Date().getTime();

        this.pack = pack;
        this.text = text;
        this.type = type;
        this.extra = extra;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }



    public void setPack(String pack) {
        this.pack = pack;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getPack(){
        return pack;
    }

    public String getText(){
        return text;
    }

    public String getType(){
        return type;
    }

    public String getExtra(){
        return extra;
    }
}
