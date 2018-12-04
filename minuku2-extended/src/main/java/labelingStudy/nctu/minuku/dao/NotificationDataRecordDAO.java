package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;


/**
 * Created by chiaenchiang on 27/10/2018.
 */
@Dao
public interface NotificationDataRecordDAO {
    @Query("SELECT * FROM NotificationDataRecord")
    List<NotificationDataRecord> getAll();

    @Query("SELECT * FROM NotificationDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(NotificationDataRecord notificationDataRecord);


}
