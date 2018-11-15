package labelingStudy.nctu.minuku.model.DataRecord;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

/**
 * ConnectivityDataRecord stores information about network connecting condition and status
 */
@Entity
public class ConnectivityDataRecord implements DataRecord {

    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "NetworkType")
    public static String NetworkType = "NA";

    @ColumnInfo(name = "IsNetworkAvailable")
    public static boolean IsNetworkAvailable = false;

    @ColumnInfo(name = "IsConnected")
    public static boolean IsConnected = false;

    @ColumnInfo(name = "IsWifiAvailable")
    public static boolean IsWifiAvailable = false;

    @ColumnInfo(name = "IsMobileAvailable")
    public static boolean IsMobileAvailable = false;

    @ColumnInfo(name = "IsWifiConnected")
    public static boolean IsWifiConnected = false;

    @ColumnInfo(name = "IsMobileConnected")
    public static boolean IsMobileConnected = false;



    public ConnectivityDataRecord(String NetworkType,boolean IsNetworkAvailable, boolean IsConnected, boolean IsWifiAvailable,
                                  boolean IsMobileAvailable, boolean IsWifiConnected, boolean IsMobileConnected){
        this.creationTime = new Date().getTime();
        this.NetworkType = NetworkType;
        this.IsNetworkAvailable = IsNetworkAvailable;
        this.IsConnected = IsConnected;
        this.IsWifiAvailable = IsWifiAvailable;
        this.IsMobileAvailable = IsMobileAvailable;
        this.IsWifiConnected = IsWifiConnected;
        this.IsMobileConnected = IsMobileConnected;

    }
    public ConnectivityDataRecord() {}

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public static String getNetworkType() {
        return NetworkType;
    }

    public static void setNetworkType(String networkType) {
        NetworkType = networkType;
    }

    public static boolean isNetworkAvailable() {
        return IsNetworkAvailable;
    }

    public static void setIsNetworkAvailable(boolean isNetworkAvailable) {
        IsNetworkAvailable = isNetworkAvailable;
    }

    public static boolean isIsConnected() {
        return IsConnected;
    }

    public static void setIsConnected(boolean isConnected) {
        IsConnected = isConnected;
    }

    public static boolean isIsWifiAvailable() {
        return IsWifiAvailable;
    }

    public static void setIsWifiAvailable(boolean isWifiAvailable) {
        IsWifiAvailable = isWifiAvailable;
    }

    public static boolean isIsMobileAvailable() {
        return IsMobileAvailable;
    }

    public static void setIsMobileAvailable(boolean isMobileAvailable) {
        IsMobileAvailable = isMobileAvailable;
    }

    public static boolean isIsWifiConnected() {
        return IsWifiConnected;
    }

    public static void setIsWifiConnected(boolean isWifiConnected) {
        IsWifiConnected = isWifiConnected;
    }

    public static boolean isIsMobileConnected() {
        return IsMobileConnected;
    }

    public static void setIsMobileConnected(boolean isMobileConnected) {
        IsMobileConnected = isMobileConnected;
    }

}
