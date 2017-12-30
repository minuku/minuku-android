package labelingStudy.nctu.minuku.model.DataRecord;

import java.util.Calendar;
import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

public class ConnectivityDataRecord implements DataRecord{

    public long creationTime;
    private int taskDayCount;
    private long hour;
    //network connectivity
    public static String NETWORK_TYPE_WIFI = "Wifi";
    public static String NETWORK_TYPE_MOBILE = "Mobile";

    private static String NetworkType = "NA";
    private static boolean IsNetworkAvailable = false;
    private static boolean IsConnected = false;
    private static boolean IsWifiAvailable = false;
    private static boolean IsMobileAvailable = false;
    private static boolean IsWifiConnected = false;
    private static boolean IsMobileConnected = false;

    public ConnectivityDataRecord(){}

    public ConnectivityDataRecord(String NetworkType,boolean IsNetworkAvailable, boolean IsConnected, boolean IsWifiAvailable,
                                  boolean IsMobileAvailable, boolean IsWifiConnected, boolean IsMobileConnected){
        this.creationTime = new Date().getTime();
//        this.taskDayCount = Constants.TaskDayCount;
//        this.hour = getmillisecondToHour(creationTime);
        this.NetworkType = NetworkType;
        this.IsNetworkAvailable = IsNetworkAvailable;
        this.IsConnected = IsConnected;
        this.IsWifiAvailable = IsWifiAvailable;
        this.IsMobileAvailable = IsMobileAvailable;
        this.IsWifiConnected = IsWifiConnected;
        this.IsMobileConnected = IsMobileConnected;

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

    public String getNetworkType(){
        return NetworkType;
    }

    public boolean getIsNetworkAvailable(){
        return IsNetworkAvailable;
    }

    public boolean getIsConnected(){
        return IsConnected;
    }

    public boolean getIsWifiAvailable(){
        return IsWifiAvailable;
    }

    public boolean getIsMobileAvailable(){
        return IsMobileAvailable;
    }

    public boolean getIsWifiConnected(){
        return IsWifiConnected;
    }

    public boolean getIsMobileConnected(){
        return IsMobileConnected;
    }

}
