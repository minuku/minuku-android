package labelingStudy.nctu.minuku_2.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.manager.TripManager;
import labelingStudy.nctu.minuku_2.MyDBHelper;
import labelingStudy.nctu.minuku_2.NearbyPlaces.GetUrl;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.controller.home.duration;
import static labelingStudy.nctu.minuku_2.controller.home.recordflag;
import static labelingStudy.nctu.minuku_2.controller.home.result;
import static labelingStudy.nctu.minuku_2.controller.timer_move.TrafficFlag;


public class Timeline extends AppCompatActivity {

    public static ArrayList<String> myTimeDataset = new ArrayList<>();
    public static ArrayList<String> myActivityDataset = new ArrayList<>();
    public static ArrayList<String> myTrafficDataset = new ArrayList<>();
    public static ArrayList<String> myAnnotationDataset = new ArrayList<>();

    public static String selectedSiteName = "請選擇地點";

    public static Button DchoosingSite = null;

    String TAG="Timeline";
    Context mContext;

    private View recordview;

    private String current_task;


    private RecyclerView listview;
    private int Trip_size;

    ArrayList<String> mlocationDataRecords;

    public Timeline(){}
    public Timeline(Context mContext){
        this.mContext = mContext;
    }

    View item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);


    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG,"onResume");

        initTime(recordview);
    }

