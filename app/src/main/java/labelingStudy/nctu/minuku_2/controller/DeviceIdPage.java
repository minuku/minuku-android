package labelingStudy.nctu.minuku_2.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.TextView;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.R;

//import edu.ohio.minuku_2.R;

/**
 * Created by Lawrence on 2017/4/19.
 */

public class DeviceIdPage extends AppCompatActivity {

    final private String TAG = "DeviceIdPage";

//    Button startdate,enddate,buildreport;
    private TextView showingDeviceId;
    private int mYear, mMonth, mDay;

    private SharedPreferences sharedPrefs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

        showingDeviceId = (TextView) findViewById(R.id.showingDeviceID);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        getDeviceid();

        String device_id = sharedPrefs.getString("DEVICE_ID", Constants.DEVICE_ID);

        showingDeviceId.setText("Device ID = "+ device_id);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            DeviceIdPage.this.finish();

            if(isTaskRoot()){
                startActivity(new Intent(this, WelcomeActivity.class));
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sharedPrefs.edit().putString("lastActivity", getClass().getName()).apply();
    }

    public void getDeviceid(){

        TelephonyManager mngr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        int permissionStatus= ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if(permissionStatus== PackageManager.PERMISSION_GRANTED){
            Constants.DEVICE_ID = mngr.getDeviceId();

            sharedPrefs.edit().putString("DEVICE_ID",  Constants.DEVICE_ID).apply();

            Log.e(TAG,"DEVICE_ID"+Constants.DEVICE_ID+" : "+mngr.getDeviceId());

        }
    }

}
