package labelingStudy.nctu.minuku_2.controller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.R;

//import edu.ohio.minuku_2.R;

/**
 * Created by Lawrence on 2017/4/19.
 */

public class report extends AppCompatActivity {

    final private String TAG = "report";

//    Button startdate,enddate,buildreport;
    private TextView showingDeviceId;
    private int mYear, mMonth, mDay;

    private SharedPreferences sharedPrefs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

//        startdate = (Button) findViewById(R.id.startdate);
//        enddate = (Button) findViewById(R.id.enddate);
//        buildreport = (Button) findViewById(R.id.buildreport);
//
//        startdate.setOnClickListener(choosingstartdate);
//        enddate.setOnClickListener(choosingenddate);
//        buildreport.setOnClickListener(buildingreport);

        showingDeviceId = (TextView) findViewById(R.id.showingDeviceID);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        String device_id = sharedPrefs.getString("DEVICE_ID", Constants.DEVICE_ID);

        showingDeviceId.setText("Device ID = "+ device_id);

    }
/*

    private Button.OnClickListener choosingstartdate = new Button.OnClickListener(){
        public void onClick(View v){
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(report.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    String format = setDateFormat(year,month,day);
                    startdate.setText(format);
                }

            }, mYear,mMonth, mDay).show();
        }
    };

    private Button.OnClickListener choosingenddate = new Button.OnClickListener(){
        public void onClick(View v){
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(report.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    String format = setDateFormat(year,month,day);
                    enddate.setText(format);
                }

            }, mYear,mMonth, mDay).show();
        }
    };

    private Button.OnClickListener buildingreport = new Button.OnClickListener(){
        public void onClick(View v){
            datediff_valid(startdate.getText().toString(),enddate.getText().toString());
            //TODO 創建報告 CSV檔
            ;
        }
    };
*/

    private String setDateFormat(int year,int monthOfYear,int dayOfMonth){
        String month = String.valueOf(monthOfYear + 1);
        String day = String.valueOf(dayOfMonth);
        //to fit SimpleDateFormat "yyyy/MM/dd"
        if((monthOfYear + 1)<10)
            month = "0" + String.valueOf(monthOfYear + 1);
        if(dayOfMonth<10)
            day = "0" + String.valueOf(dayOfMonth);


        return String.valueOf(year) + "/"
                + month + "/"
                + day;
    }

    public void datediff_valid(String FD, String SD) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        if(FD.equals("請選擇日期")||SD.equals("請選擇日期"))
            Toast.makeText(report.this,"請選擇日期",Toast.LENGTH_SHORT).show();
        else{
            try{
                long diff = 0;
                long start = sdf.parse(FD).getTime();
                long end = sdf.parse(SD).getTime();
                //diff = (end - start) / 24 * 60 * 60 * 1000;
                diff = end - start;
                if(diff<0)
                    Toast.makeText(report.this,"請調整日期範圍",Toast.LENGTH_SHORT).show();
                else{
                    Log.d(TAG,"創建報告 CSV檔");
                    //TODO 創建報告 CSV檔
                }
            }catch (ParseException e) {
                Log.d(TAG,"ParseException occurred");
            }

        }




    }

}
