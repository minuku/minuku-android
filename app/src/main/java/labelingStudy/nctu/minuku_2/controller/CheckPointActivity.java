package labelingStudy.nctu.minuku_2.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.manager.TripManager;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.streamgenerator.LocationStreamGenerator;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.service.CheckpointAndReminderService;

/**
 * Created by Lawrence on 2017/11/8.
 */

public class CheckPointActivity extends AppCompatActivity {

    private final String TAG = "CheckPointActivity";

    private Context mContext;

    private Button checkpoint;

    private float latitude;
    private float longitude;
    private float accuracy;

    private String transportation;

    public CheckPointActivity(){}

    public CheckPointActivity(Context mContext){
        this.mContext = mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoint_activity);

    }

    @Override
    public void onResume(){
        super.onResume();

    }

    public void initCheckPoint(View v) {

        checkpoint = (Button) v.findViewById(R.id.check);

        checkpoint.setOnClickListener(checkpointing);
    }

    private Button.OnClickListener checkpointing = new Button.OnClickListener() {
        public void onClick(View v) {
            Log.e(TAG,"checkpointing clicked");

            //for testing the CAR
            CheckpointAndReminderService.CheckpointOrNot = true;


            //set Trip to show the car line
            try{

                latitude = (float)LocationStreamGenerator.latitude.get();
                longitude = (float)LocationStreamGenerator.longitude.get();
                accuracy = LocationStreamGenerator.accuracy;

                Log.d(TAG + " trying to get ", "latitude : " + latitude + " longitude : " + longitude);

                if (MinukuStreamManager.getInstance().getTransportationModeDataRecord() != null) {
                    transportation = MinukuStreamManager.getInstance().getTransportationModeDataRecord().getConfirmedActivityType();

                    Log.d(TAG, "transportation : " + transportation);

                }

            }catch (Exception e){
                e.printStackTrace();

            }

            String sessionid = "0, "+ String.valueOf(TripManager.sessionid_unStatic);

            LocationDataRecord record = new LocationDataRecord(
                    sessionid,
                    latitude,
                    longitude,
                    accuracy);

            Log.d(TAG, "sessionid : "+ sessionid);

            Log.d(TAG, record.getCreationTime() + "," +
                    record.getSessionid() + "," +
                    record.getLatitude() + "," +
                    record.getLongitude() + "," +
                    record.getAccuracy());

//            LocationToTrip.add(record);

            // store to DB
            ContentValues values = new ContentValues();

            try {
                SQLiteDatabase db = DBManager.getInstance().openDatabase();

                values.put(DBHelper.TIME, record.getCreationTime());
                values.put(DBHelper.sessionid_col, record.getSessionid());
                values.put(DBHelper.latitude_col, record.getLatitude());
                values.put(DBHelper.longitude_col, record.getLongitude());
                values.put(DBHelper.Accuracy_col, record.getAccuracy());
                values.put(DBHelper.trip_transportation_col, transportation);
                values.put(DBHelper.userPressOrNot_col, true);

                db.insert(DBHelper.trip_table, null, values);
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                values.clear();
                DBManager.getInstance().closeDatabase(); // Closing database connection
            }

            //in CAR the session id can be controlled by the user
            TripManager.sessionid_unStatic ++;

            Toast.makeText(mContext, "Your checkpoint is confirmed !!", Toast.LENGTH_SHORT).show();

        }
    };
}
