package labelingStudy.nctu.minuku.streamgenerator;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.BatteryDataRecordDAO;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;
import labelingStudy.nctu.minuku.stream.BatteryStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class BatteryStreamGenerator extends AndroidStreamGenerator<BatteryDataRecord> {

    private final String TAG = "BatteryStreamGenerator";
    private BatteryStream mStream;
    private BatteryDataRecordDAO batteryDataRecordDAO;
    public static int mBatteryLevel= -1;
    public static float mBatteryPercentage = -1;
    private static String mBatteryChargingState = "NA";
    public static boolean isCharging = false;
    private long detectedTime = Constants.INVALID_TIME_VALUE;
    private Context mContext;

    private SharedPreferences sharedPrefs;

    public BatteryStreamGenerator(Context applicationContext){
        super(applicationContext);

        this.mContext = applicationContext;
        this.mStream = new BatteryStream(Constants.DEFAULT_QUEUE_SIZE);
        batteryDataRecordDAO = appDatabase.getDatabase(applicationContext).batteryDataRecordDao();
        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE);

        this.register();
    }


    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManage");

        try {
            MinukuStreamManager.getInstance().register(mStream, BatteryDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "BatteryDataRecord/BatteryStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " BatteryDataRecord/BatteryStream is already registered.");
        }
    }

    @Override
    public Stream<BatteryDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG, "updateStream called");

//        int session_id = SessionManager.getOngoingSessionId();

        int session_id = sharedPrefs.getInt("ongoingSessionid", Constants.INVALID_INT_VALUE);

        //TODO get service data
        BatteryDataRecord batteryDataRecord
                = new BatteryDataRecord(mBatteryLevel, mBatteryPercentage, mBatteryChargingState, isCharging, detectedTime);

        if((ScheduleAndSampleManager.getCurrentTimeInMillis() - detectedTime) >= Constants.MILLISECONDS_PER_MINUTE * 10
                && (detectedTime != Constants.INVALID_TIME_VALUE)){

            batteryDataRecord = new BatteryDataRecord(-1,
                    -1, "NA", false, detectedTime);
        }

        mStream.add(batteryDataRecord);
        Log.d(TAG, "CheckFamiliarOrNot to be sent to event bus" + batteryDataRecord);
        // also post an event.
        EventBus.getDefault().post(batteryDataRecord);
        try {
//            appDatabase db;
//            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
//                    .allowMainThreadQueries()
//                    .build();
            batteryDataRecordDAO.insertAll(batteryDataRecord);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//            Date start =sdf.parse("2018/03/26 00:00:00");
//            Date end =sdf.parse("2018/03/26 15:37:00");

            List<BatteryDataRecord> batteryDataRecords = batteryDataRecordDAO.getAll();
            for (BatteryDataRecord b : batteryDataRecords) {
                Log.e(TAG, " BatteryChargingState "+b.getBatteryChargingState());
                Log.e(TAG, " BatteryPercentage "+String.valueOf(b.getBatteryPercentage()));
                Log.e(TAG, " BatteryLevel: "+String.valueOf(b.getBatteryLevel()));
                Log.e(TAG, " isCharging: "+String.valueOf(b.isCharging()));
            }
//            mDAO.query_counting();
        } catch (NullPointerException e){ //Sometimes no data is normal
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mApplicationContext.registerReceiver(mBroadcastReceiver, filter);

        Log.d(TAG, "Stream " + TAG + " registered successfully");

    }

    @Override
    public void offer(BatteryDataRecord dataRecord) {

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

                detectedTime = ScheduleAndSampleManager.getCurrentTimeInMillis();

                int status = intent.getIntExtra("status", -1);
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                //boolean
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                mBatteryPercentage = mBatteryLevel / (float)scale;
                int temperature = intent.getIntExtra("temperature",0);

                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                String statusString = "";
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        statusString = "unknown";
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString = "charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = "discharging";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = "not charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = "full";
                        break;
                }

                if (!isCharging){
                    mBatteryChargingState = "not charging";
                }else if (chargePlug==BatteryManager.BATTERY_PLUGGED_USB){
                    mBatteryChargingState = "usb charging";
                }else if (chargePlug==BatteryManager.BATTERY_PLUGGED_AC){
                    mBatteryChargingState = "ac charging";
                }
                Log.d("Batterystatus", statusString);
                Log.d("mBatteryLevel", String.valueOf(mBatteryLevel));
                Log.d("BatteryScale", String.valueOf(scale));
                Log.d("mBatteryPercentage", String.valueOf(mBatteryPercentage));

                Log.d("IsCharging",String.valueOf(isCharging));

                Log.d("BatteryChargingState",mBatteryChargingState);

                Log.d("Batterytemperature", String.valueOf(temperature));
            }
        }
    };
}
