package labelingStudy.nctu.minuku.Data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import labelingStudy.nctu.minuku.dao.AccessibilityDataRecordDAO;
import labelingStudy.nctu.minuku.dao.ActivityRecognitionDataRecordDAO;
import labelingStudy.nctu.minuku.dao.AppUsageDataRecordDAO;
import labelingStudy.nctu.minuku.dao.BatteryDataRecordDAO;
import labelingStudy.nctu.minuku.dao.ConnectivityDataRecordDAO;
import labelingStudy.nctu.minuku.dao.LocationDataRecordDAO;
import labelingStudy.nctu.minuku.dao.NotificationDataRecordDAO;
import labelingStudy.nctu.minuku.dao.RingerDataRecordDAO;
import labelingStudy.nctu.minuku.dao.SensorDataRecordDAO;
import labelingStudy.nctu.minuku.dao.TelephonyDataRecordDAO;
import labelingStudy.nctu.minuku.dao.TransportationModeDataRecordDAO;
import labelingStudy.nctu.minuku.dao.UserInteractionDataRecordDAO;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.AppUsageDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserInteractionDataRecord;

@Database(entities = {SensorDataRecord.class, AccessibilityDataRecord.class,
        BatteryDataRecord.class, ActivityRecognitionDataRecord.class,
        AppUsageDataRecord.class, RingerDataRecord.class,
        TelephonyDataRecord.class, ConnectivityDataRecord.class,
        LocationDataRecord.class, TransportationModeDataRecord.class,
        NotificationDataRecord.class, UserInteractionDataRecord.class
        },version =1)
public abstract class appDatabase extends RoomDatabase {

    public abstract AccessibilityDataRecordDAO accessibilityDataRecordDao();
    public abstract ActivityRecognitionDataRecordDAO activityRecognitionDataRecordDao();
    public abstract AppUsageDataRecordDAO appUsageDataRecordDao();
    public abstract BatteryDataRecordDAO batteryDataRecordDao();
    public abstract ConnectivityDataRecordDAO connectivityDataRecordDao();
//    public abstract ImageDataRecordDAO imageDataRecordDao();
    public abstract LocationDataRecordDAO locationDataRecordDao();
//    public abstract LocationNoGoogleDataRecordDAO locationNoGoogleDataRecordDao();
    public abstract RingerDataRecordDAO ringerDataRecordDao();
    public abstract SensorDataRecordDAO sensorDataRecordDao();
    public abstract TelephonyDataRecordDAO telephonyDataRecordDao();
    public abstract TransportationModeDataRecordDAO transportationModeDataRecordDao();
    public abstract NotificationDataRecordDAO notificationDataRecordDao();
    public abstract UserInteractionDataRecordDAO userInteractionDataRecordDao();
}