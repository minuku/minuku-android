package labelingStudy.nctu.minuku_2.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.DBHelper.DataHandler;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minuku_2.NearbyPlaces.GetUrl;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.Utils;


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

    ArrayList<Session> mSessions;

    private SharedPreferences sharedPrefs;

    private boolean firstTimeOrNot;

    private String timelineOrder;

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

        timelineOrder = sharedPrefs.getString("timelineOrder", "DESC");

        if(firstTimeOrNot) {
            startpermission();
            firstTimeOrNot = false;
            sharedPrefs.edit().putBoolean("firstTimeOrNot", firstTimeOrNot).apply();
        }

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

        if(timelineOrder.equals("DESC")){

            item.setTitle("從舊到新");
        }else if (timelineOrder.equals("ASC")) {

            item.setTitle("從新到舊");
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_getWantedOrder:

                if(item.getTitle().equals("從舊到新")){

                    sharedPrefs.edit().putString("timelineOrder", "ASC").apply();

                    item.setTitle("從新到舊");
                }else if (item.getTitle().equals("從新到舊")){

                    sharedPrefs.edit().putString("timelineOrder", "DESC").apply();

                    item.setTitle("從舊到新");
                }

                timelineOrder = sharedPrefs.getString("timelineOrder", "DESC");

                initTime();

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                mSessions = new ListSessionAsyncTask(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            else
                mSessions = new ListSessionAsyncTask(mContext).execute().get();

            //TODO find better method
            if(mSessions.size() > 0){

                TimelineAdapter timelineAdapter = new TimelineAdapter(mSessions);
                RecyclerView mList = (RecyclerView) findViewById(R.id.list_view);

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

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView time, duration, date;
            public TimelineView lineView;
            public LinearLayout cardbackground;
            public android.support.v7.widget.CardView cardView;
            public ImageView traffic;
            public View car_line;
            public ViewHolder(View v) {
                super(v);

                time = (TextView) v.findViewById(R.id.tv_time);
                duration = (TextView) v.findViewById(R.id.tv_duration);
                date = (TextView) v.findViewById(R.id.tv_date);
                traffic = (ImageView) v.findViewById(R.id.iv_traffic);
                lineView = (TimelineView) v.findViewById(R.id.time_marker);
                cardView = (android.support.v7.widget.CardView) v.findViewById(R.id.cardview);
                cardbackground = (LinearLayout) v.findViewById(R.id.cardbackground);
                car_line = (View) v.findViewById(R.id.CAR_line);
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
            if(SessionManager.getOngoingSessionIdList().contains(session.getId())){

                endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();

                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_NO_ZONE_Slash);
                Log.d(TAG, "[test show Timeline] ongoing endTime : "+ ScheduleAndSampleManager.getTimeString(endTime, sdf));
            }else{

                endTime = session.getEndTime();

                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_NO_ZONE_Slash);
                Log.d(TAG, "[test show Timeline] endTime : "+ ScheduleAndSampleManager.getTimeString(endTime, sdf));
            }

            SimpleDateFormat sdf_hhmm = new SimpleDateFormat(Constants.DATE_FORMAT_AMPM_HOUR_MIN);

            final String startTimeString = ScheduleAndSampleManager.getTimeString(startTime, sdf_hhmm);
            final String endTimeString = ScheduleAndSampleManager.getTimeString(endTime, sdf_hhmm);

            holder.time.setText(startTimeString+"-"+endTimeString);

            SimpleDateFormat sdf_slash = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY_Slash);
            final String date = ScheduleAndSampleManager.getTimeString(startTime, sdf_slash);

            holder.date.setText(date);

            SimpleDateFormat sdf_date = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY);

            final String startTimeDate = ScheduleAndSampleManager.getTimeString(startTime, sdf_date);
            final String endTimeDate = ScheduleAndSampleManager.getTimeString(endTime, sdf_date);


            //if it was pressed by the user show the line
            if(session.isUserPress())
                holder.car_line.setVisibility(View.VISIBLE);

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
                String label_Transportation = "";
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

                ArrayList<Annotation> annotations = annotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATOIN_ACTIVITY);
                Annotation annotation = annotations.get(annotations.size()-1);
                String transportation = annotation.getContent();

                //if it is static check the sitename
                if(transportation.equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION)){
                    ArrayList<Annotation> annotations_sitename = annotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_DETECTED_SITENAME);

                    //if there is no sitename has been stored
                    if(annotations_sitename.size()==0){

                        //get the site from the google service
                        try {

                            String name = "";
                            //get location by session id
                            ArrayList<String> latlngs = DataHandler.getDataBySession(session.getId(), DBHelper.location_table);

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

                //if the trip is "此移動不存在", do not show it
            }else if(labelJson.has(Constants.ANNOTATION_Label_TRANSPORTATOIN)){
                try{

                    String label_transportation = labelJson.getString(Constants.ANNOTATION_Label_TRANSPORTATOIN);

                    if(label_transportation.equals("此移動不存在")){

                        holder.cardbackground.setVisibility(View.GONE);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
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
                    final Button DstartTime = (Button) layout.findViewById(R.id.startTime);
                    final Button DendTime = (Button) layout.findViewById(R.id.endTime);
                    final String[] activity = {"請選擇交通模式", "走路", "自行車", "汽車", "定點", "此移動不存在", "與上一個相同"};
                    final ArrayAdapter<String> activityList = new ArrayAdapter<>(mContext,
                            android.R.layout.simple_spinner_dropdown_item,
                            activity);

                    //get the data from the label
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

                                String testFromDuration = holder.duration.getText().toString();

                                if(checkTheTextInSpinner(Dspinner, testFromDuration)){

                                    DchoosingSite.setText("請選擇地點");
                                }else{

                                    DchoosingSite.setText(holder.duration.getText()); //+"請選擇地點"
                                }

                                DchoosingSite.setOnClickListener(new Button.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        //try catch the situation that the location hasn't been caught
                                        try {

                                            ArrayList<String> latlngs = DataHandler.getDataBySession(session.getId(), DBHelper.location_table);
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

                    DstartTime.setText(startTimeString);
                    DstartTime.setOnClickListener(new Button.OnClickListener() {
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

                                    DstartTime.setText(a_hhmm);

                                }
                            }, hour, minute, false).show();
                        }
                    });

                    DendTime.setText(endTimeString);
                    DendTime.setOnClickListener(new Button.OnClickListener() {
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

                                    DendTime.setText(a_hhmm);

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
                            .setPositiveButton(R.string.ok, null);

                    final AlertDialog mAlertDialog = builder.create();
                    mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                        @Override
                        public void onShow(final DialogInterface dialogInterface) {
                            Button button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(new View.OnClickListener() {

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

                                    if (selectedActivityString.equals("請選擇交通模式")) {

                                        Toast.makeText(mContext, "請選擇一項交通模式", Toast.LENGTH_SHORT).show();
                                    } else {

                                        String startTimeaHHmmString = DstartTime.getText().toString();
                                        String endTimeaHHmmString = DendTime.getText().toString();

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

                                        DataHandler.updateSession(sessionId, startTimeLabel, endTimeLabel, annotationSet);


                                        //preparing the updated data to show after the notifyDataSetChanged
                                        try {

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
                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            Log.d(TAG, "ExecutionException");
                                            e.printStackTrace();
                                        }

                                        notifyDataSetChanged();

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

        private int getIndex(Spinner spinner, String myString){

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

        public String textShortenIfTooLong(String name){

            String shortenedName = "";

            if(name.length() < 6){

                return name;
            }else{

                String subname = name.substring(0, 6);

                if(Utils.isEnglish(subname)){

                    if(name.length() == 6){

                        shortenedName = subname;
                    }else{

                        shortenedName = subname + "...";
                    }

                    return shortenedName;

                    //only show first five words in Chinese
                }else{

                    shortenedName = name.substring(0, 5)+"...";
                }
            }

            return shortenedName;
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

        private ProgressDialog dialog;
        private Context mContext;

        public ListSessionAsyncTask(Context context){

            mContext = context;

            Log.d(TAG, "ListSessionAsyncTask mContext : "+mContext);
        }


        @Override
        protected void onPreExecute() {

            Log.d(TAG,"[test show trip] onPreExecute");
            dialog = ProgressDialog.show(mContext, "","Loading...",true,false);
        }


        @Override
        protected void onPostExecute(ArrayList<Session> sessions) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            mSessions = sessions;

            Log.d(TAG, "[test show trip] on post return sessions " + mSessions);

        }

        @Override
        protected ArrayList<Session> doInBackground(String... params) {

            ArrayList<Session> sessions = new ArrayList<Session>();

            try {

//                sessions = SessionManager.getRecentSessions();
                sessions = SessionManager.getSessionsByOrder(timelineOrder);
            }catch (Exception e) {
                Log.d(TAG,"Exception");
                e.printStackTrace();
            }

            return sessions;
        }
    }
}
