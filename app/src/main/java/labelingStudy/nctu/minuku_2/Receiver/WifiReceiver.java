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
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.streamgenerator.ConnectivityStreamGenerator;

/**
 * Created by Lawrence on 2017/8/16.
 */

public class WifiReceiver extends BroadcastReceiver {

    private final String TAG = "WifiReceiver";

    private Handler mDumpThread, handler;

    private SharedPreferences sharedPrefs;

    private Runnable runnable_ = null, runnable = null;

    private int year,month,day,hour,min;

    private long latestUpdatedTime = -9999;
    private long nowTime = -9999;
    private long startTime = -9999;
    private long endTime = -9999;

    public static final int HTTP_TIMEOUT = 10000; // millisecond
    public static final int SOCKET_TIMEOUT = 20000; // millisecond

    private boolean noDataFlag1 = false;
    private boolean noDataFlag2 = false;
    private boolean noDataFlag3 = false;
    private boolean noDataFlag4 = false;
    private boolean noDataFlag5 = false;
    private boolean noDataFlag6 = false;
    private boolean noDataFlag7 = false;

//    private static final String PACKAGE_DIRECTORY_PATH="/Android/data/labelingStudy.nctu.minuku_2/";

    private static final String postDumpUrl_insert = "http://52.14.68.199:5000/find_latest_and_insert?collection=dump&action=insert&id=";//&action=insert, search
    private static final String postDumpUrl_search = "http://52.14.68.199:5000/find_latest_and_insert?collection=dump&action=search&id=";//&action=insert, search

    private static final String postTripUrl_insert = "http://52.14.68.199:5000/find_latest_and_insert?collection=trip&action=insert&id=";//&action=insert, search
    private static final String postTripUrl_search = "http://52.14.68.199:5000/find_latest_and_insert?collection=trip&action=search&id=";//&action=insert, search

    private static final String postIsAliveUrl_insert = "http://52.14.68.199:5000/find_latest_and_insert?collection=isalive&action=insert&id=";//&action=insert, search


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

        mDay++; //start the task tomorrow.

        sharedPrefs = context.getSharedPreferences("edu.umich.minuku_2", context.MODE_PRIVATE);

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

