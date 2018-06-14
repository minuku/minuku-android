package labelingStudy.nctu.minuku.streamgenerator;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;
import labelingStudy.nctu.minuku.stream.BatteryStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class BatteryStreamGenerator extends AndroidStreamGenerator<BatteryDataRecord> {

    private final String TAG = "BatteryStreamGenerator";
    private BatteryStream mStream;
    private static Context mContext;

    public static int mBatteryLevel= -1;
    public static float mBatteryPercentage = -1;
    private static String mBatteryChargingState = "NA";
    public static boolean isCharging = false;

    public BatteryStreamGenerator(Context applicationContext){
        super(applicationContext);
        mContext = applicationContext;

        this.mStream = new BatteryStream(Constants.DEFAULT_QUEUE_SIZE);
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
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
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
        BatteryDataRecord batteryDataRecord
                = new BatteryDataRecord(mBatteryLevel, mBatteryPercentage, mBatteryChargingState, isCharging);
        mStream.add(batteryDataRecord);
        Log.d(TAG, "CheckFamiliarOrNot to be sent to event bus" + batteryDataRecord);
        // also post an event.
        EventBus.getDefault().post(batteryDataRecord);
        try {
            appDatabase db;
            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();
            db.batteryDataRecordDao().insertAll(batteryDataRecord);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date start =sdf.parse("2018/03/26 00:00:00");
            Date end =sdf.parse("2018/03/26 15:37:00");

            List<BatteryDataRecord> batteryDataRecords = db.batteryDataRecordDao().getAll();
            for (BatteryDataRecord b : batteryDataRecords) {
                Log.d(TAG, b.getBatteryChargingState());
                Log.d(TAG, String.valueOf(b.getBatteryPercentage()));
            }

//            List<BatteryDataRecord> batteryDataRecords = db.batteryDataRecordDao().getAll();
//            for (BatteryDataRecord b : batteryDataRecords) {
//                Date dt=new Date(b.getCreationTime());
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                String time=sdf.format(dt);
//                Log.d(TAG, time);
//            }

//            List<BatteryDataRecord> batteryDataRecords = db.batteryDataRecordDao().getRecordBetweenTimes(start.getTime(), end.getTime());
//            for (BatteryDataRecord b : batteryDataRecords) {
//                Date dt=new Date(b.getCreationTime());
//                String time=sdf.format(dt);
//                Log.d(TAG, time);
//            }

        } catch (NullPointerException e){ //Sometimes no data is normal
            e.printStackTrace();
            return false;
        } catch (ParseException e) {
            e.printStackTrace();
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
                int status = intent.getIntExtra("status", -1);
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                //boolean
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                mBatteryPercentage = mBatteryLevel /(float)scale;
                int temperature = intent.getIntExtra("temperature",0);
                //String technology = intent.getStringExtra("technology");

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
                //Log.d("Batteryhealth", healthString);
                //Log.d("Batterypresent", String.valueOf(present));
                Log.d("mBatteryLevel", String.valueOf(mBatteryLevel));
                Log.d("BatteryScale", String.valueOf(scale));
                Log.d("mBatteryPercentage", String.valueOf(mBatteryPercentage));
                //Log.d("Batteryicon_small", String.valueOf(icon_small));

                Log.d("IsCharging",String.valueOf(isCharging));

                Log.d("BatteryChargingState",mBatteryChargingState);

                //Log.d("Batteryplugged", acString);
                //Log.d("Batteryvoltage", String.valueOf(voltage));
                Log.d("Batterytemperature", String.valueOf(temperature));
                //Log.d("Batterytechnology", technology);
            }
        }
    };
}