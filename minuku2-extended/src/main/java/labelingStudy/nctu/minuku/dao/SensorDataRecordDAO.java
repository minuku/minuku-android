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
import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by Lawrence on 2017/10/4.
 */

public class SensorDataRecordDAO implements DAO<SensorDataRecord> {

    private final String TAG = "SensorDataRecordDAO";

    private DBHelper dBHelper;
    private Context mContext;

    public SensorDataRecordDAO(Context applicationContext) {
        this.mContext = applicationContext;
        dBHelper = DBHelper.getInstance(applicationContext);
    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(SensorDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding Sensor data record.");

        ContentValues values = new ContentValues();
        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            values.put(DBHelper.TIME, entity.getCreationTime());
            values.put(DBHelper.ACCELEROMETER_col, entity.getmAccele_str());
            values.put(DBHelper.GYROSCOPE_col, entity.getmGyroscope_str());
            values.put(DBHelper.GRAVITY_col, entity.getmGravity_str());
            values.put(DBHelper.LINEAR_ACCELERATION_col, entity.getmLinearAcceleration_str());
            values.put(DBHelper.ROTATION_VECTOR_col, entity.getmRotationVector_str());
            values.put(DBHelper.PROXIMITY_col, entity.getmProximity_str());
            values.put(DBHelper.MAGNETIC_FIELD_col, entity.getmMagneticField_str());
            values.put(DBHelper.LIGHT_col, entity.getmLight_str());
            values.put(DBHelper.PRESSURE_col, entity.getmPressure_str());
            values.put(DBHelper.RELATIVE_HUMIDITY_col, entity.getmRelativeHumidity_str());
            values.put(DBHelper.AMBIENT_TEMPERATURE_col, entity.getmAmbientTemperature_str());

            db.insert(DBHelper.sensor_table, null, values);

        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            values.clear();
            DBManager.getInstance().closeDatabase();
        }
    }

    public void query_check() {
        SQLiteDatabase db = DBManager.getInstance().openDatabase();
        Cursor accCursor = db.rawQuery("SELECT "+ DBHelper.ACCELEROMETER_col +" FROM "+ DBHelper.sensor_table, null);

        int accrow = accCursor.getCount();
        int acccol = accCursor.getColumnCount();

        Log.d(TAG, "accrow "+accrow+" acccol "+acccol);
        String[] columns = new  String[]{"ACCELEROMETER"};
        Cursor c = db.query(DBHelper.sensor_table, columns, null, null, null, null, null, null);
        c.moveToFirst();
        Log.d(TAG, "ACCELEROMETER  "+c.getString(0));

    }

    @Override
    public void delete(SensorDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<SensorDataRecord>> getAll() throws DAOException {
        return null;
    }

    @Override
    public Future<List<SensorDataRecord>> getLast(int N) throws DAOException {
        return null;
    }

    @Override
    public void update(SensorDataRecord oldEntity, SensorDataRecord newEntity) throws DAOException {

    }
}
