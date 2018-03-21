package labelingStudy.nctu.minuku_2.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.service.TransportationModeService;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.service.CheckpointAndReminderService;

import static labelingStudy.nctu.minuku_2.controller.PlaceSelection.MarkerFlag;
import static labelingStudy.nctu.minuku_2.controller.Timer_move.BigFlag;
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
    public static String result, duration;
    public static String stoptime, starttime;
    private int tsec = 0, csec = 0, cmin = 0, chour = 0;
    public static boolean startflag = false;
    public static boolean recordflag = false;
    public static boolean stopflag = false;
    public static boolean zeroflag = false;
    public static boolean FirstPlayFlag = true;
    public static boolean CountFlag = true; //countung situation --- true:stop, false:ongoing
    public static SimpleDateFormat formatter;

    public static ImageButton  play, pause, stop;
    public static ImageView traffic;
    Timer timer01 = new Timer();

    public static TextView recentworktext; //for other class to improve it
    public static Button edituractivity
                  ,calories,distance,stepcount
                  ,move,site;

    private SharedPreferences sharedPrefs;
    private static SharedPreferences.Editor editor;

    public CounterActivity(){}

    public CounterActivity(Context mContext){
        this.mContext = mContext;
    }

    public CounterActivity(LayoutInflater mInflater){
        this.mInflater = mInflater;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.counteractivtiy);

    }

    @Override
    public void onResume(){
        super.onResume();

//        initCounterActivity();

    }

    public void initCounterActivity(){

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE);

        editor = mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE).edit();

        counter = (TextView) findViewById(R.id.tv_timer);
        play = (ImageButton) findViewById(R.id.btn_play);
//        pause = (ImageButton) findViewById(R.id.btn_pause);
        stop = (ImageButton) findViewById(R.id.btn_stop);
        move = (Button) findViewById(R.id.move);
//        site = (Button) findViewById(R.id.site);
        traffic = (ImageView)findViewById(R.id.iv_traffic);
//        traffic.setVisibility(View.INVISIBLE);

        move.setOnClickListener(moving);
//        site.setOnClickListener(siting);

        //Button監聽
        play.setOnClickListener(listener.get());
//        pause.setOnClickListener(listener);
        stop.setOnClickListener(listener.get());

        //宣告Timer
        if(CountFlag){
//            Timer timer01 =new Timer();

            //設定Timer(task為執行內容，0代表立刻開始,間格1秒執行一次)
            timer01.schedule(task, 0,1000);
        }

        formatter = new SimpleDateFormat("HH:mm:ss");

        if(TrafficFlag.equals("walk")){
            traffic.setImageResource(R.drawable.walk);
            move.setTextColor(ContextCompat.getColor(mContext, R.color.instabug_annotation_color_yellow));
            play.setBackgroundColor(ContextCompat.getColor(mContext, R.color.instabug_annotation_color_yellow));
            stop.setBackgroundColor(ContextCompat.getColor(mContext, R.color.instabug_annotation_color_yellow));
        }else if(TrafficFlag.equals("bike")){
            traffic.setImageResource(R.drawable.bike);
            move.setTextColor(ContextCompat.getColor(mContext, R.color.reject_button));
            play.setBackgroundColor(ContextCompat.getColor(mContext, R.color.reject_button));
            stop.setBackgroundColor(ContextCompat.getColor(mContext, R.color.reject_button));
        }else if(TrafficFlag.equals("car")){
            traffic.setImageResource(R.drawable.car);
            move.setTextColor(ContextCompat.getColor(mContext, R.color.step_pager_previous_tab_color));
            play.setBackgroundColor(ContextCompat.getColor(mContext, R.color.step_pager_previous_tab_color));
            stop.setBackgroundColor(ContextCompat.getColor(mContext, R.color.step_pager_previous_tab_color));
        }else if(TrafficFlag.equals("site")){
            traffic.setImageResource(R.drawable.if_94_171453);
            move.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccentDark));
            play.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            stop.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        }

    }

    public void initCounterActivity(View v, String siteName){

        editor = mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE).edit();

        this.siteName = siteName;

        counter = (TextView) v.findViewById(R.id.tv_timer);
        play = (ImageButton) v.findViewById(R.id.btn_play);
