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

import android.content.Context;
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
import com.google.common.util.concurrent.AtomicDouble;
import com.opencsv.CSVWriter;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.R;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.LocationDataRecordDAO;
import labelingStudy.nctu.minuku.event.DecrementLoadingProcessCountEvent;
import labelingStudy.nctu.minuku.event.IncrementLoadingProcessCountEvent;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.manager.TripManager;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.stream.LocationStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by neerajkumar on 7/18/16.
 */
public class LocationStreamGenerator extends AndroidStreamGenerator<LocationDataRecord> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private LocationStream mStream;
    private String TAG = "LocationStreamGenerator";

//    private static final String PACKAGE_DIRECTORY_PATH="/Android/data/edu.nctu.minuku_2/";

    private CSVWriter csv_writer = null;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_LATITUDE = "Latitude";
    public static final String RECORD_DATA_PROPERTY_LONGITUDE = "Longitude";
    public static final String RECORD_DATA_PROPERTY_ACCURACY = "Accuracy";
    public static final String RECORD_DATA_PROPERTY_ALTITUDE = "Altitude";
    public static final String RECORD_DATA_PROPERTY_PROVIDER = "Provider";
    public static final String RECORD_DATA_PROPERTY_SPEED = "Speed";
    public static final String RECORD_DATA_PROPERTY_BEARING = "Bearing";
    public static final String RECORD_DATA_PROPERTY_EXTRAS = "Extras";

    private static long sUpdateIntervalInMilliSeconds = Constants.INTERNAL_LOCATION_UPDATE_FREQUENCY;


    public static long lastposupdate = -99;

    public static AtomicDouble latitude;
    public static AtomicDouble longitude;

    private Context context;

    public static float accuracy;

//    private TripManager tripManager;

    private Location location;

    LocationDataRecordDAO mDAO;

    public static LocationDataRecord toCheckFamiliarOrNotLocationDataRecord;

//    TelephonyManager tel;
//    MyPhoneStateListener myPhoneStateListener;

    public LocationStreamGenerator(Context applicationContext) {
        super(applicationContext);
        this.mStream = new LocationStream(Constants.LOCATION_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(LocationDataRecord.class);
        this.latitude = new AtomicDouble();
        this.longitude = new AtomicDouble();

        this.context = applicationContext;
//        tripManager = new TripManager();

        createCSV();

        this.register();
    }

    @Override
    public void onStreamRegistration() {

        this.latitude.set(-999.0);
        this.longitude.set(-999.0);

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mApplicationContext)
                == ConnectionResult.SUCCESS) {
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

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Log.d(TAG, "Stream " + TAG + "initialized from previous state");
                    Future<List<LocationDataRecord>> listFuture =
                            mDAO.getLast(Constants.LOCATION_QUEUE_SIZE);
                    while(!listFuture.isDone()) {
                        Thread.sleep(1000);
                    }
                    Log.d(TAG, "Received data from Future for " + TAG);
                    mStream.addAll(new LinkedList<>(listFuture.get()));
                } catch (DAOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    EventBus.getDefault().post(new DecrementLoadingProcessCountEvent());
                }
            }
        });



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
        Log.d(TAG, "Update stream called.");
        LocationDataRecord locationDataRecord = new LocationDataRecord(
                (float)latitude.get(),
                (float)longitude.get());
        Log.e(TAG,"locationDataRecord latitude : "+latitude.get()+" longitude : "+longitude.get());

        /*
        JSONObject data = new JSONObject();

        //add location to data
        try {
            data.put(RECORD_DATA_PROPERTY_LATITUDE, location.getLatitude());
            data.put(RECORD_DATA_PROPERTY_LONGITUDE, location.getLongitude());
            data.put(RECORD_DATA_PROPERTY_ALTITUDE, location.getAltitude());
            data.put(RECORD_DATA_PROPERTY_ACCURACY, location.getAccuracy());
            data.put(RECORD_DATA_PROPERTY_SPEED, location.getSpeed());
            data.put(RECORD_DATA_PROPERTY_BEARING, location.getBearing());
            data.put(RECORD_DATA_PROPERTY_PROVIDER, location.getProvider());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        LocationDataRecord jsonlocationDataRecord = new LocationDataRecord(data);
*/
//        Log.d(TAG, "updateStream location "+location);
//        if(location!=null) {
            LocationDataRecord newlocationDataRecord = new LocationDataRecord(
                (float) latitude.get(),
                (float) longitude.get(),
                accuracy);

            /*LocationDataRecord newlocationDataRecord = new LocationDataRecord(
                    (float) latitude.get(),
                    (float) longitude.get(),
                    accuracy,
                    (float) location.getAltitude(),
                    location.getSpeed(),
                    location.getBearing(),
                    location.getProvider());*/

            Log.e(TAG,"newlocationDataRecord latitude : "+latitude.get()+" longitude : "+longitude.get());

            MinukuStreamManager.getInstance().setLocationDataRecord(newlocationDataRecord);
            toCheckFamiliarOrNotLocationDataRecord = newlocationDataRecord;

            mStream.add(newlocationDataRecord);
            Log.d(TAG, "Location to be sent to event bus" + newlocationDataRecord);

            // also post an event.
            EventBus.getDefault().post(newlocationDataRecord);
            try {

                mDAO.add(newlocationDataRecord);

                String current_task = context.getResources().getString(R.string.current_task);

                //in PART the session id will be controlled by the user
                if(!current_task.equals("PART")) {
                    Log.d(TAG, "current_task is not equal to PART");
                    TripManager.getInstance().setTrip(locationDataRecord);
                }

                mDAO.query_counting();
                mDAO.query_getAll();


            } catch (DAOException e) {
                e.printStackTrace();
                return false;
            }
