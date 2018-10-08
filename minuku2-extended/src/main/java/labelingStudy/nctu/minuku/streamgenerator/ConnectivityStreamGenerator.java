package labelingStudy.nctu.minuku.streamgenerator;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;
import labelingStudy.nctu.minuku.stream.ConnectivityStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord.isIsMobileAvailable;
import static labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord.isIsMobileConnected;
import static labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord.isIsWifiAvailable;

/**
 * Created by Lawrence on 2017/8/22.
 */

/**
 * ConnectivityStreamGenerator collects data about network connecting condition and status
 */
public class ConnectivityStreamGenerator extends AndroidStreamGenerator<ConnectivityDataRecord> {

    private final String TAG = "ConnectivityStreamGenerator";

    private Context mContext;

    public static final String NETWORK_TYPE_WIFI = "Wifi";
    public static final String NETWORK_TYPE_MOBILE = "Mobile";
    private static boolean sIsNetworkAvailable = false;
    private static boolean sIsConnected = false;
    private static boolean sIsWifiAvailable = false;
    private static boolean sIsMobileAvailable = false;
    public static boolean sIsWifiConnected = false;
    public static boolean sIsMobileConnected = false;

    public static String sNetworkType = "NA";

    public static int sMainThreadUpdateFrequencyInSeconds = 5;
    public static long sMainThreadUpdateFrequencyInMilliseconds = sMainThreadUpdateFrequencyInSeconds * Constants.MILLISECONDS_PER_SECOND;

    private static Handler sMainThread;

    private static ConnectivityManager sConnectivityManager;

    private ConnectivityStream mStream;