                //do the work here.
//                MakingJsonDumpDataMainThread();
                MakingJsonDataMainThread();

            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                //we might no need to use this.
                // connected to the mobile provider's data plan
                Log.d(TAG, "MOBILE activeNetwork" ) ;
                if(runnable_ !=null){
//                    mDumpThread.removeCallbacks(runnable_);

                }
            }
        } else {
            // not connected to the internet
            Log.d(TAG, "no Network" ) ;
            if(runnable_ !=null) {
//                mDumpThread.removeCallbacks(runnable_);
            }
        }

    }

    public void MakingJsonDumpDataMainThread(){

        Log.d(TAG, "MakingJsonDumpDataMainThread") ;

        mDumpThread = new Handler();

        runnable_ = new Runnable() {

            @Override
            public void run() {

                //TODO at official min should replaced by 0.
//                Trip_startTime = getSpecialTimeInMillis(year,month,day,hour,min);
                long startstartTime = getSpecialTimeInMillis(makingDataFormat(year,month,day,hour,min));
                startTime = sharedPrefs.getLong("StartTime", startstartTime);
//                Trip_startTime = sharedPrefs.getLong("Trip_startTime", getSpecialTimeInMillis(year,month,day,hour,min));
//                Log.d(TAG,"Start : "+ getSpecialTimeInMillis(makingDataFormat(year,month,day,hour,min)));
                Log.d(TAG,"Start year : "+ year+" month : "+ month+" day : "+ day+" hour : "+ hour+" min : "+ min);
//                Log.d(TAG,"StartTime : " + getTimeString(getSpecialTimeInMillis(makingDataFormat(year,month,day,hour,min))));
                Log.d(TAG,"StartTimeString : " + getTimeString(startTime));
                Log.d(TAG,"StartTime : " + startTime);

                long startendTime = getSpecialTimeInMillis(makingDataFormat(year,month,day,hour+1,min));
                endTime = sharedPrefs.getLong("EndTime", startendTime);
//                Trip_endTime = sharedPrefs.getLong("Trip_endTime", getSpecialTimeInMillis(year,month,day,hour,min+10));
                Log.d(TAG,"End year : "+ year+" month : "+ month+" day : "+ day+" hour+1 : "+ (hour+1)+" min : "+ min);
                Log.d(TAG,"EndTimeString : " + getTimeString(endTime));
                Log.d(TAG,"EndTime : " + endTime);

                nowTime = new Date().getTime();//getCurrentTimeInMillis();//
                Log.d(TAG,"NowTimeString : " + getTimeString(nowTime));
                Log.d(TAG,"NowTime : " + nowTime);

                if(nowTime > endTime && ConnectivityStreamGenerator.mIsWifiConnected == true) {

                    sendingDumpData();

                    //setting nextime interval
                    latestUpdatedTime = endTime;
                    startTime = latestUpdatedTime;

//                    long nextinterval = getSpecialTimeInMillis(makingDataFormat(0,0,0,0,5));
                    long nextinterval = 1 * 60 * 60000; //1 hr

                    endTime = startTime + nextinterval;//getSpecialTimeInMillis(0,0,0,0,10);

                    Log.d(TAG,"latestUpdatedTime : " + latestUpdatedTime);
                    Log.d(TAG,"latestUpdatedTime + 1 hour : " + latestUpdatedTime+ nextinterval);

                    sharedPrefs.edit().putLong("StartTime", startTime).apply();
                    sharedPrefs.edit().putLong("EndTime", endTime).apply();
                }

//                hour++;
//                if(hour>24)
//                    hour %= 24;

//                sharedPrefs.edit().putInt("StartHour", hour).apply();

                mDumpThread.postDelayed(this, mainThreadUpdateFrequencyInMilliseconds);

            }
        };

        mDumpThread.post(runnable_);
    }

    public void gettingDumpLastTime(){
        //TODO upload to MongoDB
        JSONObject data = new JSONObject();

        String curr =  getDateCurrentTimeZone(new Date().getTime());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new HttpAsyncPostJsonTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        postDumpUrl_search,
                        data.toString(),
                        "Dump",
                        curr).get();
            else
                new HttpAsyncPostJsonTask().execute(
                        postDumpUrl_search,
                        data.toString(),
                        "Dump",
                        curr).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void sendingDumpData(){

        Log.d(TAG, "sendingDumpData");

        JSONObject data = new JSONObject();

        try {
            data.put("device_id", Constants.DEVICE_ID);

            data.put("startTime", String.valueOf(startTime));
            data.put("endTime", String.valueOf(endTime));
            data.put("startTimeString", getTimeString(startTime));
            data.put("endTimeString", getTimeString(endTime));
        }catch (JSONException e){
            e.printStackTrace();
        }

        storeTransporatation(data);
        storeLocation(data);
        storeActivityRecognition(data);
        storeRinger(data);
        storeConnectivity(data);
        storeBattery(data);
        storeAppUsage(data);

        //storeTelephony(data);
        //storeSensor(data);
        //storeAccessibility(data);

        Log.d(TAG,"final data : "+ data.toString());

        //TODO check there have Data or not store in external
//        if(noDataFlag1 && noDataFlag2 && noDataFlag3 && noDataFlag4 && noDataFlag5 && noDataFlag6 && noDataFlag7) {

        int count = 0;

//       storeToLocalFolder(data, count);

        /*
            noDataFlag1 = false;
            noDataFlag2 = false;
            noDataFlag3 = false;
            noDataFlag4 = false;
            noDataFlag5 = false;
            noDataFlag6 = false;
            noDataFlag7 = false;
        }*/

        String curr =  getDateCurrentTimeZone(new Date().getTime());

        //TODO upload to MongoDB
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new HttpAsyncPostJsonTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        postDumpUrl_insert,
                    data.toString(),
                    "Dump",
                    curr).get();
            else
                new HttpAsyncPostJsonTask().execute(
                        postDumpUrl_insert,
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

                //dump only can be sent when wifi is connected
                if(ConnectivityStreamGenerator.mIsWifiConnected){

                    //TODO update endtime to get the latest data's time from MongoDB
                    //TODO endtime = latest data's time + nextinterval
                    gettingDumpLastTime();

                    //TODO delete the data in SQLite before the latest data's time


                    //default
                    long startstartTime = getSpecialTimeInMillis(makingDataFormat(year,month,day,hour,min));
                    startTime = sharedPrefs.getLong("StartTime", startstartTime); //default
//                    Log.d(TAG,"Start year : "+ year+" month : "+ month+" day : "+ day+" hour : "+ hour+" min : "+ min);
                    Log.d(TAG,"StartTimeString : " + getTimeString(startTime));
//                    Log.d(TAG,"StartTime : " + startTime);


                    //default
                    long startendTime = getSpecialTimeInMillis(makingDataFormat(year,month,day,hour+1,min));
                    endTime = sharedPrefs.getLong("EndTime", startendTime);
//                    Log.d(TAG,"End year : "+ year+" month : "+ month+" day : "+ day+" hour+1 : "+ (hour+1)+" min : "+ min);
                    Log.d(TAG,"EndTimeString : " + getTimeString(endTime));
//                    Log.d(TAG,"EndTime : " + endTime);


                    nowTime = new Date().getTime();
                    Log.d(TAG,"NowTimeString : " + getTimeString(nowTime));
                    Log.d(TAG,"NowTime : " + nowTime);

                    if(nowTime > endTime && ConnectivityStreamGenerator.mIsWifiConnected == true) {

                        sendingDumpData();

                        //setting nextime interval
                        latestUpdatedTime = endTime;
                        startTime = latestUpdatedTime;

//                    long nextinterval = getSpecialTimeInMillis(makingDataFormat(0,0,0,0,5));
                        long nextinterval = 1 * 60 * 60000; //1 hr

                        endTime = startTime + nextinterval;//getSpecialTimeInMillis(0,0,0,0,10);

                        Log.d(TAG,"latestUpdatedTime : " + latestUpdatedTime);
                        Log.d(TAG,"latestUpdatedTime + 1 hour : " + latestUpdatedTime + nextinterval);

                        sharedPrefs.edit().putLong("StartTime", startTime).apply();
                        sharedPrefs.edit().putLong("EndTime", endTime).apply();
                    }

                }

                // Trip, isAlive
                if(ConnectivityStreamGenerator.mIsWifiConnected && ConnectivityStreamGenerator.mIsMobileConnected) {

                    sendingTripData();

                    sendingIsAliveData();

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
        // onPostExecute displays the results of the AsyncTask.
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

        try {

            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  result;
    }

    /** process result **/
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

        Log.d(TAG, "storeTransporatation");

        try {

            JSONObject transportationAndtimestampsJson = new JSONObject();

            JSONArray transportations = new JSONArray();
            JSONArray timestamps = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor transCursor = db.rawQuery("SELECT * FROM "+DBHelper.transportationMode_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.
            Log.d(TAG,"SELECT * FROM "+DBHelper.transportationMode_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ");

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    String timestamp = transCursor.getString(1);
                    String transportation = transCursor.getString(2);

                    Log.d(TAG,"transportation : "+transportation+" timestamp : "+timestamp);

                    transportations.put(transportation);
                    timestamps.put(timestamp);

                    transCursor.moveToNext();
                }

                transportationAndtimestampsJson.put("Transportation",transportations);
                transportationAndtimestampsJson.put("timestamps",timestamps);

                data.put("TransportationMode",transportationAndtimestampsJson);

            }else
                noDataFlag1 = true;

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeLocation(JSONObject data){

        Log.d(TAG, "storeLocation");

        try {

            JSONObject locationAndtimestampsJson = new JSONObject();

            JSONArray accuracys = new JSONArray();
            JSONArray longtitudes = new JSONArray();
            JSONArray latitudes = new JSONArray();
            JSONArray timestamps = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor transCursor = db.rawQuery("SELECT * FROM "+DBHelper.location_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.
            Log.d(TAG,"SELECT * FROM "+DBHelper.location_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ");

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    String timestamp = transCursor.getString(1);
                    String latitude = transCursor.getString(2);
                    String longtitude = transCursor.getString(3);
                    String accuracy = transCursor.getString(4);

                    Log.d(TAG,"timestamp : "+timestamp+" latitude : "+latitude+" longtitude : "+longtitude+" accuracy : "+accuracy);

                    accuracys.put(accuracy);
                    longtitudes.put(longtitude);
                    latitudes.put(latitude);
                    timestamps.put(timestamp);

                    transCursor.moveToNext();
                }

                locationAndtimestampsJson.put("Accuracy",accuracys);
                locationAndtimestampsJson.put("Longtitudes",longtitudes);
                locationAndtimestampsJson.put("Latitudes",latitudes);
                locationAndtimestampsJson.put("timestamps",timestamps);

                data.put("Location",locationAndtimestampsJson);

            }else
                noDataFlag2 = true;

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeActivityRecognition(JSONObject data){

        Log.d(TAG, "storeActivityRecognition");

        try {

            JSONObject arAndtimestampsJson = new JSONObject();

            JSONArray mostProbableActivityz = new JSONArray();
            JSONArray probableActivitiesz = new JSONArray();
            JSONArray timestamps = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor transCursor = db.rawQuery("SELECT * FROM "+DBHelper.activityRecognition_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.
            Log.d(TAG,"SELECT * FROM "+DBHelper.activityRecognition_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ");

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    String timestamp = transCursor.getString(1);
                    String mostProbableActivity = transCursor.getString(2);
                    String probableActivities = transCursor.getString(3);

                    Log.d(TAG,"timestamp : "+timestamp+" mostProbableActivity : "+mostProbableActivity+" probableActivities : "+probableActivities);

                    mostProbableActivityz.put(mostProbableActivity);
                    probableActivitiesz.put(probableActivities);
                    timestamps.put(timestamp);

                    transCursor.moveToNext();
                }

                arAndtimestampsJson.put("MostProbableActivity",mostProbableActivityz);
                arAndtimestampsJson.put("ProbableActivities",probableActivitiesz);
                arAndtimestampsJson.put("timestamps",timestamps);

                data.put("ActivityRecognition",arAndtimestampsJson);

            }else
                noDataFlag3 = true;

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeRinger(JSONObject data){

        Log.d(TAG, "storeRinger");

        try {

            JSONObject ringerAndtimestampsJson = new JSONObject();

            JSONArray StreamVolumeSystems = new JSONArray();
            JSONArray StreamVolumeVoicecalls = new JSONArray();
            JSONArray StreamVolumeRings = new JSONArray();
            JSONArray StreamVolumeNotifications = new JSONArray();
            JSONArray StreamVolumeMusics = new JSONArray();
            JSONArray AudioModes = new JSONArray();
            JSONArray RingerModes = new JSONArray();
            JSONArray timestamps = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor transCursor = db.rawQuery("SELECT * FROM "+DBHelper.ringer_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.
            Log.d(TAG,"SELECT * FROM "+DBHelper.ringer_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ");

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    String timestamp = transCursor.getString(1);
                    String RingerMode = transCursor.getString(2);
                    String AudioMode = transCursor.getString(3);
                    String StreamVolumeMusic = transCursor.getString(4);
                    String StreamVolumeNotification = transCursor.getString(5);
                    String StreamVolumeRing = transCursor.getString(6);
                    String StreamVolumeVoicecall = transCursor.getString(7);
                    String StreamVolumeSystem = transCursor.getString(8);

                    Log.d(TAG,"timestamp : "+timestamp+" RingerMode : "+RingerMode+" AudioMode : "+AudioMode+
                            " StreamVolumeMusic : "+StreamVolumeMusic+" StreamVolumeNotification : "+StreamVolumeNotification
                            +" StreamVolumeRing : "+StreamVolumeRing +" StreamVolumeVoicecall : "+StreamVolumeVoicecall
                            +" StreamVolumeSystem : "+StreamVolumeSystem);

                    StreamVolumeSystems.put(StreamVolumeSystem);
                    StreamVolumeVoicecalls.put(StreamVolumeVoicecall);
                    StreamVolumeRings.put(StreamVolumeRing);
                    StreamVolumeNotifications.put(StreamVolumeNotification);
                    StreamVolumeMusics.put(StreamVolumeMusic);
                    AudioModes.put(AudioMode);
                    RingerModes.put(RingerMode);
                    timestamps.put(timestamp);

                    transCursor.moveToNext();
                }

                ringerAndtimestampsJson.put("RingerMode",RingerModes);
                ringerAndtimestampsJson.put("AudioMode",AudioModes);
                ringerAndtimestampsJson.put("StreamVolumeMusic",StreamVolumeMusics);
                ringerAndtimestampsJson.put("StreamVolumeNotification",StreamVolumeNotifications);
                ringerAndtimestampsJson.put("StreamVolumeRing",StreamVolumeRings);
                ringerAndtimestampsJson.put("StreamVolumeVoicecall",StreamVolumeVoicecalls);
                ringerAndtimestampsJson.put("StreamVolumeSystem",StreamVolumeSystems);
                ringerAndtimestampsJson.put("timestamps",timestamps);

                data.put("Ringer",ringerAndtimestampsJson);

            }else
                noDataFlag4 = true;

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeConnectivity(JSONObject data){

        Log.d(TAG, "storeConnectivity");

        try {

            JSONObject connectivityAndtimestampsJson = new JSONObject();

            JSONArray IsMobileConnecteds = new JSONArray();
            JSONArray IsWifiConnecteds = new JSONArray();
            JSONArray IsMobileAvailables = new JSONArray();
            JSONArray IsWifiAvailables = new JSONArray();
            JSONArray IsConnecteds = new JSONArray();
            JSONArray IsNetworkAvailables = new JSONArray();
            JSONArray NetworkTypes = new JSONArray();
            JSONArray timestamps = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor transCursor = db.rawQuery("SELECT * FROM "+DBHelper.connectivity_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.
            Log.d(TAG,"SELECT * FROM "+DBHelper.connectivity_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ");

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    String timestamp = transCursor.getString(1);
                    String NetworkType = transCursor.getString(2);
                    String IsNetworkAvailable = transCursor.getString(3);
                    String IsConnected = transCursor.getString(4);
                    String IsWifiAvailable = transCursor.getString(5);
                    String IsMobileAvailable = transCursor.getString(6);
                    String IsWifiConnected = transCursor.getString(7);
                    String IsMobileConnected = transCursor.getString(8);

                    Log.d(TAG,"timestamp : "+timestamp+" NetworkType : "+NetworkType+" IsNetworkAvailable : "+IsNetworkAvailable
                            +" IsConnected : "+IsConnected+" IsWifiAvailable : "+IsWifiAvailable
                            +" IsMobileAvailable : "+IsMobileAvailable +" IsWifiConnected : "+IsWifiConnected
                            +" IsMobileConnected : "+IsMobileConnected);

                    IsMobileConnecteds.put(IsMobileConnected);
                    IsWifiConnecteds.put(IsWifiConnected);
                    IsMobileAvailables.put(IsMobileAvailable);
                    IsWifiAvailables.put(IsWifiAvailable);
                    IsConnecteds.put(IsConnected);
                    IsNetworkAvailables.put(IsNetworkAvailable);
                    NetworkTypes.put(NetworkType);
                    timestamps.put(timestamp);

                    transCursor.moveToNext();
                }

                connectivityAndtimestampsJson.put("NetworkType",NetworkTypes);
                connectivityAndtimestampsJson.put("IsNetworkAvailable",IsNetworkAvailables);
                connectivityAndtimestampsJson.put("IsConnected",IsConnecteds);
                connectivityAndtimestampsJson.put("IsWifiAvailable",IsWifiAvailables);
                connectivityAndtimestampsJson.put("IsMobileAvailable",IsMobileAvailables);
                connectivityAndtimestampsJson.put("IsWifiConnected",IsWifiConnecteds);
                connectivityAndtimestampsJson.put("IsMobileConnected",IsMobileConnecteds);
                connectivityAndtimestampsJson.put("timestamps",timestamps);

                data.put("Connectivity",connectivityAndtimestampsJson);

            }else
                noDataFlag5 = true;

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeBattery(JSONObject data){

        Log.d(TAG, "storeBattery");

        try {

            JSONObject batteryAndtimestampsJson = new JSONObject();

            JSONArray BatteryLevels = new JSONArray();
            JSONArray BatteryPercentages = new JSONArray();
            JSONArray BatteryChargingStates = new JSONArray();
            JSONArray isChargings = new JSONArray();
            JSONArray timestamps = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor transCursor = db.rawQuery("SELECT * FROM "+DBHelper.battery_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.
            Log.d(TAG,"SELECT * FROM "+DBHelper.battery_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ");

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    String timestamp = transCursor.getString(1);
                    String BatteryLevel = transCursor.getString(2);
                    String BatteryPercentage = transCursor.getString(3);
                    String BatteryChargingState = transCursor.getString(4);
                    String isCharging = transCursor.getString(5);

                    Log.d(TAG,"timestamp : "+timestamp+" BatteryLevel : "+BatteryLevel+" BatteryPercentage : "+
                            BatteryPercentage+" BatteryChargingState : "+BatteryChargingState+" isCharging : "+isCharging);

                    BatteryLevels.put(BatteryLevel);
                    BatteryPercentages.put(BatteryPercentage);
                    BatteryChargingStates.put(BatteryChargingState);
                    isChargings.put(isCharging);
                    timestamps.put(timestamp);

                    transCursor.moveToNext();
                }

                batteryAndtimestampsJson.put("BatteryLevel",BatteryLevels);
                batteryAndtimestampsJson.put("BatteryPercentage",BatteryPercentages);
                batteryAndtimestampsJson.put("BatteryChargingState",BatteryChargingStates);
                batteryAndtimestampsJson.put("isCharging",isChargings);
                batteryAndtimestampsJson.put("timestamps",timestamps);

                data.put("Battery",batteryAndtimestampsJson);

            }else
                noDataFlag6 = true;

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeAppUsage(JSONObject data){

        Log.d(TAG, "storeAppUsage");

        try {

            JSONObject appUsageAndtimestampsJson = new JSONObject();

            JSONArray ScreenStatusz = new JSONArray();
            JSONArray Latest_Used_Apps = new JSONArray();
            JSONArray Latest_Foreground_Activitys = new JSONArray();
            JSONArray timestamps = new JSONArray();

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor transCursor = db.rawQuery("SELECT * FROM "+DBHelper.appUsage_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ", null); //cause pos start from 0.
            Log.d(TAG,"SELECT * FROM "+DBHelper.appUsage_table+" WHERE "+DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ");

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    String timestamp = transCursor.getString(1);
                    String ScreenStatus = transCursor.getString(2);
                    String Latest_Used_App = transCursor.getString(3);
                    String Latest_Foreground_Activity = transCursor.getString(4);

                    Log.d(TAG,"timestamp : "+timestamp+" ScreenStatus : "+ScreenStatus+" Latest_Used_App : "+Latest_Used_App+" Latest_Foreground_Activity : "+Latest_Foreground_Activity);

                    ScreenStatusz.put(ScreenStatus);
                    Latest_Used_Apps.put(Latest_Used_App);
                    Latest_Foreground_Activitys.put(Latest_Foreground_Activity);
                    timestamps.put(timestamp);

                    transCursor.moveToNext();
                }

                appUsageAndtimestampsJson.put("ScreenStatus",ScreenStatusz);
                appUsageAndtimestampsJson.put("Latest_Used_App",Latest_Used_Apps);
//                appUsageAndtimestampsJson.put("Latest_Foreground_Activity",Latest_Foreground_Activitys);
                appUsageAndtimestampsJson.put("timestamps",timestamps);

                data.put("AppUsage",appUsageAndtimestampsJson);

            }else
                noDataFlag7 = true;

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        Log.d(TAG,"data : "+ data.toString());

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
