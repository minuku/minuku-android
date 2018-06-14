package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;


/**
 * Created by Lawrence on 2017/5/22.
 */
@Dao
public interface ActivityRecognitionDataRecordDao  {

    @Query("SELECT * FROM ActivityRecognitionDataRecord")
    List<ActivityRecognitionDataRecord> getAll();

    @Insert
    void insertAll(ActivityRecognitionDataRecord activityRecognitionDataRecord);
}