package labelingStudy.nctu.minuku_2.controller;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import labelingStudy.nctu.minuku.Data.DBHelper;
import labelingStudy.nctu.minuku.Data.DataHandler;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minuku_2.NearbyPlaces.GetUrl;
import labelingStudy.nctu.minuku_2.R;


public class Timeline extends AppCompatActivity {

    public static ArrayList<String> myTimeDataset = new ArrayList<>();
    public static ArrayList<String> myActivityDataset = new ArrayList<>();
    public static ArrayList<String> myTrafficDataset = new ArrayList<>();
    public static ArrayList<String> myAnnotationDataset = new ArrayList<>();

    public static String selectedSiteName = "請選擇地點";

    public static Button DchoosingSite = null;

    private String TAG = "Timeline";
    Context mContext;

    private View recordview;

    private int mYear, mMonth, mDay;

    ArrayList<Session> mSessions;

    private SharedPreferences sharedPrefs;

    private boolean firstTimeOrNot;

    private String timelineOrder;

    private String dateToQuery;

    public Timeline(){}
    public Timeline(Context mContext){
        this.mContext = mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        firstTimeOrNot = sharedPrefs.getBoolean("firstTimeOrNot", true);
        android.util.Log.d(TAG,"firstTimeOrNot : "+ firstTimeOrNot);

        timelineOrder = sharedPrefs.getString("timelineOrder", "ASC");

        if(firstTimeOrNot) {
            startpermission();
            firstTimeOrNot = false;
            sharedPrefs.edit().putBoolean("firstTimeOrNot", firstTimeOrNot).apply();
        }

        SimpleDateFormat sdf_date = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY);
        dateToQuery = ScheduleAndSampleManager.getTimeString(ScheduleAndSampleManager.getCurrentTimeInMillis(), sdf_date);
        Log.d(TAG, "init dateToQuery : " + dateToQuery);

    }

    public void startpermission(){

        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));  // 協助工具

        Intent intent1 = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);  //usage
        startActivity(intent1);

