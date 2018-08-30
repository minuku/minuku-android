package labelingStudy.nctu.minuku.manager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

import labelingStudy.nctu.minuku.Data.DBHelper;


/**
 * Created by Lawrence on 2017/6/6.
 */

public class DBManager  {

    private AtomicInteger mOpenDBCounter = new AtomicInteger();

    private static DBManager instance;
    private static DBHelper mLocalDBHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(DBHelper helper) {
        if (instance == null) {
            instance = new DBManager();
            mLocalDBHelper = helper;
//            Log.d(TAG, "[test instantiate db]  after instanstiate the database");
        }

    }

    public static synchronized DBManager getInstance() {
        if (instance == null) {
            //initializeInstance(mLocalDBHelper);
            throw new IllegalStateException(DBManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");

        }

        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        //     Log.d("test opendatabase", "test opendatabase mDAtabase is" + mOpenDBCounter.incrementAndGet()) ;

        if(mOpenDBCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mLocalDBHelper.getWritableDatabase();
            Log.d("test opendatabase", "test opendatabase mDAtabase is" + mDatabase);

        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        //      Log.d("test opendatabase", "test opendatabase mDAtabase is" + mOpenDBCounter.decrementAndGet()) ;
        if(mOpenDBCounter.decrementAndGet() == 0) {
            // Closing database
            if (mDatabase!=null)
                mDatabase.close();

        }
    }
}
