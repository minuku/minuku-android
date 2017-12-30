package labelingStudy.nctu.minuku.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by Lawrence on 2017/5/22.
 */

public class TransportationModeDAO implements DAO<TransportationModeDataRecord>{

    private String TAG = "TransportationModeDAO";
    private DBHelper dBHelper;

    private static TransportationModeDAO instance;


    public TransportationModeDAO(){}

    public TransportationModeDAO(Context applicationContext){



        dBHelper = DBHelper.getInstance(applicationContext);

    }

    public static TransportationModeDAO getInstance() {
        if(TransportationModeDAO.instance == null) {
            try {
//                TransportationModeDAO.instance = new TransportationModeDAO();
                Log.d("TransportationModeDAO","getInstance without context.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TransportationModeDAO.instance;
    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(TransportationModeDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding transportationMode data record.");

        ContentValues values = new ContentValues();

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            values.put(DBHelper.TIME, entity.getCreationTime());
//            values.put(DBHelper.TaskDayCount, entity.getTaskDayCount());
//            values.put(DBHelper.HOUR, entity.getHour());
            values.put(DBHelper.confirmTransportation_col, entity.getConfirmedActivityType());

            db.insert(DBHelper.transportationMode_table, null, values);
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
        Cursor transportationCursor = db.rawQuery("SELECT "+ DBHelper.confirmTransportation_col +" FROM "+ DBHelper.transportationMode_table, null);

        int transportationrow = transportationCursor.getCount();
        int transportationcol = transportationCursor.getColumnCount();

        Log.d(TAG,"transportationrow : " + transportationrow +" transportationcol : " + transportationcol);

    }

    @Override
    public void delete(TransportationModeDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<TransportationModeDataRecord>> getAll() throws DAOException {
        return null;
    }

    @Override
    public Future<List<TransportationModeDataRecord>> getLast(int N) throws DAOException {
        return null;
    }

    @Override
    public void update(TransportationModeDataRecord oldEntity, TransportationModeDataRecord newEntity) throws DAOException {

    }
}
