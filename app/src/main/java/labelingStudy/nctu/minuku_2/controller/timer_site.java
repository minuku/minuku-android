package labelingStudy.nctu.minuku_2.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.R;

//import edu.ohio.minuku_2.R;


/**
 * Created by Lawrence on 2017/4/22.
 */

public class timer_site extends AppCompatActivity {

    final private String TAG = "timer_site";

    ImageButton site_1,site_2,Customize;
    private Button move;
    private ListView listview;
    public static ArrayList<String> data;

    public static int dataSize;
    private SharedPreferences sharedPrefs;

    public timer_site(){}

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_site);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE);

        dataSize = sharedPrefs.getInt("dataSize", 1);

        Log.d(TAG, " dataSize : "+ dataSize);

//        // Init
//        MyDBHelper mHelper = new MyDBHelper(timer_site.this);
//        SQLiteDatabase mDB = null;
//        // Insert by raw SQL
//        mDB = mHelper.getWritableDatabase();
//        Cursor cursor = mDB.rawQuery("SELECT _Data FROM Minuku", null);
//        cursor.moveToFirst();
//        while(!cursor.isAfterLast()) {
//            Log.e("Site_SQLiteDBTestingActivity","_Data = "+cursor.getString(0));
////            data.add(cursor.getString(1));
//            cursor.moveToNext();
//        }
//        cursor.close();
//
//        for(int index = 0 ; index < dataSize ; index ++) {
//            String dataContent = sharedPrefs.getString("dataContent" + index, "新增地點");
//            data.add(dataContent);
//
//            Log.d(TAG ,"data : "+ dataContent);
//
//        }
//
//        Log.d(TAG ,"data : "+ data);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG,"onResume");

        inittimer_site();
    }

    public void inittimer_site(){

        listview = (ListView) findViewById(R.id.customizeList);

        data = new ArrayList<String>();

        data.add("新增地點");

        //output their custom sites from sqlite
        SQLiteDatabase db = DBManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery("SELECT "+ DBHelper.customsitename_col +" FROM "+ DBHelper.customsite_table, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Log.e(TAG,"sitename : "+cursor.getString(0));
            data.add(cursor.getString(0));
            cursor.moveToNext();
        }

        Log.d(TAG, "data : "+ data);

        Timer_site_ListAdapter timer_site_ListAdapter = new Timer_site_ListAdapter(
                this,
                R.id.recording_list,
                data
        );

        listview.setAdapter(timer_site_ListAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(position == 0)
                    startActivity(new Intent(timer_site.this, PlaceSelection.class));
                else{
                    startActivity(new Intent(timer_site.this, MainActivity.class));
                    timer_site.this.finish();
                }
            }
        });

//        site_1 = (ImageButton) findViewById(R.id.site_1);
//        site_2 = (ImageButton) findViewById(R.id.site_2);
//        Customize = (ImageButton) findViewById(R.id.customize);

//        move = (Button) findViewById(R.id.move);

//        move.setOnClickListener(moving);

//        Customize.setOnClickListener(Customizing);
    }

//    private ImageButton.OnClickListener Customizing = new ImageButton.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            startActivity(new Intent(timer_site.this, PlaceSelection.class));
//
//            timer_site.this.finish();
//        }
//    };

    //to view timer_move
//    private Button.OnClickListener moving = new Button.OnClickListener() {
//        public void onClick(View v) {
//        Log.e(LOG_TAG,"move clicked");
//
//        //TODO this function will increase in stack, need to be optimized.
//        startActivity(new Intent(timer_site.this, timer_move.class));
//        timer_site.this.finish(); //TODO maybe would add it back
//        }
//    };

}
