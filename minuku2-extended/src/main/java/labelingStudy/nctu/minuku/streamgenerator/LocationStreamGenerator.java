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

import labelingStudy.nctu.minuku.Data.appDatabase;
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

/**
 * Created by neerajkumar on 7/18/16.
 */

/**
 * LocationStreamGenerator collects data about location data from GPS
 */
public class LocationStreamGenerator extends AndroidStreamGenerator<LocationDataRecord> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private LocationStream mStream;
    private String TAG = "LocationStreamGenerator";
    
    private CSVWriter csv_writer = null;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    //for the replay purpose
    private Timer mReplayTimer;
    private TimerTask mReplayTimerTask;

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_LATITUDE = "Latitude";
    public static final String RECORD_DATA_PROPERTY_LONGITUDE = "Longitude";
    public static final String RECORD_DATA_PROPERTY_ACCURACY = "Accuracy";
    public static final String RECORD_DATA_PROPERTY_ALTITUDE = "Altitude";
    public static final String RECORD_DATA_PROPERTY_PROVIDER = "Provider";
    public static final String RECORD_DATA_PROPERTY_SPEED = "Speed";
    public static final String RECORD_DATA_PROPERTY_BEARING = "Bearing";
    public static final String RECORD_DATA_PROPERTY_EXTRAS = "Extras";

    public static long sLastPosUpdateTime = -99;

    private static AtomicDouble sLatestLatitude;
    private static AtomicDouble sLatestLongitude;
    private static float latestAccuracy;

    private Context context;



    public static boolean sStartIndoorOutdoor;
    public static ArrayList<LatLng> sLocForIndoorOutdoor;

    private static ArrayList<LocationDataRecord> sLocationDataRecords;

//    private SessionManager tripManager;

    private Location mLocation;


    private SharedPreferences mSharedPrefs;

    public LocationStreamGenerator(Context applicationContext) {
        super(applicationContext);
        mStream = new LocationStream(Constants.LOCATION_QUEUE_SIZE);
        sLatestLatitude = new AtomicDouble();
        sLatestLongitude = new AtomicDouble();

        context = applicationContext;
//        tripManager = new SessionManager();


        sLocationDataRecords = new ArrayList<LocationDataRecord>();

        sStartIndoorOutdoor = false;

//        createCSV();
        mSharedPrefs = context.getSharedPreferences(Constants.sharedPrefString, context.MODE_PRIVATE);


        register();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d(TAG, "GPS: "
                    + location.getLatitude() + ", "
                    + location.getLongitude() + ", "
                    + "latestAccuracy: " + location.getAccuracy()
                    + "Extras : " + location.getExtras());

            // If the location is accurate to 30 meters, it's good enough for us.
            // Post an update event and exit.
            float dist = 0;
            float[] results = new float[1];

            Log.d(TAG, "last time GPS : "
                    + sLatestLatitude.get() + ", "
                    + sLatestLongitude.get() + ", "
                    + "latestAccuracy: " + location.getAccuracy());

            if (!(sLatestLatitude.get() == -999.0 && sLatestLongitude.get() == -999.0)) {
                dist = results[0];
            } else {
                dist = 1000;
            }

            Log.d(TAG, "dist : " + dist);

            //TODO uncomment them when we stop testing
            sLatestLatitude.set(location.getLatitude());
            sLatestLongitude.set(location.getLongitude());
            latestAccuracy = location.getAccuracy();

            //the lastposition update value timestamp
            sLastPosUpdateTime = new Date().getTime();

            StoreToCSV(sLastPosUpdateTime, location.getLatitude(), location.getLongitude(), location.getAccuracy());

            Log.d(TAG,"onLocationChanged latestLatitude : "+ sLatestLatitude +" latestLongitude : "+ sLatestLongitude);
            Log.d(TAG,"onLocationChanged location : "+this.mLocation);

            //store it to the sharePrefrence
            mSharedPrefs.edit().putFloat("latestLatitude", (float) location.getLatitude()).apply();
            mSharedPrefs.edit().putFloat("latestLongitude", (float) location.getLongitude()).apply();

        }
    }

    @Override
    public void onStreamRegistration() {

        float latestLatitudeFromSharedPref = mSharedPrefs.getFloat("latestLatitude", -999);
        float latestLongitudeFromSharedPref = mSharedPrefs.getFloat("latestLongitude", -999);

        sLatestLatitude.set(latestLatitudeFromSharedPref);
        sLatestLongitude.set(latestLongitudeFromSharedPref);


        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mApplicationContext) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(mApplicationContext)
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

    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, LocationDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which LocationDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides LocationDataRecord is already registered.");
        }
    }

    @Override
    public Stream<LocationDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {

        int sessionId = 0;

        int countOfOngoingSession = SessionManager.getInstance().getOngoingSessionIdList().size();

        //if there exists an ongoing session
        if (countOfOngoingSession>0) {
            sessionId = SessionManager.getInstance().getOngoingSessionIdList().get(0);
        }

//        Log.d(TAG, "[test replay] Update stream session is " + session_id);

        LocationDataRecord newLocationDataRecord;
        try {
            newLocationDataRecord = new LocationDataRecord(
                    (float) sLatestLatitude.get(),
                    (float) sLatestLongitude.get(),
                    latestAccuracy,
                    //TODO improve it to ArrayList, ex. the session id should be "0, 10".
                    String.valueOf(sessionId));
        }catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            //no session now
            newLocationDataRecord = new LocationDataRecord(
                    (float) sLatestLatitude.get(),
                    (float) sLatestLongitude.get(),
                    latestAccuracy,
                    String.valueOf(sessionId));
        }
