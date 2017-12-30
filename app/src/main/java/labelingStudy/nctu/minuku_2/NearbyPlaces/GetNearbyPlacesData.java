package labelingStudy.nctu.minuku_2.NearbyPlaces;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lawrence on 2017/10/18.
 */

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private final String TAG = "GetNearbyPlacesData";

    private String googlePlacesData;
    private GoogleMap mMap;
    String url;

    @Override
    protected String doInBackground(Object... objects){
//        mMap = (GoogleMap)objects[0];
//        url = (String)objects[1]; TODO we will show at static map.

        url = (String)objects[0];

        DownloadURL downloadURL = new DownloadURL();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "googlePlacesData : "+googlePlacesData);

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s){
        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        //nearbyPlaceList = parser.parse(s);
        Log.d(TAG,"called parse method");
        //showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
        for(int i = 0; i < nearbyPlaceList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble( googlePlace.get("lat"));
            double lng = Double.parseDouble( googlePlace.get("lng"));

            Log.d(TAG , "placeName : " + placeName);

            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : "+ vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

//            mMap.addMarker(markerOptions);
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }
    }
}