    public ConnectivityStreamGenerator() {

        sConnectivityManager = (ConnectivityManager)mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);

    }

    public ConnectivityStreamGenerator(Context applicationContext) {
        super(applicationContext);

        mContext = applicationContext;

        mStream = new ConnectivityStream(Constants.DEFAULT_QUEUE_SIZE);

        sConnectivityManager = (ConnectivityManager)mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);


        register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, ConnectivityDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which ConnectivityDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides ConnectivityDataRecord is already registered.");
        }
    }

    @Override
    public Stream<ConnectivityDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {

        Log.d(TAG, "updateStream called");
        //TODO get service data
        ConnectivityDataRecord connectivityDataRecord =
                new ConnectivityDataRecord(sNetworkType, sIsNetworkAvailable, sIsConnected, sIsWifiAvailable,
                        sIsMobileAvailable, sIsWifiConnected, sIsMobileConnected);
        mStream.add(connectivityDataRecord);
        Log.d(TAG, "CheckFamiliarOrNot to be sent to event bus" + connectivityDataRecord);
        // also post an event.
        EventBus.getDefault().post(connectivityDataRecord);
        try {
            appDatabase db;
            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();
            db.connectivityDataRecordDao().insertAll(connectivityDataRecord);
            List<ConnectivityDataRecord> connectivityDataRecords = db.connectivityDataRecordDao().getAll();
            for (ConnectivityDataRecord c : connectivityDataRecords) {
                Log.e(TAG, " isIsWifiConnected: " + String.valueOf(c.isIsWifiConnected()));
                Log.e(TAG," NetworkType: " + c.getNetworkType());
                Log.e(TAG, " isNetworkAvailable: " + String.valueOf(c.isNetworkAvailable()));
                Log.e(TAG, " isIsConnected: " + String.valueOf(c.isIsConnected()));
                Log.e(TAG, " isIsWifiAvailable: " +String.valueOf(isIsWifiAvailable()));
                Log.e(TAG, " isIsMobileAvailable: " +String.valueOf(isIsMobileAvailable()));
                Log.e(TAG, " isIsMobileConnected: " + String.valueOf(isIsMobileConnected()));

            }
        } catch (NullPointerException e) { //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {
        Log.e(TAG,"onStreamRegistration");

        runPhoneStatusMainThread();

    }

    public void runPhoneStatusMainThread() {

        Log.d(TAG, "runPhoneStatusMainThread") ;

        sMainThread = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                getNetworkConnectivityUpdate();

                sMainThread.postDelayed(this, sMainThreadUpdateFrequencyInMilliseconds);
            }
        };

        sMainThread.post(runnable);
    }

    private void getNetworkConnectivityUpdate() {

        sIsNetworkAvailable = false;
        sIsConnected = false;
        sIsWifiAvailable = false;
        sIsMobileAvailable = false;
        sIsWifiConnected = false;
        sIsMobileConnected = false;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Network[] networks = sConnectivityManager.getAllNetworks();

            NetworkInfo activeNetwork;

            for (Network network : networks) {
                activeNetwork = sConnectivityManager.getNetworkInfo(network);

                if (activeNetwork.getType()== ConnectivityManager.TYPE_WIFI) {
                    sIsWifiAvailable = activeNetwork.isAvailable();
                    sIsWifiConnected = activeNetwork.isConnected();
                } else if (activeNetwork.getType()==ConnectivityManager.TYPE_MOBILE) {
                    sIsMobileAvailable = activeNetwork.isAvailable();
                    sIsMobileConnected = activeNetwork.isConnected();
                }

            }

            if (sIsWifiConnected) {
                sNetworkType = NETWORK_TYPE_WIFI;
            } else if (sIsMobileConnected) {
                sNetworkType = NETWORK_TYPE_MOBILE;
            }

            sIsNetworkAvailable = sIsWifiAvailable | sIsMobileAvailable;
            sIsConnected = sIsWifiConnected | sIsMobileConnected;


            Log.d(TAG, "[test save records] connectivity change available? WIFI: available " + sIsWifiAvailable +
                    "  mIsConnected: " + sIsWifiConnected + " Mobile: available: " + sIsMobileAvailable + " mIs connected: " + sIsMobileConnected
                    + " network type: " + sNetworkType + ",  mIs connected: " + sIsConnected + " mIs network available " + sIsNetworkAvailable);


        } else {

            Log.d(TAG, "[test save records] api under lollipop " );


            if (sConnectivityManager != null) {

                NetworkInfo activeNetworkWifi = sConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo activeNetworkMobile = sConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                boolean isWiFi = activeNetworkWifi.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isMobile = activeNetworkMobile.getType() == ConnectivityManager.TYPE_MOBILE;

                Log.d(TAG, "[test save records] connectivity change available? " + isWiFi);


                if(activeNetworkWifi !=null) {

                    sIsWifiConnected = activeNetworkWifi != null &&
                            activeNetworkWifi.isConnected();
                    sIsMobileConnected = activeNetworkWifi != null &&
                            activeNetworkMobile.isConnected();

                    sIsConnected = sIsWifiConnected | sIsMobileConnected;

                    sIsWifiAvailable = activeNetworkWifi.isAvailable();
                    sIsMobileAvailable = activeNetworkMobile.isAvailable();

                    sIsNetworkAvailable = sIsWifiAvailable | sIsMobileAvailable;


                    if (sIsWifiConnected) {
                        sNetworkType = NETWORK_TYPE_WIFI;
                    }

                    else if (sIsMobileConnected) {
                        sNetworkType = NETWORK_TYPE_MOBILE;
                    }


                    //assign value
//
                    Log.d(TAG, "[test save records] connectivity change available? WIFI: available " + sIsWifiAvailable +
                            "  mIsConnected: " + sIsWifiConnected + " Mobile: available: " + sIsMobileAvailable + " mIs connected: " + sIsMobileConnected
                            + " network type: " + sNetworkType + ",  mIs connected: " + sIsConnected + " mIs network available " + sIsNetworkAvailable);

                }
            }

        }
    }

    @Override
    public void offer(ConnectivityDataRecord dataRecord) {

    }
}