//    public void initTime(){
//
//        String current_task = mContext.getResources().getString(R.string.current_task);
//
//        if(current_task.equals("PART")) {
//            setDataListItems();
//            Log.d(TAG, "myTimeDataset : "+ myTimeDataset);
//            Log.d(TAG, "myActivityDataset : "+ myActivityDataset);
//            Log.d(TAG, "myTrafficDataset : "+ myTrafficDataset);
//            Log.d(TAG, "myAnnotationDataset : "+ myAnnotationDataset);
//
//            MyAdapter myAdapter = new MyAdapter(myTimeDataset, myActivityDataset,myTrafficDataset,myAnnotationDataset);
//            RecyclerView mList = (RecyclerView) findViewById(R.id.list_view);
//            //initialize RecyclerView
////            final View vitem = LayoutInflater.from(Timeline.this).inflate(R.layout.item_dialog, null);
////            item  = vitem;
//            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//
//            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//            mList.setLayoutManager(layoutManager);
//            mList.setAdapter(myAdapter);
//        }else{
//            ArrayList<String> locationDataRecords = null;
//
//            listview = (RecyclerView) findViewById(R.id.list_view);
////            listview.setEmptyView(findViewById(R.id.emptyView));
//
//            try{
//
//                Log.d(TAG,"ListRecordAsyncTask");
//
////            locationDataRecords = new ListRecordAsyncTask().execute(mReviewMode).get();
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
//                    locationDataRecords = new ListRecordAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
//                else
//                    locationDataRecords = new ListRecordAsyncTask().execute().get();
//
//                setDataListItems();
//                MyAdapter myAdapter = new MyAdapter(locationDataRecords, myActivityDataset,myTrafficDataset,myAnnotationDataset);
//                RecyclerView mList = (RecyclerView) findViewById(R.id.list_view);
//
//                final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//
//                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//                mList.setLayoutManager(layoutManager);
//                mList.setAdapter(myAdapter);
//
//                Log.d(TAG,"locationDataRecords = new ListRecordAsyncTask().execute().get();");
//
//                mlocationDataRecords = locationDataRecords;
//
//            }catch(InterruptedException e) {
//                Log.d(TAG,"InterruptedException");
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                Log.d(TAG,"ExecutionException");
//                e.printStackTrace();
//            }
//        }
//
//    }

    public void initTime(View v){

        recordview = v;

        current_task = v.getResources().getString(R.string.current_task);

//        if(current_task.equals("PART")) {
////            setDataListItems();
//
//
//            MyAdapter myAdapter = new MyAdapter(myTimeDataset, myActivityDataset,myTrafficDataset,myAnnotationDataset);
//            RecyclerView mList = (RecyclerView) v.findViewById(R.id.list_view);
//            //initialize RecyclerView
////            final View vitem = LayoutInflater.from(Timeline.this).inflate(R.layout.item_dialog, null);
////            item  = vitem;
//            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//
//            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//            mList.setLayoutManager(layoutManager);
//            mList.setAdapter(myAdapter);
//        }else{
            ArrayList<String> locationDataRecords = null;

            listview = (RecyclerView) v.findViewById(R.id.list_view);
//            listview.setEmptyView(v.findViewById(R.id.emptyView));

            try{

                Log.d(TAG,"ListRecordAsyncTask");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    locationDataRecords = new ListRecordAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                else
                    locationDataRecords = new ListRecordAsyncTask().execute().get();

//                setDataListItems();

                List<String> times = new ArrayList<>();
                List<String> activities = new ArrayList<>();
                List<String> sessionids = new ArrayList<>();
                List<LatLng> locations = new ArrayList<>();
                List<Boolean> userPressOrNot = new ArrayList<>();
//                Boolean userPressOrNot = false;

                for(String data : locationDataRecords){
                    String[] datasplit = data.split("-");

                    for(String datasplitpart : datasplit){
                        Log.d(TAG, "datasplit : "+datasplitpart);
                    }

//                    String[] getRidofYear = datasplit[0].split("/");
//                    String[] getRidofYear2 = datasplit[1].split("/");
//
                    times.add(data);
//                    times.add(getRidofYear[1]+"/"+getRidofYear[2]+"-"+getRidofYear2[1]+"/"+getRidofYear2[2]);
                    activities.add(datasplit[2]);
                    sessionids.add(datasplit[3]);
                    LatLng latLng = new LatLng(Double.valueOf(datasplit[4]),Double.valueOf(datasplit[5]));
                    Log.d(TAG, "datasplit[4] : "+datasplit[4]+"; datasplit[5] : "+ datasplit[5]);
                    locations.add(latLng);

                    Log.d(TAG, "Boolean.valueOf(datasplit[6]) : "+Boolean.valueOf(datasplit[6]));

                    userPressOrNot.add(Boolean.valueOf(datasplit[6]));

//                    userPressOrNot = userPressOrNot || Boolean.valueOf(datasplit[6]);

//                    Log.d(TAG, "userPressOrNot : "+userPressOrNot);
                }

                MyAdapter myAdapter = new MyAdapter(times, activities, myTrafficDataset,myAnnotationDataset, sessionids, locations, userPressOrNot);//
                RecyclerView mList = (RecyclerView) v.findViewById(R.id.list_view);

                final LinearLayoutManager layoutManager = new LinearLayoutManager(this);

                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mList.setLayoutManager(layoutManager);
                mList.setAdapter(myAdapter);

                Log.d(TAG,"locationDataRecords = new ListRecordAsyncTask().execute().get();");

                mlocationDataRecords = locationDataRecords;

            } catch (InterruptedException e) {
                Log.d(TAG,"InterruptedException");
                e.printStackTrace();
            } /*catch (IndexOutOfBoundsException e) {
                Log.d(TAG,"InterruptedException");
                e.printStackTrace();
            }*/ catch (ExecutionException e) {
                Log.d(TAG,"ExecutionException");
                e.printStackTrace();
            }

//        }

    }

    private void setDataListItems(){

        //TODO need to combine with ESM CAR
        Log.d(TAG, "recordflag: " + recordflag);
        if(recordflag){
            myTimeDataset.add(result);
            myActivityDataset.add(duration);
            myTrafficDataset.add(TrafficFlag);
            myAnnotationDataset.add("Annotation");
            //SQLite
            // Init
            MyDBHelper mHelper = new MyDBHelper(mContext);
            SQLiteDatabase mDB = null;
            // Insert by raw SQL
            mDB = mHelper.getWritableDatabase();
            String sql = String.format("INSERT INTO Timeline (_Time, _Activity, _Annotation) VALUES ('%s', '%s', '%s')",result, TrafficFlag, "");

            mDB.execSQL(sql);
            Cursor cursor = mDB.rawQuery("SELECT _ID, _Time, _Activity, _Annotation FROM Timeline", null);
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                Log.e("SQLiteDBTestingActivity","_id = "+cursor.getInt(0));
                Log.e("SQLiteDBTestingActivity","_Time = "+cursor.getString(1));
                Log.e("SQLiteDBTestingActivity","_Activity = "+cursor.getString(2));
                Log.e("SQLiteDBTestingActivity","_Annotation = "+cursor.getString(3));
                cursor.moveToNext();
            }
            cursor.close();
            mDB.close();

            recordflag=false;
        }

    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> mTime, mActivity, mTraffic, mAnnotation, mSession;
        private List<LatLng> mlocation;
        private List<Boolean> mUserPressOrNot;

