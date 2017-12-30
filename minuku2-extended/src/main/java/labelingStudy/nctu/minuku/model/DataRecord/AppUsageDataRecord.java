package labelingStudy.nctu.minuku.model.DataRecord;

import android.content.Context;
import android.os.PowerManager;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/7/22.
 */

public class AppUsageDataRecord implements DataRecord{

    public long creationTime;
    private String TAG = "AppUsageDataRecord";

    private String Screen_Status;
    private String Latest_Used_App;
    private String Latest_Used_App_Time;
    private String Recent_Apps;
    //private String Users;
    private String Latest_Foreground_Activity;

    private Context context;

    //screen on and off
    private static final String STRING_SCREEN_OFF = "Screen_off";
    private static final String STRING_SCREEN_ON = "Screen_on";
    private static final String STRING_INTERACTIVE = "Interactive";
    private static final String STRING_NOT_INTERACTIVE = "Not_Interactive";

    private PowerManager mPowerManager;

    protected JSONObject jSONObject;


    public AppUsageDataRecord(){
        this.creationTime = new java.util.Date().getTime();
    }

    public AppUsageDataRecord(String Screen_Status, String Latest_Used_App, String Latest_Foreground_Activity) {
        this.creationTime = new java.util.Date().getTime();
        this.Screen_Status = Screen_Status;
        this.Latest_Used_App = Latest_Used_App;
        this.Latest_Foreground_Activity = Latest_Foreground_Activity;
        //this.Users = Users;
    }

    public AppUsageDataRecord(String Screen_Status, String Latest_Used_App, String Latest_Used_App_Time, String Recent_Apps) {
        this.creationTime = new java.util.Date().getTime();
        this.Screen_Status = Screen_Status;
        this.Latest_Used_App = Latest_Used_App;
        this.Latest_Used_App_Time = Latest_Used_App_Time;
        this.Recent_Apps = Recent_Apps;
        //this.Users = Users;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long t){this.creationTime = t;}

//    public String getScreenStatus() {
//        Log.e(TAG, "GetScreenStatus called.");
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//
//            //use isInteractive after api 20
//            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
//            for (Display display : dm.getDisplays()) {
//                if (display.getState() != Display.STATE_OFF) {
//                    Screen_Status = STRING_INTERACTIVE;
//                }
//                else
//                    Screen_Status = STRING_SCREEN_OFF;
//            }
//
//
//
//        }
//        //before API20, we use screen on or off
//        else {
//            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
//            if(mPowerManager.isScreenOn())
//                Screen_Status = STRING_SCREEN_ON;
//            else
//                Screen_Status = STRING_SCREEN_OFF;
//
//        }
//
//        Log.e(TAG, "test source being requested [testing app] SCREEN:  " + Screen_Status);
//
//        return Screen_Status;
//    }

    public String getScreen_Status(){
        Log.e(TAG, "getScreen_Status called.");

        return Screen_Status;
    }

    public String getLatestUsedApp() {
        Log.e(TAG, "GetLatestUsedApp called.");
        return Latest_Used_App;
    }

    public String getLatestUsedAppTime() {
        Log.e(TAG, "GetLatestUsedAppTime called.");
        return Latest_Used_App_Time;
    }

    public String getRecentApps() {
        Log.e(TAG, "GetRecentApps called.");
        return Recent_Apps;
    }

//      public String getUsers() {
//        return Users;
//    }



//    public static void setCurrentForegroundActivityAndPackage(String curForegroundActivity) {
//
//        Latest_Foreground_Activity=curForegroundActivity;
//
//
//        Log.d(TAG, "[setCurrentForegroundActivityAndPackage] the current running package mIs " + Latest_Foreground_Activity );
//    }

    public String getLatestForegroundActivity() {
        return Latest_Foreground_Activity;
    }

    public JSONObject getData() {
        return jSONObject;
    }

    public void setData(JSONObject data) {
        this.jSONObject = data;
    }


    /**get the current time in milliseconds**/
    public static long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }


}
