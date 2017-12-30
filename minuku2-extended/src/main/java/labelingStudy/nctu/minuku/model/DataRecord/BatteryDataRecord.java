package labelingStudy.nctu.minuku.model.DataRecord;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

public class BatteryDataRecord implements DataRecord{

    private final String TAG = "BatteryDataRecord";

    public long creationTime;
    private int taskDayCount;
    private long hour;
    public int BatteryLevel;
    public float BatteryPercentage;
    private String BatteryChargingState = "NA";
    public boolean isCharging;

    public BatteryDataRecord(){}

    public BatteryDataRecord(int BatteryLevel, float BatteryPercentage, String BatteryChargingState, boolean isCharging){
        this.creationTime = new Date().getTime();
        Log.d(TAG, "creationTime : "+creationTime);
        Log.d(TAG, "creationTime String : "+getmillisecondToDateWithTime(creationTime));
//        this.taskDayCount = Constants.TaskDayCount;
//        this.hour = getmillisecondToHour(creationTime);
        this.BatteryLevel = BatteryLevel;
        this.BatteryPercentage = BatteryPercentage;
        this.BatteryChargingState = BatteryChargingState;
        this.isCharging = isCharging;

        Log.d(TAG,"BatteryLevel : "+ this.BatteryLevel+" BatteryPercentage : "+ this.BatteryPercentage
        +" BatteryChargingState : "+ this.BatteryChargingState +" isCharging : "+ this.isCharging);
    }

    private long getmillisecondToHour(long timeStamp){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        long mhour = calendar.get(Calendar.HOUR_OF_DAY);

        return mhour;

    }

    private String getmillisecondToDateWithTime(long timeStamp){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH)+1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mhour = calendar.get(Calendar.HOUR);
        int mMin = calendar.get(Calendar.MINUTE);
        int mSec = calendar.get(Calendar.SECOND);

        return addZero(mYear)+"/"+addZero(mMonth)+"/"+addZero(mDay)+" "+addZero(mhour)+":"+addZero(mMin)+":"+addZero(mSec);

    }

    private String addZero(int date){
        if(date<10)
            return String.valueOf("0"+date);
        else
            return String.valueOf(date);
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

    public int getBatteryLevel(){
        return BatteryLevel;
    }

    public float getBatteryPercentage(){
        return BatteryPercentage;
    }

    public String getBatteryChargingState(){
        return BatteryChargingState;
    }

    public boolean getisCharging(){
        return isCharging;
    }
}
