package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.AppUsageDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface AppUsageDataRecordDAO {
    @Query("SELECT * FROM AppUsageDataRecord")
    List<AppUsageDataRecord> getAll();

    @Insert
    void insertAll(AppUsageDataRecord appUsageDataRecord);
}