//        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS); //notification
//        startActivity(intent);

        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//location
    }

    @Override
    protected void onPause() {
        super.onPause();

        sharedPrefs.edit().putString("lastActivity", getClass().getName()).apply();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            Timeline.this.finish();

            if(isTaskRoot()){

                startActivity(new Intent(this, WelcomeActivity.class));
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.action_getWantedOrder);

        if(timelineOrder.equals(Constants.DESC)){

            item.setTitle("從舊到新");
        }else if (timelineOrder.equals(Constants.ASC)) {

            item.setTitle("從新到舊");
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_getWantedOrder:

                if(item.getTitle().equals("從舊到新")){

                    sharedPrefs.edit().putString("timelineOrder", Constants.ASC).apply();

                    item.setTitle("從新到舊");
                }else if (item.getTitle().equals("從新到舊")){

                    sharedPrefs.edit().putString("timelineOrder", Constants.DESC).apply();

                    item.setTitle("從舊到新");
                }

                timelineOrder = sharedPrefs.getString("timelineOrder", Constants.DESC);

                //reset the timeline
                initTime();

                return true;

            case R.id.action_selectdate:
                final Calendar c = Calendar.getInstance();

                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year,
                                          int month, int day) {

                            Log.d(TAG,"month : " + month + ", year : " + year + ", day : " + day);

                            String monthString = String.valueOf(month+1);

                            if((month + 1) < 10){
                                monthString = "0"+monthString;
                            }

                            String dayString = String.valueOf(day);

                            if(day < 10){
                                dayString = "0"+day;
                            }

                            dateToQuery = year + "-" + monthString + "-" + dayString;

                            Log.d(TAG, "dateToQuery : " + dateToQuery);
                    }
                };

                final DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this, datePickerListener,
                        mYear, mMonth, mDay);

                datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        "OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {

                                    DatePicker datePicker = datePickerDialog
                                            .getDatePicker();
                                    datePickerListener.onDateSet(datePicker,
                                            datePicker.getYear(),
                                            datePicker.getMonth(),
                                            datePicker.getDayOfMonth());

                                    Log.d(TAG, "dateToQuery onClick : " + dateToQuery);

                                    //reset the timeline
                                    initTime();
                                }
                            }
                        });

                datePickerDialog.show();

                return true;
        }
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG,"onResume");

        initTime();
    }

    public void initTime(){

        mContext = Timeline.this;

        Log.d(TAG, "[test show Timeline] initTime");

        try{

            mSessions = new ArrayList<>();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                mSessions = new ListSessionAsyncTask(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            else
                mSessions = new ListSessionAsyncTask(mContext).execute().get();

            Log.d(TAG, "mSessions size : " + mSessions.size());

            if(mSessions.size() > 0){

                TimelineAdapter timelineAdapter = new TimelineAdapter(mSessions);
                RecyclerView mList = (RecyclerView) findViewById(R.id.list_view);
                mList.setVisibility(View.VISIBLE);

                //TODO if there have some new availSite, start from the top (reset)
                int currentposition = sharedPrefs.getInt("currentposition", 0);
                mList.scrollToPosition(currentposition);

                final LinearLayoutManager layoutManager = new LinearLayoutManager(Timeline.this);

                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mList.setLayoutManager(layoutManager);
                mList.setAdapter(timelineAdapter);
            }else{

                //set Empty view
                RecyclerView mList = (RecyclerView) findViewById(R.id.list_view);
                mList.setVisibility(View.GONE);
            }
        } catch (InterruptedException e) {
            Log.d(TAG,"InterruptedException");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d(TAG,"ExecutionException");
            e.printStackTrace();
        }

    }

    public void initTime(View v){

        Log.d(TAG, "[test show Timeline] initTime");

        recordview = v;

        try{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new ListSessionAsyncTask(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            else
                new ListSessionAsyncTask(mContext).execute().get();

            //TODO find better method
            if(mSessions != null){

                TimelineAdapter timelineAdapter = new TimelineAdapter(mSessions);
                RecyclerView mList = (RecyclerView) v.findViewById(R.id.list_view);

                final LinearLayoutManager layoutManager = new LinearLayoutManager(Timeline.this);

                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mList.setLayoutManager(layoutManager);
                mList.setAdapter(timelineAdapter);
            }

        } catch (InterruptedException e) {
            Log.d(TAG,"InterruptedException");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d(TAG,"ExecutionException");
            e.printStackTrace();
        }
    }

    public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

        private List<Session> mSessions;
        public String detectedSiteName = "";

        //split
        private LatLng splittingLatlng = new LatLng(-999, -999);
        private long splittingTime = -9999;
        private boolean IsSplitLocationChosen = false;
        private HashMap<Integer, Marker> addedSplitMarker = new HashMap<>();
        private int currentMarkerKey = -1;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView time, duration, date, sessionType;
            public TimelineView lineView;
            public LinearLayout cardbackground;
            public android.support.v7.widget.CardView cardView;
            public ImageView traffic;
            public View car_line, car_line_down;
            public ViewHolder(View v) {
                super(v);

                time = (TextView) v.findViewById(R.id.tv_time);
                duration = (TextView) v.findViewById(R.id.tv_duration);
                date = (TextView) v.findViewById(R.id.tv_date);
                sessionType = (TextView) v.findViewById(R.id.sessionType);
                traffic = (ImageView) v.findViewById(R.id.iv_traffic);
                lineView = (TimelineView) v.findViewById(R.id.time_marker);
                cardView = (android.support.v7.widget.CardView) v.findViewById(R.id.cardview);
                cardbackground = (LinearLayout) v.findViewById(R.id.cardbackground);
                car_line = (View) v.findViewById(R.id.CAR_line);
                car_line_down = (View) v.findViewById(R.id.CAR_line_down);
            }
        }

        public TimelineAdapter(List<Session> sessions){

            mSessions = sessions;
        }

        @Override
        public TimelineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_card_view, parent, false);
            ViewHolder vh = new ViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final Session session = mSessions.get(position);

            final long startTime = session.getStartTime();

            final long endTime;

            //if the session is still ongoing, set the endTime with the current time.
            if(SessionManager.isSessionOngoing(session.getId()) || SessionManager.isSessionEmptyOngoing(session.getId())){

                endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();

                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_NO_ZONE_Slash);
                Log.d(TAG, "[test show Timeline] ongoing endTime : "+ ScheduleAndSampleManager.getTimeString(endTime, sdf));
            } else{

                endTime = session.getEndTime();

                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_NO_ZONE_Slash);
                Log.d(TAG, "[test show Timeline] endTime : "+ ScheduleAndSampleManager.getTimeString(endTime, sdf));
            }

            SimpleDateFormat sdf_a_hhmm = new SimpleDateFormat(Constants.DATE_FORMAT_AMPM_HOUR_MIN);

            final String startTimeString = ScheduleAndSampleManager.getTimeString(startTime, sdf_a_hhmm);
            final String endTimeString = ScheduleAndSampleManager.getTimeString(endTime, sdf_a_hhmm);

            holder.time.setText(startTimeString+"-"+endTimeString);

            SimpleDateFormat sdf_slash = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY_Slash);
            final String date = ScheduleAndSampleManager.getTimeString(startTime, sdf_slash);

            holder.date.setText(date);

            final SimpleDateFormat sdf_date = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY);

            final String startTimeDate = ScheduleAndSampleManager.getTimeString(startTime, sdf_date);
            final String endTimeDate = ScheduleAndSampleManager.getTimeString(endTime, sdf_date);


            //if it was pressed by the user show the line
            if(session.isUserPress()) {

                if(timelineOrder.equals(Constants.ASC)) {

                    holder.car_line.setVisibility(View.VISIBLE);

                    holder.car_line_down.setVisibility(View.INVISIBLE);
                }else{

                    holder.car_line_down.setVisibility(View.VISIBLE);

                    holder.car_line.setVisibility(View.INVISIBLE);
                }
            }

            Log.d(TAG, "[test triggering] timeline session id : "+ session.getId());
            Log.d(TAG, "[test triggering] timeline session isUserPress ? "+session.isUserPress());
            Log.d(TAG, "[test triggering] timeline session isModified ? "+session.isModified());


            //check the annotation first, show the modification from the user
            AnnotationSet annotationSet = session.getAnnotationsSet();

            //check the transportation from the label, if it hasn't been labeled then check the detected one
            ArrayList<Annotation> annotations_label = annotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_Label);

            Log.d(TAG, "[storing sitename] annotations_label size : "+annotations_label.size());

            JSONObject labelJson = new JSONObject();

            //if the user has labeled
            try {

                Annotation annotation_label = annotations_label.get(annotations_label.size() - 1);
                String label = annotation_label.getContent();
                String label_Transportation;
                labelJson = new JSONObject(label);

                label_Transportation = labelJson.getString(Constants.ANNOTATION_Label_TRANSPORTATOIN);

                //set the transportation (from label) icon and text
                String transportation = getTransportationFromSpinnerItem(label_Transportation);
                int icon = getIconToShowTransportation(transportation);

                holder.traffic.setImageResource(icon);

                Log.d(TAG, "[storing sitename] labeled transportation : "+ label_Transportation);

                if(icon == R.drawable.transparent){
                    holder.traffic.setVisibility(View.INVISIBLE);
                }

                if(transportation.equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION)){

                    String labeledSitename = labelJson.getString(Constants.ANNOTATION_Label_SITENAME);
                    Log.d(TAG, "[storing sitename] Sitename from DB : "+ labeledSitename);

                    holder.duration.setText(labeledSitename);
                }else{

                    holder.duration.setText(label_Transportation);
                }
            }catch (JSONException e){

            }catch (IndexOutOfBoundsException e){
                Log.d(TAG, "[storing sitename] No label yet.");
            }

            //if the user hasn't labeled, check the detected one
            if(!labelJson.has(Constants.ANNOTATION_Label_TRANSPORTATOIN)){

                ArrayList<Annotation> annotations = annotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY);

                String transportation;

                if(annotations.size()==0)
                    transportation = TransportationModeStreamGenerator.TRANSPORTATION_MODE_HASNT_DETECTED_FLAG;
                else {
                    Annotation annotation = annotations.get(annotations.size() - 1);
                    transportation = annotation.getContent();
                }

                //if it is static check the sitename
                if(transportation.equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION)){

                    ArrayList<Annotation> annotations_sitename = annotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_DETECTED_SITENAME);

                    //if there is no sitename has been stored
                    if(annotations_sitename.size()==0){

                        //get the site from the google service
                        try {

                            String name = "";
                            //get location by session id
                            ArrayList<String> latlngs = DataHandler.getDataBySession(session.getId(), DBHelper.LOCATION_TABLE);

                            double lat = -999;
                            double lng = -999;

                            if(latlngs.size()!=0) {

                                String[] latlng_first = latlngs.get(0).split(Constants.DELIMITER);
                                String latString = latlng_first[2];
                                String lngString = latlng_first[3];

                                lat = Double.parseDouble(latString);
                                lng = Double.parseDouble(lngString);
                            }

                            // before find by the google service, check the customized one is in the range of 100 meters.
                            // if there are some in the range, set the closest one; if not, check the google service
                            ArrayList<String> customizedSite = DBHelper.queryCustomizedSites();

                            String transportationDuration = "";

                            if(customizedSite.size() != 0){

                                //check the distance between the session's first location and the customizedSite

                                float smallestDist = 999;
                                String closestSite = "";

                                for(int index = 0; index < customizedSite.size(); index++){

                                    String eachData = customizedSite.get(index);

                                    String[] dataPieces = eachData.split(Constants.DELIMITER);

                                    double siteLat = Double.parseDouble(dataPieces[2]);
                                    double siteLng = Double.parseDouble(dataPieces[3]);

                                    float[] results = new float[1];
                                    Location.distanceBetween(lat, lng, siteLat, siteLng, results);
                                    float distance = results[0];

                                    if(distance < smallestDist && distance <= Constants.siteRange){

                                        smallestDist = distance;
                                        closestSite = dataPieces[1];
                                    }
                                }

                                if(!closestSite.equals("")){

//                                    transportationDuration = textShortenIfTooLong(closestSite);
                                    transportationDuration = closestSite;
                                }else {

                                    name = getSiteNameFromNet(lat, lng);

//                                    transportationDuration = textShortenIfTooLong(name);
                                    transportationDuration = name;
                                }
                            }else {

                                name = getSiteNameFromNet(lat, lng);

//                                transportationDuration = textShortenIfTooLong(name);
                                transportationDuration = name;
                            }

                            detectedSiteName = transportationDuration;

                            holder.duration.setText(transportationDuration);

                            int icon = getIconToShowTransportation(transportation);
                            holder.traffic.setImageResource(icon);

                        }catch (InterruptedException e){
                            Log.e(TAG, "InterruptedException", e);
                        }catch (ExecutionException e){
                            Log.e(TAG, "ExecutionException", e);
                        }catch (JSONException e){
                            Log.e(TAG, "JSONException", e);
                        }catch (IndexOutOfBoundsException e){

                        }

                        //if there is a sitename has been stored
                    }else{

                        Annotation annotation_sitename = annotations_sitename.get(annotations_sitename.size()-1);
                        String sitename = annotation_sitename.getContent();
                        holder.duration.setText(sitename);

                        int icon = getIconToShowTransportation(transportation);
                        holder.traffic.setImageResource(icon);
                    }

                    //if it isn't static set the text and icon directly
                }else if(transportation.equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_HASNT_DETECTED_FLAG)){

                    holder.duration.setText(transportation);

                    int icon = getIconToShowTransportation(transportation);
                    holder.traffic.setImageResource(icon);
                }else {

                    //set the transportation (from detected) icon and text
                    String activityName = getActivityNameFromTransportationString(transportation);

                    holder.duration.setText(activityName);

                    int icon = getIconToShowTransportation(transportation);
                    holder.traffic.setImageResource(icon);
                }
            }

            //change the line color to red if its session annotation hasn't been filled.
            if(annotations_label.size()==0){

                Log.d(TAG, "[storing sitename] there are no labels in the session.");

                GradientDrawable sd = new GradientDrawable();
                int backgroundColor = mContext.getResources().getColor(R.color.custom);
                int strokeColor = mContext.getResources().getColor(R.color.stroke);
                sd.setColor(backgroundColor);
                sd.setStroke(10, strokeColor);
                holder.cardView.setBackground(sd);
            }
            //TODO deprecated

            String currentWork = getResources().getString(labelingStudy.nctu.minuku.R.string.current_task);

            if(currentWork.equals("CAR")){

                holder.sessionType.setVisibility(View.VISIBLE);

                String typeNameInCHI = getSessionTypeNameInChinese(session.getType());

                holder.sessionType.setText(typeNameInCHI);
            }else{

                holder.sessionType.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Log.d(TAG, "mContext : " + mContext);

                    final LayoutInflater inflater = LayoutInflater.from(mContext);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    final View layout = inflater.inflate(R.layout.custom_dialog,null);
                    final Spinner Dspinner = (Spinner) layout.findViewById(R.id.spinner);
                    DchoosingSite = (Button) layout.findViewById(R.id.choosingSite);
                    final Button showMapButton = (Button) layout.findViewById(R.id.showMap);
                    final Button startTimeButton = (Button) layout.findViewById(R.id.startTime);
                    final Button endTimeButton = (Button) layout.findViewById(R.id.endTime);
                    final String[] activity = {"請選擇交通模式", "走路", "自行車", "汽車", "定點", "此移動不存在", "與上一個相同"};
                    final ArrayAdapter<String> activityList = new ArrayAdapter<>(mContext,
                            android.R.layout.simple_spinner_dropdown_item,
                            activity);

                    //Trick: https://stackoverflow.com/questions/5977735/setting-outer-variable-from-anonymous-inner-class
                    final Long[] modifiedStartTime = new Long[1];
                    modifiedStartTime[0] = startTime;

                    final Long[] modifiedEndTime = new Long[1];
                    modifiedEndTime[0] = endTime;

                    //get the availSite from the label
                    final String labeled_transportation = holder.duration.getText().toString();

                    Dspinner.setAdapter(activityList);
                    Dspinner.setSelection(getIndex(Dspinner, labeled_transportation));
                    Dspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                            String selectedItem = parent.getSelectedItem().toString();
                            String selectedItemTransportationName = getTransportationFromSelectedItem(selectedItem);

                            //show the button "DchoosingSite" when the user choose "定點", for choosing the real site
                            //otherwise, conceal the button
                            if(selectedItemTransportationName.equals("static") && selectedItem.equals("定點")){

                                DchoosingSite.setVisibility(View.VISIBLE);

                                String textFromDuration = holder.duration.getText().toString();

                                if(checkTheTextInSpinner(Dspinner, textFromDuration)){

                                    DchoosingSite.setText("請選擇地點");
                                }else{

                                    DchoosingSite.setText(holder.duration.getText());
                                }

                                DchoosingSite.setOnClickListener(new Button.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        //try catch the situation that the location hasn't been caught
                                        try {

                                            ArrayList<String> latlngs = DataHandler.getDataBySession(session.getId(), DBHelper.LOCATION_TABLE);
                                            String[] latlng_first = latlngs.get(0).split(Constants.DELIMITER);
                                            String latString = latlng_first[2];
                                            String lngString = latlng_first[3];

                                            double lat = Double.parseDouble(latString);
                                            double lng = Double.parseDouble(lngString);

                                            Intent intent = new Intent(mContext, PlaceSelection.class);

                                            Bundle latlng = new Bundle();
                                            latlng.putDouble("lat", lat);
                                            latlng.putDouble("lng", lng);
                                            latlng.putBoolean("fromTimeLineFlag", true);
                                            intent.putExtras(latlng);

                                            mContext.startActivity(intent);

                                        }catch (IndexOutOfBoundsException e){

                                            e.printStackTrace();
                                            Toast.makeText(mContext, "尚未抓到GPS", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{

                                DchoosingSite.setVisibility(View.INVISIBLE);
                                DchoosingSite.setText("請選擇地點");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    showMapButton.setOnClickListener(new Button.OnClickListener(){
                        @Override
                        public void onClick(View v) {

                            final LayoutInflater inflater = LayoutInflater.from(Timeline.this);
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Timeline.this);
                            final View layout = inflater.inflate(R.layout.splitedmap_dialog,null);

                            builder.setView(layout)
                                    .setPositiveButton("確認", null)
                                    .setNegativeButton("取消", null);

                            final AlertDialog mAlertDialog = builder.create();
                            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                                @Override
                                public void onShow(final DialogInterface dialogInterface) {

                                    showMapInDialog(mAlertDialog, layout, session);

                                    Log.d(TAG, "splittingLatlng : " + splittingLatlng);

                                    Button button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    button.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {

                                            if(IsSplitLocationChosen) {

                                                try {

                                                    //update the session into two different sessions
                                                    //1st session
                                                    DBHelper.updateSessionTable(session.getId(), session.getStartTime(), splittingTime);

                                                    //2nd session
                                                    Session lastSession = SessionManager.getLastSession();
                                                    int sessionCount = lastSession.getId();

                                                    int sessionId = (int) sessionCount + 1;
                                                    Session addedSession = session;
                                                    addedSession.setStartTime(splittingTime);
                                                    addedSession.setId(sessionId);

                                                    DBHelper.insertSessionTable(addedSession);

                                                    Log.d(TAG, "Current session id : "+session.getId());
                                                    Log.d(TAG, "Added session id : "+addedSession.getId());

                                                    //update session locations' session id,
                                                    //set the time after splittingTime to the new id
                                                    DBHelper.updateRecordsInSession(DBHelper.LOCATION_TABLE, splittingTime, session.getId(), addedSession.getId());
                                                }catch (ArrayIndexOutOfBoundsException e){

                                                }

                                                Toast.makeText(Timeline.this, "您的旅程已成功分開", Toast.LENGTH_SHORT).show();

                                                //reset the Timeline
                                                initTime();

                                                dialogInterface.dismiss();

                                            }else {

                                                Toast.makeText(Timeline.this, "請點擊地圖以選擇中斷點", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });

                            mAlertDialog.show();
                        }
                    });

                    startTimeButton.setText(startTimeString);
                    startTimeButton.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG,"startTime clicked");

                            final SimpleDateFormat sdf_HHmm = new SimpleDateFormat(Constants.DATE_FORMAT_HOUR_MIN);
                            String startTimeString_HHmm = ScheduleAndSampleManager.getTimeString(startTime, sdf_HHmm);

                            String[] date = startTimeString_HHmm.split(":");

                            int hour = Integer.parseInt(date[0]);//c.get(Calendar.HOUR_OF_DAY);
                            int minute = Integer.parseInt(date[1]);
                            new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener(){
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                    String hour=String.valueOf(hourOfDay);
                                    String min =String.valueOf(minute);

                                    if(hourOfDay<10)
                                        hour = "0" + String.valueOf(hourOfDay);

                                    if(minute<10)
                                        min = "0" + String.valueOf(minute);

                                    String HHmm = hour + ":" + min;
                                    long time = ScheduleAndSampleManager.getTimeInMillis(HHmm, sdf_HHmm);
                                    final SimpleDateFormat sdf_a_hhmm = new SimpleDateFormat(Constants.DATE_FORMAT_AMPM_HOUR_MIN);
                                    String a_hhmm = ScheduleAndSampleManager.getTimeString(time, sdf_a_hhmm);

                                    startTimeButton.setText(a_hhmm);

                                    String startTimeString = startTimeDate + " " + a_hhmm;

                                    SimpleDateFormat sdf_date_HHmma = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_AMPM_HOUR_MIN);

                                    long startTimeLabel = ScheduleAndSampleManager.getTimeInMillis(startTimeString, sdf_date_HHmma);

                                    modifiedStartTime[0] = startTimeLabel;

                                    Log.d(TAG, "[check time setting] StartTime " + ScheduleAndSampleManager.getTimeString(time));
                                    Log.d(TAG, "[check time setting] modified StartTime " + ScheduleAndSampleManager.getTimeString(modifiedStartTime[0]));

                                }
                            }, hour, minute, false).show();
                        }
                    });

                    endTimeButton.setText(endTimeString);
                    endTimeButton.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Log.d(TAG,"startTime clicked");

                            final SimpleDateFormat sdf_HHmm = new SimpleDateFormat(Constants.DATE_FORMAT_HOUR_MIN);
                            String endTimeString_HHmm = ScheduleAndSampleManager.getTimeString(endTime, sdf_HHmm);

                            String[] date = endTimeString_HHmm.split(":");

                            int hour = Integer.parseInt(date[0]);//c.get(Calendar.HOUR_OF_DAY);
                            int minute = Integer.parseInt(date[1]);
                            new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener(){
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                    String hour=String.valueOf(hourOfDay);
                                    String min =String.valueOf(minute);
                                    if(hourOfDay<10)
                                        hour = "0" + String.valueOf(hourOfDay);

                                    if(minute<10)
                                        min  = "0" + String.valueOf(minute);

                                    String HHmm = hour + ":" + min;
                                    long time = ScheduleAndSampleManager.getTimeInMillis(HHmm, sdf_HHmm);
                                    final SimpleDateFormat sdf_a_hhmm = new SimpleDateFormat(Constants.DATE_FORMAT_AMPM_HOUR_MIN);
                                    String a_hhmm = ScheduleAndSampleManager.getTimeString(time, sdf_a_hhmm);

                                    endTimeButton.setText(a_hhmm);

                                    String endTimeString = endTimeDate + " " + a_hhmm;

                                    SimpleDateFormat sdf_date_HHmma = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_AMPM_HOUR_MIN);

                                    long endTimeLabel = ScheduleAndSampleManager.getTimeInMillis(endTimeString, sdf_date_HHmma);

                                    modifiedEndTime[0] = endTimeLabel;

                                    Log.d(TAG, "[check time setting] EndTime " + ScheduleAndSampleManager.getTimeString(time));
                                    Log.d(TAG, "[check time setting] modified EndTime " + ScheduleAndSampleManager.getTimeString(modifiedEndTime[0]));

                                }
                            }, hour, minute, false).show();
                        }
                    });


                    final EditText Dannotation_goal = (EditText)layout.findViewById(R.id.ed_annotate_goal);
                    final EditText Dannotation_specialEvent = (EditText)layout.findViewById(R.id.ed_annotate_specialEvent);

                    Dannotation_goal.setText("");
                    Dannotation_specialEvent.setText("");

                    try {

                        AnnotationSet annotationSet = session.getAnnotationsSet();

                        ArrayList<Annotation> annotations_label = annotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_Label);
                        Annotation annotation_label = annotations_label.get(annotations_label.size() - 1);
                        String label = annotation_label.getContent();
                        JSONObject labelJson = new JSONObject(label);

                        String goal = labelJson.getString(Constants.ANNOTATION_Label_GOAL);
                        Dannotation_goal.setText(goal);
                        String specialEvent = labelJson.getString(Constants.ANNOTATION_Label_SPECIALEVENT);
                        Dannotation_specialEvent.setText(specialEvent);
                    }catch (IndexOutOfBoundsException e){
                        Log.d(TAG, "IndexOutOfBoundsException");
//                        e.printStackTrace();
                    }catch (JSONException e){
                        Log.d(TAG, "JSONException");
//                        e.printStackTrace();
                    }catch (NullPointerException e){
                        Log.d(TAG, "NullPointerException");
//                        e.printStackTrace();
                    }

                    builder.setView(layout)
                            .setPositiveButton(R.string.ok, null)
                            .setNegativeButton("Cancel", null);

                    final AlertDialog mAlertDialog = builder.create();
                    mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                        @Override
                        public void onShow(final DialogInterface dialogInterface) {
                            Button posButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            posButton.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {

                                    String selectedActivityString = Dspinner.getSelectedItem().toString();
                                    String goal = Dannotation_goal.getText().toString();
                                    String specialEvent = Dannotation_specialEvent.getText().toString();

                                    String sitename = "";

                                    if (selectedActivityString.equals("定點")) {

//                                        sitename = holder.duration.getText().toString();
                                        sitename = DchoosingSite.getText().toString();
                                        Log.d(TAG, "[storing sitename] Sitename going to store : "+ sitename);
                                    }

                                    long timeRangeCheck = modifiedStartTime[0] - modifiedEndTime[0];

                                    Log.d(TAG, "[check time setting] timeRangeCheck, modifiedStartTime = "+ ScheduleAndSampleManager.getTimeString(modifiedStartTime[0]));
                                    Log.d(TAG, "[check time setting] timeRangeCheck, modifiedEndTime = "+ ScheduleAndSampleManager.getTimeString(modifiedEndTime[0]));
                                    Log.d(TAG, "[check time setting] timeRangeCheck, timeRangeCheck > 0 ? "+ (timeRangeCheck > 0));

                                    if (selectedActivityString.equals("請選擇交通模式")) {

                                        Toast.makeText(mContext, "請選擇一項交通模式", Toast.LENGTH_SHORT).show();
                                    } else if(timeRangeCheck > 0){

                                        Toast.makeText(mContext, "請檢查您的時間設定", Toast.LENGTH_SHORT).show();
                                    } else {

                                        String startTimeaHHmmString = startTimeButton.getText().toString();
                                        String endTimeaHHmmString = endTimeButton.getText().toString();

                                        String startTimeString = startTimeDate + " " + startTimeaHHmmString;
                                        String endTimeString = endTimeDate + " " + endTimeaHHmmString;

                                        SimpleDateFormat sdf_date_HHmma = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_AMPM_HOUR_MIN);

                                        long startTimeLabel = ScheduleAndSampleManager.getTimeInMillis(startTimeString, sdf_date_HHmma);
                                        long endTimeLabel = ScheduleAndSampleManager.getTimeInMillis(endTimeString, sdf_date_HHmma);

                                        //judging that we need to update session id or not
                                        int sessionId = session.getId();
                                        AnnotationSet annotationSet = session.getAnnotationsSet();

                                        //now, we keep the same trip which is claimed by users with different Ids,
                                        //because it is _id, but we show them by checking their labels
                                        /*if(Dspinner.getSelectedItem().equals("與上一個相同")){

                                        }*/

                                        Annotation siteNameByDetectionAnnotation = new Annotation();

                                        siteNameByDetectionAnnotation.setContent(detectedSiteName);
                                        siteNameByDetectionAnnotation.addTag(Constants.ANNOTATION_TAG_DETECTED_SITENAME);
                                        annotationSet.addAnnotation(siteNameByDetectionAnnotation);

                                        //store the labels into the corresponding session
                                        Annotation labeledAnnotation = new Annotation();

                                        JSONObject labelJson = new JSONObject();

                                        try {

                                            labelJson.put(Constants.ANNOTATION_Label_TRANSPORTATOIN, selectedActivityString);
                                            labelJson.put(Constants.ANNOTATION_Label_GOAL, goal);
                                            labelJson.put(Constants.ANNOTATION_Label_SPECIALEVENT, specialEvent);
                                            labelJson.put(Constants.ANNOTATION_Label_SITENAME, sitename);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        labeledAnnotation.setContent(labelJson.toString());
                                        labeledAnnotation.addTag(Constants.ANNOTATION_TAG_Label);
                                        annotationSet.addAnnotation(labeledAnnotation);

                                        DataHandler.updateSession(sessionId, startTimeLabel, endTimeLabel, annotationSet, Constants.SESSION_SHOULD_BE_SENT_FLAG);


                                        //TODO deprecated
                                        //preparing the updated availSite to show after the notifyDataSetChanged
                                        /*try {

                                            mSessions = new ArrayList<>();

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                                mSessions = new ListSessionAsyncTask(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                            else
                                                mSessions = new ListSessionAsyncTask(mContext).execute().get();

                                            //conceal the red highlight circle
                                            GradientDrawable sd = new GradientDrawable();

                                            int backgroundColor = mContext.getResources().getColor(R.color.custom);
                                            sd.setColor(backgroundColor);
                                            holder.cardView.setBackground(sd);

                                        } catch (InterruptedException e) {
                                            Log.d(TAG, "InterruptedException");
//                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            Log.d(TAG, "ExecutionException");
//                                            e.printStackTrace();
                                        }*/

                                        initTime();

//                                        notifyDataSetChanged();

                                        sharedPrefs.edit().putInt("currentposition", position).apply();

                                        DchoosingSite.setVisibility(View.INVISIBLE); // set back to default

                                        Toast.makeText(mContext, "感謝您的填答", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                    }
                                }
                            });
                        }
                    });

                    mAlertDialog.show();
                }

            });

        }


        private void showMapInDialog(Dialog dialog, final View view, final Session currentSession){

            MapView mapView = (MapView) view.findViewById(R.id.mapView);
            Log.d(TAG, "mapView is existed ? " + (mapView != null));
            MapsInitializer.initialize(Timeline.this);

            mapView.onCreate(dialog.onSaveInstanceState());
            mapView.onResume();
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {

                    Log.d(TAG, "MapView onMapReady");
                    Log.d(TAG, "Is mSession existed ? " + (currentSession!=null));

                    if (currentSession!=null) {

                        showRecordingVizualization((int) currentSession.getId(), googleMap);

                        ArrayList<LatLng> points = getLocationPointsToDrawOnMap(currentSession.getId());
                        Log.d(TAG, "[test show trip] in onPostExecute, the poitns obtained are : " + points.size());
                        if (points.size()>0){

                            LatLng startLatLng  = points.get(0);
                            LatLng endLatLng = points.get(points.size()-1);
                            LatLng middleLagLng = points.get((points.size()/2));

                            Log.d(TAG, "[test show trips] the session is not in the currently recording session");
                            //we first need to know what visualization we want to use, then we get availSite for that visualization

                            //show maps with path (draw polylines)
                            showMapWithPaths(googleMap, points, middleLagLng, startLatLng, endLatLng);
                        }

                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {

                                //remove the current marker
                                if(currentMarkerKey != -1){

                                    try{

                                        Marker currentMarker = addedSplitMarker.get(currentMarkerKey);
                                        currentMarker.remove();
                                        addedSplitMarker.remove(currentMarkerKey);
                                    }catch (Exception e){

                                    }
                                }

                                Pair<Long, LatLng> closestLocationAndTime = getSplitTimeAndClosestLocation(latLng, currentSession);

                                splittingTime = closestLocationAndTime.first;
                                splittingLatlng = closestLocationAndTime.second;

                                Marker marker = googleMap.addMarker(new MarkerOptions()
                                        .position(splittingLatlng)
                                        .draggable(true).visible(true));;

                                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                                currentMarkerKey++;
                                addedSplitMarker.put(currentMarkerKey, marker);

                                IsSplitLocationChosen = true;
                            }
                        });

                    }
                }
            });

        }

        private Pair<Long, LatLng> getSplitTimeAndClosestLocation(final LatLng latLng, final Session currentSession){

            ArrayList<String> data = DataHandler.getDataBySession(currentSession.getId(), DBHelper.LOCATION_TABLE);
            Log.d(TAG, "[test show trip] get availSite id: " + currentSession.getId());

            Log.d(TAG, "[test show trip] get availSite: " + data.size() + " rows");

            long chosenTime = -999;

            double cutpointLat = latLng.latitude;
            double cutpointLng = latLng.longitude;
            double shortestDist = -1;

            double chosenLat = cutpointLat, chosenLng = cutpointLng;

            for (int i=0; i < data.size(); i++){

                String[] record = data.get(i).split(Constants.DELIMITER);

                double lat = Double.parseDouble(record[2]);
                double lng = Double.parseDouble(record[3]);

                float[] results = new float[1];
                Location.distanceBetween(cutpointLat, cutpointLng, lat, lng, results);

                if(shortestDist < 0){

                    shortestDist = results[0];
                    chosenLat = lat;
                    chosenLng = lng;
                    chosenTime = Long.valueOf(record[1]);
                }else{

                    Location.distanceBetween(cutpointLat, cutpointLng, lat, lng, results);
                    double currDist = results[0];

                    if(shortestDist > currDist){

                        shortestDist = currDist;
                        chosenLat = lat;
                        chosenLng = lng;
                        chosenTime = Long.valueOf(record[1]);
                    }
                }
            }

            final LatLng chosenLatlng = new LatLng(chosenLat, chosenLng);

            Pair<Long, LatLng> pair = new Pair<>(chosenTime, chosenLatlng);


            return pair;
        }

        public void showMapWithPaths(GoogleMap map, ArrayList<LatLng> points, LatLng cameraCenter, LatLng startLatLng, LatLng endLatLng) {

            //map option
            GoogleMapOptions options = new GoogleMapOptions();
            options.tiltGesturesEnabled(false);
            options.rotateGesturesEnabled(false);

            //center the map
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraCenter, 13));

            //draw linges between points and add end and start points
            PolylineOptions pathPolyLineOption = new PolylineOptions().color(Color.RED).geodesic(true);
            pathPolyLineOption.addAll(points);

            //draw lines
            Polyline path = map.addPolyline(pathPolyLineOption);

            //after getting the start and ened point of location trace, we put a marker
            map.addMarker(new MarkerOptions().position(startLatLng).title("Start"))
                    .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            map.addMarker(new MarkerOptions().position(endLatLng).title("End"));
        }

        private void showRecordingVizualization(final int sessionId, GoogleMap mGoogleMap){

            //draw map
            if (mGoogleMap!=null){

                //if we're reviewing a previous session ( the trip is not ongoing), get session from the database (note that we have to use session id to check instead of a session instance)
                if (!SessionManager.isSessionOngoing(sessionId)) {

                    //because there could be many points for already ended trace, so we use asynch to download the annotations
                    try{

                        ArrayList<LatLng> points = new ArrayList<>();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            points = new LoadLocationsAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sessionId).get();
                        else
                            points = new LoadLocationsAsyncTask().execute(sessionId).get();

                        if (points.size()>0){

                            LatLng startLatLng  = points.get(0);
                            LatLng endLatLng = points.get(points.size()-1);
                            LatLng middleLagLng = points.get((points.size()/2));

                            Log.d(TAG, "[test show trips] the session is not in the currently recording session");
                            //we first need to know what visualization we want to use, then we get availSite for that visualization

                            //show maps with path (draw polylines)
                            showMapWithPaths(mGoogleMap, points, middleLagLng, startLatLng, endLatLng);
                        }

                    } catch(InterruptedException e) {

                    } catch (ExecutionException e) {

                    }
                }
                //the recording is ongoing, so we periodically query the database to show the latest path
                else {

                    try{

                        //get location points to draw on the map..
                        ArrayList<LatLng> points = getLocationPointsToDrawOnMap(sessionId);

                        //we use endLatLng, which is the user's current location as the center of the camera
                        LatLng startLatLng, endLatLng;

                        //only has one point
                        if (points.size()==1){

                            startLatLng  = points.get(0);
                            endLatLng = points.get(0);

                            showMapWithPathsAndCurLocation(mGoogleMap, points, endLatLng);
                        }
                        //when have multiple locaiton points
                        else if (points.size()>1) {

                            startLatLng  = points.get(0);
                            endLatLng = points.get(points.size()-1);

                            showMapWithPathsAndCurLocation(mGoogleMap, points, endLatLng);
                        }


                    }catch (IllegalArgumentException e){

                    }
                }
            }
        }

        public ArrayList<LatLng> getLocationPointsToDrawOnMap(int sessionId) {

            ArrayList<LatLng> points = new ArrayList<>();

            //get availSite from the database
            ArrayList<String> data = DataHandler.getDataBySession(sessionId, DBHelper.LOCATION_TABLE);
            Log.d(TAG, "[test show trip] getLocationPointsToDrawOnMap get availSite:" + data.size() + "rows");

            for (int i=0; i<data.size(); i++){

                String[] record = data.get(i).split(Constants.DELIMITER);

                double lat = Double.parseDouble(record[2]);
                double lng = Double.parseDouble(record[3]);

                points.add(new LatLng(lat, lng));
            }

            return points;
        }

        public void showMapWithPathsAndCurLocation(GoogleMap map, ArrayList<LatLng> points, LatLng curLoc) {

            map.clear();

            //map option
            GoogleMapOptions options = new GoogleMapOptions();
            options.tiltGesturesEnabled(false);
            options.rotateGesturesEnabled(false);

            //get current zoom level
            float zoomlevel = map.getCameraPosition().zoom;

            //center the map
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 13));

            Marker me =  map.addMarker(new MarkerOptions()
                    .position(curLoc)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation))

            );

            //draw linges between points and add end and start points
            PolylineOptions pathPolyLineOption = new PolylineOptions().color(Color.RED).geodesic(true);
            pathPolyLineOption.addAll(points);

            //draw lines
            Polyline path = map.addPolyline(pathPolyLineOption);

        }

        private int getIndex(Spinner spinner, String myString){

            if(myString.equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_HASNT_DETECTED_FLAG))
                return 0;

            int index;

            for (int i=0;i<spinner.getCount();i++){

                if (spinner.getItemAtPosition(i).equals(myString)){

                    index = i;
                    return index;
                }
            }

            //return 定點 by default
            return 4;
        }

        private boolean checkTheTextInSpinner(Spinner spinner, String myString){

            for (int i=0;i<spinner.getCount();i++){

                if (spinner.getItemAtPosition(i).equals(myString)){

                    return true;
                }
            }

            //return 定點 by default
            return false;
        }

        public String getSiteNameFromNet(double lat, double lng) throws InterruptedException, ExecutionException, JSONException{

            String jsonInString, name;

            String url = GetUrl.getUrl(lat, lng);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                jsonInString = new HttpAsyncGetSiteTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        url
                ).get();
            else
                jsonInString = new HttpAsyncGetSiteTask().execute(
                        url
                ).get();

            JSONObject jsonObject = new JSONObject(jsonInString);
            JSONArray results = jsonObject.getJSONArray("results");
            //default now we choose the second index from the json.(first index is ken(縣名) name.)
            name = results.getJSONObject(1).getString("name");

            return name;
        }

        @Override
        public int getItemCount() {
            return mSessions.size();
        }


        //use Asynk task to load sessions
        private class LoadLocationsAsyncTask extends AsyncTask<Integer, Void, ArrayList<LatLng>> {

            @Override
            protected ArrayList<LatLng> doInBackground(Integer... params) {

                int sessionId = params[0];

                ArrayList<LatLng> points = getLocationPointsToDrawOnMap(sessionId);

                return points;
            }

            // can use UI thread here
            @Override
            protected void onPreExecute() {
                //Log.d(TAG, "[test show trip] onPreExecute ");
            }

            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(ArrayList<LatLng> points) {
                super.onPostExecute(points);

                Log.d(TAG, "[test show trip] in onPostExecute, the poitns obtained are : " + points.size());
            }
        }

    }

    private String getSessionTypeNameInChinese(String selectedItem){

        switch (selectedItem){

            case Constants.SESSION_TYPE_DETECTED_BY_SYSTEM:
                return "系統偵測";
            case Constants.SESSION_TYPE_DETECTED_BY_USER:
                return "手動偵測";
            default:
                return "未知";
        }
    }

    private String getTransportationFromSelectedItem(String selectedItem){

        switch (selectedItem){

            case "走路":
                return "on_foot";
            case "自行車":
                return "on_bicycle";
            case "汽車":
                return "in_vehicle";
            default:
                return "static";
        }
    }

    private String getTransportationFromSpinnerItem(String selectedTransportation){

        switch (selectedTransportation){
            case "走路":
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_FOOT;
            case "自行車":
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_BICYCLE;
            case "汽車":
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_IN_VEHICLE;
            case "定點":
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION;
            case "此移動不存在":
                return "此移動不存在";
            case "與上一個相同":
                return "與上一個相同";
            default:
                return "Unknown";
        }

    }

    private String getActivityNameFromTransportationString(String transportation){

        switch (transportation){
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_FOOT:
            case "走路":
//                return "walk";
                return "走路";
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_BICYCLE:
            case "自行車":
//                return "bike";
                return "自行車";
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_IN_VEHICLE:
            case "汽車":
//                return "car";
                return "汽車";

            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION:
            case "定點":
                return "定點（未知地點）";

            default:
                return transportation;
        }
    }

    private String getTransportationNameInChinese(String transportation){

        switch (transportation){
            case "walk":
                return "走路";
            case "bike":
                return "騎自行車";
            case "car":
                return "汽車";
            default:
                return "定點（未知地點）";
        }
    }

    private int getIconToShowTransportation(String transportation){
        switch (transportation){
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_FOOT:
                return R.drawable.walk;
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_BICYCLE:
                return R.drawable.bike;
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_IN_VEHICLE:
                return R.drawable.car;
            case "與上一個相同":
                return R.drawable.transparent;
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_HASNT_DETECTED_FLAG:
                return R.drawable.question_mark;
            case "此移動不存在":
                return R.drawable.close_black;
            default:
                return R.drawable.if_94_171453;
        }

    }

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

            json = sb.toString();
            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    /**
     * Load Session Data from the SessionManager
     */
    private class ListSessionAsyncTask extends AsyncTask<String, Void, ArrayList<Session> > {

        private Context mContext;

        public ListSessionAsyncTask(Context context){

            mContext = context;

            Log.d(TAG, "ListSessionAsyncTask mContext : "+mContext);
        }


        @Override
        protected void onPreExecute() {

            Log.d(TAG,"[test show trip] onPreExecute");
        }


        @Override
        protected void onPostExecute(ArrayList<Session> sessions) {

            mSessions = sessions;

            Log.d(TAG, "[test show trip] on post return sessions " + mSessions);

        }

        @Override
        protected ArrayList<Session> doInBackground(String... params) {

            ArrayList<Session> sessions = new ArrayList<Session>();

            try {

//                sessions = SessionManager.getRecentSessions();

                SimpleDateFormat sdf_date = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY);
                long todayStartLong = ScheduleAndSampleManager.getTimeInMillis(dateToQuery, sdf_date);
                long todayEndLong = todayStartLong + Constants.MILLISECONDS_PER_DAY;

                Log.d(TAG, "todayStart : " + ScheduleAndSampleManager.getTimeString(todayStartLong));
                Log.d(TAG, "todayEnd : " + ScheduleAndSampleManager.getTimeString(todayEndLong));

                sessions = SessionManager.getSessionsBetweenTimesAndOrder(todayStartLong, todayEndLong, timelineOrder);

                Log.d(TAG, "queried sessions size : " + sessions.size());

            }catch (Exception e) {
                Log.d(TAG,"Exception");
                e.printStackTrace();
            }

            return sessions;
        }
    }

}
