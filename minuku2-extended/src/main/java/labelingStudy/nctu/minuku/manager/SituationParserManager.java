package labelingStudy.nctu.minuku.manager;


import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class SituationParserManager {

    private static Context mContext;

    private static SituationParserManager instance;
    private static String TAG = "SituationParserManager";
    //~/Downloads/minuku.json

    public static SituationParserManager getInstance() {
        if(instance == null) {
            instance = new SituationParserManager();
        }
//        instance.ReadJsonFile(mContext);
        return instance;
    }

    public String ReadJsonFile(Context context){
        Log.e(TAG, "get Json File");
        String json = null;
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

        Log.e(TAG, "Json content: " + json);
        return json;
    }

    public void AccessibilityStreamSituation(){
//        Pack: com.android.providers.downloads
//        Extra:
//        Text: android.widget.FrameLayout:[鎖定螢幕。]
//        Type: TYPE_VIEW_SCROLLED
    }

    public void AppUsageStreamSituation(){
//        Latest_Used_App: com.facebook.katana
//        Screen_Status: Interactive
//        Latest_Foreground_Activity: NA
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
