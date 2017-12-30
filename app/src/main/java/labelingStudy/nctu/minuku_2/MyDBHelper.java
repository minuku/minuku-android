package labelingStudy.nctu.minuku_2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jimmy on 2017/10/31.
 */

public class MyDBHelper extends SQLiteOpenHelper {
    final private static int _DB_VERSION = 1;
    final private static String _DB_DATABASE_NAME = "MyDatabases.db";
    private static SQLiteDatabase database;

    public MyDBHelper(Context context) {
        super(context,_DB_DATABASE_NAME,null,_DB_VERSION);
    }

    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // 需要資料庫的元件呼叫這個方法，這個方法在一般的應用都不需要修改
    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new MyDBHelper(context, _DB_DATABASE_NAME,
                    null, _DB_VERSION).getWritableDatabase();
        }

        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Minuku " +
                "(_id INTEGER PRIMARY KEY  NOT NULL , " +
                "_Data VARCHAR(50) " + ")"
                 );
        db.execSQL("CREATE TABLE Timeline " +
                "(_id INTEGER PRIMARY KEY  NOT NULL , " +
                "_Time VARCHAR(50) , " +
                "_Activity VARCHAR(50) , " +
                "_Annotation VARCHAR(100) " +")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Minuku");
        db.execSQL("DROP TABLE IF EXISTS Timeline");
        onCreate(db);
    }
}
