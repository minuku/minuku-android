package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;

/**
 * Created by chiaenchiang on 08/03/2018.
 */
@Dao
public interface AccessibilityDataRecordDAO {
    @Query("SELECT * FROM AccessibilityDataRecord")
    List<AccessibilityDataRecord> getAll();

    @Insert
    void insertAll(AccessibilityDataRecord accessibilityDataRecord);
}

