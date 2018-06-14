package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;

/**
 * Created by chiaenchiang on 07/03/2018.
 */
@Dao
public interface SensorDataRecordDao {
    @Query("SELECT * FROM SensorDataRecord")
    List<SensorDataRecord> getAll();

    @Insert
    void insertAll(SensorDataRecord sensorDataRecord);
}
