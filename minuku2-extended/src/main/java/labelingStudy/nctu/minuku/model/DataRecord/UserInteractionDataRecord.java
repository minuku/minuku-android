package labelingStudy.nctu.minuku.model.DataRecord;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */

/**
 * UserInteractionDataRecord stores information about
 */
@Entity
public class UserInteractionDataRecord implements DataRecord {

    @PrimaryKey(autoGenerate = true)
    private int _id;

    public long creationTime;

    private String present;
    private String unlock;
    private String background;
    private String foreground;

    public UserInteractionDataRecord(String present, String unlock, String background, String foreground){
        this.creationTime = new Date().getTime();

        this.present = present;
        this.unlock = unlock;
        this.background = background;
        this.foreground = foreground;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getUnlock() {
        return unlock;
    }

    public void setUnlock(String unlock) {
        this.unlock = unlock;
    }

    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getForeground() {
        return foreground;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }



}
