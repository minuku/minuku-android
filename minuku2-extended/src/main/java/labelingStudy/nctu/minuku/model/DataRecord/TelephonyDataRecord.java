package labelingStudy.nctu.minuku.model.DataRecord;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */
@Entity
public class TelephonyDataRecord implements DataRecord {

    private String TAG = "TelephonyDataRecord";

    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "NetworkOperatorName")
    public String NetworkOperatorName = "NA";

    @ColumnInfo(name = "CallState")
    public int CallState = -9999;

    @ColumnInfo(name = "PhoneSignalType")
    public int PhoneSignalType = -9999;

    @ColumnInfo(name = "GsmSignalStrength")
    public int GsmSignalStrength = -9999;

    @ColumnInfo(name = "LTESignalStrength")
    public int LTESignalStrength = -9999;

    @ColumnInfo(name = "CdmaSignalStrengthLevel")
    public int CdmaSignalStrengthLevel = -9999;



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

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

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

    public String getNetworkOperatorName() {
        return NetworkOperatorName;
    }

    public void setNetworkOperatorName(String networkOperatorName) {
        NetworkOperatorName = networkOperatorName;
    }

    public int getCallState() {
        return CallState;
    }

    public void setCallState(int callState) {
        CallState = callState;
    }

    public int getPhoneSignalType() {
        return PhoneSignalType;
    }

    public void setPhoneSignalType(int phoneSignalType) {
        PhoneSignalType = phoneSignalType;
    }

    public int getGsmSignalStrength() {
        return GsmSignalStrength;
    }

    public void setGsmSignalStrength(int gsmSignalStrength) {
        GsmSignalStrength = gsmSignalStrength;
    }

    public int getLTESignalStrength() {
        return LTESignalStrength;
    }

    public void setLTESignalStrength(int LTESignalStrength) {
        this.LTESignalStrength = LTESignalStrength;
    }

    public int getCdmaSignalStrengthLevel() {
        return CdmaSignalStrengthLevel;
    }

    public void setCdmaSignalStrengthLevel(int cdmaSignalStrengthLevel) {
        CdmaSignalStrengthLevel = cdmaSignalStrengthLevel;
    }
}
