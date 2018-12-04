package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.service.NotificationListenService;
import labelingStudy.nctu.minuku.stream.NotificationStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by chiaenchiang on 18/11/2018.
 */

public class NotificationStreamGenerator extends AndroidStreamGenerator<NotificationDataRecord> {

    private Context mContext;
    String TAG = "NotificationStreamGenerator";
    String room = "room";
    private NotificationStream mStream;
    private static NotificationManager notificationManager;
    public static String mNotificaitonTitle = "";
    public static String mNotificaitonText = "";
    public static String mNotificaitonSubText = "";
    public static String mNotificationTickerText = "";
    public static  String mNotificaitonPackageName ="";
    private NotificationListenService notificationlistener;
    public static Integer accessid=-1;
    appDatabase db;



    public NotificationStreamGenerator(){
        super();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with Stream Manager");
        try {
            MinukuStreamManager.getInstance().register(mStream, NotificationDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which NotificationDataRecord/NotificationStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
            Log.e(TAG, "Another stream which provides NotificationDataRecord/NotificationStream is already registered.");
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("ServiceCast")
    public NotificationStreamGenerator(Context applicationContext) {
        super(applicationContext);


        mContext = applicationContext;
        notificationlistener = new NotificationListenService(this);
        this.mStream = new NotificationStream(Constants.DEFAULT_QUEUE_SIZE);

        //  notificationManager = (android.app.NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);


        mNotificaitonTitle = "Default";
        mNotificaitonText = "Default";
        mNotificaitonSubText = "Default";
        mNotificationTickerText = "Default";
        mNotificaitonPackageName ="Default";
        accessid = -1;
        this.register();
    }



    @Override
    public Stream<NotificationDataRecord> generateNewStream() {
        return null;
    }

    @Override
    public boolean updateStream() {

        NotificationDataRecord notificationDataRecord =
                new NotificationDataRecord(mNotificaitonTitle, mNotificaitonText, mNotificaitonSubText
                        , mNotificationTickerText, mNotificaitonPackageName ,accessid);

        mStream.add(notificationDataRecord);
        Log.d(TAG, "Check notification to be sent to event bus " + notificationDataRecord);
        // also post an event.
        Log.d("creationTime : ", "notiData : "+notificationDataRecord.getCreationTime());

        EventBus.getDefault().post(notificationDataRecord);

        try {
            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();
            db.notificationDataRecordDao().insertAll(notificationDataRecord);
            List<NotificationDataRecord> notificationDataRecords = db.notificationDataRecordDao().getAll();
            for (NotificationDataRecord l : notificationDataRecords) {
                Log.e(TAG, " NotificationPackageName: " + String.valueOf(l.getNotificaitonPackageName()));
                Log.e(TAG, " NotificaitonTitle: " + String.valueOf(l.getNotificaitonTitle()));
                Log.e(TAG, " NotificaitonText: " + String.valueOf(l.getNotificaitonText()));
            }
//            List<NotificationDataRecord> NotificationDataRecords = db.notificationDataRecordDao().getAll();
//            for (NotificationDataRecord n : NotificationDataRecords) {
//                Log.d(room,"Notification: "+n.getNotificaitonPackageName());
//            }

        } catch (NullPointerException e) { //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {

    }

    public void setNotificationDataRecord(String title, String text, String subText, String tickerText,String pack,Integer accessid){

        this.mNotificaitonTitle = title;
        this.mNotificaitonText = text;
        this.mNotificaitonSubText = subText;
        this.mNotificationTickerText = tickerText;
        this.mNotificaitonPackageName = pack;
        this.accessid = accessid;
        Log.d(TAG, "title:"+title+" text: "+text + " subText: "+subText+" ticker: "+tickerText+" pack: "+pack);

    }


    @Override
    public void offer(NotificationDataRecord dataRecord) {

    }
}
