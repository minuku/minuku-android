package labelingStudy.nctu.minuku_2.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuNotificationManager;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku.model.Annotation;
import labelingStudy.nctu.minuku.model.AnnotationSet;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minuku_2.R;


//import edu.ohio.minuku_2.R;

/**
 * Created by Lawrence on 2017/4/22.
 */

public class Timer_move extends AppCompatActivity {

    final private String TAG = "Timer_move";

    private Button walk, bike, car, site;

    private TextView blackTextView;
    private String blackTextViewDefault = "請選擇您的移動方式";

    public static String trafficType;

    static String BigFlag = "";

    private SharedPreferences sharedPrefs;

    private boolean firstTimeOrNot;

    public Timer_move(){}

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_move);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        firstTimeOrNot = sharedPrefs.getBoolean("firstTimeOrNot", true);
        Log.d(TAG,"firstTimeOrNot : "+ firstTimeOrNot);

        if(firstTimeOrNot) {
            startpermission();
            firstTimeOrNot = false;
            sharedPrefs.edit().putBoolean("firstTimeOrNot", firstTimeOrNot).apply();
        }

        inittimer_move();
    }

    @Override
    protected void onResume(){
        super.onResume();

        inittimer_move();
    }

    @Override
    protected void onPause() {
        super.onPause();

        sharedPrefs.edit().putString("lastActivity", getClass().getName()).apply();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            Timer_move.this.finish();

            if(isTaskRoot()){
                startActivity(new Intent(this, WelcomeActivity.class));
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void startpermission(){

        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));  // 協助工具

        Intent intent1 = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);  //usage
        startActivity(intent1);

//        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS); //notification
//        startActivity(intent);

        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//location
    }

    public void inittimer_move(){

        walk = (Button) findViewById(R.id.walk);
        bike = (Button) findViewById(R.id.bike);
        car = (Button) findViewById(R.id.car);
        site = (Button) findViewById(R.id.site);

        walk.setOnClickListener(walkingTime);
        bike.setOnClickListener(bikingTime);
        car.setOnClickListener(carTime);
        site.setOnClickListener(siting);

        blackTextView = (TextView) findViewById(R.id.blackTextView);

        if(!MinukuNotificationManager.sOngoingNotificationText.equals(Constants.RUNNING_APP_DECLARATION)){

            blackTextView.setText(MinukuNotificationManager.sOngoingNotificationText);
        }else{

            blackTextView.setText(blackTextViewDefault);
        }

    }

    private void imagebuttonWork(String activityType){

        ArrayList<Integer> ongoingSessionIdList = SessionManager.getOngoingSessionIdList();

        //if there is an ongoing session
        if(ongoingSessionIdList.size()>0){

            int sessionId = ongoingSessionIdList.get(0);
            Session ongoingSession = SessionManager.getSession(sessionId);

            AnnotationSet ongoingAnnotationSet = ongoingSession.getAnnotationsSet();
            ArrayList<Annotation> ongoingAnnotations = ongoingAnnotationSet.getAnnotationByTag(Constants.ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY);
            Annotation ongoingAnnotation = ongoingAnnotations.get(ongoingAnnotations.size()-1);
            String ongoingActivity = ongoingAnnotation.getContent();

            String buttonActivity = getActivityTypeString(activityType);

            if(!buttonActivity.equals(ongoingActivity)){

                Toast toast = Toast.makeText(Timer_move.this, "您必須先結束目前的移動方式 : " + getActivityTypeInChinese(trafficType), Toast.LENGTH_SHORT);
                toast.show();
            }else {

                startButtonActivity(activityType);
            }
        }else {

            startButtonActivity(activityType);
        }
    }

    private void startButtonActivity(String activityType){

        if(activityType.equals("static")){

            trafficType = "site";
            startActivity(new Intent(Timer_move.this, Timer_site.class));
        }else {

            trafficType = activityType;

            Bundle bundle = new Bundle();
            bundle.putString("trafficType", trafficType);

            Intent intentToRecord = new Intent(Timer_move.this, CounterActivity.class);
            intentToRecord.putExtras(bundle);

            startActivity(intentToRecord);
        }
    }

    //CountFlag: countung situation --- true:stop, false:ongoing
    private ImageButton.OnClickListener bikingTime = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {

            String buttonActivity = "bike";

            imagebuttonWork(buttonActivity);
        }
    };

    private ImageButton.OnClickListener carTime = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {

            String buttonActivity = "car";

            imagebuttonWork(buttonActivity);
        }
    };

    private ImageButton.OnClickListener walkingTime = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {

            String buttonActivity = "walk";

            imagebuttonWork(buttonActivity);
        }
    };

    //to view Timer_site
    private Button.OnClickListener siting = new Button.OnClickListener() {
        public void onClick(View v) {

            String buttonActivity = "static";

            imagebuttonWork(buttonActivity);
        }
    };

    private String getActivityTypeString(String activityType){

        switch (activityType){
            case "walk":
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_FOOT;
            case "bike":
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_BICYCLE;
            case "car" :
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_IN_VEHICLE;
            case "static":
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION;
            default:
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA;
        }
    }

    private String getActivityTypeInChinese(String activityType){

        switch (activityType){
            case "walk":
                return "走路";
            case "bike":
                return "自行車";
            case "car" :
                return "汽車";
            case "static":
                return "定點";
            default:
                return TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NA;
        }
    }

    private String getActivityStringType(String activityString){

        switch (activityString){

            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_FOOT:
                return "walk";
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_ON_BICYCLE:
                return "bike";
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_IN_VEHICLE:
                return "car";
            case TransportationModeStreamGenerator.TRANSPORTATION_MODE_NAME_NO_TRANSPORTATION:
                return "static";
            default:
                return "";
        }
    }

}
