package labelingStudy.nctu.minuku.model.DataRecord;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

public class RingerDataRecord implements DataRecord {

    private final String TAG = "RingerDataRecord";

    public long creationTime;
    private int taskDayCount;
    private long hour;
    private String RingerMode = "NA";
    private String AudioMode = "NA";
    private int StreamVolumeMusic = -9999;
    private int StreamVolumeNotification = -9999;
    private int StreamVolumeRing = -9999;
    private int StreamVolumeVoicecall = -9999;
    private int StreamVolumeSystem = -9999;
    private String sessionid;

    public RingerDataRecord(){}

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

    public RingerDataRecord(String RingerMode, String AudioMode, int StreamVolumeMusic
            , int StreamVolumeNotification, int StreamVolumeRing, int StreamVolumeVoicecall, int StreamVolumeSystem, String sessionid){
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
        this.sessionid = sessionid;
    }


    public String getSessionid() {
        return sessionid;
    }

    private long getmillisecondToHour(long timeStamp){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        long mhour = calendar.get(Calendar.HOUR_OF_DAY);

        return mhour;

    }

    public long getHour(){
        return hour;
    }

    public int getTaskDayCount(){
        return taskDayCount;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getRingerMode(){
        return RingerMode;
    }

    public String getAudioMode(){
        return AudioMode;
    }

    public int getStreamVolumeMusic(){
        return StreamVolumeMusic;
    }

    public int getStreamVolumeNotification(){
        return StreamVolumeNotification;
    }

    public int getStreamVolumeRing(){
        return StreamVolumeRing;
    }

    public int getStreamVolumeVoicecall(){
        return StreamVolumeVoicecall;
    }

    public int getStreamVolumeSystem(){
        return StreamVolumeSystem;
    }

}
