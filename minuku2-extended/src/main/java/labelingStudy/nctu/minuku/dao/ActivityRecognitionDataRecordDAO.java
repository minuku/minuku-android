package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface ActivityRecognitionDataRecordDAO {
    @Query("SELECT * FROM ActivityRecognitionDataRecord")
    List<ActivityRecognitionDataRecord> getAll();

    @Insert
    void insertAll(ActivityRecognitionDataRecord activityRecognitionDataRecord);
}
