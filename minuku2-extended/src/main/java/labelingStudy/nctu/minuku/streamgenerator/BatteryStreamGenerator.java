package labelingStudy.nctu.minuku.streamgenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

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
    private BatteryDataRecordDAO mDAO;

    public static int mBatteryLevel= -1;
    public static float mBatteryPercentage = -1;
    private static String mBatteryChargingState = "NA";
    public static boolean isCharging = false;

    public BatteryStreamGenerator(Context applicationContext){
        super(applicationContext);

        this.mStream = new BatteryStream(Constants.DEFAULT_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(BatteryDataRecord.class);;
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
        //TODO get service data
        BatteryDataRecord batteryDataRecord
                = new BatteryDataRecord(mBatteryLevel, mBatteryPercentage, mBatteryChargingState, isCharging);
        mStream.add(batteryDataRecord);
        Log.d(TAG, "CheckFamiliarOrNot to be sent to event bus" + batteryDataRecord);
        // also post an event.
        EventBus.getDefault().post(batteryDataRecord);
        try {
            mDAO.add(batteryDataRecord);
            mDAO.query_counting();
        } catch (DAOException e) {
            e.printStackTrace();
            return false;
        }catch (NullPointerException e){ //Sometimes no data is normal
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
                int status = intent.getIntExtra("status", -1);
                //int health = intent.getIntExtra("health", 0);
                //boolean present = intent.getBooleanExtra("present",false);
                //int mBatteryLevel = intent.getIntExtra("mBatteryLevel", 0);
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                //boolean
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                mBatteryPercentage = mBatteryLevel /(float)scale;
//                int icon_small = intent.getIntExtra("icon-small", 0);
//                int plugged = intent.getIntExtra("plugged", 0);
//                int voltage = intent.getIntExtra("voltage", 0);
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

//                String healthString = "";
//                switch (health) {
//                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
//                        healthString = "unknown";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_GOOD:
//                        healthString = "good";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
//                        healthString = "overheat";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_DEAD:
//                        healthString = "dead";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
//                        healthString = "voltage";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
//                        healthString = "unspecified failure";
//                        break;
//                }
//                String acString = "";
//                switch (plugged) {
//                    case BatteryManager.BATTERY_PLUGGED_AC:
//                        acString = "plugged ac";
//                        break;
//                    case BatteryManager.BATTERY_PLUGGED_USB:
//
//                        acString = "plugged usb";
//                        break;
//                }
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
