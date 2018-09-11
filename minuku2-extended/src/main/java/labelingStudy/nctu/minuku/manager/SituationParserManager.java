package labelingStudy.nctu.minuku.manager;


import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class SituationParserManager {

    private static Context mContext;

    private static SituationParserManager instance;
    private static String TAG = "SituationParserManager";

    String json = null;


    public static SituationParserManager getInstance() {
        if(instance == null) {
            instance = new SituationParserManager();
        }
        return instance;
    }

    public String ReadJsonFile(Context context) throws JSONException {
        Log.e(TAG, "get Json File");
//        String json = null;
        try {
            InputStream is = context.getAssets().open("minuku.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

//        Log.e(TAG, "Json content: " + json);

        AppUsageStreamSituation();

        return json;
    }

    public void AccessibilityStreamSituation() throws JSONException {
//        Pack: com.android.providers.downloads
//        Extra:
//        Text: android.widget.FrameLayout:[鎖定螢幕。]
//        Type: TYPE_VIEW_SCROLLED
        JSONObject jObject = new JSONObject(json);

    }

    public void AppUsageStreamSituation() throws JSONException {
//        Latest_Used_App: com.facebook.katana
//        Screen_Status: Interactive
//        Latest_Foreground_Activity: NA

        JSONObject jObject = new JSONObject(json);
        JSONArray projects = jObject.getJSONArray("projects");

        //projects array count
        if (projects != null){

            for(int i = 0; i < projects.length(); i++ ){
                Log.e(TAG, "project [" + i + "]");

                jObject = new JSONObject(projects.get(i).toString());
                JSONArray situations = jObject.getJSONArray("situations");

                //situations array count
                if (situations != null){

                    for(int j = 0; j < situations.length(); j++ ){
                        Log.e(TAG, "situation [" + j + "]");

                        jObject = new JSONObject(situations.get(i).toString());
                        JSONArray dataCollections = jObject.getJSONArray("dataCollections");

                        //dataCollections array count
                        if (dataCollections != null){

                            for(int k = 0; k < dataCollections.length(); k++ ){
                                Log.e(TAG, "dataCollections [" + k + "]");

                                jObject = new JSONObject(dataCollections.get(i).toString());
                                JSONArray devices = jObject.getJSONArray("devices");

                                //devices array count
                                if (devices != null){

                                    for(int l = 0; l < devices.length(); l++ ){
                                        Log.e(TAG, "devices [" + l + "]");

                                        jObject = new JSONObject(devices.get(i).toString());
                                        JSONArray deviceContent = jObject.getJSONArray("deviceContent");

//                                        ////deviceContent array count
//                                        if (deviceContent != null){
//
//                                            for(int m = 0; m < devices.length(); m++ ){
//                                                Log.e(TAG, "devices [" + l + "]");
//
//                                                jObject = new JSONObject(devices.get(i).toString());
//                                                JSONArray deviceContent = jObject.getJSONArray("deviceContent");

                                        String Screen_Status_situ = "";
                                        String Latest_Foreground_Activity_situ = "";
                                        String Latest_Used_App_situ = "";

                                        jObject = new JSONObject(devices.get(i).toString());
                                        JSONObject Appusage_record;
                                        Log.e(TAG, jObject.getJSONObject("deviceContent").toString());
                                        if (jObject.getJSONObject("deviceContent") != null){
                                            Appusage_record = jObject.getJSONObject("deviceContent").getJSONObject("AppUsageDataRecord");


                                            if (Appusage_record.getJSONObject("Screen_Status").getString("active") != null)
                                                Screen_Status_situ = Appusage_record.getJSONObject("Screen_Status").getString("active");
                                            if (Appusage_record.getJSONObject("Latest_Foreground_Activity").getString("active") != null)
                                                Latest_Foreground_Activity_situ = Appusage_record.getJSONObject("Latest_Foreground_Activity").getString("active");
                                            if (Appusage_record.getJSONObject("Latest_Used_Appe").getString("active") != null)
                                                Latest_Used_App_situ = Appusage_record.getJSONObject("Latest_Used_App").getString("active");
                                        }


                                        Log.e(TAG, "Screen_Status_situ: " + Screen_Status_situ);
                                        Log.e(TAG, "Latest_Foreground_Activity_situ: " + Latest_Foreground_Activity_situ);
                                        Log.e(TAG, "Latest_Used_App_situ: " + Latest_Used_App_situ);
                                    }

                                }
                            }

                        }
                    }

                }
            }
        }




    }

    public void ActivityRecognitionStreamSituation(){
//        Detectedtime: 1534400043723
//        ProbableActivities: [DetectedActivity [type=STILL, confidence=100]]
//        MostProbableActivity: DetectedActivity [type=STILL, confidence=100]

    }

    public void BatteryStreamSituation(){
//        BatteryChargingState: ac charging
//        BatteryPercentage: 1.0
//        BatteryLevel: 100
//        isCharging: true
    }

    public void ConnectivityStreamSituation(){
//        isIsWifiConnected: true
//        NetworkType: Wifi
//        isNetworkAvailable: true
//        isIsConnected: true
//        isIsWifiAvailable: true
//        isIsMobileAvailable: false
//        isIsMobileConnected: false
    }

    public void LocationStreamSituation(){
//        Latitude: -999.0
//        Longitude: -999.0
//        Accuracy: 0.0
//        Altitude: 0.0
//        Speed: 0.0
//        Bearing: 0.0
//        Provider: null
    }

    public void RingerStreamSituation(){
//        RingerMode: Vibrate
//        AudioMode: Normal
//        StreamVolumeMusic: 8
//        StreamVolumeNotification: 0
//        StreamVolumeRing: 0
//        StreamVolumeVoicecall: 7
//        StreamVolumeSystem: 0
    }

    public void SensorStreamSituation(){
//        Accele: -0.136, -0.583, 9.925
//        AmbientTemperature: null
//        Gravity: null
//        Gyroscope: null
//        Light: Sensor-Light: 696.0, 0.0, 0.0
//        LinearAcceleration: null
//        MagneticField: Sensor-MagneticField: -57.78, 0.36, 17.76
//        Pressure: null
//        Proximity: Sensor-Proximity: 10.0, 0.0, 0.0
//        RelativeHumidity: null
//        RotationVector: null
    }

    public void TelephonyStreamSituation(){
//        NetworkOperatorName: Android
//        CallState: 0
//        CdmaSignalStrengthLevel: -9999
//        GsmSignalStrength: -9999
//        LTESignalStrength: -10
//        PhoneSignalType: 13
    }

    public void TransportationModeStreamSituation(){
//        ConfirmedActivity: static
//        SuspectedStartActivityString: static
//        SuspectedStopActivityString: static
//        SuspectedTime: 0
//        taskDayCount: 0
//        hour: 0
    }

}
