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
import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class RingerDataRecordDAO implements DAO<RingerDataRecord> {

    private final String TAG = "RingerDataRecordDAO";

    private DBHelper dBHelper;
    private Context mContext;

    public RingerDataRecordDAO(){}

    public RingerDataRecordDAO(Context applicationContext){
        this.mContext = applicationContext;

        dBHelper = DBHelper.getInstance(applicationContext);

    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(RingerDataRecord entity) throws DAOException {

        Log.d(TAG, "Adding Ringer data record.");

        ContentValues values = new ContentValues();

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            values.put(DBHelper.TIME, entity.getCreationTime());
//            values.put(DBHelper.TaskDayCount, entity.getTaskDayCount());
//            values.put(DBHelper.HOUR, entity.getHour());
            values.put(DBHelper.RingerMode_col, entity.getRingerMode());
            values.put(DBHelper.AudioMode_col, entity.getAudioMode());
            values.put(DBHelper.StreamVolumeMusic_col, entity.getStreamVolumeMusic());
            values.put(DBHelper.StreamVolumeNotification_col, entity.getStreamVolumeNotification());
            values.put(DBHelper.StreamVolumeRing_col, entity.getStreamVolumeRing());
            values.put(DBHelper.StreamVolumeVoicecall_col, entity.getStreamVolumeVoicecall());
            values.put(DBHelper.StreamVolumeSystem_col, entity.getStreamVolumeSystem());

            db.insert(DBHelper.ringer_table, null, values);
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
        Cursor RingerModeCursor = db.rawQuery("SELECT "+ DBHelper.RingerMode_col +" FROM "+ DBHelper.ringer_table, null);
        Cursor StreamVolumeMusicCursor = db.rawQuery("SELECT "+ DBHelper.StreamVolumeMusic_col +" FROM "+ DBHelper.ringer_table, null);
        Cursor StreamVolumeNotificationCursor = db.rawQuery("SELECT "+ DBHelper.StreamVolumeNotification_col +" FROM "+ DBHelper.ringer_table, null);
        Cursor StreamVolumeRingCursor = db.rawQuery("SELECT "+ DBHelper.StreamVolumeRing_col +" FROM "+ DBHelper.ringer_table, null);
        Cursor StreamVolumeVoicecallCursor = db.rawQuery("SELECT "+ DBHelper.StreamVolumeVoicecall_col +" FROM "+ DBHelper.ringer_table, null);
        Cursor StreamVolumeSystemCursor = db.rawQuery("SELECT "+ DBHelper.StreamVolumeSystem_col +" FROM "+ DBHelper.ringer_table, null);

        int RingerModerow = RingerModeCursor.getCount();
        int RingerModecol = RingerModeCursor.getColumnCount();
        int StreamVolumeMusicrow = StreamVolumeMusicCursor.getCount();
        int StreamVolumeMusiccol = StreamVolumeMusicCursor.getColumnCount();
        int StreamVolumeNotificationrow = StreamVolumeNotificationCursor.getCount();
        int StreamVolumeNotificationcol = StreamVolumeNotificationCursor.getColumnCount();
        int StreamVolumeRingrow = StreamVolumeRingCursor.getCount();
        int StreamVolumeRingcol = StreamVolumeRingCursor.getColumnCount();
        int StreamVolumeVoicecallrow = StreamVolumeVoicecallCursor.getCount();
        int StreamVolumeVoicecallcol = StreamVolumeVoicecallCursor.getColumnCount();
        int StreamVolumeSystemrow = StreamVolumeSystemCursor.getCount();
        int StreamVolumeSystemcol = StreamVolumeSystemCursor.getColumnCount();

        Log.d(TAG,"RingerModerow : " + RingerModerow +" RingerModecol : " + RingerModecol+ "StreamVolumeMusicrow"+ StreamVolumeMusicrow
                +" StreamVolumeMusiccol : " + StreamVolumeMusiccol+ " StreamVolumeNotificationrow : " + StreamVolumeNotificationrow
                +" StreamVolumeNotificationcol : " + StreamVolumeNotificationcol+" StreamVolumeRingrow : " + StreamVolumeRingrow
                +" StreamVolumeRingcol : " + StreamVolumeRingcol +" StreamVolumeVoicecallrow : " + StreamVolumeVoicecallrow
                +" StreamVolumeVoicecallcol : " + StreamVolumeVoicecallcol+" StreamVolumeSystemrow : " + StreamVolumeSystemrow
                +" StreamVolumeSystemcol : " + StreamVolumeSystemcol);
    }

    @Override
    public void delete(RingerDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<RingerDataRecord>> getAll() throws DAOException {
        return null;
    }

    @Override
    public Future<List<RingerDataRecord>> getLast(int N) throws DAOException {
        return null;
    }

    @Override
    public void update(RingerDataRecord oldEntity, RingerDataRecord newEntity) throws DAOException {

    }
}
