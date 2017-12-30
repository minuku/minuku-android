package labelingStudy.nctu.minuku.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class BatteryDataRecordDAO implements DAO<BatteryDataRecord> {

    private final String TAG = "BatteryDataRecordDAO";

    private DBHelper dBHelper;
    private Context mContext;

    public BatteryDataRecordDAO(Context applicationContext){
        this.mContext = applicationContext;

        dBHelper = DBHelper.getInstance(applicationContext);
    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(BatteryDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding Battery data record.");

        ContentValues values = new ContentValues();

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            values.put(DBHelper.TIME, entity.getCreationTime());
//            values.put(DBHelper.TaskDayCount, entity.getTaskDayCount());
//            values.put(DBHelper.HOUR, entity.getHour());
            values.put(DBHelper.BatteryLevel_col, entity.getBatteryLevel());
            values.put(DBHelper.BatteryPercentage_col, entity.getBatteryPercentage());
            values.put(DBHelper.BatteryChargingState_col, entity.getBatteryChargingState());
            values.put(DBHelper.isCharging_col, entity.getisCharging());

            db.insert(DBHelper.battery_table, null, values);
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            values.clear();
            DBManager.getInstance().closeDatabase(); // Closing database connection
        }
    }

    public void query_counting(){
        SQLiteDatabase db = DBManager.getInstance().openDatabase();
        Cursor BatteryLevelCursor = db.rawQuery("SELECT "+ DBHelper.BatteryLevel_col +" FROM "+ DBHelper.battery_table, null);
        Cursor BatteryPercentageCursor = db.rawQuery("SELECT "+ DBHelper.BatteryPercentage_col +" FROM "+ DBHelper.battery_table, null);
        Cursor BatteryChargingStateCursor = db.rawQuery("SELECT "+ DBHelper.BatteryChargingState_col +" FROM "+ DBHelper.battery_table, null);
        Cursor isChargingCursor = db.rawQuery("SELECT "+ DBHelper.isCharging_col +" FROM "+ DBHelper.battery_table, null);

        int BatteryLevelrow = BatteryLevelCursor.getCount();
        int BatteryLevelcol = BatteryLevelCursor.getColumnCount();
        int BatteryPercentagerow = BatteryPercentageCursor.getCount();
        int BatteryPercentagecol = BatteryPercentageCursor.getColumnCount();
        int BatteryChargingStaterow = BatteryChargingStateCursor.getCount();
        int BatteryChargingStatecol = BatteryChargingStateCursor.getColumnCount();
        int isChargingrow = isChargingCursor.getCount();
        int isChargingcol = isChargingCursor.getColumnCount();

        Log.d(TAG,"BatteryLevelrow : " + BatteryLevelrow +" BatteryLevelcol : " + BatteryLevelcol
                + "BatteryPercentagerow"+ BatteryPercentagerow +" BatteryPercentagecol : " + BatteryPercentagecol
                + " BatteryChargingStaterow : " + BatteryChargingStaterow +" BatteryChargingStatecol : " + BatteryChargingStatecol
                +" isChargingrow : " + isChargingrow +" isChargingcol : " + isChargingcol);
    }

    @Override
    public void delete(BatteryDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<BatteryDataRecord>> getAll() throws DAOException {
        return null;
    }

    @Override
    public Future<List<BatteryDataRecord>> getLast(int N) throws DAOException {
        return null;
    }

    @Override
    public void update(BatteryDataRecord oldEntity, BatteryDataRecord newEntity) throws DAOException {

    }
}
