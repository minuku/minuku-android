package labelingStudy.nctu.minuku.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.UserInteractionDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface UserInteractionDataRecordDAO {
    @Query("SELECT * FROM UserInteractionDataRecord")
    List<UserInteractionDataRecord> getAll();

    @Insert
    void insertAll(UserInteractionDataRecord userInteractionDataRecord);
}