//        pause = (ImageButton) v.findViewById(R.id.btn_pause);
        stop = (ImageButton) v.findViewById(R.id.btn_stop);
        move = (Button) v.findViewById(R.id.move);
//        site = (Button) v.findViewById(R.id.site);
        traffic = (ImageView)v.findViewById(R.id.iv_traffic);

        move.setOnClickListener(moving);
//        site.setOnClickListener(siting);

        //Button監聽
        play.setOnClickListener(listener.get());
//        pause.setOnClickListener(listener);
        stop.setOnClickListener(listener.get());

//        Toast toast = Toast.makeText(mContext, "CountFlag" + CountFlag, Toast.LENGTH_SHORT).show();

        //宣告Timer
        if(CountFlag){
//            Timer timer01 =new Timer();

            //設定Timer(task為執行內容，0代表立刻開始,間格1秒執行一次)
            timer01.schedule(task, 0,1000);
        }

        formatter = new SimpleDateFormat("HH:mm:ss");

        if(TrafficFlag.equals("walk")){
            traffic.setImageResource(R.drawable.walk);
            move.setTextColor(ContextCompat.getColor(mContext, R.color.instabug_annotation_color_yellow));
            play.setBackgroundColor(ContextCompat.getColor(mContext, R.color.instabug_annotation_color_yellow));
            stop.setBackgroundColor(ContextCompat.getColor(mContext, R.color.instabug_annotation_color_yellow));
        }else if(TrafficFlag.equals("bike")){
            traffic.setImageResource(R.drawable.bike);
            move.setTextColor(ContextCompat.getColor(mContext, R.color.reject_button));
            play.setBackgroundColor(ContextCompat.getColor(mContext, R.color.reject_button));
            stop.setBackgroundColor(ContextCompat.getColor(mContext, R.color.reject_button));
        }else if(TrafficFlag.equals("car")){
            traffic.setImageResource(R.drawable.car);
            move.setTextColor(ContextCompat.getColor(mContext, R.color.instabug_annotation_color_blue));
            play.setBackgroundColor(ContextCompat.getColor(mContext, R.color.step_pager_previous_tab_color));
            stop.setBackgroundColor(ContextCompat.getColor(mContext, R.color.step_pager_previous_tab_color));
        }else if(TrafficFlag.equals("site")){
            traffic.setImageResource(R.drawable.if_94_171453);
            move.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccentDark));
            play.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            stop.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        }

    }

    //to view Timer_move
    private Button.OnClickListener moving = new Button.OnClickListener() {
        public void onClick(View v) {
            Log.e(TAG,"move clicked");
            if(!stopflag){
                Toast toast = Toast.makeText(mContext, "You need to stop the timer before changing  to different ways of movement", Toast.LENGTH_LONG);
                toast.show();
            }else{

                CounterActivity.this.finish();
//                mContext.startActivity(new Intent(mContext, Timer_move.class));
            }

        }
    };

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
                    if(chour <10){
                        s="0"+chour;
                    }else{
                        s=""+chour;
                    }
                    if(cmin <10){
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

    private TimerTask task = new TimerTask(){

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (startflag){
                //如果startflag是true則每秒tsec+1
                tsec++;
                Message message = new Message();

                //TODO session: keep getting data from DB
                //TODO let it run

                //update the session data to Sqlite
                /*TripManager.sessionid = TripManager.sessionid + ", " + String.valueOf(TripManager.sessionid_unStatic);

                //in PART lat lng acc is useless with the trip data
                LocationDataRecord record = new LocationDataRecord(
                        TripManager.sessionid,
                        999,
                        999,
                        999);

                Log.d(TAG, record.getCreationTime() + "," +
                        record.getSessionid() + "," +
                        record.getLatitude() + "," +
                        record.getLongitude() + "," +
                        record.getAccuracy());

                // store to DB
                ContentValues values = new ContentValues();

                try {
                    SQLiteDatabase db = DBManager.getInstance().openDatabase();

                    values.put(DBHelper.TIME, record.getCreationTime());
                    values.put(DBHelper.sessionid_col, record.getSessionid());
                    values.put(DBHelper.latitude_col, record.getLatitude());
                    values.put(DBHelper.longitude_col, record.getLongitude());
                    values.put(DBHelper.Accuracy_col, record.getAccuracy());

                    String transportation = "";

                    if(TrafficFlag.equals("walk")){
                        transportation = "on_foot";
                    }else if(TrafficFlag.equals("bike")){
                        transportation = "on_bicycle";
                    }else if(TrafficFlag.equals("car")){
                        transportation = "in_vehicle";
                    }else if(TrafficFlag.equals("site")){
                        transportation = "static";

                        Log.d(TAG, "siteName : "+ siteName);

                        values.put(DBHelper.trip_site_col, siteName);
                    }

                    values.put(DBHelper.trip_transportation_col, transportation);

                    db.insert(DBHelper.trip_table, null, values);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    values.clear();
                    DBManager.getInstance().closeDatabase();
                }

                TripManager.sessionid = "0";

                editor.putInt("sessionid_unStatic",TripManager.sessionid_unStatic);

                editor.commit();*/

                //傳送訊息1
                message.what =1;
                handler.sendMessage(message);
            }
            if(zeroflag){
                timer01.cancel();
                timer01.purge();
                zeroflag=false;
            }

        }

    };

    private final ThreadLocal<View.OnClickListener> listener = new ThreadLocal<View.OnClickListener>() {
        @Override
        protected View.OnClickListener initialValue() {
            return new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    switch (v.getId()) {
                        case R.id.btn_play:
                            Log.d(TAG, "test show trip : button play");
                            Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                            starttime = formatter.format(curDate);

                            //if there is a ongoing sessoin mean that the state now are pause,
                            //do not start a new one, continue the current one.

                            int lastSessionId = 0;

                            try {
                                Session session = SessionManager.getLastSession();
                                lastSessionId = session.getId();
                            }catch (IndexOutOfBoundsException e){
                                e.printStackTrace();
                                Log.d(TAG, "No session yet.");
                            }

                            if(!SessionManager.getOngoingSessionIdList().contains(lastSessionId)){

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

                                SessionManager.startNewSession(session);
                            }

                            if (!startflag) { //play → pause
                                play.setImageResource(R.drawable.icon_pause);
                                startflag = true;
                                recordflag = false;
                                stopflag = false;
                                CountFlag = false;
                            } else {
                                play.setImageResource(R.drawable.icon_play);
                                startflag = false;
                                recordflag = false;
                                stopflag = false;
                                CountFlag = false;
                            }

                            break;
                        case R.id.btn_pause:
                            startflag = false;
                            break;
                        case R.id.btn_stop:
                            Log.d(TAG, "test show trip : button stop");

                            play.setImageResource(R.drawable.icon_play);
                            tsec = 0;
                            CountFlag = true;
                            startflag = false;
                            recordflag = true;
                            stopflag = true;
                            zeroflag = true;
                            Date curDate2 = new Date(System.currentTimeMillis()); // 獲取當前時間
                            stoptime = formatter.format(curDate2);
                            result = starttime + " - " + stoptime;
                            if (BigFlag.equals("move"))
                                duration = "";
                            else if (BigFlag.equals("site"))
                                duration = MarkerFlag + "(" + counter.getText().toString() + ")";
                            Log.d(TAG, "recordflag: " + recordflag + ", counter:" + result);

                            //TextView 初始化
                            counter.setText("00:00:00");

                            //TODO stop the session
                            Session lastSession = SessionManager.getLastSession();
                            long endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
                            lastSession.setEndTime(endTime);

                            //end the current session
                            //try catch the situation that no session occur
                            try {
                                SessionManager.endCurSession(lastSession);
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }

                            break;
                    }
                }

            };
        }
    };

    private String getTrafficActivityString(){
        if(TrafficFlag.equals("walk")){
            return TransportationModeService.TRANSPORTATION_MODE_NAME_ON_FOOT;
        }else if(TrafficFlag.equals("bike")){
            return TransportationModeService.TRANSPORTATION_MODE_NAME_ON_BICYCLE;
        }else if(TrafficFlag.equals("car")){
            return TransportationModeService.TRANSPORTATION_MODE_NAME_IN_VEHICLE;
        }

        return TransportationModeService.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION;
    }
}
