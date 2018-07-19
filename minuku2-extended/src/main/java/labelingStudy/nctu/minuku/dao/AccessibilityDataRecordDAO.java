package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
/**
 * Created by tingwei on 2018/7/19.
 */

@Dao
public interface AccessibilityDataRecordDao {
    @Query("SELECT * FROM AccessibilityDataRecord")
    List<AccessibilityDataRecord> getAll();

    @Insert
    void insertAll(AccessibilityDataRecord accessibilityDataRecord);
}


