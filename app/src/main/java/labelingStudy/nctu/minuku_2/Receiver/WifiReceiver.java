package labelingStudy.nctu.minuku_2.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import org.javatuples.Decade;
import org.javatuples.Octet;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Septet;
import org.javatuples.Triplet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.streamgenerator.ConnectivityStreamGenerator;
import labelingStudy.nctu.minuku_2.Utils;

/**
 * Created by Lawrence on 2017/8/16.
 */

public class WifiReceiver extends BroadcastReceiver {

    private final String TAG = "WifiReceiver";

    private Handler handler;

    private SharedPreferences sharedPrefs;

    private Runnable runnable = null;

    private int year,month,day,hour,min;

    private long latestUpdatedTime = -9999;
    private long nowTime = -9999;
    private long startTime = -9999;
    private long endTime = -9999;

    private long startTripTime = -9999;

    public static final int HTTP_TIMEOUT = 10000; // millisecond
    public static final int SOCKET_TIMEOUT = 10000; // millisecond

    private static final String postDumpUrl_insert = "http://18.219.118.106:5000/find_latest_and_insert?collection=dump&action=insert&id=";//&action=insert, search
    private static final String postDumpUrl_search = "http://18.219.118.106:5000/find_latest_and_insert?collection=dump&action=search&id=";//&action=insert, search

    private static final String postTripUrl_insert = "http://18.219.118.106:5000/find_latest_and_insert?collection=trip&action=insert&id=";//&action=insert, search
    private static final String postTripUrl_search = "http://18.219.118.106:5000/find_latest_and_insert?collection=trip&action=search&id=";//&action=insert, search

    private static final String postIsAliveUrl_insert = "http://18.219.118.106:5000/find_latest_and_insert?collection=isAlive&action=insert&id=";//&action=insert, search


    public static int mainThreadUpdateFrequencyInSeconds = 10;
    public static long mainThreadUpdateFrequencyInMilliseconds = mainThreadUpdateFrequencyInSeconds * Constants.MILLISECONDS_PER_SECOND;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive");

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //get timzone //prevent the issue when the user start the app in wifi available environment.
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        int mYear = cal.get(Calendar.YEAR);
        int mMonth = cal.get(Calendar.MONTH)+1;
        int mDay = cal.get(Calendar.DAY_OF_MONTH);

        sharedPrefs = context.getSharedPreferences(Constants.sharedPrefString, context.MODE_PRIVATE);

        year = sharedPrefs.getInt("StartYear", mYear);
        month = sharedPrefs.getInt("StartMonth", mMonth);
        day = sharedPrefs.getInt("StartDay", mDay);

        Constants.USER_ID = sharedPrefs.getString("userid","NA");
        Constants.GROUP_NUM = sharedPrefs.getString("groupNum","NA");

        hour = sharedPrefs.getInt("StartHour", 0);
        min = sharedPrefs.getInt("StartMin",0);

        Log.d(TAG, "year : "+ year+" month : "+ month+" day : "+ day+" hour : "+ hour+" min : "+ min);

        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                Log.d(TAG,"Wifi activeNetwork");

