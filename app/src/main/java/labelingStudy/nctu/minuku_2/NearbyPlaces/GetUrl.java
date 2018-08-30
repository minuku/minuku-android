package labelingStudy.nctu.minuku_2.NearbyPlaces;

import android.util.Log;

import labelingStudy.nctu.minuku.config.Constants;

/**
 * Created by Lawrence on 2017/10/18.
 */

public class GetUrl {

    private final static String TAG = "GetUrl";

    static final int PROXIMITY_RADIUS = Constants.siteRange;

    public static String getUrl(double latitude , double longitude) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        //googlePlaceUrl.append("&type=");
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyDNWjTqYe9J1Nvse0IbVLciBycQGouZtUQ");

        Log.d(TAG, "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }
}
