package labelingStudy.nctu.minuku.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.Data.DBHelper;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by Lawrence on 2017/9/6.
 */

public class AccessibilityDataRecordDAO implements DAO<AccessibilityDataRecord> {
    private final String TAG = "AccessibilityDataRecordDAO";

    private DBHelper dBHelper;
    private Context mContext;

    public AccessibilityDataRecordDAO(Context applicationContext){
        this.mContext = applicationContext;
        dBHelper = DBHelper.getInstance(applicationContext);
    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(AccessibilityDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding accessibility data record.");

        ContentValues values = new ContentValues();

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            values.put(DBHelper.TIME, entity.getCreationTime());

            values.put(DBHelper.pack_col, entity.getPack());
            values.put(DBHelper.text_col, entity.getText());
            values.put(DBHelper.type_col, entity.getType());
            values.put(DBHelper.extra_col, entity.getExtra());

            db.insert(DBHelper.accessibility_table, null, values);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            values.clear();
            DBManager.getInstance().closeDatabase();
        }
    }

    public void query_check() {
        SQLiteDatabase db = DBManager.getInstance().openDatabase();
        Cursor packCursor = db.rawQuery("SELECT "+ DBHelper.pack_col +" FROM "+ DBHelper.accessibility_table, null);

        int packrow = packCursor.getCount();
        int packcol = packCursor.getColumnCount();

        Log.d(TAG, "packrow "+packrow+"packcol "+packcol);
        String[] columns = new  String[]{"pack"};
        Cursor c = db.query(DBHelper.accessibility_table, columns, null, null, null, null, null, null);
        c.moveToFirst();
        Log.d(TAG, "pack  "+c.getString(0));
    }

    @Override
    public void delete(AccessibilityDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<AccessibilityDataRecord>> getAll() throws DAOException {
        return null;
    }

    @Override
    public Future<List<AccessibilityDataRecord>> getLast(int N) throws DAOException {
        return null;
    }

    @Override
    public void update(AccessibilityDataRecord oldEntity, AccessibilityDataRecord newEntity) throws DAOException {

    }
}

