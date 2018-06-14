package labelingStudy.nctu.minuku_2.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuNotificationManager;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.service.BackgroundService;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";

    public final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private Button chooseMyMobility, watchMyTimeline;

    private String current_task;

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        chooseMyMobility = (Button) findViewById(R.id.chooseMyMobility);
        chooseMyMobility.setOnClickListener(choosingMyMobility);

        watchMyTimeline = (Button) findViewById(R.id.watchMyTimeline);
        watchMyTimeline.setOnClickListener(watchingMyTimeline);

        startService(new Intent(getBaseContext(), BackgroundService.class));

        current_task = getResources().getString(R.string.current_task);
        if(current_task.equals("ESM")) {

            //conceal the button
            chooseMyMobility.setVisibility(View.GONE);
        }else if(current_task.equals("CAR")){

            //startService(new Intent(getBaseContext(), CheckpointAndReminderService.class));
        }
//        EventBus.getDefault().register(this);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        int sdk_int = Build.VERSION.SDK_INT;
        if(sdk_int>=23) {
            checkAndRequestPermissions();
        }else{
            startServiceWork();
        }

        Constants.currentWork = getResources().getString(R.string.current_task);

        Intent intent = new Intent(getApplicationContext(), Timeline.class);
        MinukuNotificationManager.setIntentToTimeline(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();

        sharedPrefs.edit().putString("lastActivity", getClass().getName()).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //TODO we might not need to select date, see on pilot
        /*if(Constants.tabpos)
            menu.findItem(R.id.action_selectdate).setVisible(true);
        else
            menu.findItem(R.id.action_selectdate).setVisible(false);*/

        super.onPrepareOptionsMenu(menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_checkingDeviceId:
                startActivity(new Intent(WelcomeActivity.this, DeviceIdPage.class));
                return true;

            //TODO we might not need to select date, see on pilot
            /*case R.id.action_selectdate:
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        String format = setDateFormat(year,month,day);

                        Constants.Day = day;
                        Constants.Year = year;
                        Constants.Month = month+ 1;//

                        Log.d(TAG,"month : " + (month) + "year : " + year + "day : " + day);

                        timeline = new Timeline(); //Timeline
                        timeline.initTime(recordview);
                        //startdate.setText(format);
                    }

                }, mYear, mMonth, mDay).show();
                return true;*/
        }
        return true;
    }

    private Button.OnClickListener choosingMyMobility = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

            String current_task = getResources().getString(R.string.current_task);


            if(current_task.equals("PART")) {

                Intent intent = new Intent(WelcomeActivity.this, Timer_move.class);
                startActivity(intent);
            }else if(current_task.equals("CAR")){

                Intent intent = new Intent(WelcomeActivity.this, CheckPointActivity.class);
                startActivity(intent);
            }
        }
    };

    private Button.OnClickListener watchingMyTimeline = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(WelcomeActivity.this, Timeline.class);

            startActivity(intent);

        }
    };

    private void checkAndRequestPermissions() {

        Log.e(TAG,"checkingAndRequestingPermissions");

        int permissionReadExternalStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionFineLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionStatus= ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();


        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
        }else{
            startServiceWork();
        }

    }

    public void startServiceWork(){

        getDeviceid();

        //Use service to catch user's log, GPS, activity;
        //TODO Bootcomplete 復原

    }

    public void getDeviceid(){

        TelephonyManager mngr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        int permissionStatus= ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if(permissionStatus==PackageManager.PERMISSION_GRANTED){
            Constants.DEVICE_ID = mngr.getDeviceId();

            sharedPrefs.edit().putString("DEVICE_ID",  Constants.DEVICE_ID).apply();

            Log.e(TAG,"DEVICE_ID"+Constants.DEVICE_ID+" : "+mngr.getDeviceId());

        }
    }
}
