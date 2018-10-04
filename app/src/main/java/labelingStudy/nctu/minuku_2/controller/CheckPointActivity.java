package labelingStudy.nctu.minuku_2.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import labelingStudy.nctu.minuku.Data.DBHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku_2.R;

/**
 * Created by Lawrence on 2017/11/8.
 */

public class CheckPointActivity extends AppCompatActivity {

    private final String TAG = "CheckPointActivity";

    private Context mContext;

    private Button checkpoint;

    private SharedPreferences sharedPrefs;

    private boolean firstTimeOrNot;

    public CheckPointActivity(){}

    public CheckPointActivity(Context mContext){
        this.mContext = mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoint_activity);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        firstTimeOrNot = sharedPrefs.getBoolean("firstTimeOrNot", true);

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
    public void onResume(){
        super.onResume();

        initCheckPoint();
    }

    @Override
    protected void onPause() {
        super.onPause();

        sharedPrefs.edit().putString("lastActivity", getClass().getName()).apply();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            CheckPointActivity.this.finish();

            if(isTaskRoot()){
                startActivity(new Intent(this, WelcomeActivity.class));
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void initCheckPoint() {

        checkpoint = (Button) findViewById(R.id.check);

        checkpoint.setOnClickListener(checkpointing);
    }

    public void initCheckPoint(View v) {

        checkpoint = (Button) v.findViewById(R.id.check);

        checkpoint.setOnClickListener(checkpointing);
    }

    private Button.OnClickListener checkpointing = new Button.OnClickListener() {

        public void onClick(View v) {

            Log.d(TAG,"checkpointing clicked");

            try {

                Session lastSession = SessionManager.getLastSession();

                if( SessionManager.sSessionIsWaiting){

                    lastSession.setUserPressOrNot(true);

                    SessionManager.updateCurSession(lastSession.getId(), ScheduleAndSampleManager.getCurrentTimeInMillis(), lastSession.isUserPress());

                    SessionManager.sSessionIsWaiting = false;

                    return;
                }

                boolean emptySessionOn = SessionManager.isSessionEmptyOngoing(Integer.valueOf(lastSession.getId()));

                if(emptySessionOn){

                    Toast.makeText(CheckPointActivity.this, "當前活動已經Checkpoint過了", Toast.LENGTH_SHORT).show();
                    return;
                }

                //end the ongoing session first
                long endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
                lastSession.setEndTime(endTime);

                //end the current session
                SessionManager.endCurSession(lastSession);

                //if the current session is empty ongoing, remove it from the empty ongoing list
                //then add a new session for the future activity
                addEmptySession();

            }catch (IndexOutOfBoundsException e){

                Log.d(TAG, "[test triggering] No ongoing session now");

                addEmptySession();

//                Toast.makeText(CheckPointActivity.this, "尚未有進行中的活動", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(CheckPointActivity.this, "成功Checkpoint！", Toast.LENGTH_SHORT).show();

        }
    };

    private void addEmptySession(){

        int sessionCount = SessionManager.getNumOfSession();
        int sessionId = (int) sessionCount + 1;
        Session sessionEmptyActivity = new Session(sessionId);

        long initTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
        sessionEmptyActivity.setStartTime(initTime);
        sessionEmptyActivity.setCreatedTime(initTime);

        //don't add transportation here

        //set the UserPressOrNot true, because it's the checkpointed session
        sessionEmptyActivity.setUserPressOrNot(true);

        sessionEmptyActivity.setModified(false);
        sessionEmptyActivity.setIsSent(Constants.SESSION_SHOULDNT_BEEN_SENT_FLAG);
        sessionEmptyActivity.setType(Constants.SESSION_TYPE_DETECTED_BY_USER);

        SessionManager.addEmptyOngoingSessionId(sessionId);
        SessionManager.addOngoingSessionId(sessionId);

        DBHelper.insertSessionTable(sessionEmptyActivity);
    }

}
