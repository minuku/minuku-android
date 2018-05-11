package labelingStudy.nctu.minuku.model.DataRecord;

import java.util.Calendar;
import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/5/22.
 */

public class TransportationModeDataRecord implements DataRecord{

    public long creationTime;
    private int taskDayCount;
    private long hour;
    public String confirmedActivityString; //

    public TransportationModeDataRecord(){}

    public TransportationModeDataRecord(String confirmedActivityString){
        this.creationTime = new Date().getTime();
//        this.taskDayCount = Constants.TaskDayCount;
//        this.hour = getmillisecondToHour(creationTime);
        this.confirmedActivityString = confirmedActivityString;
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

    public String getConfirmedActivityString(){
        return confirmedActivityString;
    }

    public void setConfirmedActivityString(String confirmedActivityString){
        this.confirmedActivityString = confirmedActivityString;
    }

}
