package labelingStudy.nctu.minuku.model.DataRecord;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

/**
 * AppUsageDataRecord stores information about conditions of other applications user has used, while also collect the screen status to see the interaction with the screen.
 */
@Entity
public class AppUsageDataRecord implements DataRecord {

    public String TAG = "AppUsageDataRecord";

    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name="creationTime")
    public long creationTime;

    @ColumnInfo(name = "Screen_Status")
    public String Screen_Status;

    @ColumnInfo(name = "Latest_Foreground_Activity")
    public String Latest_Foreground_Activity;

    @ColumnInfo(name = "Latest_Used_App")
    public String Latest_Used_App;



    public AppUsageDataRecord(String Screen_Status, String Latest_Used_App, String Latest_Foreground_Activity) {
        this.creationTime = new Date().getTime();
        this.Screen_Status = Screen_Status;
        this.Latest_Used_App = Latest_Used_App;
        this.Latest_Foreground_Activity = Latest_Foreground_Activity;
    }
    @Ignore
    public AppUsageDataRecord() {

    }

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
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

    public String getScreen_Status() {
        return Screen_Status;
    }

    public void setScreen_Status(String screen_Status) {
        Screen_Status = screen_Status;
    }

    public String getLatest_Foreground_Activity() {
        return Latest_Foreground_Activity;
    }

    public void setLatest_Foreground_Activity(String latest_Foreground_Activity) {
        Latest_Foreground_Activity = latest_Foreground_Activity;
    }

    public String getLatest_Used_App() {
        return Latest_Used_App;
    }

    public void setLatest_Used_App(String latest_Used_App) {
        Latest_Used_App = latest_Used_App;
    }

    /**get the current time in milliseconds**/
    public static long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }
}

