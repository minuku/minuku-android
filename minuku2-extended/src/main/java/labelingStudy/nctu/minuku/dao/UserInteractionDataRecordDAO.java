package labelingStudy.nctu.minuku.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.Data.DBHelper;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.model.DataRecord.UserInteractionDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by Lawrence on 2018/8/29.
 */

public class UserInteractionDataRecordDAO implements DAO<UserInteractionDataRecord> {

    private String TAG = "UserInteractionDataRecordDAO";

    private Context mContext;

    public UserInteractionDataRecordDAO(Context applicationContext) {
        this.mContext = applicationContext;

    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(UserInteractionDataRecord entity) throws DAOException {

        Log.d(TAG, "Adding userInteraction data record.");

        ContentValues values = new ContentValues();

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            values.put(DBHelper.TIME, entity.getCreationTime());
            values.put(DBHelper.Present_col, entity.getPresent());
            values.put(DBHelper.Unlock_col, entity.getUnlock());
            values.put(DBHelper.Background_col, entity.getBackground());
            values.put(DBHelper.Foreground_col, entity.getForeground());

            db.insert(DBHelper.userInteraction_table, null, values);
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            values.clear();
            DBManager.getInstance().closeDatabase(); // Closing database connection
        }
    }

    @Override
    public void delete(UserInteractionDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<UserInteractionDataRecord>> getAll() throws DAOException {
        return null;
    }

    @Override
    public Future<List<UserInteractionDataRecord>> getLast(int N) throws DAOException {
        return null;
    }

    @Override
    public void update(UserInteractionDataRecord oldEntity, UserInteractionDataRecord newEntity) throws DAOException {

    }
}
