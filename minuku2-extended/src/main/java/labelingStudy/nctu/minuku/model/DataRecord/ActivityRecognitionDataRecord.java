package labelingStudy.nctu.minuku.model.DataRecord;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import labelingStudy.nctu.minukucore.model.DataRecord;


/**
 * Created by tingwei on 2018/3/15.
 */
@Entity
public class ActivityRecognitionDataRecord implements DataRecord {

    public static String TAG = "ActivityRecognitionDataRecord";

    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "MostProbableActivity")
    // TODO: to string
    public String MostProbableActivity;

    @ColumnInfo(name = "ProbableActivities")
    public String ProbableActivities;

    @ColumnInfo(name = "Detectedtime")
    public long Detectedtime;

    public ActivityRecognitionDataRecord() {

    }

    public ActivityRecognitionDataRecord(DetectedActivity mostProbableActivity, List<DetectedActivity> ProbableActivities,
                                         long detectedtime) {
        this.creationTime = new Date().getTime();
        Log.d(TAG, "in constructor");
        setMostProbableActivity(mostProbableActivity);
        setProbableActivities(ProbableActivities);
        setDetectedtime(detectedtime);
    }

    public ActivityRecognitionDataRecord(DetectedActivity mostProbableActivity, long detectedtime) {
        this.Detectedtime = detectedtime;
        this.MostProbableActivity = mostProbableActivity.toString();
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

    public DetectedActivity getMostProbableActivity() {
        return StringToDetectedActivity(MostProbableActivity);
    }

    public void setMostProbableActivity(DetectedActivity mostProbableActivity) {
        this.MostProbableActivity = mostProbableActivity.toString();
    }

    public List<DetectedActivity> getProbableActivities() {
        List<DetectedActivity> detectedActivities = new ArrayList<>();
        String[] split = ProbableActivities.split(";");
        for (String s : split) {
            detectedActivities.add(StringToDetectedActivity(s));
        }
        return detectedActivities;
    }

    public void setProbableActivities(List<DetectedActivity> mProbableActivities) {
        String p = "";

        for (DetectedActivity a: mProbableActivities) {
            String tmp = a.toString();
            tmp += ";";
            p += tmp;
        }
//        Log.d(TAG, p);
        this.ProbableActivities = p;
    }

    public long getDetectedtime() {
        return Detectedtime;
    }

    public void setDetectedtime(long detectedtime) {
        Detectedtime = detectedtime;
    }

    /// Convert String: MostProbableActivity to DetectActivity
    public DetectedActivity StringToDetectedActivity(String da) {
        // DetectedActivity [type=STILL, confidence=...]
        String[] split_line = da.split(",");
        int var1Start = split_line[0].indexOf("=");
        int var1 = -1;
        String activityString = da.substring(var1Start+1, split_line[0].length());
        switch(activityString) {
            case "IN_VEHICLE":
                var1 = 0;
                break;
            case "ON_BICYCLE":
                var1 = 1;
                break;
            case "ON_FOOT":
                var1 = 2;
                break;
            case "STILL":
                var1 = 3;
                break;
            case "UNKNOWN":
                var1 = 4;
                break;
            case "TILTING":
                var1 = 5;
                break;
            case "WALKING":
                var1 = 7;
                break;
            case "RUNNING":
                var1 = 8;
                break;
        }
        int var2Start = da.lastIndexOf("=");
        int var2 = Integer.valueOf(da.substring(var2Start+1, da.length()-1));

        DetectedActivity detectedActivity = new DetectedActivity(var1, var2);
        return detectedActivity;
    }
}