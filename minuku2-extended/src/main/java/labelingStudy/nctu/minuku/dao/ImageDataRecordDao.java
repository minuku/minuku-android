package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.ImageDataRecord;

/**
 * Created by tingwei on 2018/3/28.
 */

@Dao
public interface ImageDataRecordDao {

    @Query("SELECT * FROM ImageDataRecord")
    List<ImageDataRecord> getAll();

    @Query("SELECT * FROM ImageDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<ImageDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(ImageDataRecord imageDataRecord);

}

