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
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;
import labelingStudy.nctu.minuku.stream.ConnectivityStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord.isIsMobileAvailable;
import static labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord.isIsMobileConnected;
import static labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord.isIsWifiAvailable;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class ConnectivityStreamGenerator extends AndroidStreamGenerator<ConnectivityDataRecord> {

    private final String TAG = "ConnectivityStreamGenerator";

    private Context mContext;

    public static String NETWORK_TYPE_WIFI = "Wifi";
    public static String NETWORK_TYPE_MOBILE = "Mobile";
    private static boolean mIsNetworkAvailable = false;
    private static boolean mIsConnected = false;
    private static boolean mIsWifiAvailable = false;
    private static boolean mIsMobileAvailable = false;
    public static boolean mIsWifiConnected = false;
    public static boolean mIsMobileConnected = false;

    public static String mNetworkType = "NA";

    public static int mainThreadUpdateFrequencyInSeconds = 5;
    public static long mainThreadUpdateFrequencyInMilliseconds = mainThreadUpdateFrequencyInSeconds *Constants.MILLISECONDS_PER_SECOND;

    private static Handler mMainThread;

    private static ConnectivityManager mConnectivityManager;

    private ConnectivityStream mStream;

    public ConnectivityStreamGenerator(){

        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);

    }

    public ConnectivityStreamGenerator(Context applicationContext){
        super(applicationContext);

        mContext = applicationContext;

        this.mStream = new ConnectivityStream(Constants.DEFAULT_QUEUE_SIZE);

        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);


        this.register();
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
                new ConnectivityDataRecord(mNetworkType,mIsNetworkAvailable, mIsConnected, mIsWifiAvailable,
                        mIsMobileAvailable, mIsWifiConnected, mIsMobileConnected);
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
                Log.d(TAG+" isIsWifiConnected: ", String.valueOf(c.isIsWifiConnected()));
                Log.d(TAG+" NetworkType: ",c.getNetworkType());
                Log.d(TAG+" isNetworkAvailable: ", String.valueOf(c.isNetworkAvailable()));
                Log.d(TAG+" isIsConnected: ", String.valueOf(c.isIsConnected()));
                Log.d(TAG+" isIsWifiAvailable: ",String.valueOf(isIsWifiAvailable()));
                Log.d(TAG+" isIsMobileAvailable: ",String.valueOf(isIsMobileAvailable()));
                Log.d(TAG+" isIsMobileConnected: ", String.valueOf(isIsMobileConnected()));

            }
        }catch (NullPointerException e){ //Sometimes no data is normal
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

    public void runPhoneStatusMainThread(){

        Log.d(TAG, "runPhoneStatusMainThread") ;

        mMainThread = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                getNetworkConnectivityUpdate();

                mMainThread.postDelayed(this, mainThreadUpdateFrequencyInMilliseconds);
            }
        };

        mMainThread.post(runnable);
    }

    private void getNetworkConnectivityUpdate(){

        mIsNetworkAvailable = false;
        mIsConnected = false;
        mIsWifiAvailable = false;
        mIsMobileAvailable = false;
        mIsWifiConnected = false;
        mIsMobileConnected = false;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Network[] networks = mConnectivityManager.getAllNetworks();

            NetworkInfo activeNetwork;

            for (Network network : networks) {
                activeNetwork = mConnectivityManager.getNetworkInfo(network);

                if (activeNetwork.getType()== ConnectivityManager.TYPE_WIFI){
                    mIsWifiAvailable = activeNetwork.isAvailable();
                    mIsWifiConnected = activeNetwork.isConnected();
                }

                else if (activeNetwork.getType()==ConnectivityManager.TYPE_MOBILE){
                    mIsMobileAvailable = activeNetwork.isAvailable();
                    mIsMobileConnected = activeNetwork.isConnected();
                }

            }

            if (mIsWifiConnected) {
                mNetworkType = NETWORK_TYPE_WIFI;
            }
            else if (mIsMobileConnected) {
                mNetworkType = NETWORK_TYPE_MOBILE;
            }

            mIsNetworkAvailable = mIsWifiAvailable | mIsMobileAvailable;
            mIsConnected = mIsWifiConnected | mIsMobileConnected;


            Log.d(TAG, "[test save records] connectivity change available? WIFI: available " + mIsWifiAvailable  +
                    "  mIsConnected: " + mIsWifiConnected + " Mobile: available: " + mIsMobileAvailable + " mIs connected: " + mIsMobileConnected
                    +" network type: " + mNetworkType + ",  mIs connected: " + mIsConnected + " mIs network available " + mIsNetworkAvailable);


        } else{

            Log.d(TAG, "[test save records] api under lollipop " );


            if (mConnectivityManager!=null) {

                NetworkInfo activeNetworkWifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo activeNetworkMobile = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                boolean isWiFi = activeNetworkWifi.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isMobile = activeNetworkMobile.getType() == ConnectivityManager.TYPE_MOBILE;

                Log.d(TAG, "[test save records] connectivity change available? " + isWiFi);


                if(activeNetworkWifi !=null) {

                    mIsWifiConnected = activeNetworkWifi != null &&
                            activeNetworkWifi.isConnected();
                    mIsMobileConnected = activeNetworkWifi != null &&
                            activeNetworkMobile.isConnected();

                    mIsConnected = mIsWifiConnected | mIsMobileConnected;

                    mIsWifiAvailable = activeNetworkWifi.isAvailable();
                    mIsMobileAvailable = activeNetworkMobile.isAvailable();

                    mIsNetworkAvailable = mIsWifiAvailable | mIsMobileAvailable;


                    if (mIsWifiConnected) {
                        mNetworkType = NETWORK_TYPE_WIFI;
                    }

                    else if (mIsMobileConnected) {
                        mNetworkType = NETWORK_TYPE_MOBILE;
                    }


                    //assign value
//
                    Log.d(TAG, "[test save records] connectivity change available? WIFI: available " + mIsWifiAvailable  +
                            "  mIsConnected: " + mIsWifiConnected + " Mobile: available: " + mIsMobileAvailable + " mIs connected: " + mIsMobileConnected
                            +" network type: " + mNetworkType + ",  mIs connected: " + mIsConnected + " mIs network available " + mIsNetworkAvailable);

                }
            }

        }
    }

    @Override
    public void offer(ConnectivityDataRecord dataRecord) {

    }
}
