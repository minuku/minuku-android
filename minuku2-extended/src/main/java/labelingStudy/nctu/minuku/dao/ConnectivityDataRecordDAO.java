package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;

/**
 * Created by tingwei on 2018/3/27.
 */
@Dao
public interface ConnectivityDataRecordDao {

    @Query("SELECT * FROM ConnectivityDataRecord")
    List<ConnectivityDataRecord> getAll();

    @Query("SELECT * FROM ConnectivityDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<ConnectivityDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(ConnectivityDataRecord connectivityDataRecord);

}
