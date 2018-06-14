package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;

/**
 * Created by tingwei on 2018/3/28.
 */

@Dao
public interface LocationDataRecordDao {

    @Query("SELECT * FROM LocationDataRecord")
    List<LocationDataRecord> getAll();

    @Query("SELECT * FROM LocationDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<LocationDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(LocationDataRecord locationDataRecord);

}

