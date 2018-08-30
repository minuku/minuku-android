package labelingStudy.nctu.minuku_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import labelingStudy.nctu.minuku.Data.DBHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku_2.service.BackgroundService;

/**
 * Created by Lawrence on 2017/7/19.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";
    private  static DBHelper dbhelper = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {

            Log.d(TAG,"boot_complete in first");

            try{

                dbhelper = new DBHelper(context);
                dbhelper.getWritableDatabase();
                Log.d(TAG,"db is ok");

                /*if(!InstanceManager.isInitialized()) {
                    InstanceManager.getInstance(context);
                }*/

                SharedPreferences sharedPrefs = context.getSharedPreferences(Constants.sharedPrefString, context.MODE_PRIVATE);

                //recover the ongoing session
                int ongoingSessionId = sharedPrefs.getInt("ongoingSessionid", -1);

                if(ongoingSessionId != -1){

                    SessionManager.getInstance(context).addOngoingSessionid(ongoingSessionId);
                }

            }finally {

                Log.d(TAG, "Successfully receive reboot request");

                //here we start the service

                startBackgroundService(context);

                Log.d(TAG,"BackgroundService is ok");

            }

        }

    }

    private void startBackgroundService(Context context){

        Intent intentToStartBackground = new Intent(context, BackgroundService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentToStartBackground);
        } else {
            context.startService(intentToStartBackground);
        }
    }

}