//        private String getRidofMD = "", getRidofMD2 = "";

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView time, duration;
            public TimelineView lineView;
            public LinearLayout cardbackground;
            public android.support.v7.widget.CardView cardView;
            public ImageView traffic;
            public View car_line;
            public ViewHolder(View v) {
                super(v);
                time = (TextView) v.findViewById(R.id.tv_time);
                duration = (TextView) v.findViewById(R.id.tv_duration);
                traffic = (ImageView) v.findViewById(R.id.iv_traffic);
                lineView = (TimelineView) v.findViewById(R.id.time_marker);
                cardView = (android.support.v7.widget.CardView) v.findViewById(R.id.cardview);
                cardbackground = (LinearLayout) v.findViewById(R.id.cardbackground);
                car_line = (View) v.findViewById(R.id.CAR_line);
            }
        }

        public MyAdapter(List<String> timedata, List<String> activitydata, List<String> trafficdata, List<String> annotationdata) {
            mTime = timedata;
            mActivity = activitydata;
            mTraffic = trafficdata;
            mAnnotation = annotationdata;
        }

        public MyAdapter(List<String> timedata, List<String> activitydata, List<String> trafficdata, List<String> annotationdata, List<String> sessioniddata, List<LatLng> locationdata, List<Boolean> userPressOrNot) {
            mTime = timedata;
            mActivity = activitydata;
            mTraffic = trafficdata;
            mAnnotation = annotationdata;
            mSession = sessioniddata;
            mlocation = locationdata;
            mUserPressOrNot = userPressOrNot;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_card_view, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            //TODO get rid of Year and month and date
            String[] datasplit = mTime.get(position).split("-");

            String[] getRidofYear = datasplit[0].split("/");
            String[] getRidofYear2 = datasplit[1].split("/");

            String getRidofMD = getRidofYear[2].split(" ")[1];
            String getRidofMD2 = getRidofYear2[2].split(" ")[1];

            final String[] getRidofSec = getRidofMD.split(":");
            final String[] getRidofSec2 = getRidofMD2.split(":");
//            times.add(getRidofYear[1]+"/"+getRidofYear[2]+"-"+getRidofYear2[1]+"/"+getRidofYear2[2]);

//            holder.time.setText(mTime.get(position));
            holder.time.setText(getRidofSec[0]+":"+getRidofSec[1]+"-"+getRidofSec2[0]+":"+getRidofSec2[1]);

            //if it was pressed by the user show the line
            if(mUserPressOrNot.get(position))
                holder.car_line.setVisibility(View.VISIBLE);

            //TODO when it is static search the corresponding site
            String json = "";
            String name = "";

            Log.d(TAG, "mActivity : "+mActivity);

            if(mActivity.get(position).equals("static")) {
//                json = getJSON(GetUrl.getUrl(mlocation.get(position).latitude, mlocation.get(position).longitude));

                holder.traffic.setImageResource(R.drawable.if_94_171453);// default

                //TODO the reason why checking annotation after the if condition is because of there are same with the session id.
                //TODO try to get the data from annotation table first.
                try {
                    //TODO take data from annotation table
                    SQLiteDatabase db = DBManager.getInstance().openDatabase();
                    Cursor annotationCursor = db.rawQuery("SELECT * FROM " + DBHelper.annotate_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'"
                            +" ORDER BY "+DBHelper.StartTime_col+" ASC", null);
                    Log.d(TAG,"SELECT * FROM " + DBHelper.annotate_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'"
                            +" ORDER BY "+DBHelper.StartTime_col+" ASC");
                    int rows = annotationCursor.getCount();

                    if(rows!=0){
                        annotationCursor.moveToLast();
                        String activity = annotationCursor.getString(6);
                        Log.d(TAG,"activity : "+activity);
                        holder.duration.setText(activity);

                        if(activity.equals("定點")){
                            holder.traffic.setImageResource(R.drawable.if_94_171453);
                        }else if(activity.equals("走路")){
                            holder.traffic.setImageResource(R.drawable.walk);
                        }else if(activity.equals("自行車")){
                            holder.traffic.setImageResource(R.drawable.bike);
                        }else if(activity.equals("汽車")){
                            holder.traffic.setImageResource(R.drawable.car);
                        }

                    }else{
                        //if not in annotation table, search on the google service to get the recent site.

                        if(!current_task.equals("PART")) {
                            String url =  GetUrl.getUrl(mlocation.get(position).latitude, mlocation.get(position).longitude);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                json = new HttpAsyncGetSiteTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                        url
                                ).get();
                            else
                                json = new HttpAsyncGetSiteTask().execute(
                                        url
                                ).get();

                            JSONObject jsonObject = null;
                            jsonObject = new JSONObject(json);
                            JSONArray results = jsonObject.getJSONArray("results");
                            //TODO default now we choose the second index from the json.(first index is ken name.)
                            name = results.getJSONObject(1).getString("name");
                            holder.duration.setText(name);

                            //in PART there will store a sitename in triptable
                        }else {

                            try {
                                //TODO take data from annotation table
                                SQLiteDatabase db2 = DBManager.getInstance().openDatabase();
                                Cursor tripSiteCursor = db2.rawQuery("SELECT * FROM " + DBHelper.trip_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'"
                                        +" ORDER BY "+DBHelper.TIME+" ASC", null);
                                Log.d(TAG,"SELECT * FROM " + DBHelper.trip_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'"
                                        +" ORDER BY "+DBHelper.TIME+" ASC");
                                int rows2 = tripSiteCursor.getCount();

                                if(rows2!=0){
                                    tripSiteCursor.moveToLast();
                                    name = tripSiteCursor.getString(7);

                                    Log.d(TAG,"name : "+name);
                                }
                            }catch (Exception e2){
                                e2.printStackTrace();
                                android.util.Log.e(TAG, "exception", e2);
                            }

                            holder.duration.setText(name);

                        }
                    }

                } catch (Exception e){
                    e.printStackTrace();
                    android.util.Log.e(TAG, "exception", e);

                    if(!current_task.equals("PART")) {
                        String url = GetUrl.getUrl(mlocation.get(position).latitude, mlocation.get(position).longitude);
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                json = new HttpAsyncGetSiteTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                        url
                                ).get();
                            else
                                json = new HttpAsyncGetSiteTask().execute(
                                        url
                                ).get();

                            JSONObject jsonObject = null;
                            jsonObject = new JSONObject(json);
                            JSONArray results = jsonObject.getJSONArray("results");
                            //TODO default now we choose the second index from the json.(first index is ken name.)
                            name = results.getJSONObject(1).getString("name");
                            holder.duration.setText(name);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            android.util.Log.e(TAG, "exception", e2);
                        }

                        //in PART there will store a sitename in triptable
                    }else {

                        try {
                            //TODO take data from annotation table
                            SQLiteDatabase db = DBManager.getInstance().openDatabase();
                            Cursor tripSiteCursor = db.rawQuery("SELECT * FROM " + DBHelper.trip_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'"
                                    +" ORDER BY "+DBHelper.TIME+" ASC", null);
                            Log.d(TAG,"SELECT * FROM " + DBHelper.trip_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'"
                                    +" ORDER BY "+DBHelper.TIME+" ASC");
                            int rows = tripSiteCursor.getCount();

                            if(rows!=0){
                                tripSiteCursor.moveToLast();
                                name = tripSiteCursor.getString(7);

                                Log.d(TAG,"name : "+name);
                            }
                        }catch (Exception e2){
                            e2.printStackTrace();
                            android.util.Log.e(TAG, "exception", e2);
                        }

                        holder.duration.setText(name);
                    }

                }

            }else{
//                holder.duration.setText(mActivity.get(position));
                if (mActivity.get(position).equals("on_foot")) {
                    holder.duration.setText("walk");
                } else if (mActivity.get(position).equals("on_bicycle")) {
                    holder.duration.setText("bike");
                } else if (mActivity.get(position).equals("in_vehicle")) {
                    holder.duration.setText("car");
                }

            }

