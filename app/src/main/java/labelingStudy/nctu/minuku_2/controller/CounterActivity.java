package labelingStudy.nctu.minuku_2.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuNotificationManager;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.Utils;

import static labelingStudy.nctu.minuku_2.controller.Timer_move.TrafficFlag;

//import edu.ohio.minuku_2.R;

/**
 * Created by Lawrence on 2017/4/20.
 */

public class CounterActivity extends AppCompatActivity {

    final private String TAG = "CounterActivity";

    private Context mContext;
    private LayoutInflater mInflater;

    private String siteName;
    public static TextView counter;
    public static String stoptime, starttime;
    private int tsec = 0, csec = 0, cmin = 0, chour = 0;
    public static SimpleDateFormat formatter;

    public static ImageButton play_stop;
    public static ImageView traffic;

    Timer timer;

    public static Button changedMovement;

    private SharedPreferences sharedPrefs;

    private final int SHOW_PLAY_BUTTON = 1;
    private final int SHOW_STOP_BUTTON = 2;

    private final String NotASite = "NotASite";

    NotificationManager mNotificationManager;

    public CounterActivity(){}

    public CounterActivity(Context mContext){
        this.mContext = mContext;
    }

    public CounterActivity(LayoutInflater mInflater){
        this.mInflater = mInflater;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.counteractivtiy);

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public void onResume(){
        super.onResume();

        Log.d(TAG, "onResume");

        initCounterActivity();
    }

    public void initCounterActivity(){

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE);

        try {

            siteName = getIntent().getExtras().getString("SiteName", NotASite);
            Log.d(TAG, "[test site] siteName : " + siteName);
        }catch (NullPointerException e){

            siteName = NotASite;
        }

        counter = (TextView) findViewById(R.id.tv_timer);
        traffic = (ImageView)findViewById(R.id.iv_traffic);

        changedMovement = (Button) findViewById(R.id.changedMovement);
        changedMovement.setOnClickListener(changedMoving);


        play_stop = (ImageButton) findViewById(R.id.btn_play_stop);

        //we set play button for default
        int play_stopStartTag = sharedPrefs.getInt("play_stopTag", SHOW_PLAY_BUTTON);
        play_stop.setTag(play_stopStartTag);

        Log.d(TAG, "play_stop button's Tag : "+play_stop.getTag());

        switch (play_stopStartTag){

            case SHOW_PLAY_BUTTON:

                play_stop.setImageResource(R.drawable.icon_play);
                break;
            case SHOW_STOP_BUTTON:

                int ongoingId = SessionManager.getOngoingSessionIdList().get(0);
                Session ongoingSession = SessionManager.getSession(ongoingId);

                long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
                long startTimeOngoingSession = ongoingSession.getStartTime();
                long ongoingSec = (currentTime - startTimeOngoingSession)/Constants.MILLISECONDS_PER_SECOND;

                tsec = (int) ongoingSec;

                //if last time we leave the page with the stop button, continue the timer's work.
                startTimer();

                play_stop.setImageResource(R.drawable.icon_stop);
                break;
        }

        play_stop.setOnClickListener(play_StopListener);

        formatter = new SimpleDateFormat(Constants.DATE_FORMAT_HOUR_MIN_SECOND);

        setColorForActivity();

    }

    private void updateOngoingNotification(){

        ArrayList<Integer> ongoingSessionIdList = SessionManager.getOngoingSessionIdList();

        if(ongoingSessionIdList.size() > 0){
            int sessionId = ongoingSessionIdList.get(0);
            Session ongoingSession = SessionManager.getSession(sessionId);

            AnnotationSet ongoingAnnotationSet = ongoingSession.getAnnotationsSet();
            ArrayList<Annotation> ongoingAnnotations = ongoingAnnotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATOIN_ACTIVITY);
            Annotation ongoingAnnotation = ongoingAnnotations.get(ongoingAnnotations.size()-1);
            String ongoingActivity = ongoingAnnotation.getContent();

            ongoingActivity = Utils.getActivityStringType(ongoingActivity);

            MinukuNotificationManager.ongoingNotificationText = ongoingActivity + " is recording";
        }else {
            MinukuNotificationManager.ongoingNotificationText = Constants.RUNNING_APP_DECLARATION;
        }

        Notification note = getOngoingNotification(MinukuNotificationManager.ongoingNotificationText);

        // using the same tag and Id causes the new notification to replace an existing one
        mNotificationManager.notify(MinukuNotificationManager.ongoingNotificationID, note);
