package labelingStudy.nctu.minuku_2.controller;

import android.content.Context;
import android.content.Intent;
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

import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.service.CheckpointAndReminderService;

import static labelingStudy.nctu.minuku_2.MainActivity.recordview;
import static labelingStudy.nctu.minuku_2.controller.PlaceSelection.MarkerFlag;
import static labelingStudy.nctu.minuku_2.controller.timer_move.BigFlag;
import static labelingStudy.nctu.minuku_2.controller.timer_move.TrafficFlag;

//import edu.ohio.minuku_2.R;

/**
 * Created by Lawrence on 2017/4/20.
 */

public class home extends AppCompatActivity {

    final private String TAG = "home";

    private Context mContext;
    private LayoutInflater mInflater;

    public static TextView counter;
    public static String result, duration;
    public static String stoptime, starttime;
    private int tsec=0,csec=0,cmin=0,chour=0;
    public static boolean startflag = false;
    public static boolean recordflag = false;
    public static boolean stopflag = false;
    public static boolean zeroflag = false;
    public static boolean FirstPlayFlag = true;
    public static boolean CountFlag = true; //countung situation --- true:stop, false:ongoing
//    String TAG="Counter";
    public static SimpleDateFormat formatter;

    public static ImageButton  play, pause, stop;
    public static ImageView traffic;
    Timer timer01 =new Timer();

    public static TextView recentworktext; //for other class to improve it
    public static Button edituractivity
                  ,calories,distance,stepcount
                  ,move,site;



    public home(){}

    public home(Context mContext){
        this.mContext = mContext;
    }

    public home(LayoutInflater mInflater){
        this.mInflater = mInflater;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

    }

    @Override
    public void onResume(){
        super.onResume();

        inithome();

    }

    public void inithome(){

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
        play.setOnClickListener(listener);
//        pause.setOnClickListener(listener);
        stop.setOnClickListener(listener);

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

    public void inithome(View v){

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
        play.setOnClickListener(listener);
//        pause.setOnClickListener(listener);
        stop.setOnClickListener(listener);
        Toast toast = Toast.makeText(mContext, "CountFlag" + CountFlag, Toast.LENGTH_SHORT);
        toast.show();
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

        /*recentworktext = (TextView) v.findViewById(R.id.recentworktext);

        edituractivity = (Button) v.findViewById(R.id.edituractivity);
        calories = (Button) v.findViewById(R.id.calories);
        distance = (Button) v.findViewById(R.id.distance);
        stepcount = (Button) v.findViewById(R.id.stepcount);
        move = (Button) v.findViewById(R.id.move);
        site = (Button) v.findViewById(R.id.site);

        calories.setOnClickListener(caloriesactivity);
        edituractivity.setOnClickListener(editinguractivity);
        move.setOnClickListener(moving);
        site.setOnClickListener(siting);*/

    }

    private Button.OnClickListener caloriesactivity = new Button.OnClickListener() {
        public void onClick(View v) {
            Log.e(TAG,"caloriesactivity clicked");

            //for testing the CAR
            CheckpointAndReminderService.CheckpointOrNot = true;
            Toast.makeText(mContext, "Your checkpoint is confirmed !!", Toast.LENGTH_SHORT).show();
        }
    };



    //to view timer_move
    private Button.OnClickListener moving = new Button.OnClickListener() {
        public void onClick(View v) {
            Log.e(TAG,"move clicked");
            if(!stopflag){
                Toast toast = Toast.makeText(mContext, "You need to stop the timer before changing  to different ways of movement", Toast.LENGTH_LONG);
                toast.show();
            }else{
                mContext.startActivity(new Intent(mContext, timer_move.class));
            }

        }
    };
    //to view timer_site
    private Button.OnClickListener siting = new Button.OnClickListener() {
        public void onClick(View v) {
            Log.e(TAG,"site clicked");

            mContext.startActivity(new Intent(mContext, timer_site.class));

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

    private View.OnClickListener listener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.btn_play:
//                    if(FirstPlayFlag){
                        Date curDate = new Date(System.currentTimeMillis()) ; // 獲取當前時間
                        starttime = formatter.format(curDate);
//                        FirstPlayFlag = false;
//                    }

                    if(!startflag){ //play → pause
                        play.setImageResource(R.drawable.icon_pause);
                        startflag = true;
                        recordflag = false;
                        stopflag = false;
                        CountFlag=false;
                    }else{
                        play.setImageResource(R.drawable.icon_play);
                        startflag = false;
                        recordflag = false;
                        stopflag = false;
                        CountFlag=false;
                    }

                    break;
                case R.id.btn_pause:
                    startflag=false;
                    break;
                case R.id.btn_stop:
                    play.setImageResource(R.drawable.icon_play);
                    tsec=0;
                    CountFlag=true;
                    startflag=false;
                    recordflag=true;
                    stopflag = true;
                    zeroflag=true;
                    Date curDate2 = new Date(System.currentTimeMillis()) ; // 獲取當前時間
                    stoptime = formatter.format(curDate2);
                    result = starttime + " - " + stoptime  ;
                    if(BigFlag.equals("move"))
                        duration = "";
                    else if(BigFlag.equals("site"))
                        duration = MarkerFlag + "(" + counter.getText().toString() + ")";
                    Log.d(TAG, "recordflag: " + recordflag + ", counter:" + result);

                    Timeline mtimeline = new Timeline(mContext);
                    mtimeline.initTime(recordview);

                    //TextView 初始化
                    counter.setText("00:00:00");

                    break;
            }
        }

    };
}
