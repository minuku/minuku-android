package labelingStudy.nctu.minuku.DBHelper;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import labelingStudy.nctu.minuku.dao.AccessibilityDataRecordDAO;
import labelingStudy.nctu.minuku.dao.ActivityRecognitionDataRecordDao;
import labelingStudy.nctu.minuku.dao.AppUsageDataRecordDao;
import labelingStudy.nctu.minuku.dao.BatteryDataRecordDao;
import labelingStudy.nctu.minuku.dao.ConnectivityDataRecordDao;
import labelingStudy.nctu.minuku.dao.ImageDataRecordDao;
import labelingStudy.nctu.minuku.dao.LocationDataRecordDao;
import labelingStudy.nctu.minuku.dao.LocationNoGoogleDataRecordDao;
import labelingStudy.nctu.minuku.dao.RingerDataRecordDao;
import labelingStudy.nctu.minuku.dao.SensorDataRecordDao;
import labelingStudy.nctu.minuku.dao.TelephonyDataRecordDao;
import labelingStudy.nctu.minuku.dao.TransportationModeDataRecordDao;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.AppUsageDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ImageDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.LocationNoGoogleDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;

/**
 * Created by chiaenchiang on 07/03/2018.
 */
@Database(entities = {SensorDataRecord.class, AccessibilityDataRecord.class,
        BatteryDataRecord.class, ActivityRecognitionDataRecord.class,
        AppUsageDataRecord.class, RingerDataRecord.class,
        TelephonyDataRecord.class, ConnectivityDataRecord.class,
        LocationDataRecord.class, ImageDataRecord.class,
        TransportationModeDataRecord.class, LocationNoGoogleDataRecord.class},version =1)
public abstract class appDatabase extends RoomDatabase {

    public abstract AccessibilityDataRecordDAO accessibilityDataRecordDao();
    public abstract ActivityRecognitionDataRecordDao activityRecognitionDataRecordDao();
    public abstract AppUsageDataRecordDao appUsageDataRecordDao();
    public abstract BatteryDataRecordDao batteryDataRecordDao();
    public abstract ConnectivityDataRecordDao connectivityDataRecordDao();
    public abstract ImageDataRecordDao imageDataRecordDao();
    public abstract LocationDataRecordDao locationDataRecordDao();
    public abstract LocationNoGoogleDataRecordDao locationNoGoogleDataRecordDao();
    public abstract RingerDataRecordDao ringerDataRecordDao();
    public abstract SensorDataRecordDao sensorDataRecordDao();
    public abstract TelephonyDataRecordDao telephonyDataRecordDao();
    public abstract TransportationModeDataRecordDao transportationModeDataRecordDao();
//    public abstract UserSubmissionStatsDao userSubmissionStatsDao();
}
