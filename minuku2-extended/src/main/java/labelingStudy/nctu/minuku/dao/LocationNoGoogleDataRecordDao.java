package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.LocationNoGoogleDataRecord;

/**
 * Created by tingwei on 2018/3/28.
 */

@Dao
public interface LocationNoGoogleDataRecordDao {

    @Query("SELECT * FROM LocationNoGoogleDataRecord")
    List<LocationNoGoogleDataRecord> getAll();

    @Query("SELECT * FROM LocationNoGoogleDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<LocationNoGoogleDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(LocationNoGoogleDataRecord locationNoGoogleDataRecord);

}