                if(runnable==null) {

//                    //Log.d(TAG, "there is no runnable running yet.");

                    MakingJsonDataMainThread();
                }
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                //we might no need to use this.
                // connected to the mobile provider's data plan
                Log.d(TAG, "MOBILE activeNetwork" ) ;
                if(runnable==null) {

//                    //Log.d(TAG, "there is no runnable running yet.");

                    MakingJsonDataMainThread();
                }
            }
        } else {
            // not connected to the internet
            Log.d(TAG, "no Network" ) ;

        }
    }

    public String gettingTripLastTime(){

        JSONObject data = new JSONObject();

        String curr =  getDateCurrentTimeZone(new Date().getTime());

        String result = "";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                result = new HttpAsyncPostJsonTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        postTripUrl_search+Constants.DEVICE_ID,
                        data.toString(),
                        "Trip",
                        curr).get();
            else
                result = new HttpAsyncPostJsonTask().execute(
                        postTripUrl_search+Constants.DEVICE_ID,
                        data.toString(),
                        "Trip",
                        curr).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return result;

    }

    public String gettingDumpLastTime(){
        //TODO upload to MongoDB
        JSONObject data = new JSONObject();

        String curr =  getDateCurrentTimeZone(new Date().getTime());

        String result = "";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                result = new HttpAsyncPostJsonTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        postDumpUrl_search+Constants.DEVICE_ID,
                        data.toString(),
                        "Dump",
                        curr).get();
            else
                result = new HttpAsyncPostJsonTask().execute(
                        postDumpUrl_search+Constants.DEVICE_ID,
                        data.toString(),
                        "Dump",
                        curr).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return result;

    }

    public void sendingDumpData(){

        Log.d(TAG, "sendingDumpData");

        JSONObject data = new JSONObject();

        try {

            data.put("device_id", Constants.DEVICE_ID);

            data.put("startTime", String.valueOf(startTime));
            data.put("endTime", String.valueOf(endTime));
            data.put("startTimeString", ScheduleAndSampleManager.getTimeString(startTime));
            data.put("endTimeString", ScheduleAndSampleManager.getTimeString(endTime));
        }catch (JSONException e){

        }

        storeTransporatation(data);
        storeLocation(data);
        storeActivityRecognition(data);
        storeRinger(data);
        storeConnectivity(data);
        storeBattery(data);
        storeAppUsage(data);
        storeTelephony(data);
        storeSensor(data);
        storeAccessibility(data);

        Log.d(TAG,"final data : "+ data.toString());

        CSVHelper.storeToCSV("Dump.csv", data.toString());

        String curr =  getDateCurrentTimeZone(new Date().getTime());

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new HttpAsyncPostJsonTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        postDumpUrl_insert+ Constants.DEVICE_ID,
                    data.toString(),
                    "Dump",
                    curr).get();
            else
                new HttpAsyncPostJsonTask().execute(
                        postDumpUrl_insert+ Constants.DEVICE_ID,
                        data.toString(),
                        "Dump",
                        curr).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void MakingJsonDataMainThread(){

        handler = new Handler();

        runnable = new Runnable() {

            @Override
            public void run() {

                Log.d(TAG, "MakingJsonDataMainThread runnable");

                Constants.DEVICE_ID = sharedPrefs.getString("DEVICE_ID",  Constants.DEVICE_ID);

                Log.d(TAG, "DEVICE_ID : "+ Constants.DEVICE_ID);

                if(!Constants.DEVICE_ID.equals("NA")) {

                    //dump only can be sent when wifi is connected
                    if (ConnectivityStreamGenerator.mIsWifiConnected) {

                        //TODO update endtime to get the latest data's time from MongoDB
                        //TODO endtime = latest data's time + nextinterval

                        long lastSentStarttime = sharedPrefs.getLong("lastSentStarttime", 0);

                        if (lastSentStarttime == 0) {

                            //if it doesn't reponse the setting with initialize ones
                            //initialize
                            long startstartTime = getSpecialTimeInMillis(makingDataFormat(year, month, day, hour, min));
//                        long startstartTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
                            startTime = sharedPrefs.getLong("StartTime", startstartTime); //default
                            Log.d(TAG, "StartTimeString : " + ScheduleAndSampleManager.getTimeString(startTime));

                            long startendTime = getSpecialTimeInMillis(makingDataFormat(year, month, day, hour + 1, min));
//                        long startendTime = startstartTime + Constants.MILLISECONDS_PER_HOUR;
                            endTime = sharedPrefs.getLong("EndTime", startendTime);
                            Log.d(TAG, "EndTimeString : " + ScheduleAndSampleManager.getTimeString(endTime));
                        } else {

                            //if it do reponse the setting with initialize ones
                            startTime = Long.valueOf(lastSentStarttime);
                            Log.d(TAG, "StartTimeString : " + ScheduleAndSampleManager.getTimeString(startTime));

                            long nextinterval = Constants.MILLISECONDS_PER_HOUR; //1 hr
                            endTime = Long.valueOf(lastSentStarttime) + nextinterval;
                            Log.d(TAG, "EndTimeString : " + ScheduleAndSampleManager.getTimeString(endTime));
                        }

                        nowTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
                        Log.d(TAG, "NowTimeString : " + ScheduleAndSampleManager.getTimeString(nowTime));

                        if (nowTime > endTime && ConnectivityStreamGenerator.mIsWifiConnected == true) {

                            sendingDumpData();

                            //setting nextime interval
                            latestUpdatedTime = endTime;
                            startTime = latestUpdatedTime;

                            long nextinterval = Constants.MILLISECONDS_PER_HOUR; //1 hr

                            endTime = startTime + nextinterval;

//                        Log.d(TAG,"latestUpdatedTime : " + ScheduleAndSampleManager.getTimeString(latestUpdatedTime));
//                        Log.d(TAG,"latestUpdatedTime + 1 hour : " + latestUpdatedTime + nextinterval);
//
//                        sharedPrefs.edit().putLong("StartTime", startTime).apply();
//                        sharedPrefs.edit().putLong("EndTime", endTime).apply();

                            sharedPrefs.edit().putLong("lastSentStarttime", startTime).apply();
                        }
                    }

                    // Trip, isAlive
                    if (ConnectivityStreamGenerator.mIsWifiConnected && ConnectivityStreamGenerator.mIsMobileConnected) {

                        String lastTriptime = gettingTripLastTime();
                        if (lastTriptime == null || lastTriptime.equals("")) {
                            lastTriptime = "0";

                            //if it doesn't reponse the setting with initialize ones
                            //initialize
                            long startstartTime = getSpecialTimeInMillis(makingDataFormat(year, month, day, hour, min));
                            startTripTime = sharedPrefs.getLong("startTripTime", startstartTime); //default
                            Log.d(TAG, "StartTripTimeString : " + getTimeString(startTripTime));


                        } else {
                            //if it do reponse the setting with initialize ones
                            startTripTime = Long.valueOf(lastTriptime);
                            Log.d(TAG, "StartTripTimeString : " + getTimeString(startTripTime));

                        }

                        sendingTripData();

                        sendingIsAliveData();

                    }
                }

                handler.postDelayed(this, mainThreadUpdateFrequencyInMilliseconds);
            }
        };

        handler.post(runnable);

    }

    private void sendingTripData(){
        JSONObject data = storeTrip();

        Log.d(TAG, "trip data uploading : " + data.toString());

        String curr = getDateCurrentTimeZone(new Date().getTime());

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new HttpAsyncPostJsonTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        postTripUrl_insert + Constants.DEVICE_ID,
                        data.toString(),
                        "Trip",
                        curr).get();
            else
                new HttpAsyncPostJsonTask().execute(
                        postTripUrl_insert + Constants.DEVICE_ID,
                        data.toString(),
                        "Trip",
                        curr).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    private void sendingIsAliveData(){

        //making isAlive
        JSONObject data = new JSONObject();
        try {
            long currentTime = new Date().getTime();
            String currentTimeString = getTimeString(currentTime);

            data.put("time", currentTime);
            data.put("timeString", currentTimeString);
            data.put("device_id", Constants.DEVICE_ID);

        }catch (JSONException e){
            e.printStackTrace();
        }

        Log.d(TAG, "isAlive data uploading : " + data.toString());

        String curr = getDateCurrentTimeZone(new Date().getTime());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new HttpAsyncPostJsonTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        postIsAliveUrl_insert + Constants.DEVICE_ID,
                        data.toString(),
                        "isAlive",
                        curr).get();
            else
                new HttpAsyncPostJsonTask().execute(
                        postIsAliveUrl_insert + Constants.DEVICE_ID,
                        data.toString(),
                        "isAlive",
                        curr).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    //use HTTPAsyncTask to poHttpAsyncPostJsonTaskst data
    private class HttpAsyncPostJsonTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String result=null;
            String url = params[0];
            String data = params[1];
            String dataType = params[2];
            String lastSyncTime = params[3];

            postJSON(url, data, dataType, lastSyncTime);

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "get http post result : " + result);
        }

    }

    public HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public String postJSON (String address, String json, String dataType, String lastSyncTime) {

        Log.d(TAG, "[postJSON] testbackend post data to " + address);

        InputStream inputStream = null;
        String result = "";
        HttpURLConnection conn = null;
        try {

            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "[postJSON] testbackend connecting to " + address);

            if (url.getProtocol().toLowerCase().equals("https")) {
                Log.d(TAG, "[postJSON] [using https]");
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                conn = https;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            SSLContext sc;
            sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());

            conn.setReadTimeout(HTTP_TIMEOUT);
            conn.setConnectTimeout(SOCKET_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");
            conn.connect();

            OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
            wr.write(json);
            wr.close();

            Log.d(TAG, "Post:\t" + dataType + "\t" + "for lastSyncTime:" + lastSyncTime);

            int responseCode = conn.getResponseCode();

            if(responseCode >= 400)
                inputStream = conn.getErrorStream();
            else
                inputStream = conn.getInputStream();

            result = convertInputStreamToString(inputStream);

            Log.d(TAG, "[postJSON] the result response code is " + responseCode);
            Log.d(TAG, "[postJSON] the result is " + result);

            if (conn!=null)
                conn.disconnect();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (java.net.SocketTimeoutException e){

            Log.d(TAG, "SocketTimeoutException EE", e);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            if (conn != null) {

                try {

                    conn.disconnect();
                } catch (Exception e) {

                    Log.d(TAG, "exception", e);
                }
            }
        }

        return result;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException{

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null){
//            Log.d(LOG_TAG, "[syncWithRemoteDatabase] " + line);
            result += line;
        }

        inputStream.close();
        return result;

    }

    /***
     * trust all hsot....
     */
    private void trustAllHosts() {

        X509TrustManager easyTrustManager = new X509TrustManager() {

            public void checkClientTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
                // Oh, I am easy!
            }

            public void checkServerTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
                // Oh, I am easy!
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }


        };

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {easyTrustManager};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject storeTrip(){

        Log.d(TAG, "storeTrip");

        JSONObject tripJson = new JSONObject();

        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor tripCursor = db.rawQuery("SELECT * FROM "+DBHelper.annotate_table+ " WHERE " + DBHelper.uploaded_col + " = '" + false + "' ", null); //cause pos start from 0.
            Log.d(TAG,"SELECT * FROM "+DBHelper.annotate_table+ " WHERE " + DBHelper.uploaded_col + " = '" + false + "' "); //+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' "

            int rows = tripCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                tripCursor.moveToFirst();
                for(int i=0;i<rows;i++) {

                    String _id = tripCursor.getString(0);

                    JSONObject dataJson = new JSONObject();

                    String startTime = tripCursor.getString(1);
                    String endTime = tripCursor.getString(2);
                    String startTimeString = tripCursor.getString(3);
                    String endTimeString = tripCursor.getString(4);
                    String sessionid = tripCursor.getString(5);

                    JSONObject annotation_Json = new JSONObject();

                    String activity = tripCursor.getString(6);
                    String annotation_Goal = tripCursor.getString(7);
                    String annotation_SpecialEvent = tripCursor.getString(8);

                    String siteName = tripCursor.getString(10);

                    Log.d(TAG,"_id : "+_id+" startTime : "+startTime+" endTime : "+endTime+" sessionid : "+sessionid);
                    Log.d(TAG,"activity : "+activity+" annotation_Goal : "+annotation_Goal+" annotation_SpecialEvent : "+annotation_SpecialEvent);

                    annotation_Json.put("activity", activity);
                    annotation_Json.put("annotation_Goal", annotation_Goal);
                    annotation_Json.put("annotation_SpecialEvent", annotation_SpecialEvent);

                    dataJson.put("device_id", Constants.DEVICE_ID);
                    dataJson.put("uid", _id); //we can't call "_id" because of MongoDB, it will have its own.
                    dataJson.put("startTime", startTime);
                    dataJson.put("endTime", endTime);
                    dataJson.put("startTimeString", startTimeString);
                    dataJson.put("endTimeString", endTimeString);
                    dataJson.put("sessionid", sessionid);
                    dataJson.put("annotation", annotation_Json);
                    dataJson.put("siteName", siteName);

                    tripJson = dataJson;

//                    tripJson.put(_id, dataJson);

                    tripCursor.moveToNext();
                }

                Log.d(TAG,"tripJson : "+ tripJson.toString());

            }

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        return tripJson;
    }

    private void storeTransporatation(JSONObject data){

        try {

            JSONArray transportationAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.transportationMode_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.

            int rows = cursor.getCount();
            if(rows!=0){

                cursor.moveToFirst();
                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String transportation = cursor.getString(2);

                    //Log.d(TAG,"transportation : "+transportation+" timestamp : "+timestamp);

                    //convert into second
//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestamps, Transportation>
                    Pair<String, String> transportationTuple = new Pair<>(timestamp, transportation);

                    String dataInPythonTuple = Utils.toPythonTuple(transportationTuple);

                    transportationAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("TransportationMode",transportationAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    private void storeLocation(JSONObject data){

        try {

            JSONArray locationAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.location_table +" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.
            //Log.d(TAG,"SELECT * FROM "+DBHelper.STREAM_TYPE_LOCATION +" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ");

            int rows = cursor.getCount();

            if(rows!=0){

                cursor.moveToFirst();
                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String latitude = cursor.getString(2);
                    String longtitude = cursor.getString(3);
                    String accuracy = cursor.getString(4);

                    //Log.d(TAG,"timestamp : "+timestamp+" latitude : "+latitude+" longtitude : "+longtitude+" accuracy : "+accuracy);

                    //convert into second
//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestamp, latitude, longitude, accuracy>
                    Quartet<String, String, String, String> locationTuple =
                            new Quartet<>(timestamp, latitude, longtitude, accuracy);

                    String dataInPythonTuple = Utils.toPythonTuple(locationTuple);

                    locationAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("Location",locationAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    private void storeActivityRecognition(JSONObject data){

        try {

            JSONArray arAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.activityRecognition_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.

            int rows = cursor.getCount();

            if(rows!=0){

                cursor.moveToFirst();
                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String mostProbableActivity = cursor.getString(2);
                    String probableActivities = cursor.getString(3);

                    //split the mostProbableActivity into "type:conf"
                    String[] subMostActivity = mostProbableActivity.split(",");

                    String type = subMostActivity[0].split("=")[1];
                    String confidence = subMostActivity[1].split("=")[1].replaceAll("]","");

                    mostProbableActivity = type+":"+confidence;

                    //choose the top two of the probableActivities and split it into "type:conf"
                    String[] subprobableActivities = probableActivities.split("\\,");
//                    //Log.d(TAG, "subprobableActivities : "+ subprobableActivities);

                    int lastIndex = 0;
                    int count = 0;

                    while(lastIndex != -1){

                        lastIndex = probableActivities.indexOf("DetectedActivity",lastIndex);

                        if(lastIndex != -1){
                            count ++;
                            lastIndex += "DetectedActivity".length();
                        }
                    }

                    if(count == 1){
                        String type1 = subprobableActivities[0].split("=")[1];
                        String confidence1 = subprobableActivities[1].split("=")[1].replaceAll("]","");

                        probableActivities = type1+":"+confidence1;

                    }else if(count > 1){
                        String type1 = subprobableActivities[0].split("=")[1];
                        String confidence1 = subprobableActivities[1].split("=")[1].replaceAll("]","");
                        String type2 = subprobableActivities[2].split("=")[1];
                        String confidence2 = subprobableActivities[3].split("=")[1].replaceAll("]","");

                        probableActivities = type1+":"+confidence1+Constants.DELIMITER+type2+":"+confidence2;

                    }

                    //Log.d(TAG,"timestamp : "+timestamp+", mostProbableActivity : "+mostProbableActivity+", probableActivities : "+probableActivities);

                    //convert into Second
//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestamps, MostProbableActivity, ProbableActivities>
                    Triplet<String, String, String> arTuple =
                            new Triplet<>(timestamp, mostProbableActivity, probableActivities);

                    String dataInPythonTuple = Utils.toPythonTuple(arTuple);

                    arAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("ActivityRecognition",arAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    private void storeRinger(JSONObject data){

        try {

            JSONArray ringerAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.ringer_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.

            int rows = cursor.getCount();

            if(rows!=0){

                cursor.moveToFirst();
                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String ringerMode = cursor.getString(2);
                    String audioMode = cursor.getString(3);
                    String streamVolumeMusic = cursor.getString(4);
                    String streamVolumeNotification = cursor.getString(5);
                    String streamVolumeRing = cursor.getString(6);
                    String streamVolumeVoicecall = cursor.getString(7);
                    String streamVolumeSystem = cursor.getString(8);

                    //Log.d(TAG,"timestamp : "+timestamp+" RingerMode : "+RingerMode+" AudioMode : "+AudioMode+
//                            " StreamVolumeMusic : "+StreamVolumeMusic+" StreamVolumeNotification : "+StreamVolumeNotification
//                            +" StreamVolumeRing : "+StreamVolumeRing +" StreamVolumeVoicecall : "+StreamVolumeVoicecall
//                            +" StreamVolumeSystem : "+StreamVolumeSystem);

//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestampInSec, streamVolumeSystem, streamVolumeVoicecall, streamVolumeRing,
                    // streamVolumeNotification, streamVolumeMusic, audioMode, ringerMode>
                    Octet<String, String, String, String, String, String, String, String> ringerTuple
                            = new Octet<>(timestamp, streamVolumeSystem, streamVolumeVoicecall, streamVolumeRing,
                            streamVolumeNotification, streamVolumeMusic, audioMode, ringerMode);

                    String dataInPythonTuple = Utils.toPythonTuple(ringerTuple);

                    ringerAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("Ringer",ringerAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    private void storeConnectivity(JSONObject data){

        try {

            JSONArray connectivityAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.connectivity_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.

            int rows = cursor.getCount();

            if(rows!=0){

                cursor.moveToFirst();
                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String NetworkType = cursor.getString(2);
                    String IsNetworkAvailable = cursor.getString(3);
                    String IsConnected = cursor.getString(4);
                    String IsWifiAvailable = cursor.getString(5);
                    String IsMobileAvailable = cursor.getString(6);
                    String IsWifiConnected = cursor.getString(7);
                    String IsMobileConnected = cursor.getString(8);

                    //Log.d(TAG,"timestamp : "+timestamp+" NetworkType : "+NetworkType+" IsNetworkAvailable : "+IsNetworkAvailable
//                            +" IsConnected : "+IsConnected+" IsWifiAvailable : "+IsWifiAvailable
//                            +" IsMobileAvailable : "+IsMobileAvailable +" IsWifiConnected : "+IsWifiConnected
//                            +" IsMobileConnected : "+IsMobileConnected);

//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestampInSec, IsMobileConnected, IsWifiConnected, IsMobileAvailable,
                    // IsWifiAvailable, IsConnected, IsNetworkAvailable, NetworkType>
                    Octet<String, String, String, String, String, String, String, String> connectivityTuple
                            = new Octet<>(timestamp, IsMobileConnected, IsWifiConnected, IsMobileAvailable,
                            IsWifiAvailable, IsConnected, IsNetworkAvailable, NetworkType);

                    String dataInPythonTuple = Utils.toPythonTuple(connectivityTuple);

                    connectivityAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("Connectivity",connectivityAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    private void storeBattery(JSONObject data){

        try {

            JSONArray batteryAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.battery_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.

            int rows = cursor.getCount();

            if(rows!=0){

                cursor.moveToFirst();
                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String BatteryLevel = cursor.getString(2);
                    String BatteryPercentage = cursor.getString(3);
                    String BatteryChargingState = cursor.getString(4);
                    String isCharging = cursor.getString(5);

                    //Log.d(TAG,"timestamp : "+timestamp+" BatteryLevel : "+BatteryLevel+" BatteryPercentage : "+
//                            BatteryPercentage+" BatteryChargingState : "+BatteryChargingState+" isCharging : "+isCharging);

//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestamps, isCharging, BatteryChargingState, BatteryPercentage, BatteryLevel>
                    Quintet<String, String, String, String, String> batteryTuple
                            = new Quintet<>(timestamp, isCharging, BatteryChargingState, BatteryPercentage, BatteryLevel);

                    String dataInPythonTuple = Utils.toPythonTuple(batteryTuple);

                    batteryAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("Battery", batteryAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    private void storeAppUsage(JSONObject data){

        try {

            JSONArray appUsageAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.appUsage_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.

            int rows = cursor.getCount();

            if(rows!=0){

                cursor.moveToFirst();

                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String ScreenStatus = cursor.getString(2);
                    String Latest_Used_App = cursor.getString(3);
                    String Latest_Foreground_Activity = cursor.getString(4);

//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestamp, ScreenStatus, Latest_Used_App, Latest_Foreground_Activity>
                    Quartet<String, String, String, String> appUsageTuple
                            = new Quartet<>(timestamp, ScreenStatus, Latest_Used_App, Latest_Foreground_Activity);

                    String dataInPythonTuple = Utils.toPythonTuple(appUsageTuple);

                    appUsageAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("AppUsage",appUsageAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    public void storeTelephony(JSONObject data){

        try {

            JSONArray telephonyAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.telephony_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.

            int rows = cursor.getCount();

            if(rows!=0){

                cursor.moveToFirst();

                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String networkOperatorName = cursor.getString(2);
                    String callState = cursor.getString(3);
                    String phoneSignalType = cursor.getString(4);
                    String gsmSignalStrength = cursor.getString(5);
                    String LTESignalStrength = cursor.getString(6);
                    String CdmaSignalStrengthLevel = cursor.getString(7);

//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestamp, networkOperatorName, CallState, PhoneSignalType_col, gsmSignalStrength, LTESignalStrength, CdmaSignalStrengthLevel>
                    Septet<String, String, String, String, String, String ,String> telephonyTuple
                            = new Septet<>(timestamp, networkOperatorName, callState, phoneSignalType, gsmSignalStrength, LTESignalStrength, CdmaSignalStrengthLevel);

                    String dataInPythonTuple = Utils.toPythonTuple(telephonyTuple);

                    telephonyAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("Telephony",telephonyAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    public void storeSensor(JSONObject data){

        try {

            JSONArray sensorAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.sensor_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.

            int rows = cursor.getCount();

            if(rows!=0){

                cursor.moveToFirst();

                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String accelerometer = cursor.getString(2);
                    String gyroscope = cursor.getString(3);
                    String gravity = cursor.getString(4);
                    String linear_acceleration = cursor.getString(5);
                    String rotation_vector = cursor.getString(6);
                    String proximity = cursor.getString(7);
                    String magnetic_field = cursor.getString(8);
                    String light = cursor.getString(9);
                    String pressure = cursor.getString(10);

//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestamp, accelerometer, gyroscope, gravity, linear_acceleration, ROTATION_VECTOR, PROXIMITY, MAGNETIC_FIELD, LIGHT, PRESSURE>
                    Decade<String, String, String, String, String, String ,String, String, String, String> sensorTuple1
                            = new Decade<>(timestamp, accelerometer, gyroscope, gravity, linear_acceleration, rotation_vector, proximity, magnetic_field, light, pressure);

                    String relative_humidity = cursor.getString(11);
                    String ambient_temperature = cursor.getString(12);

                    //<RELATIVE_HUMIDITY, AMBIENT_TEMPERATURE>
                    Pair<String, String> sensorTuple2 = new Pair<>(relative_humidity, ambient_temperature);

                    String dataInPythonTuple = Utils.tupleConcat(sensorTuple1, sensorTuple2);

                    Log.d(TAG, "Sensor data : "+dataInPythonTuple);

                    sensorAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("Sensor",sensorAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    public void storeAccessibility(JSONObject data){

        try {

            JSONArray accessibilityAndtimestampsJson = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DBHelper.sensor_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.

            int rows = cursor.getCount();

            if(rows!=0){

                cursor.moveToFirst();

                for(int i=0;i<rows;i++) {

                    String timestamp = cursor.getString(1);
                    String pack = cursor.getString(2);
                    String text = cursor.getString(3);
                    String type = cursor.getString(4);
                    String extra = cursor.getString(5);

//                    String timestampInSec = timestamp.substring(0, timestamp.length()-3);

                    //<timestamp, pack, text, type, extra>
                    Quintet<String, String, String, String, String> accessibilityTuple
                            = new Quintet<>(timestamp, pack, text, type, extra);

                    String dataInPythonTuple = Utils.toPythonTuple(accessibilityTuple);

                    accessibilityAndtimestampsJson.put(dataInPythonTuple);

                    cursor.moveToNext();
                }

                data.put("Accessibility", accessibilityAndtimestampsJson);
            }
        }catch (JSONException e){
        }catch(NullPointerException e){
        }
    }

    private long getSpecialTimeInMillis(String givenDateFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        long timeInMilliseconds = 0;
        try {
            Date mDate = sdf.parse(givenDateFormat);
            timeInMilliseconds = mDate.getTime();
            Log.d(TAG,"Date in milli :: " + timeInMilliseconds);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeInMilliseconds;
    }

    private long getSpecialTimeInMillis(int year,int month,int date,int hour,int min){
//        TimeZone tz = TimeZone.getDefault(); tz
        Calendar cal = Calendar.getInstance();
//        cal.set(year,month,date,hour,min,0);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, 0);

        long t = cal.getTimeInMillis();

        return t;
    }

    private void storeTripToLocalFolder(JSONObject completedJson){
        Log.d(TAG, "storeTripToLocalFolder");

        String sFileName = "Trip_"+getTimeString(startTime)+"_"+getTimeString(endTime)+".json";

        Log.d(TAG, "sFileName : "+ sFileName);

        try {
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            Log.d(TAG, "root : " + root);

            FileWriter fileWriter = new FileWriter(root+sFileName, true);
            fileWriter.write(completedJson.toString());
            fileWriter.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    private void storeToLocalFolder(JSONObject completedJson, int count){
        Log.d(TAG, "storeToLocalFolder");

//        String sFileName = "Dump_"+getTimeString(startTime)+"_"+getTimeString(endTime)+".json";

        count ++ ;

        String sFileName = "Dump_"+count+".json";

        Log.d(TAG, "sFileName : "+ sFileName);

        try {
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            Log.d(TAG, "root : " + root);

            FileWriter fileWriter = new FileWriter(root+sFileName, true);
            fileWriter.write(completedJson.toString());
            fileWriter.close();

        } catch(IOException e) {
            e.printStackTrace();
        }

    }
    //TODO remember the format is different from the normal one.
    public static String getTimeString(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_for_storing);
        String currentTimeString = sdf_now.format(time);

        return currentTimeString;
    }

    public String makingDataFormat(int year,int month,int date,int hour,int min){
        String dataformat= "";

        dataformat = addZero(year)+"/"+addZero(month)+"/"+addZero(date)+" "+addZero(hour)+":"+addZero(min)+":00";
        Log.d(TAG,"dataformat : " + dataformat);

        return dataformat;
    }

    public String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getmillisecondToDateWithTime(long timeStamp){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH)+1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mhour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMin = calendar.get(Calendar.MINUTE);
        int mSec = calendar.get(Calendar.SECOND);

        return addZero(mYear)+"/"+addZero(mMonth)+"/"+addZero(mDay)+" "+addZero(mhour)+":"+addZero(mMin)+":"+addZero(mSec);

    }

    private String addZero(int date){
        if(date<10)
            return String.valueOf("0"+date);
        else
            return String.valueOf(date);
    }

    /**get the current time in milliseconds**/
    private long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        //get the date of now: the first month is Jan:0
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int Hour = cal.get(Calendar.HOUR);
        int Min = cal.get(Calendar.MINUTE);

        long t = getSpecialTimeInMillis(year,month,day,Hour,Min);
        return t;
    }
}
