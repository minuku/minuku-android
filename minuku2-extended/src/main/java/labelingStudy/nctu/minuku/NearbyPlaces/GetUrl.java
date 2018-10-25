package labelingStudy.nctu.minuku.NearbyPlaces;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import labelingStudy.nctu.minuku.Utilities.Utils;
import labelingStudy.nctu.minuku.config.Constants;

/**
 * Created by Lawrence on 2017/10/18.
 */

public class GetUrl {

    private final static String TAG = "GetUrl";

    static final int PROXIMITY_RADIUS = Constants.siteRange;

    public static String getUrl(double latitude, double longitude) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        //googlePlaceUrl.append("&type=");
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyDNWjTqYe9J1Nvse0IbVLciBycQGouZtUQ");

        Log.d(TAG, "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    public static String getSiteInformFromNet(double lat, double lng) {

        String jsonInString, siteName = Constants.UNKNOWN_SITE;

        String siteLat = Constants.INVALID_STRING_VALUE;
        String siteLng = Constants.INVALID_STRING_VALUE;

        try {
            String url = getUrl(lat, lng);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                jsonInString = new HttpAsyncGetSiteTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        url
                ).get();
            else
                jsonInString = new HttpAsyncGetSiteTask().execute(
                        url
                ).get();

            JSONObject jsonObject = new JSONObject(jsonInString);
            JSONArray results = jsonObject.getJSONArray("results");

            //default now we choose the second index from the json.(first index is ken(縣名) name.)
            siteName = results.getJSONObject(1).getString("name");
            String geometry = results.getJSONObject(1).getString("geometry");

            JSONObject geometryJson = new JSONObject(geometry);
            String location = geometryJson.getString("location");
            JSONObject locationJson = new JSONObject(location);
            siteLat = locationJson.getString("lat");
            siteLng = locationJson.getString("lng");

        }catch (InterruptedException e){

        }catch (ExecutionException e){

        }catch (JSONException e){

        }

        return siteName+Constants.DELIMITER+"("+siteLat+","+siteLng+")";
    }

    private static class HttpAsyncGetSiteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String url = params[0];

            String result = Utils.getJSON(url);

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

        }

    }

}
