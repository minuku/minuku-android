package labelingStudy.nctu.minuku.model.DataRecord;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

/**
 * RingerDataRecord stores information about volume and ringer status of the use phone
 */
@Entity
public class RingerDataRecord implements DataRecord {

    private String TAG = "RingerDataRecord";

    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "RingerMode")
    public String RingerMode = "NA";

    @ColumnInfo(name = "AudioMode")
    public String AudioMode = "NA";

    @ColumnInfo(name = "StreamVolumeMusic")
    public int StreamVolumeMusic = -9999;

    @ColumnInfo(name = "StreamVolumeNotification")
    public int StreamVolumeNotification = -9999;

    @ColumnInfo(name = "StreamVolumeRing")
    public int StreamVolumeRing = -9999;

    @ColumnInfo(name = "StreamVolumeVoicecall")
    public int StreamVolumeVoicecall = -9999;

    @ColumnInfo(name = "StreamVolumeSystem")
    public int StreamVolumeSystem = -9999;

//    public RingerDataRecord(){}

    public RingerDataRecord(String RingerMode, String AudioMode, int StreamVolumeMusic
            , int StreamVolumeNotification, int StreamVolumeRing, int StreamVolumeVoicecall, int StreamVolumeSystem){
        this.creationTime = new Date().getTime();
//        this.taskDayCount = Constants.TaskDayCount;
//        this.hour = getmillisecondToHour(creationTime);
        this.RingerMode = RingerMode;
        this.AudioMode = AudioMode;
        this.StreamVolumeMusic = StreamVolumeMusic;
        this.StreamVolumeNotification = StreamVolumeNotification;
        this.StreamVolumeRing = StreamVolumeRing;
        this.StreamVolumeVoicecall = StreamVolumeVoicecall;
        this.StreamVolumeSystem = StreamVolumeSystem;

        Log.d(TAG,"mRingerMode : "+ RingerMode +" mAudioMode : "+ AudioMode+" mStreamVolumeMusic : "+ StreamVolumeMusic
                +" mStreamVolumeNotification : "+ StreamVolumeNotification+" mStreamVolumeRing : "+ StreamVolumeRing
                +" mStreamVolumeVoicecall : "+ StreamVolumeVoicecall +" mStreamVolumeSystem : "+ StreamVolumeSystem);

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

    public String getRingerMode() {
        return RingerMode;
    }

    public void setRingerMode(String ringerMode) {
        RingerMode = ringerMode;
    }

    public String getAudioMode() {
        return AudioMode;
    }

    public void setAudioMode(String audioMode) {
        AudioMode = audioMode;
    }

    public int getStreamVolumeMusic() {
        return StreamVolumeMusic;
    }

    public void setStreamVolumeMusic(int streamVolumeMusic) {
        StreamVolumeMusic = streamVolumeMusic;
    }

    public int getStreamVolumeNotification() {
        return StreamVolumeNotification;
    }

    public void setStreamVolumeNotification(int streamVolumeNotification) {
        StreamVolumeNotification = streamVolumeNotification;
    }

    public int getStreamVolumeRing() {
        return StreamVolumeRing;
    }

    public void setStreamVolumeRing(int streamVolumeRing) {
        StreamVolumeRing = streamVolumeRing;
    }

    public int getStreamVolumeVoicecall() {
        return StreamVolumeVoicecall;
    }

    public void setStreamVolumeVoicecall(int streamVolumeVoicecall) {
        StreamVolumeVoicecall = streamVolumeVoicecall;
    }

    public int getStreamVolumeSystem() {
        return StreamVolumeSystem;
    }

    public void setStreamVolumeSystem(int streamVolumeSystem) {
        StreamVolumeSystem = streamVolumeSystem;
    }
}
