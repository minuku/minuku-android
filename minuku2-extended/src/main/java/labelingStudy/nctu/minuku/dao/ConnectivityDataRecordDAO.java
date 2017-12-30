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
import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class ConnectivityDataRecordDAO implements DAO<ConnectivityDataRecord>{

    private final String TAG = "ConnectivityDataRecordDAO";

    private DBHelper dBHelper;
    private Context mContext;

    public ConnectivityDataRecordDAO(Context applicationContext){
        this.mContext = applicationContext;

        dBHelper = DBHelper.getInstance(applicationContext);
    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(ConnectivityDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding Connectivity data record.");

        ContentValues values = new ContentValues();

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            values.put(DBHelper.TIME, entity.getCreationTime());
//            values.put(DBHelper.TaskDayCount, entity.getTaskDayCount());
//            values.put(DBHelper.HOUR, entity.getHour());
            values.put(DBHelper.NetworkType_col, entity.getNetworkType());
            values.put(DBHelper.IsNetworkAvailable_col, entity.getIsNetworkAvailable());
            values.put(DBHelper.IsConnected_col, entity.getIsConnected());
            values.put(DBHelper.IsWifiAvailable_col, entity.getIsWifiAvailable());
            values.put(DBHelper.IsMobileAvailable_col, entity.getIsMobileAvailable());
            values.put(DBHelper.IsWifiConnected_col, entity.getIsWifiConnected());
            values.put(DBHelper.IsMobileConnected_col, entity.getIsMobileConnected());

            db.insert(DBHelper.connectivity_table, null, values);
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
        Cursor NetworkTypeCursor = db.rawQuery("SELECT "+ DBHelper.NetworkType_col +" FROM "+ DBHelper.connectivity_table, null);
        Cursor IsNetworkAvailableCursor = db.rawQuery("SELECT "+ DBHelper.IsNetworkAvailable_col +" FROM "+ DBHelper.connectivity_table, null);
        Cursor IsConnectedCursor = db.rawQuery("SELECT "+ DBHelper.IsConnected_col +" FROM "+ DBHelper.connectivity_table, null);
        Cursor IsWifiAvailableCursor = db.rawQuery("SELECT "+ DBHelper.IsWifiAvailable_col +" FROM "+ DBHelper.connectivity_table, null);
        Cursor IsMobileAvailableCursor = db.rawQuery("SELECT "+ DBHelper.IsMobileAvailable_col +" FROM "+ DBHelper.connectivity_table, null);
        Cursor IsWifiConnectedCursor = db.rawQuery("SELECT "+ DBHelper.IsWifiConnected_col +" FROM "+ DBHelper.connectivity_table, null);
        Cursor IsMobileConnectedCursor = db.rawQuery("SELECT "+ DBHelper.IsMobileConnected_col +" FROM "+ DBHelper.connectivity_table, null);

        int NetworkTyperow = NetworkTypeCursor.getCount();
        int IsNetworkAvailablerow = IsNetworkAvailableCursor.getCount();
        int IsConnectedrow = IsConnectedCursor.getCount();
        int IsWifiAvailablerow = IsWifiAvailableCursor.getCount();
        int IsMobileAvailablerow = IsMobileAvailableCursor.getCount();
        int IsWifiConnectedrow = IsWifiConnectedCursor.getCount();
        int IsMobileConnectedrow = IsMobileConnectedCursor.getCount();

        Log.d(TAG,"NetworkTyperow : " + NetworkTyperow +" IsNetworkAvailablerow : " + IsNetworkAvailablerow
                + "IsConnectedrow"+ IsConnectedrow +" IsWifiAvailablerow : " + IsWifiAvailablerow
                + " IsMobileAvailablerow : " + IsMobileAvailablerow +" IsWifiConnectedrow : "
                + IsWifiConnectedrow+" IsMobileConnectedrow : " + IsMobileConnectedrow);
    }

    @Override
    public void delete(ConnectivityDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<ConnectivityDataRecord>> getAll() throws DAOException {
        return null;
    }

    @Override
    public Future<List<ConnectivityDataRecord>> getLast(int N) throws DAOException {
        return null;
    }

    @Override
    public void update(ConnectivityDataRecord oldEntity, ConnectivityDataRecord newEntity) throws DAOException {

    }
}