//        note.flags |= Notification.FLAG_NO_CLEAR;

    }

    private Notification getOngoingNotification(String text){

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(Constants.APP_NAME);
        bigTextStyle.bigText(text);

        Intent resultIntent = new Intent(this, Dispatch.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(this);

        return noti.setContentTitle(Constants.APP_NAME)
                .setContentText(text)
                .setStyle(bigTextStyle)
                .setSmallIcon(getNotificationIcon(noti))
                .setContentIntent(pending)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
    }

    private int getNotificationIcon(Notification.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            notificationBuilder.setColor(Color.TRANSPARENT);
            return R.drawable.muilab_icon_noti;

        }
        return R.drawable.muilab_icon;
    }

    private void setColorForActivity(){

        switch (TrafficFlag){

            case "walk":
                traffic.setImageResource(R.drawable.walk);
                changedMovement.setTextColor(ContextCompat.getColor(this, R.color.instabug_annotation_color_yellow));
                play_stop.setBackgroundColor(ContextCompat.getColor(this, R.color.instabug_annotation_color_yellow));
                break;
            case "bike":
                traffic.setImageResource(R.drawable.bike);
                changedMovement.setTextColor(ContextCompat.getColor(this, R.color.reject_button));
                play_stop.setBackgroundColor(ContextCompat.getColor(this, R.color.reject_button));
                break;
            case "car":
                traffic.setImageResource(R.drawable.car);
                changedMovement.setTextColor(ContextCompat.getColor(this, R.color.instabug_annotation_color_blue));
                play_stop.setBackgroundColor(ContextCompat.getColor(this, R.color.instabug_annotation_color_blue));
                break;
            case "site":
                traffic.setImageResource(R.drawable.if_94_171453);
                changedMovement.setTextColor(ContextCompat.getColor(this, R.color.colorAccentDark));
                play_stop.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                break;

        }

    }

    //to view Timer_move
    private Button.OnClickListener changedMoving = new Button.OnClickListener() {
        public void onClick(View v) {
            Log.e(TAG,"changedMoving clicked");

            userLeavingPage();

            if(isTaskRoot()){
                startActivity(new Intent(CounterActivity.this, WelcomeActivity.class));
            }

            CounterActivity.this.finish();

        }
    };

    private View.OnClickListener play_StopListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            Log.d(TAG, "view Tag : "+ view.getTag());

            int whichToShowTag = Integer.valueOf(view.getTag().toString());

            switch (whichToShowTag){

                case SHOW_PLAY_BUTTON:

                    playSession();

                    view.setTag(SHOW_STOP_BUTTON);
                    play_stop.setImageResource(R.drawable.icon_stop);
                    sharedPrefs.edit().putInt("play_stopTag", SHOW_STOP_BUTTON).apply();

                    updateOngoingNotification();

                    break;

                case SHOW_STOP_BUTTON:

                    stopSession();

                    view.setTag(SHOW_PLAY_BUTTON);
                    play_stop.setImageResource(R.drawable.icon_play);
                    sharedPrefs.edit().putInt("play_stopTag", SHOW_PLAY_BUTTON).apply();

                    updateOngoingNotification();

                    break;

            }

        }
    };

    private void startTimer(){

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {

                tsec++;
                sharedPrefs.edit().putInt("tsec", tsec).apply();

                Message message = new Message();
                message.what =1;
                handler.sendMessage(message);
            }
        }, 0,1000);
    }

    private void endTimer(){

        timer.cancel();
    }

    private void playSession(){

        tsec = sharedPrefs.getInt("tsec", tsec);

        startTimer();

        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
        starttime = formatter.format(curDate);

        ArrayList<Integer> ongoingSessionIdList = SessionManager.getOngoingSessionIdList();

        //if there hasn't a ongoing sessoin, start a new one.
        if(ongoingSessionIdList.size() == 0){
            //start new Trip
            String transportation = getTrafficActivityString();

            int sessionCount = SessionManager.getNumOfSession();

            //insert into the session table;
            int sessionId = sessionCount + 1;
            Session session = new Session(sessionId);
            session.setStartTime(ScheduleAndSampleManager.getCurrentTimeInMillis());
            Annotation annotation = new Annotation();
            annotation.setContent(transportation);
            annotation.addTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATOIN_ACTIVITY);
            session.addAnnotation(annotation);

            //if there is a sitename, add into the session
            if(transportation.equals(TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_IN_NO_TRANSPORTATION)){

                Annotation annotationSiteName = new Annotation();
                annotationSiteName.setContent(siteName);
                annotationSiteName.addTag(Constants.ANNOTATION_TAG_SITENAME);
                session.addAnnotation(annotationSiteName);
            }

            SessionManager.startNewSession(session);
        }
    }

    private void stopSession(){

        //stop the timer first
        endTimer();
//        timer.cancel();
//        timer.purge();

        sharedPrefs.edit().putInt("tsec", 0).apply();


        Date curDate2 = new Date(System.currentTimeMillis()); // 獲取當前時間
        stoptime = formatter.format(curDate2);

        counter.setText("00:00:00");

        //stop the session
        Session lastSession = SessionManager.getLastSession();
        long endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
        lastSession.setEndTime(endTime);

        Log.d(TAG, "[test show trip] lastSession id : "+lastSession.getId());

        //end the current session
        //try catch the situation that no session occur
        try {
            SessionManager.endCurSession(lastSession);
        } catch (IndexOutOfBoundsException e) {
//                e.printStackTrace();
        }

    }

    public void userLeavingPage(){
        int play_stopTag = Integer.valueOf(play_stop.getTag().toString());

        sharedPrefs.edit().putInt("play_stopTag", play_stopTag).apply();

        //if there have a ongoing session, close it.
        if(play_stopTag==SHOW_STOP_BUTTON){

            //close the timer, we will show the timer when the user reenter it.
            endTimer();
            sharedPrefs.edit().putInt("tsec", tsec).apply();
        }

    }

    public void onPause(){
        super.onPause();

//        Log.d(TAG, "onPause");
        sharedPrefs.edit().putString("lastActivity", getClass().getName()).apply();

        userLeavingPage();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            userLeavingPage();

            CounterActivity.this.finish();

            if(isTaskRoot()){
                startActivity(new Intent(this, WelcomeActivity.class));
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onUserLeaveHint() {
        super.onUserLeaveHint();

        userLeavingPage();
    }

    //TimerTask無法直接改變元件因此要透過Handler來當橋樑
    private Handler handler = new Handler(){
        public  void  handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    csec=tsec%60;
                    cmin=tsec/60;
                    chour=tsec/3600;
                    String s="";
                    if(chour < 10){
                        s="0"+chour;
                    }else{
                        s=""+chour;
                    }
                    if(cmin < 10){
                        s=s+":0"+cmin;
                    }else{
                        s=s+":"+cmin;
                    }
                    if(csec < 10){
                        s=s+":0"+csec;
                    }else{
                        s=s+":"+csec;
                    }

                    //s字串為00:00:00格式
                    counter.setText(s);
                    break;
            }

        }
    };

    private String getTrafficActivityString(){
        if(TrafficFlag.equals("walk")){
            return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_FOOT;
        }else if(TrafficFlag.equals("bike")){
            return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_BICYCLE;
        }else if(TrafficFlag.equals("car")){
            return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_IN_VEHICLE;
        }

        return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_IN_NO_TRANSPORTATION;
    }

}
