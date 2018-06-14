package labelingStudy.nctu.minuku.streamgenerator;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;

import com.google.common.util.concurrent.AtomicDouble;
import com.opencsv.CSVWriter;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.LocationNoGoogleDataRecord;
import labelingStudy.nctu.minuku.stream.LocationNoGoogleStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Lawrence on 2017/11/20.
 */

public class LocationNoGoogleStreamGenerator extends AndroidStreamGenerator<LocationNoGoogleDataRecord> {

    private LocationNoGoogleStream mStream;
    private Context mContext;
    private String TAG = "LocationNoGoogleStreamGenerator";
    private AtomicDouble latitude;
    private AtomicDouble longitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    private float accuracy;

    Location loc;

    private static final String PACKAGE_DIRECTORY_PATH="/Android/data/edu.ohio.minuku_2/";

    private CSVWriter csv_writer = null;

    private LocationManager lms;
    private String bestProvider = LocationManager.GPS_PROVIDER;



//    LocationListener locationListener;
    LocationManager locationManager;
    String mprovider;

    boolean isGPS = false;
    boolean isNetwork = false;

    private boolean getService;

    public LocationNoGoogleStreamGenerator(Context applicationContext){
        super(applicationContext);

        this.mStream = new LocationNoGoogleStream(Constants.LOCATION_QUEUE_SIZE);
        this.latitude = new AtomicDouble();
        this.longitude = new AtomicDouble();

        this.mContext = applicationContext;

       /*while(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            //如果GPS或網路定位沒開啟，繼續等

        }*/

        this.register();

    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, provider);
            Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void locationServiceInitial() {
        lms = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);	//取得系統定位服務
//        Criteria criteria = new Criteria();	//資訊提供者選取標準
//        bestProvider = lms.getBestProvider(criteria, true);
        try {
            //TODO
            Location location = lms.getLastKnownLocation(LocationManager.GPS_PROVIDER);    //使用GPS定位座標

            Log.d(TAG, "latitude : "+ location.getLatitude()+"longitude : "+ location.getLongitude());

            this.latitude.set(location.getLatitude());
            this.longitude.set(location.getLongitude());
            accuracy = location.getAccuracy();

        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG, "Update stream called.");
        try {
//            lms.requestLocationUpdates(bestProvider, 1000, 1, this);

//            getLocation();

            LocationNoGoogleDataRecord locationNoGoogleDataRecord = new LocationNoGoogleDataRecord(
                    (float)latitude.get(),
                    (float)longitude.get(),
                    accuracy);
            Log.e(TAG,"locationDataRecord latitude : "+latitude.get()+" longitude : "+longitude.get());


            mStream.add(locationNoGoogleDataRecord);
            Log.d(TAG, "Location to be sent to event bus" + locationNoGoogleDataRecord);

            // also post an event.
            EventBus.getDefault().post(locationNoGoogleDataRecord);
            try {
                appDatabase db;
                db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
                        .allowMainThreadQueries()
                        .build();
                db.locationNoGoogleDataRecordDao().insertAll(locationNoGoogleDataRecord);
//                mDAO.add(locationNoGoogleDataRecord);

            } catch (NullPointerException e){ //Sometimes no data is normal
                e.printStackTrace();
                return false;
            }

        }catch(SecurityException e){
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, LocationNoGoogleDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which LocationDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides LocationDataRecord is already registered.");
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged");

            Log.d(TAG, "latitude" + location.getLatitude());

            latitude.set(location.getLatitude());
            longitude.set(location.getLongitude());
            accuracy = location.getAccuracy();

            long lastposupdate = new Date().getTime();

            StoreToCSV(lastposupdate,location.getLatitude(),location.getLongitude(),location.getAccuracy());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "onProviderDisabled");

        }
    };

    @Override
    public void onStreamRegistration() {

        this.latitude.set(-999.0);
        this.longitude.set(-999.0);

        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        }catch (SecurityException e){
            e.printStackTrace();
            try{
                // delay 5 second, wait for user confirmed.
                Thread.sleep(5000);

            } catch(InterruptedException e2){
                e2.printStackTrace();
            }

            onStreamRegistration();
        }

        Log.d(TAG, "Stream " + TAG + " registered successfully");

    }

    @Override
    public Stream<LocationNoGoogleDataRecord> generateNewStream() {
        return mStream;
    }

    public void StoreToCSV(long timestamp, double latitude, double longitude, float accuracy){

        Log.d(TAG,"StoreToCSV");

        String sFileName = "LocationNoGoogle.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp),timeString,String.valueOf(latitude),String.valueOf(longitude),String.valueOf(accuracy)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private String getTimeString(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_SLASH);
        String currentTimeString = sdf_now.format(time);

        return currentTimeString;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {
        Log.d(TAG, "sendStateChangeEvent");

    }

    @Override
    public void offer(LocationNoGoogleDataRecord dataRecord) {
        Log.d(TAG, "offer");

    }
}
