package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;
import labelingStudy.nctu.minuku.stream.RingerStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class RingerStreamGenerator extends AndroidStreamGenerator<RingerDataRecord> {

    private String TAG = "RingerStreamGenerator";
    private RingerStream mStream;

    //audio and ringer
    public static final String RINGER_MODE_NORMAL = "Normal";
    public static final String RINGER_MODE_VIBRATE = "Silent";
    public static final String RINGER_MODE_SILENT = "Vibrate";

    public static final String MODE_CURRENT = "Current";
    public static final String MODE_INVALID = "Invalid";
    public static final String MODE_IN_CALL = "InCall";
    public static final String MODE_IN_COMMUNICATION = "InCommunication";
    public static final String MODE_NORMAL = "Normal";
    public static final String MODE_RINGTONE = "Ringtone";

    //after api 23
    public static AudioDeviceInfo[] sAllAudioDevices;

    private String mRingerMode = "NA";
    private String mAudioMode = "NA";
    private int mStreamVolumeMusic = -9999;
    private int mStreamVolumeNotification = -9999;
    private int mStreamVolumeRing = -9999;
    private int mStreamVolumeVoiceCall = -9999;
    private int mStreamVolumeSystem = -9999;
//    private static int mStreamVolumeDTMF = -9999;

    private static AudioManager sAudioManager;

    public static int sMainThreadUpdateFrequencyInSeconds = 10;
    public static long sMainThreadUpdateFrequencyInMilliseconds = sMainThreadUpdateFrequencyInSeconds * Constants.MILLISECONDS_PER_SECOND;

    private Context mContext;

    private static Handler sMainThread;

    public RingerStreamGenerator (Context applicationContext) {
        super(applicationContext);

        mContext = applicationContext;

        mStream = new RingerStream(Constants.DEFAULT_QUEUE_SIZE);

        sAudioManager = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);

        register();
    }
    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager");

        try {
            MinukuStreamManager.getInstance().register(mStream, RingerDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "RingerDataRecord/RingerStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " RingerDataRecord/RingerStream is already registered.");
        }
    }

    @Override
    public Stream<RingerDataRecord> generateNewStream() {
        return mStream;
    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean updateStream() {

        Log.d(TAG, "updateStream called");
        //TODO get service data
        RingerDataRecord ringerDataRecord = new RingerDataRecord(mRingerMode,mAudioMode,mStreamVolumeMusic
                ,mStreamVolumeNotification,mStreamVolumeRing, mStreamVolumeVoiceCall,mStreamVolumeSystem);
        mStream.add(ringerDataRecord);
        Log.d(TAG, "Ringer to be sent to event bus" + ringerDataRecord);
        // also post an event.
        EventBus.getDefault().post(ringerDataRecord);
        try {
            appDatabase db;
            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();
            db.ringerDataRecordDao().insertAll(ringerDataRecord);

            List<RingerDataRecord> ringerDataRecords = db.ringerDataRecordDao().getAll();
            for (RingerDataRecord r : ringerDataRecords) {
                Log.e(TAG," RingerMode: " + r.getRingerMode());
                Log.e(TAG," AudioMode: " + r.getAudioMode());
                Log.e(TAG," StreamVolumeMusic: " + String.valueOf(r.getStreamVolumeMusic()));
                Log.e(TAG," StreamVolumeNotification: " + String.valueOf(r.getStreamVolumeNotification()));
                Log.e(TAG," StreamVolumeRing: " + String.valueOf(r.getStreamVolumeRing()));
                Log.e(TAG," StreamVolumeVoiceCall: " + String.valueOf(r.getStreamVolumeVoicecall()));
                Log.e(TAG," StreamVolumeSystem: " + String.valueOf(r.getStreamVolumeSystem()));
            }
        } catch (NullPointerException e) { //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    } //call updateStream everyminute

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {
//        new Threading().start();
        Log.e(TAG,"onStreamRegistration");

        runPhoneStatusMainThread();

    }

    public void runPhoneStatusMainThread() {

        Log.d(TAG, "runPhoneStatusMainThread") ;

        sMainThread = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                getAudioRingerUpdate();

                sMainThread.postDelayed(this, sMainThreadUpdateFrequencyInMilliseconds);

            }
        };

        sMainThread.post(runnable);
    }

    private void getAudioRingerUpdate() {
        if (sAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            mRingerMode = RINGER_MODE_NORMAL;
        } else if (sAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            mRingerMode = RINGER_MODE_VIBRATE;
        } else if (sAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            mRingerMode = RINGER_MODE_SILENT;
        }

        int mode = sAudioManager.getMode();
//        Log.d(LOG_TAG, "[getAudioRingerUpdate] ringer mode: " + mRingerMode + " mode: " + mode);

        mStreamVolumeMusic = sAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mStreamVolumeNotification = sAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        mStreamVolumeRing = sAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        mStreamVolumeVoiceCall = sAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        mStreamVolumeSystem= sAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

        mAudioMode = getAudioMode(sAudioManager.getMode());

        Log.d(TAG,"mRingerMode : " + mRingerMode + " mAudioMode : " + mAudioMode + " mStreamVolumeMusic : " + mStreamVolumeMusic
                + " mStreamVolumeNotification : " + mStreamVolumeNotification + " mStreamVolumeRing : " + mStreamVolumeRing
                + " mStreamVolumeVoiceCall : " + mStreamVolumeVoiceCall + " mStreamVolumeSystem : " + mStreamVolumeSystem);

        //android 6
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            sAllAudioDevices = sAudioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        }


        sAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);

    }

    public String getAudioMode(int mode) {

        switch (mode) {
            case AudioManager.MODE_CURRENT:
                return MODE_CURRENT;
            case AudioManager.MODE_INVALID:
                return MODE_INVALID;
            case AudioManager.MODE_IN_CALL:
                return MODE_IN_CALL;
            case AudioManager.MODE_IN_COMMUNICATION:
                return MODE_IN_COMMUNICATION;
            case AudioManager.MODE_NORMAL:
                return MODE_NORMAL;
            case AudioManager.MODE_RINGTONE:
                return MODE_RINGTONE;
            default:
                return "NA";
        }
    }

    @Override
    public void offer(RingerDataRecord ringerdataRecord) {
        mStream.add(ringerdataRecord);
    }

}
