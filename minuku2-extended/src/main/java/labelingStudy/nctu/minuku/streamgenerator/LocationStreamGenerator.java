/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku.streamgenerator;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.Timer;
import java.util.TimerTask;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.event.IncrementLoadingProcessCountEvent;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.stream.LocationStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;
import labelingStudy.nctu.minukucore.streamgenerator.StreamGenerator;

/**
 * Created by neerajkumar on 7/18/16.
 */
public class LocationStreamGenerator extends AndroidStreamGenerator<LocationDataRecord> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private LocationStream mStream;
    private String TAG = "LocationStreamGenerator";

    private static final String PACKAGE_DIRECTORY_PATH="/Android/data/edu.ohio.minuku_2/";

    private CSVWriter csv_writer = null;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    //for the replay purpose
    private Timer ReplayTimer;
    private TimerTask ReplayTimerTask;

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_LATITUDE = "Latitude";
    public static final String RECORD_DATA_PROPERTY_LONGITUDE = "Longitude";
    public static final String RECORD_DATA_PROPERTY_ACCURACY = "Accuracy";
    public static final String RECORD_DATA_PROPERTY_ALTITUDE = "Altitude";
    public static final String RECORD_DATA_PROPERTY_PROVIDER = "Provider";
    public static final String RECORD_DATA_PROPERTY_SPEED = "Speed";
    public static final String RECORD_DATA_PROPERTY_BEARING = "Bearing";
    public static final String RECORD_DATA_PROPERTY_EXTRAS = "Extras";

    public static long lastposupdate = -99;

    public static AtomicDouble latestLatitude;
    public static AtomicDouble latestLongitude;
    public static float latestAccuracy;

    private Context context;



    public static boolean startIndoorOutdoor;
    public static ArrayList<LatLng> locForIndoorOutdoor;

    private static ArrayList<LocationDataRecord> mLocationDataRecords;

//    private SessionManager tripManager;

    private Location location;

    public static LocationDataRecord toCheckFamiliarOrNotLocationDataRecord;

    private SharedPreferences sharedPrefs;

