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
import labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by Lawrence on 2017/9/19.
 */

public class TelephonyDataRecordDAO implements DAO<TelephonyDataRecord> {
    private final String TAG = "TelephonyDataRecordDAO";

    private DBHelper dBHelper;
    private Context mContext;


    public TelephonyDataRecordDAO(Context applicationContext) {
        this.mContext = applicationContext;
        dBHelper = DBHelper.getInstance(applicationContext);
    }
    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(TelephonyDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding Telephony data record.");

        ContentValues values = new ContentValues();

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            values.put(DBHelper.TIME, entity.getCreationTime());
            //values.put(DBHelper.TaskDayCount, entity.getTaskDayCount());
            //values.put(DBHelper.HOUR, entity.getHour());
            values.put(DBHelper.NetworkOperatorName_col, entity.getNetworkOperatorName());
            values.put(DBHelper.CallState_col, entity.getCallState());
            values.put(DBHelper.PhoneSignalType_col, entity.getPhoneSignalType());
            values.put(DBHelper.GsmSignalStrength_col, entity.getGsmSignalStrength());
            values.put(DBHelper.LTESignalStrength_col, entity.getLTESignalStrength());
            values.put(DBHelper.CdmaSignalStrengthLevel_col, entity.getCdmaSignalStrengthLevel());
            values.put(DBHelper.COL_SESSION_ID, entity.getSessionid());

            //db.insert(DBHelper.telephony_table, null, values);
            db.insertWithOnConflict(DBHelper.telephony_table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            //Log.d(TAG, "ADD"+DBHelper.id+"");
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            values.clear();
            // Closing database connection
            DBManager.getInstance().closeDatabase();
        }
    }

    public void query_check() {
        SQLiteDatabase db = DBManager.getInstance().openDatabase();
        String[] columns = new  String[]{"NetworkOperatorName"};
        Cursor c = db.query(DBHelper.telephony_table, columns, null, null, null, null, null, null);
        //Cursor c = db.rawQuery("select count(*) from "+DBHelper.telephony_table, null);
        /*Cursor NetworkOperatorNameCursor = db.rawQuery("SELECT '"+ DBHelper.NetworkOperatorName_col+"'FROM "+DBHelper.telephony_table, null);
        Cursor CallStateCursor = db.rawQuery("SELECT '"+ DBHelper.CallState_col+"'FROM "+DBHelper.telephony_table, null);
        Cursor PhoneSignalTypeCursor = db.rawQuery("SELECT '"+ DBHelper.PhoneSignalType_col+"'FROM "+DBHelper.telephony_table, null);
        Cursor GsmSignalStrengthCursor = db.rawQuery("SELECT '"+ DBHelper.GsmSignalStrength_col+"'FROM "+DBHelper.telephony_table, null);
        Cursor LTESignalStrengthCursor = db.rawQuery("SELECT '"+ DBHelper.LTESignalStrength_col+"'FROM "+DBHelper.telephony_table, null);
        Cursor CdmaSignalStrenthLevelCursor = db.rawQuery("SELECT '"+ DBHelper.CdmaSignalStrengthLevel_col+"'FROM "+DBHelper.telephony_table, null);*/

        c.moveToFirst();
        //NetworkOperatorNameCursor.moveToNext();
        Log.d(TAG, "Operator name  "+c.getString(0));
    }

    @Override
    public void delete(TelephonyDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<TelephonyDataRecord>> getAll() throws DAOException {
        return null;
    }

    @Override
    public Future<List<TelephonyDataRecord>> getLast(int N) throws DAOException {
        return null;
    }

    @Override
    public void update(TelephonyDataRecord oldEntity, TelephonyDataRecord newEntity) throws DAOException {

    }
}
