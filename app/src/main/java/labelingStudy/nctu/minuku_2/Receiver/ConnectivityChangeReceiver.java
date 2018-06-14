package labelingStudy.nctu.minuku_2.Receiver;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class ConnectivityChangeReceiver extends BroadcastReceiver{

    private final String TAG = "ConnectivityChangeReceiver";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "[ConnectivityChangeReceiver]syncWithRemoteDatabase connectivity change");

        ConnectivityManager conMngr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Network[] networks = conMngr.getAllNetworks();

            NetworkInfo activeNetwork;

            boolean isWifi = false;

            for (Network network : networks) {
                activeNetwork = conMngr.getNetworkInfo(network);

                if (activeNetwork.getType()==ConnectivityManager.TYPE_WIFI){
                    isWifi = activeNetwork.isConnected();

                    if (isWifi){

                        Log.d(TAG, "[ConnectivityChangeReceiver]syncWithRemoteDatabase connect to wifi");

                        //if we only submit the data over wifh. this should be configurable
//                        if (RemoteDBHelper.getSubmitDataOnlyOverWifi()){
//                            Log.d(TAG, "[ConnectivityChangeReceiver]syncWithRemoteDatabase only submit over wifi");
//                            RemoteDBHelper.syncWithRemoteDatabase();
//
//                        }
                    }
                }
            }


        }

        else{

            if (conMngr!=null) {

                NetworkInfo[] info = conMngr.getAllNetworkInfo();
                NetworkInfo activeNetworkWifi = conMngr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo activeNetworkMobile = conMngr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                boolean isWiFi = activeNetworkWifi.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isMobile = activeNetworkWifi.getType() == ConnectivityManager.TYPE_MOBILE;


                if (info != null) {

                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
//                            Log.d(TAG, "[ConnectivityChangeReceiver"+
//                                    " NETWORKNAME: " + anInfo.getTypeName());

                        }
                    }
                }

                if(activeNetworkWifi !=null) {

                    boolean isConnectedtoWifi = activeNetworkWifi != null &&
                            activeNetworkWifi.isConnected();
                    boolean isConnectedtoMobile = activeNetworkWifi != null &&
                            activeNetworkMobile.isConnected();


                    boolean isWifiAvailable = activeNetworkWifi.isAvailable();
                    boolean isMobileAvailable = activeNetworkMobile.isAvailable();

                    if (isWiFi) {

                        Log.d(TAG, "[ConnectivityChangeReceiver]syncWithRemoteDatabase connect to wifi");

                        //if we only submit the data over wifh. this should be configurable
//                        if (RemoteDBHelper.getSubmitDataOnlyOverWifi()){
//                            Log.d(TAG, "[ConnectivityChangeReceiver]syncWithRemoteDatabase only submit over wifi");
//                            RemoteDBHelper.syncWithRemoteDatabase();
//
//                        }


                    }

                    else if (isMobile) {

//                        Log.d(TAG, "[ConnectivityChangeReceiver] connect to mobile");
                    }


//                    Log.d(TAG, "[ConnectivityChangeReceiver] connectivity change available? WIFI: available " + isWifiAvailable  +
//                            "  isConnected: " + isConnectedtoWifi + " Mobile: available: " + isMobileAvailable + " is connected: " + isConnectedtoMobile);

                }
            }

        }
    }
}
