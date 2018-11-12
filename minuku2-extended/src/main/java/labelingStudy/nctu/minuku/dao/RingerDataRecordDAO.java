package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface RingerDataRecordDAO {
    @Query("SELECT * FROM RingerDataRecord")
    List<RingerDataRecord> getAll();

    @Query("SELECT * FROM RingerDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<RingerDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(RingerDataRecord ringerDataRecord);
}