//        }
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

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d(TAG, "GPS: "
                    + location.getLatitude() + ", "
                    + location.getLongitude() + ", "
                    + "accuracy: " + location.getAccuracy());

            // If the location is accurate to 30 meters, it's good enough for us.
            // Post an update event and exit. //TODO maybe be
            /*if (location.getAccuracy() < 100.0f) {
                if(!this.latitude.equals(location.getLatitude())
                        || !this.longitude.equals(location.getLongitude())) {
                    Log.d(TAG, "Location is accurate upto 50 meters");
                    this.latitude.set(location.getLatitude());
                    this.longitude.set(location.getLongitude());
                    accuracy = location.getAccuracy();

                    lastposupdate = new Date().getTime();

                    StoreToCSV(lastposupdate,location.getLatitude(),location.getLongitude(),location.getAccuracy());

                    Log.d(TAG,"onLocationChanged latitude : "+latitude+" longitude : "+ longitude);
                    /*//**** additional
//                    this.location = location;
                    Log.d(TAG,"onLocationChanged location : "+this.location);
//                    updateStream();
                }
            } else {
                Log.d(TAG, "Location is not accurate");
            }*/

           // Log.d(TAG, "Location is accurate upto 50 meters");
            this.latitude.set(location.getLatitude());
            this.longitude.set(location.getLongitude());
            accuracy = location.getAccuracy();

            //the lastposition update value timestamp
            lastposupdate = new Date().getTime();

            StoreToCSV(lastposupdate,location.getLatitude(),location.getLongitude(),location.getAccuracy());

            LocationDataRecord locationDataRecord = new LocationDataRecord(
                    (float) latitude.get(),
                    (float) longitude.get(),
                    accuracy);

            String current_task = context.getResources().getString(R.string.current_task);

            //in PART the session id will be controlled by the user
            if(!current_task.equals("PART")) {
                Log.d(TAG, "current_task is not equal to PART");
                TripManager.getInstance().setTrip(locationDataRecord);
            }

            Log.d(TAG,"onLocationChanged latitude : "+latitude+" longitude : "+ longitude);
            //**** additional
//                    this.location = location;
            Log.d(TAG,"onLocationChanged location : "+this.location);
//                    updateStream();

        }
    }

    public void StoreToCSV(long timestamp, double latitude, double longitude, float accuracy){

        Log.d(TAG,"StoreToCSV");

        String sFileName = "LocationOnChange.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

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
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            data.add(new String[]{"timestamp","timeString","Latitude","Longitude","Accuracy"});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String getTimeString(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        String currentTimeString = sdf_now.format(time);

        return currentTimeString;
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
        mLocationRequest.setInterval(sUpdateIntervalInMilliSeconds);
        mLocationRequest.setFastestInterval(sUpdateIntervalInMilliSeconds);
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
/*

    public void setLocationUpdateInterval(long updateInterval) {

        Log.i("LocationStreamGenerator", "[testLocationUpdate] attempt to update the location request interval to " + updateInterval);

        //before we update we make sure GoogleClient is connected.
        if (!mGoogleApiClient.isConnected()){
            //do nothing
        }
        else{
            sUpdateIntervalInMilliSeconds = updateInterval;

            //after we get location we need to update the location request
            //1. remove the update
            removeUpdates();
            //2. create new update, and then start update
            createLocationRequest();
            requestUpdates();

        }


    }

    @Override
    public void removeUpdates() {
        //stop requesting location udpates

        mRequestingLocationUpdates = false;
        Log.d(TAG, "[testLocationUpdate]  going to remove location update ");

        if (!mGoogleApiClient.isConnected()) {
            connentClient();
        }
        else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d(TAG, "[testLocationUpdate] we have removed location update ");
            disconnectClient();
        }
    }

    @Override
    public void requestUpdates() {

        Log.d(TAG, "[testLocationUpdate] going to request location update ");
        //we need to get location. Set this true
        mRequestingLocationUpdates = true;

        //first check whether we have GoogleAPIClient connected. if yes, we request location. Otherwise
        //we connect the client and then in onConnected() we request location
        if (!mGoogleApiClient.isConnected()){
            Log.d(TAG,"[testLocationUpdate] Google Service is not connected, need to connect ");
            connentClient();
        }
        else {
            Log.d(TAG, "[testLocationUpdate] Google Service is connected, now starts to start location update ");
            startLocationUpdates();
            disconnectClient();
        }
    }
*/

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