//    TelephonyManager tel;
//    MyPhoneStateListener myPhoneStateListener;

    public LocationStreamGenerator(Context applicationContext) {
//        super(applicationContext);
        this.mStream = new LocationStream(Constants.LOCATION_QUEUE_SIZE);
//        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(LocationDataRecord.class);
        this.latestLatitude = new AtomicDouble();
        this.latestLongitude = new AtomicDouble();

        this.context = applicationContext;
//        tripManager = new SessionManager();


        mLocationDataRecords = new ArrayList<LocationDataRecord>();

        startIndoorOutdoor = false;

//        createCSV();
        sharedPrefs = context.getSharedPreferences("edu.umich.minuku_2", context.MODE_PRIVATE);

        //for replay location record
//            startReplayLocationRecordTimer();

        this.register();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d(TAG, "GPS: "
                    + location.getLatitude() + ", "
                    + location.getLongitude() + ", "
                    + "latestAccuracy: " + location.getAccuracy()
                    +"Extras : " + location.getExtras());

            // If the location is accurate to 30 meters, it's good enough for us.
            // Post an update event and exit. //TODO maybe be
            float dist = 0;
            float[] results = new float[1];

            Log.d(TAG, "last time GPS : "
                    + latestLatitude.get() + ", "
                    + latestLongitude.get() + ", "
                    + "latestAccuracy: " + location.getAccuracy());

//            Location.distanceBetween(location.getLatitude(),location.getLongitude(), latestLatitude.get(), latestLongitude.get(),results);

            if(!(latestLatitude.get() == -999.0 && latestLongitude.get() == -999.0))
                dist = results[0];
            else
                dist = 1000;

            Log.d(TAG, "dist : " + dist);
            //if the newest
            //TODO cancel the dist restriction
//            if(dist < 100 || (latestLatitude.get() == -999.0 && latestLongitude.get() == -999.0)){
            // Log.d(TAG, "Location is accurate upto 50 meters");
                this.latestLatitude.set(location.getLatitude());
                this.latestLongitude.set(location.getLongitude());
                latestAccuracy = location.getAccuracy();

            //the lastposition update value timestamp
            lastposupdate = new Date().getTime();

            StoreToCSV(lastposupdate,location.getLatitude(),location.getLongitude(),location.getAccuracy());

            Log.d(TAG,"onLocationChanged latestLatitude : "+ latestLatitude +" latestLongitude : "+ latestLongitude);
            Log.d(TAG,"onLocationChanged location : "+this.location);

//            }

            if(startIndoorOutdoor){
                LatLng latLng = new LatLng(latestLatitude.get(), latestLongitude.get());
                locForIndoorOutdoor.add(latLng);
            }else{
                locForIndoorOutdoor = new ArrayList<LatLng>();
            }

            StoreToCSV(new Date().getTime(), locForIndoorOutdoor);

        }
    }

    public void onStreamRegistration() {

        this.latestLatitude.set(-999.0);
        this.latestLongitude.set(-999.0);

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)
                == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Error occurred while attempting to access Google play.");
        }

        Log.d(TAG, "Stream " + TAG + " registered successfully");

        EventBus.getDefault().post(new IncrementLoadingProcessCountEvent());

    }

    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, LocationDataRecord.class, (StreamGenerator) this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which LocationDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides LocationDataRecord is already registered.");
        }
    }

    public Stream<LocationDataRecord> generateNewStream() {
        return mStream;
    }

    public boolean updateStream() {

        LocationDataRecord newlocationDataRecord;
        int session_id = 0;

        int countOfOngoingSession = SessionManager.getInstance().getOngoingSessionIdList().size();

        //if there exists an ongoing session
        if (countOfOngoingSession>0){
            session_id = SessionManager.getInstance().getOngoingSessionIdList().get(0);
        }

        Log.d(TAG, "[test replay] Update stream  ssession is " + session_id);

        try {
            newlocationDataRecord = new LocationDataRecord(
                    (float) latestLatitude.get(),
                    (float) latestLongitude.get(),
                    latestAccuracy,
                    //TODO improve it to ArrayList, ex. the session id should be "0, 10".
                    String.valueOf(session_id));
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            //no session now
            newlocationDataRecord = new LocationDataRecord(
                    (float) latestLatitude.get(),
                    (float) latestLongitude.get(),
                    latestAccuracy,
                    String.valueOf(session_id));
        }
        Log.e(TAG,"[test replay] newlocationDataRecord latestLatitude : "+ latestLatitude.get()+" latestLongitude : "+ latestLongitude.get() + "  session_id " +  session_id);

        MinukuStreamManager.getInstance().setLocationDataRecord(newlocationDataRecord);
        toCheckFamiliarOrNotLocationDataRecord = newlocationDataRecord;

        mStream.add(newlocationDataRecord);
        Log.d(TAG, "Location to be sent to event bus" + newlocationDataRecord);

        // also post an event.
        EventBus.getDefault().post(newlocationDataRecord);
        try {
            appDatabase db;
            db = Room.databaseBuilder(context,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();
            db.locationDataRecordDao().insertAll(newlocationDataRecord);
            List<LocationDataRecord> locationDataRecords = db.locationDataRecordDao().getAll();
            for (LocationDataRecord l : locationDataRecords) {
                Log.d(TAG, String.valueOf(l.getLatitude()));
                Log.d(TAG, String.valueOf(l.getLongitude()));
            }


        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public long getUpdateFrequency() {
        return 1; // 1 = 1 minutes
    }

    public void sendStateChangeEvent() {

    }

    public void offer(LocationDataRecord dataRecord) {
        Log.e(TAG, "Offer for location data record does nothing!");
    }

    /**
     * Location Listener events start here.
     */



    public static void setStartIndoorOutdoor(boolean value){
        startIndoorOutdoor = value;
    }

    public void StoreToCSV(long timestamp, ArrayList<LatLng> latLngs){

        Log.d(TAG,"StoreToCSV startIndoorOutdoor");

        String sFileName = "startIndoorOutdoor.csv";

        Boolean startIndoorOutdoorfirstOrNot = sharedPrefs.getBoolean("startIndoorOutdoorfirstOrNot", true);

        try{
            File root = new File(Environment.getExternalStorageDirectory() + PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = getTimeString(timestamp);

            if(startIndoorOutdoorfirstOrNot) {
                data.add(new String[]{"timestamp", "timeString", "all latlngs", "distance"});
                sharedPrefs.edit().putBoolean("startIndoorOutdoorfirstOrNot", false).apply();
            }

            //.........................because it will iterate two elements at one iteration.
            double dist = 0;
            if(!latLngs.isEmpty()) {
                for (int index = 0; index < latLngs.size() - 1; index++) {
                    LatLng latLng = latLngs.get(index);
                    LatLng latLng2 = latLngs.get(index + 1);
                    float[] results = new float[1];
                    Location.distanceBetween(latLng.latitude, latLng.longitude, latLng2.latitude, latLng2.longitude, results);
                    dist += results[0];
                }
            }

            data.add(new String[]{String.valueOf(timestamp), timeString, String.valueOf(latLngs), String.valueOf(dist)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void StoreToCSV(long timestamp, double latitude, double longitude, float accuracy){

        Log.d(TAG,"StoreToCSV");

        String sFileName = "LocationOnChange.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

//            data.add(new String[]{"timestamp","timeString","Latitude","Longitude","Accuracy"});
            String timeString = getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp),timeString,String.valueOf(latitude),String.valueOf(longitude),String.valueOf(accuracy)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void createCSV(){
        String sFileName = "LocationOnChange.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            data.add(new String[]{"timestamp","timeString","Latitude","Longitude","Accuracy","Extras"});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String getTimeString(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_SLASH);
        String currentTimeString = sdf_now.format(time);

        return currentTimeString;
    }

    public static void addLocationDataRecord(LocationDataRecord record) {
        getLocationDataRecords().add(record);
        android.util.Log.d("LocationStreamGenerator", "[test replay] adding " +   record.toString()  + " to LocationRecords in LocationStreamGenerator");
    }

    public static ArrayList<LocationDataRecord> getLocationDataRecords() {

        if (mLocationDataRecords==null){
            mLocationDataRecords = new ArrayList<LocationDataRecord>();
        }
        return mLocationDataRecords;

    }

    public void startReplayLocationRecordTimer() {

        //set a new Timer
        ReplayTimer = new Timer();

        //start the timertask for replay
        RePlayActivityRecordTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        ReplayTimer.schedule(ReplayTimerTask,0,1000);

    }

    public void RePlayActivityRecordTimerTask() {


        ReplayTimerTask = new TimerTask() {

            int locationRecordCurIndex = 0;
            int sec = 0;
            public void run() {

                sec++;

                //for every 5 seconds and if we still have more AR labels in the list to reply, we will set an AR label to the streamgeneratro
                if(sec%5 == 0 && mLocationDataRecords.size()>0 && locationRecordCurIndex < mLocationDataRecords.size()-1){

                    LocationDataRecord locationDataRecord =mLocationDataRecords.get(locationRecordCurIndex);

                    latestLatitude.set(locationDataRecord.getLatitude());
                    latestLongitude.set(locationDataRecord.getLongitude());
                    latestAccuracy = locationDataRecord.getAccuracy();

                    //the lastposition update value timestamp
                    lastposupdate = new Date().getTime();

                    android.util.Log.d(TAG, "[test replay] going to feed location " +   locationDataRecord.getLatitude()+  " :"  + locationDataRecord.getLongitude()  +" at index " + locationRecordCurIndex  + " in the location streamgenerator");

                    //move on to the next activity Record
                    locationRecordCurIndex++;
                }
            }
        };
    }

    @Override
    public void onConnected(Bundle bundle) {

        try{
            // delay 5 second, wait for user confirmed.
            Thread.sleep(5000);

        } catch(InterruptedException e){
            e.printStackTrace();
        }


        Log.d(TAG, "onConnected");

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(Constants.INTERNAL_LOCATION_UPDATE_FREQUENCY);
        mLocationRequest.setFastestInterval(Constants.INTERNAL_LOCATION_UPDATE_FREQUENCY);
        //mLocationRequest.setSmallestDisplacement(Constants.LOCATION_MINUMUM_DISPLACEMENT_UPDATE_THRESHOLD);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        try {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                            this);

        }catch (SecurityException e){
//            TODO ask for this method good or not.
//            Log.d(TAG, "SecurityException");
            onConnected(bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection to Google play services failed.");
        stopCheckingForLocationUpdates();
    }

    private void stopCheckingForLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            try {
                MinukuStreamManager.getInstance().unregister(mStream, this);
                Log.e(TAG, "Unregistering location stream generator from stream manager");
            } catch (StreamNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