//        Log.e(TAG,"[test replay] newlocationDataRecord latestLatitude : "+ latestLatitude.get()+" latestLongitude : "+ latestLongitude.get() + "  session_id " +  session_id);

        MinukuStreamManager.getInstance().setLocationDataRecord(newLocationDataRecord);

        mStream.add(newLocationDataRecord);
        Log.d(TAG, "Location to be sent to event bus" + newLocationDataRecord);

        // also post an event.
        EventBus.getDefault().post(newLocationDataRecord);
        try {
            appDatabase db;
            db = Room.databaseBuilder(context,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();
            db.locationDataRecordDao().insertAll(newLocationDataRecord);
            List<LocationDataRecord> locationDataRecords = db.locationDataRecordDao().getAll();
            for (LocationDataRecord l : locationDataRecords) {
                Log.e(TAG, " Latitude: "+String.valueOf(l.getLatitude()));
                Log.e(TAG, " Longitude: "+String.valueOf(l.getLongitude()));
                Log.e(TAG," Accuracy: "+ String.valueOf(l.getAccuracy()));
                Log.e(TAG," Altitude: "+ String.valueOf(l.getAltitude()));
                Log.e(TAG," Speed: "+ String.valueOf(l.getSpeed()));
                Log.e(TAG," Bearing: "+ String.valueOf(l.getBearing()));
                Log.e(TAG," Provider: "+l.getProvider());
            }


        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 1; // 1 = 1 minutes
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void offer(LocationDataRecord dataRecord) {
        Log.e(TAG, "Offer for location data record does nothing!");
    }

    /**
     * Location Listener events start here.
     */
    public static void setStartIndoorOutdoor(boolean value) {
        sStartIndoorOutdoor = value;
    }

    public void StoreToCSV(long timestamp, double latitude, double longitude, float accuracy) {

        Log.d(TAG,"StoreToCSV");

        String sFileName = "LocationOnChange.csv";

        try {
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH + sFileName,true));


//            data.add(new String[]{"timestamp","timeString","Latitude","Longitude","Accuracy"});
            String timeString = getTimeString(timestamp);

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{String.valueOf(timestamp),timeString,String.valueOf(latitude),String.valueOf(longitude),String.valueOf(accuracy)});

            csv_writer.writeAll(data);

            csv_writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTimeString(long time) {

        SimpleDateFormat sdfNow = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_SLASH);
        String currentTimeString = sdfNow.format(time);

        return currentTimeString;
    }

    public static void addLocationDataRecord(LocationDataRecord record) {
        getLocationDataRecords().add(record);
        android.util.Log.d("LocationStreamGenerator", "[test replay] adding " + record.toString() + " to LocationRecords in LocationStreamGenerator");
    }

    public static ArrayList<LocationDataRecord> getLocationDataRecords() {

        if (sLocationDataRecords ==null) {
            sLocationDataRecords = new ArrayList<LocationDataRecord>();
        }
        return sLocationDataRecords;

    }

    public void startReplayLocationRecordTimer() {

        //set a new Timer
        mReplayTimer = new Timer();

        //start the timer task for replay
        RePlayActivityRecordTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        mReplayTimer.schedule(mReplayTimerTask,0,1000);

    }

    public void RePlayActivityRecordTimerTask() {


        mReplayTimerTask = new TimerTask() {

            int locationRecordCurIndex = 0;
            int sec = 0;
            public void run() {

                sec++;

                //for every 5 seconds and if we still have more AR labels in the list to reply, we will set an AR label to the streamgenerator
                if (sec % 5 == 0 && sLocationDataRecords.size() > 0 && locationRecordCurIndex < sLocationDataRecords.size() - 1) {

                    LocationDataRecord locationDataRecord = sLocationDataRecords.get(locationRecordCurIndex);

                    sLatestLatitude.set(locationDataRecord.getLatitude());
                    sLatestLongitude.set(locationDataRecord.getLongitude());
                    latestAccuracy = locationDataRecord.getAccuracy();

                    //the lastposition update value timestamp
                    sLastPosUpdateTime = new Date().getTime();

                    android.util.Log.d(TAG, "[test replay] going to feed location " +   locationDataRecord.getLatitude()+  " :"  + locationDataRecord.getLongitude()  +" at index " + locationRecordCurIndex  + " in the location streamgenerator");


                    //set Location

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

        } catch(InterruptedException e) {
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

        } catch (SecurityException e) {
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
