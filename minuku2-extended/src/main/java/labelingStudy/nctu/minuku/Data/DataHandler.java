package labelingStudy.nctu.minuku.Data;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.AnnotationSet;

/**
 * Created by Lawrence on 2018/3/13.
 */

public class DataHandler {

    private static final DataHandler ourInstance = new DataHandler();

    public static DataHandler getInstance() {
        return ourInstance;
    }

    private static final String TAG = "DataHandler";
    private static Context mContext;

    private DataHandler() {
    }

    public static ArrayList<String> getDataBySession(int sessionId, String sourceName) {

        Log.d(TAG, "[test show trip] getDataBySession, seession id" + sessionId + " table name " + DBHelper.location_table);
        ArrayList<String> resultList = new ArrayList<String>();

        //first know which table and column to query..
        String tableName = sourceName;
        Log.d(TAG, "[test show trip] before queryRecordinSEssion");
        resultList = DBHelper.queryRecordsInSession(tableName, sessionId);
        Log.d(TAG, "[test show trip] got " + resultList.size() + " of results from queryRecordsInSession");

        return resultList;
    }


    public static ArrayList<String> getDataBySession(int sessionId, String sourceName, long startTime, long endTime) {

        //for each record type get data
        ArrayList<String> resultList = new ArrayList<String>();

        //first know which table and column to query..
        String tableName= sourceName;

        //get data from the table
        if (tableName !=null) {
            resultList = DBHelper.queryRecordsInSession(tableName, sessionId, startTime, endTime);
        }

        return resultList;
    }

    public static long getTimeOfLastSavedRecordInSession(int sessionId, String sourceName) {

        ArrayList<String> res = new ArrayList<String>();

        long latestTime = 0;

        res = DBHelper.queryLastRecordBySession(
                sourceName,
                sessionId);

        if (res!=null && res.size()>0){

            String lastRecord = res.get(0);
            long time = Long.parseLong(lastRecord.split(Constants.DELIMITER)[DBHelper.COL_INDEX_RECORD_TIMESTAMP_LONG] );

            //compare time
            if (time>latestTime){
                latestTime = time;
//                Log.d(TAG, "[test show trip] update, latest time is changed to  " + ScheduleAndSampleManager.getTimeString(time));

            }
        }

//        Log.d(TAG, "[test show trip]return the latest time: " + ScheduleAndSampleManager.getTimeString(latestTime));

        return latestTime;

    }

    public static void updateSession(int id, long startTime, long endTime, AnnotationSet annotationSet, int toBeSent){

        Log.d(TAG, "updateSession");
        DBHelper.updateSessionTable(id, startTime, endTime, annotationSet, toBeSent);
    }

    public static void updateSession(int id, AnnotationSet annotationSet){

        Log.d(TAG, "[storing sitename] Sitename going to store session : "+ id);

        Log.d(TAG, "updateSession");
        DBHelper.updateSessionTable(id, annotationSet);
    }

    public static void updateSession(int id, int toBeSent){

        Log.d(TAG, "[storing sitename] Sitename going to store session : "+ id);

        Log.d(TAG, "updateSession");
        DBHelper.updateSessionTable(id, toBeSent);
    }

    public static void updateSession(int id, long endTime){

        Log.d(TAG, "[storing sitename] Sitename going to store session : "+ id);

        Log.d(TAG, "updateSession");
        DBHelper.updateSessionTable(id, endTime);
    }

}
