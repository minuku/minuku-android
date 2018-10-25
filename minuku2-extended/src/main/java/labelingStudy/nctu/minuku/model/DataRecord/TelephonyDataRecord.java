package labelingStudy.nctu.minuku.model.DataRecord;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

public class TelephonyDataRecord implements DataRecord {
    private final String TAG = "TelephonyDataRecord";

    public long creationTime;
    private int taskDayCount;
    private long hour;
    private String NetworkOperatorName = "NA";
    private int CallState = -9999;
    private int PhoneSignalType = -9999;
    private int GsmSignalStrength = -9999;
    private int LTESignalStrength = -9999;
    private int CdmaSignalStrengthLevel = -9999;
    private String sessionid;

    public TelephonyDataRecord(String NetworkOperatorName, int CallState, int PhoneSignalType
            , int GsmSignalStrength, int LTESignalStrength, int CdmaSignalStrengthLevel) {

        this.creationTime = new Date().getTime();
        this.NetworkOperatorName = NetworkOperatorName;
        this.CallState = CallState;
        this.PhoneSignalType = PhoneSignalType;
        this.GsmSignalStrength = GsmSignalStrength;
        this.LTESignalStrength = LTESignalStrength;
        this.CdmaSignalStrengthLevel = CdmaSignalStrengthLevel;

        Log.d(TAG,"mNetworkOperatorName : "+ NetworkOperatorName +" mCallState : "+ CallState+" mPhoneSignalType : "+ PhoneSignalType
                +" mGsmSignalStrength : "+ GsmSignalStrength+" mLTESignalStrength : "+ LTESignalStrength
                +" mCdmaSignalStrenthLevel : "+ CdmaSignalStrengthLevel);
    }

    public TelephonyDataRecord(String NetworkOperatorName, int CallState, int PhoneSignalType
            , int GsmSignalStrength, int LTESignalStrength, int CdmaSignalStrengthLevel, String sessionid) {

        this.creationTime = new Date().getTime();
        this.NetworkOperatorName = NetworkOperatorName;
        this.CallState = CallState;
        this.PhoneSignalType = PhoneSignalType;
        this.GsmSignalStrength = GsmSignalStrength;
        this.LTESignalStrength = LTESignalStrength;
        this.CdmaSignalStrengthLevel = CdmaSignalStrengthLevel;
        this.sessionid = sessionid;

    }

    public String getSessionid() {
        return sessionid;
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

    public String getNetworkOperatorName() {
        return NetworkOperatorName;
    }

    public int getCallState() {
        return CallState;
    }

    public int getPhoneSignalType() {
        return PhoneSignalType;
    }

    public int getGsmSignalStrength() {
        return GsmSignalStrength;
    }

    public int getLTESignalStrength() {
        return LTESignalStrength;
    }

    public int getCdmaSignalStrengthLevel() {
        return CdmaSignalStrengthLevel;
    }
}
