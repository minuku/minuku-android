package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord;

/**
 * Created by tingwei on 2018/3/26.
 */

@Dao
public interface TelephonyDataRecordDao {
    @Query("SELECT * FROM TelephonyDataRecord")
    List<TelephonyDataRecord> getAll();

    @Query("SELECT * FROM TelephonyDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<TelephonyDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(TelephonyDataRecord telephonyDataRecord);
}