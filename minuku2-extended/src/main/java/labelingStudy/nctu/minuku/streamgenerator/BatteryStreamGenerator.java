package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
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

import labelingStudy.nctu.minuku.Data.appDatabase;
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
/**
 * BatteryStreamGenerator collects data about battery conditions and status.
 */
public class BatteryStreamGenerator extends AndroidStreamGenerator<BatteryDataRecord> {

    private final String TAG = "BatteryStreamGenerator";
    private BatteryStream mStream;
    private static Context sContext;

    public static int sBatteryLevel = -1;
    public static float sBatteryPercentage = -1;
    private static String sBatteryChargingState = "NA";
    public static boolean sIsCharging = false;

    public BatteryStreamGenerator(Context applicationContext) {
        super(applicationContext);
        sContext = applicationContext;

        mStream = new BatteryStream(Constants.DEFAULT_QUEUE_SIZE);
        register();
    }


    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager");

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

    @SuppressLint("LongLogTag")
    @Override
    public boolean updateStream() {
        Log.d(TAG, "updateStream called");
        //TODO get service data
        BatteryDataRecord batteryDataRecord
                = new BatteryDataRecord(sBatteryLevel, sBatteryPercentage, sBatteryChargingState, sIsCharging);
        mStream.add(batteryDataRecord);
        Log.d(TAG, "CheckFamiliarOrNot to be sent to event bus" + batteryDataRecord);
        // also post an event.
        EventBus.getDefault().post(batteryDataRecord);
        try {
            appDatabase db;
            db = Room.databaseBuilder(sContext,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();
            db.batteryDataRecordDao().insertAll(batteryDataRecord);

            List<BatteryDataRecord> batteryDataRecords = db.batteryDataRecordDao().getAll();
            for (BatteryDataRecord b : batteryDataRecords) {
                Log.e(TAG, " BatteryChargingState " + b.getBatteryChargingState());
                Log.e(TAG, " BatteryPercentage " + String.valueOf(b.getBatteryPercentage()));
                Log.e(TAG, " BatteryLevel: " + String.valueOf(b.getBatteryLevel()));
                Log.e(TAG, " isCharging: " + String.valueOf(b.isCharging()));
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

        } catch (NullPointerException e) { //Sometimes no data is normal
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
                //int health = intent.getIntExtra("health", 0);
                //boolean present = intent.getBooleanExtra("present",false);
                //int mBatteryLevel = intent.getIntExtra("mBatteryLevel", 0);
                sBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

                //boolean
                sIsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                sBatteryPercentage = sBatteryLevel / (float)scale;
//                int icon_small = intent.getIntExtra("icon-small", 0);
//                int plugged = intent.getIntExtra("plugged", 0);
//                int voltage = intent.getIntExtra("voltage", 0);
                int temperature = intent.getIntExtra("temperature",0);
                //String technology = intent.getStringExtra("technology");


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

                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                if (!sIsCharging) {
                    sBatteryChargingState = "not charging";
                } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) {
                    sBatteryChargingState = "usb charging";
                } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
                    sBatteryChargingState = "ac charging";
                }

                Log.d("BatteryStatus", statusString);
                //Log.d("Batteryhealth", healthString);
                //Log.d("Batterypresent", String.valueOf(present));
                Log.d("mBatteryLevel", String.valueOf(sBatteryLevel));
                Log.d("BatteryScale", String.valueOf(scale));
                Log.d("sBatteryPercentage", String.valueOf(sBatteryPercentage));
                //Log.d("Batteryicon_small", String.valueOf(icon_small));

                Log.d("IsCharging", String.valueOf(sIsCharging));

                Log.d("BatteryChargingState", sBatteryChargingState);

                //Log.d("Batteryplugged", acString);
                //Log.d("Batteryvoltage", String.valueOf(voltage));
                Log.d("Batterytemperature", String.valueOf(temperature));
                //Log.d("Batterytechnology", technology);
            }
        }
    };
}