//            if(current_task.equals("PART")) {
//                if (mTraffic.get(position).equals("walk")) {
//                    holder.traffic.setImageResource(R.drawable.walk);
//                } else if (mTraffic.get(position).equals("bike")) {
//                    holder.traffic.setImageResource(R.drawable.bike);
//                } else if (mTraffic.get(position).equals("car")) {
//                    holder.traffic.setImageResource(R.drawable.car);
//                } /*else if (mTraffic.get(position).equals("site")) {
//                    holder.traffic.setImageResource(R.drawable.if_94_171453);
//                }*/
//            }else{
                if (mActivity.get(position).equals("on_foot")) {
                    holder.traffic.setImageResource(R.drawable.walk);
                } else if (mActivity.get(position).equals("on_bicycle")) {
                    holder.traffic.setImageResource(R.drawable.bike);
                } else if (mActivity.get(position).equals("in_vehicle")) {
                    holder.traffic.setImageResource(R.drawable.car);
                } /*else if (mActivity.get(position).equals("static")) {
                    holder.traffic.setImageResource(R.drawable.if_94_171453);
                }*/
//            }

            //change the line color to red if its sessionid is not in annotation table.
            //and not showing if the trip is "此移動不存在"
            try{
                SQLiteDatabase db = DBManager.getInstance().openDatabase();
                Cursor tripCursor = db.rawQuery("SELECT * FROM " + DBHelper.annotate_table +
                        " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'", null);
                Log.d(TAG,"SELECT * FROM " + DBHelper.annotate_table +
                        " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'");
                int rows = tripCursor.getCount();

                if(rows==0){
                    //setting the red wrap on the cardview.
                    GradientDrawable sd = new GradientDrawable();
                    sd.setColor(Color.parseColor("#eaeef7"));
                    sd.setStroke(10, Color.parseColor("#EF767A"));
                    holder.cardView.setBackground(sd);
                }else{
                    //because we wanna check the newest one.
                    tripCursor.moveToLast();
                    Log.d(TAG, "checking annotate : tripCursor.getString(4) = "+tripCursor.getString(4));

                    if(tripCursor.getString(4).equals("此移動不存在")){
                        //don't show it
                        holder.cardView.setVisibility(View.GONE);
                        holder.lineView.setMarkerSize(0);
                    }
                }
            }catch (SQLException e){
                e.printStackTrace();
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d(TAG, "mContext : " + mContext);

                    final LayoutInflater inflater = LayoutInflater.from(mContext);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    final View layout = inflater.inflate(R.layout.custom_dialog,null);
//                    final EditText Dtime = (EditText) layout.findViewById(R.id.ed_time);
                    final Spinner Dspinner = (Spinner) layout.findViewById(R.id.spinner);
                    DchoosingSite = (Button) layout.findViewById(R.id.choosingSite);
                    final Button DstartTime = (Button) layout.findViewById(R.id.startTime);
                    final Button DendTime = (Button) layout.findViewById(R.id.endTime);
                    final String[] activity = {"請選擇交通模式", "走路", "自行車", "汽車", "定點", "此移動不存在", "與上一個相同"};
                    final ArrayAdapter<String> activityList = new ArrayAdapter<>(mContext,
                            android.R.layout.simple_spinner_dropdown_item,
                            activity);
                    Dspinner.setAdapter(activityList);

                    Dspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            if(current_task.equals("PART")) {
                                if (parent.getSelectedItem().equals("走路")) {
                                    mTraffic.set(position, "walk");
                                } else if (parent.getSelectedItem().equals("自行車")) {
                                    mTraffic.set(position, "bike");
                                } else if (parent.getSelectedItem().equals("汽車")) {
                                    mTraffic.set(position, "car");
                                } else if (parent.getSelectedItem().equals("定點")) {
                                    mTraffic.set(position, "site");
                                } else if (parent.getSelectedItem().equals(" ")) {
                                    ;
                                }
                            }else{
                                if (parent.getSelectedItem().equals("走路")) {
                                    mActivity.set(position, "on_foot");
                                } else if (parent.getSelectedItem().equals("自行車")) {
                                    mActivity.set(position, "on_bicycle");
                                } else if (parent.getSelectedItem().equals("汽車")) {
                                    mActivity.set(position, "in_vehicle");
                                } else if (parent.getSelectedItem().equals("定點")) {
                                    mActivity.set(position, "static");
                                } else if (parent.getSelectedItem().equals(" ")) {
                                    ;
                                }
                            }

                            if(parent.getSelectedItem().equals("定點")){
                                DchoosingSite.setVisibility(View.VISIBLE);
                                DchoosingSite.setText("請選擇地點");
                                DchoosingSite.setOnClickListener(new Button.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        double lat = mlocation.get(position).latitude;
                                        double lng = mlocation.get(position).longitude;
//                                        PlaceSelection placeSelection = new PlaceSelection(lat, lng);
                                        Intent intent = new Intent(mContext, PlaceSelection.class);
                                        Bundle latlng = new Bundle();
                                        latlng.putDouble("lat",lat);
                                        latlng.putDouble("lng",lng);
                                        intent.putExtras(latlng);
                                        mContext.startActivity(intent);
//                                        DchoosingSite.setText(selectedSiteName);
                                    }
                                });
                            }else{
                                DchoosingSite.setVisibility(View.INVISIBLE);
                                DchoosingSite.setText(" ");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    final EditText Dannotation_goal = (EditText)layout.findViewById(R.id.ed_annotate_goal);
                    final EditText Dannotation_specialEvent = (EditText)layout.findViewById(R.id.ed_annotate_specialEvent);

//                    Dtime.setText(mTime.get(position));

                    //Time
                    Log.d(TAG, " Time : " + mTime.get(position));
                    String[] datasplit = mTime.get(position).split("-");

                    final String startTimeString = datasplit[0];
                    final String endTimeString = datasplit[1];

                    String[] getRidofYear = datasplit[0].split("/"); //2017 / 12 / 20 07:12:53
                    String[] getRidofYear2 = datasplit[1].split("/");

                    String getRidofMD = getRidofYear[2].split(" ")[1]; // 20 07:12:53
                    String getRidofMD2 = getRidofYear2[2].split(" ")[1];

                    final String[] getRidofSec = getRidofMD.split(":");
                    final String[] getRidofSec2 = getRidofMD2.split(":");

                    final String correspondingDate = getRidofYear[0]+"/"+getRidofYear[1]+"/"+getRidofYear[2].split(" ")[0];
                    final String correspondingDate2 = getRidofYear2[0]+"/"+getRidofYear2[1]+"/"+getRidofYear2[2].split(" ")[0];

//                    Dtime.setText(getRidofMD+"-"+getRidofMD2);
                    final SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW); //2017/12/20 07:12:53

                    DstartTime.setText(getRidofSec[0]+":"+getRidofSec[1]);
                    DstartTime.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG,"startTime clicked");
                            Date date1 = null;
                            try {
                                date1 = sdf.parse(startTimeString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Calendar c = Calendar.getInstance();
                            c.setTime(date1);

                            int hour = c.get(Calendar.HOUR_OF_DAY);
                            int minute = c.get(Calendar.MINUTE);
                            new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener(){
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                    String hour=String.valueOf(hourOfDay);
                                    String min =String.valueOf(minute);
                                    if(hourOfDay<10)
                                        hour = "0" + String.valueOf(hourOfDay);

                                    if(minute<10)
                                        min = "0" + String.valueOf(minute);

                                    DstartTime.setText( hour + ":" + min );

                                }
                            }, hour, minute, false).show();
                        }
                    });

                    DendTime.setText(getRidofSec2[0]+":"+getRidofSec2[1]);
                    DendTime.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG,"startTime clicked");
                            Date date2 = null;
                            try {
                                date2 = sdf.parse(endTimeString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Calendar c = Calendar.getInstance();
                            c.setTime(date2);
                            int hour = c.get(Calendar.HOUR_OF_DAY);
                            int minute = c.get(Calendar.MINUTE);
                            new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener(){
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                    String hour=String.valueOf(hourOfDay);
                                    String min =String.valueOf(minute);
                                    if(hourOfDay<10)
                                        hour = "0" + String.valueOf(hourOfDay);

                                    if(minute<10)
                                        min  = "0" + String.valueOf(minute);

                                    DendTime.setText( hour + ":" + min );

                                }
                            }, hour, minute, false).show();
                        }
                    });

                    try {
                        //TODO take data from annotation table
                        SQLiteDatabase db = DBManager.getInstance().openDatabase();
                        Cursor annotationCursor = db.rawQuery("SELECT * FROM " + DBHelper.annotate_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'"
                                +" ORDER BY "+DBHelper.StartTime_col+" ASC", null);
                        Log.d(TAG,"SELECT * FROM " + DBHelper.annotate_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ mSession.get(position) + "'"
                                +" ORDER BY "+DBHelper.StartTime_col+" ASC");
                        int rows = annotationCursor.getCount();

                        if(rows!=0){
                            annotationCursor.moveToLast();
//                            String activity_FromAnnotate = annotationCursor.getString(6);
                            String goal = annotationCursor.getString(7);
                            Dannotation_goal.setText(goal);
                            String specialEvent = annotationCursor.getString(8);
                            Dannotation_specialEvent.setText(specialEvent);

                        }else{
//                            Dannotation_goal.setText(mAnnotation.get(position));
                            //add for PART?
                        }

                    }catch (IndexOutOfBoundsException e){
                        e.printStackTrace();
                        android.util.Log.e(TAG, "exception", e);
                        Dannotation_goal.setText("");
                        Dannotation_specialEvent.setText("");
                    }catch (SQLException e){
                        e.printStackTrace();
                        android.util.Log.e(TAG, "exception", e);
                        Dannotation_goal.setText("");
                        Dannotation_specialEvent.setText("");
                    }

                    builder.setView(layout)
                            .setPositiveButton(R.string.ok, null);

                    final AlertDialog mAlertDialog = builder.create();
                    mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
                        @Override
                        public void onShow(final DialogInterface dialog) {
                            Button button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {

//                                    if(current_task.equals("PART")) {
//                                        if (Dspinner.getSelectedItem().equals("走路")) {
//                                            mTraffic.set(position, "walk");
//                                        } else if (Dspinner.getSelectedItem().equals("自行車")) {
//                                            mTraffic.set(position, "bike");
//                                        } else if (Dspinner.getSelectedItem().equals("汽車")) {
//                                            mTraffic.set(position, "car");
//                                        } else if (Dspinner.getSelectedItem().equals("定點")) {
//                                            mTraffic.set(position, "site");
//                                        } else if (Dspinner.getSelectedItem().equals(" ")) {
//                                            ;
//                                        }
//                                    }else{
                                        if (Dspinner.getSelectedItem().equals("走路")) {
                                            mActivity.set(position, "on_foot");
                                        } else if (Dspinner.getSelectedItem().equals("自行車")) {
                                            mActivity.set(position, "on_bicycle");
                                        } else if (Dspinner.getSelectedItem().equals("汽車")) {
                                            mActivity.set(position, "in_vehicle");
                                        } else if (Dspinner.getSelectedItem().equals("定點")) {
                                            mActivity.set(position, "static");
                                        } else if (Dspinner.getSelectedItem().equals(" ")) {
                                            ;
                                        }
//                                    }

                                    if(Dspinner.getSelectedItem().equals("請選擇交通模式")){
                                        Toast.makeText(mContext, "請選擇一項交通模式！！", Toast.LENGTH_SHORT).show();
                                    } else{
//                                        mTime.set(position, Dtime.getText().toString());
//                                        mAnnotation.set(position, Dannotation.getText().toString());

                                        //TODO judging that we need to update session id or not
                                        String currentSessionid = mSession.get(position); //mSession is ASC got from DB.
                                        if(Dspinner.getSelectedItem().equals("與上一個相同")){
                                            currentSessionid = mSession.get(position+1); //TODO because the order of the list is DESC

                                            //TODO update session id in Trip and Session table
                                            //Trip
                                            String lastSessionid = mSession.get(position);
                                            Log.d(TAG,"lastSessionid : "+lastSessionid);
                                            try{
                                                SQLiteDatabase db = DBManager.getInstance().openDatabase();
                                                Cursor tripCursor = db.rawQuery("SELECT * FROM " + DBHelper.trip_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ lastSessionid + "'"
                                                        +"ORDER BY "+DBHelper.TIME+" ASC", null);
                                                Log.d(TAG,"SELECT * FROM " + DBHelper.trip_table + " WHERE "+ DBHelper.sessionid_col+ " =' "+ lastSessionid + "'"
                                                        +"ORDER BY "+DBHelper.TIME+" ASC"); //+" ORDER BY "+DBHelper.TIME+" ASC"
                                                int rows = tripCursor.getCount();

                                                if(rows!=0){
                                                    tripCursor.moveToFirst();
                                                    int first_id = tripCursor.getInt(0);
                                                    tripCursor.moveToLast();
                                                    int last_id = tripCursor.getInt(0);
                                                    ContentValues contentValues = new ContentValues();

                                                    contentValues.put(DBHelper.sessionid_col, currentSessionid); //update the session id to previous one in trip table
                                                    contentValues.put(DBHelper.trip_transportation_col, mActivity.get(position+1));

                                                    db.update(DBHelper.trip_table, contentValues, "_id >= ? AND _id <= ?" , new String[] {String.valueOf(first_id), String.valueOf(last_id)});
                                                }
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }

                                            //Session
                                            try{
                                                String sessionidNoZero = lastSessionid.split(", ")[1];

                                                SQLiteDatabase db = DBManager.getInstance().openDatabase();
                                                Cursor sessionCursor = db.rawQuery("SELECT * FROM " + DBHelper.session_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ sessionidNoZero + "'"
                                                        +" ORDER BY "+DBHelper.TIME+" ASC", null);
                                                Log.d(TAG,"SELECT * FROM " + DBHelper.session_table + " WHERE "+ DBHelper.sessionid_col+ " ='"+ sessionidNoZero + "'"
                                                        +" ORDER BY "+DBHelper.TIME+" ASC");
                                                int rows = sessionCursor.getCount();

                                                if(rows!=0){
                                                    sessionCursor.moveToFirst();
                                                    int first_id = sessionCursor.getInt(0);
                                                    sessionCursor.moveToLast();
                                                    int last_id = sessionCursor.getInt(0);
                                                    ContentValues contentValues = new ContentValues();

                                                    Log.d(TAG, "first_id : " + first_id + ", last_id : " + last_id);

                                                    String currentSessionidNoZero = currentSessionid.split(", ")[1];

                                                    Log.d(TAG, "currentSessionidNoZero : " + currentSessionidNoZero);
                                                    contentValues.put(DBHelper.sessionid_col, currentSessionidNoZero); //update the session id to previous one in trip table
//TODO check that...
                                                    db.update(DBHelper.session_table, contentValues, "_id >= ? AND _id <= ?" , new String[] {String.valueOf(first_id), String.valueOf(last_id)});
                                                }
                                            }catch(Exception e){
                                                e.printStackTrace();
                                                android.util.Log.e(TAG, "exception", e);
                                            }
                                        }

                                        //TODO then store in annotation table
                                        ContentValues values = new ContentValues();
                                        try {
                                            SQLiteDatabase db = DBManager.getInstance().openDatabase();

                                            String sitename = "";

                                            if(Dspinner.getSelectedItem().toString().equals("定點")){
                                                sitename = holder.duration.getText().toString();
                                            }

//                                            String times[] = mTime.get(position).split("-");
//                                            String startTime = times[0];
//                                            String endTime = times[1];
                                            //TODO change the data format to store
                                            String startTime = DstartTime.getText().toString();
                                            String endTime = DendTime.getText().toString();

                                            startTime = correspondingDate+" "+startTime+ ":00";
                                            endTime = correspondingDate2 +" "+endTime+ ":00";

                                            long startTimeLong = getSpecialTimeInMillis(startTime);
                                            long endTimeLong = getSpecialTimeInMillis(endTime);

                                            values.put(DBHelper.StartTime_col, startTimeLong);
                                            values.put(DBHelper.EndTime_col, endTimeLong);
                                            values.put(DBHelper.StartTimeString_col, startTime);
                                            values.put(DBHelper.EndTimeString_col, endTime);
                                            values.put(DBHelper.sessionid_col, currentSessionid);
                                            values.put(DBHelper.Activity_col, Dspinner.getSelectedItem().toString());
                                            values.put(DBHelper.Annotation_Goal_col, Dannotation_goal.getText().toString());
                                            values.put(DBHelper.Annotation_SpecialEvent_col, Dannotation_specialEvent.getText().toString());
                                            values.put(DBHelper.SiteName_col, sitename);
                                            values.put(DBHelper.uploaded_col, false);

                                            db.insert(DBHelper.annotate_table, null, values);

                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        } finally {
                                            values.clear();
                                            DBManager.getInstance().closeDatabase();
                                        }


                                        //reset data
                                        ArrayList<String> locationDataRecords = null;
                                        try{
                                            Log.d(TAG,"reset data");

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                                locationDataRecords = new ListRecordAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                            else
                                                locationDataRecords = new ListRecordAsyncTask().execute().get();

                                            List<String> times = new ArrayList<>();
                                            List<String> activities = new ArrayList<>();
                                            List<String> sessionids = new ArrayList<>();
                                            List<LatLng> locations = new ArrayList<>();

                                            for(String data : locationDataRecords){
                                                String[] datasplit = data.split("-");

                                                times.add(data);

                                                activities.add(datasplit[2]);
                                                sessionids.add(datasplit[3]);
                                                LatLng latLng = new LatLng(Double.valueOf(datasplit[4]),Double.valueOf(datasplit[5]));
                                                Log.d(TAG, "datasplit[4] : "+datasplit[4]+"; datasplit[5] : "+ datasplit[5]);
                                                locations.add(latLng);
                                            }
                                            mTime = times;
                                            mActivity = activities;
//                                            mTraffic = trafficdata;
//                                            mAnnotation = annotationdata;
                                            mSession = sessionids;
                                            mlocation = locations;
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        notifyDataSetChanged();

                                        DchoosingSite.setVisibility(View.INVISIBLE); // set back to default

                                        Toast.makeText(mContext,"感謝您的填答", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    });

                    mAlertDialog.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return mTime.size();
        }

    }

    public long getSpecialTimeInMillis(String givenDateFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        long timeInMilliseconds = 0;
        try {
            Date mDate = sdf.parse(givenDateFormat);
            timeInMilliseconds = mDate.getTime();
            Log.d(TAG,"Date in milli :: " + timeInMilliseconds);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeInMilliseconds;
    }

    //use HTTPAsyncTask to poHttpAsyncPostJsonTaskst data
    private class HttpAsyncGetSiteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String result = null;
            String url = params[0];

            result = getJSON(url);

            return result;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

        }

    }

    public String getJSON(String url) {
        HttpsURLConnection con = null;
        String json = "";
        try {
            URL u = new URL(url);
            con = (HttpsURLConnection) u.openConnection();

            con.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
//            Log.d(TAG, sb.toString());
            json = sb.toString();
            br.close();
//            return sb.toString();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return json;
    }

    private class ListRecordAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {

        private ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {
            Log.d(TAG,"onPreExecute");
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            Log.d(TAG, "listRecordAsyncTask going to list recording");

            ArrayList<String> locationDataRecords = new ArrayList<String>();

            try {

                locationDataRecords = TripManager.getTripDatafromSQLite();
//                locationDataRecords = TripManager.getInstance().getTripDatafromSQLite();
//                Trip_size = TripManager.getInstance().getSessionidForTripSize();
                Trip_size = TripManager.getInstance().getTrip_size();

//                Log.d(TAG,"locationDataRecords(0) : " + locationDataRecords.get(0));
//                Log.d(TAG,"locationDataRecords(max) : " + locationDataRecords.get(locationDataRecords.size()-1));

//                if(locationDataRecords.isEmpty())
//                    ;

                Log.d(TAG,"try locationDataRecords");
            }catch (Exception e) {
                locationDataRecords = new ArrayList<String>();
                Log.d(TAG,"Exception");
                e.printStackTrace();
            }
//            return locationDataRecords;

            return locationDataRecords;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<String> result) {

            super.onPostExecute(result);

        }

    }



}
