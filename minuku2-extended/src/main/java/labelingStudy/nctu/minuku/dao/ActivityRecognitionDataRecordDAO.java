package labelingStudy.nctu.minuku.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by Lawrence on 2017/5/22.
 */

public class ActivityRecognitionDataRecordDAO implements DAO<ActivityRecognitionDataRecord> {

    final private String TAG = "ActivityRecognitionDataRecordDAO";

    File file;
    BufferedWriter fw;
    JSONObject obj;
    private DBHelper dBHelper;


    public ActivityRecognitionDataRecordDAO(){

    }

    public ActivityRecognitionDataRecordDAO(Context applicationContext){

        dBHelper = DBHelper.getInstance(applicationContext);

    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(ActivityRecognitionDataRecord entity) throws DAOException {

        Log.d(TAG, "Adding ActivityRecognition data record.");
        //TODO store in CSV file.

        ContentValues values = new ContentValues();

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            values.put(DBHelper.TIME, entity.getCreationTime());
//            values.put(DBHelper.TaskDayCount, entity.getTaskDayCount());
//            values.put(DBHelper.HOUR, entity.getHour());
            values.put(DBHelper.MostProbableActivity_col, entity.getMostProbableActivity().toString());
            values.put(DBHelper.ProbableActivities_col, entity.getProbableActivities().toString());

            db.insert(DBHelper.activityRecognition_table, null, values);
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
        Cursor MostProbableActivityCursor = db.rawQuery("SELECT "+ DBHelper.MostProbableActivity_col +" FROM "+ DBHelper.activityRecognition_table, null);
        Cursor ProbableActivitiesCursor = db.rawQuery("SELECT "+ DBHelper.ProbableActivities_col +" FROM "+ DBHelper.activityRecognition_table, null);

        int MostProbableActivityrow = MostProbableActivityCursor.getCount();
        int MostProbableActivitycol = MostProbableActivityCursor.getColumnCount();
        int ProbableActivitiesrow = ProbableActivitiesCursor.getCount();
        int ProbableActivitiescol = ProbableActivitiesCursor.getColumnCount();

        Log.d(TAG,"MostProbableActivityrow : " + MostProbableActivityrow +" MostProbableActivitycol : " + MostProbableActivitycol+
                " ProbableActivitiesrow : " + ProbableActivitiesrow+ " ProbableActivitiescol : " + ProbableActivitiescol);
    }

    @Override
    public void delete(ActivityRecognitionDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<ActivityRecognitionDataRecord>> getAll() throws DAOException {
        return null;
    }

    @Override
    public Future<List<ActivityRecognitionDataRecord>> getLast(int N) throws DAOException {
        return null;
    }

    @Override
    public void update(ActivityRecognitionDataRecord oldEntity, ActivityRecognitionDataRecord newEntity) throws DAOException {

    }
}
