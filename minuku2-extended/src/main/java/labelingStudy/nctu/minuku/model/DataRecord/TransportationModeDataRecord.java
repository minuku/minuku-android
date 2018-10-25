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

    private long suspectedTime;
    public String confirmedActivityString;
    public String suspectedStartActivityString;
    public String suspectedStopActivityString;
    private String sessionid;

    public String getSuspectedStopActivityString() {
        return suspectedStopActivityString;
    }

    public void setSuspectedStopActivityString(String suspectedStopActivityString) {
        this.suspectedStopActivityString = suspectedStopActivityString;
    }

    public long getSuspectedTime() {
        return suspectedTime;
    }

    public void setSuspectedTime(long suspectedTime) {
        this.suspectedTime = suspectedTime;
    }

    public String getSuspectedStartActivityString() {
        return suspectedStartActivityString;
    }

    public void setSuspectedStartActivityString(String suspectedStartActivityString) {
        this.suspectedStartActivityString = suspectedStartActivityString;
    }

    public String getSessionid() {
        return sessionid;
    }

    public TransportationModeDataRecord(){}

    public TransportationModeDataRecord(String confirmedActivityString){

        this.creationTime = new Date().getTime();
        this.confirmedActivityString = confirmedActivityString;
    }

    public TransportationModeDataRecord(String confirmedActivityString, long suspectedTime, String suspectedStartActivityString, String suspectedStopActivityString){

        this.creationTime = new Date().getTime();
        this.confirmedActivityString = confirmedActivityString;
        this.suspectedTime = suspectedTime;
        this.suspectedStartActivityString = suspectedStartActivityString;
        this.suspectedStopActivityString = suspectedStopActivityString;
    }

    public TransportationModeDataRecord(String confirmedActivityString, long suspectedTime, String suspectedStartActivityString, String suspectedStopActivityString, String sessionid){

        this.creationTime = new Date().getTime();
        this.confirmedActivityString = confirmedActivityString;
        this.suspectedTime = suspectedTime;
        this.suspectedStartActivityString = suspectedStartActivityString;
        this.suspectedStopActivityString = suspectedStopActivityString;
        this.sessionid = sessionid;
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
