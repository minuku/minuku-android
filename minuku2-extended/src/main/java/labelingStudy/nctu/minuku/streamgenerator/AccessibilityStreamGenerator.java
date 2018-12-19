package labelingStudy.nctu.minuku.streamgenerator;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.AccessibilityDataRecordDAO;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minuku.service.MobileAccessibilityService;
import labelingStudy.nctu.minuku.stream.AccessibilityStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static labelingStudy.nctu.minuku.manager.MinukuStreamManager.getInstance;

/**
 * Created by Lawrence on 2017/9/6.
 */

public class AccessibilityStreamGenerator extends AndroidStreamGenerator<AccessibilityDataRecord> {

    private final String TAG = "AccessibilityStreamGenerator";
    private AccessibilityStream mStream;
    private Context mContext;
    private AccessibilityDataRecordDAO accessibilityDataRecordDAO;
    MobileAccessibilityService mobileAccessibilityService;

    private String pack;
    private String text;
    private String type;
    private String extra;

    private long detectedTime;

    private SharedPreferences sharedPrefs;

    public AccessibilityStreamGenerator(Context applicationContext){
        super(applicationContext);
        this.mContext = applicationContext;
        this.mStream = new AccessibilityStream(Constants.DEFAULT_QUEUE_SIZE);
        accessibilityDataRecordDAO = appDatabase.getDatabase(applicationContext).accessibilityDataRecordDao();
        mobileAccessibilityService = new MobileAccessibilityService(this);

        pack = text = type = extra = Constants.INVALID_STRING_VALUE;

        detectedTime = Constants.INVALID_TIME_VALUE;

        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString,Context.MODE_PRIVATE);

        this.register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManage");

        try {
            getInstance().register(mStream, AccessibilityDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "AccessibilityDataRecord/AccessibilityStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " AccessibilityDataRecord/AccessibilityStream is already registered.");
        }
    }

    private void activateAccessibilityService() {

        Log.d(TAG, "testing logging task and requested activateAccessibilityService");
        Intent intent = new Intent(mContext, MobileAccessibilityService.class);
        mContext.startService(intent);
    }


    @Override
    public Stream<AccessibilityDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {

        Log.d(TAG, "updateStream called");

//        int session_id = SessionManager.getOngoingSessionId();

        int session_id = sharedPrefs.getInt("ongoingSessionid", Constants.INVALID_INT_VALUE);

        AccessibilityDataRecord accessibilityDataRecord
                = new AccessibilityDataRecord(pack, text, type, extra, detectedTime);
        mStream.add(accessibilityDataRecord);
        Log.d(TAG,"pack = "+pack+" text = "+text+" type = "+type+" extra = "+extra);
        Log.d(TAG, "detectedTime : "+ScheduleAndSampleManager.getTimeString(detectedTime));
        Log.d(TAG, "Accessibility to be sent to event bus" + accessibilityDataRecord);

        //if there don't have any updates for 10 minutes, add the NA one to represent it
        if((ScheduleAndSampleManager.getCurrentTimeInMillis() - detectedTime) >= Constants.MILLISECONDS_PER_MINUTE * 10
                && (detectedTime != Constants.INVALID_TIME_VALUE)){

            accessibilityDataRecord = new AccessibilityDataRecord(Constants.INVALID_STRING_VALUE,
                    Constants.INVALID_STRING_VALUE, Constants.INVALID_STRING_VALUE, Constants.INVALID_STRING_VALUE, ScheduleAndSampleManager.getCurrentTimeInMillis());
        }

        // also post an event.
        EventBus.getDefault().post(accessibilityDataRecord);
        try {

//            appDatabase db;
//            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
//                    .allowMainThreadQueries()
//                    .build();

            accessibilityDataRecordDAO.insertAll(accessibilityDataRecord);
            List<AccessibilityDataRecord> accessibilityDataRecords = accessibilityDataRecordDAO.getAll();

            for (AccessibilityDataRecord a : accessibilityDataRecords) {
                Log.e(TAG, "pack in db: "+a.getPack());
                Log.e(TAG, "Type in db: "+a.getType());
                Log.e(TAG, "Text in db: "+a.getText());
                Log.e(TAG, "Extra in db: "+a.getExtra());
            }
        } catch (NullPointerException e){
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

    public void setLatestInAppAction(String pack, String text, String type, String extra, long detectedTime){

        this.pack = pack;
        this.text = text;
        this.type = type;
        this.extra = extra;

        this.detectedTime = detectedTime;
    }

    @Override
    public void onStreamRegistration() {

        activateAccessibilityService();

    }

    @Override
    public void offer(AccessibilityDataRecord dataRecord) {

    }
}
