package labelingStudy.nctu.minuku_2.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.HttpsURLConnection;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku_2.NearbyPlaces.GetUrl;
import labelingStudy.nctu.minuku_2.R;

public class PlaceSelection extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = "PlaceSelection";

    private SharedPreferences sharedPrefs;

    private ArrayList<String> MarkerName = new ArrayList<String>();
    private ArrayList<String> MarkerLat = new ArrayList<String>();
    private ArrayList<String> MarkerLng = new ArrayList<String>();

    private Button AddPlace;
    private static String json = "";

    private static double lat = 0;
    private static double lng = 0;
    public static String markerTitle = "";
    public static LatLng markerLocation;
    public static int MarkerCount = 0;

    private String yourSite = "您的位置";

    private Marker customizedMarker, currentLocationMarker;
    private ArrayList<Marker> customizedMarkers = new ArrayList<>();
    private Bundle bundle;

    public static boolean fromTimeLineFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selection);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE);

        bundle = getIntent().getExtras();
    }

    public PlaceSelection(){
        fromTimeLineFlag = true;
    }

    public PlaceSelection(double lat, double lng){

        fromTimeLineFlag = true;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected void onPause() {
        super.onPause();

        sharedPrefs.edit().putString("lastActivity", getClass().getName()).apply();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            PlaceSelection.this.finish();

            if(isTaskRoot()){

                startActivity(new Intent(this, WelcomeActivity.class));
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG,"onResume");

        try {

            fromTimeLineFlag = bundle.getBoolean("fromTimeLineFlag", false);
        }catch (NullPointerException e){

            fromTimeLineFlag = false;
            Log.d(TAG, "no bundle be sent");
        }

        initPlaceSelection();
    }

    private void initPlaceSelection(){

        AddPlace = (Button)findViewById(R.id.btn_addplace);

        AddPlace.setOnClickListener(onClick);

        ((MapFragment) getFragmentManager().findFragmentById(R.id.Mapfragment)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap map) {

                //prepare the customizedSite from the DB for the marker
                ArrayList<String> customizedSite = DBHelper.queryCustomizedSites();

                if(customizedSite.size() != 0){

                    //check the distance between the session's first location and the customizedSite
                    for(int index = 0; index < customizedSite.size(); index++){

                        String eachData = customizedSite.get(index);

                        String[] dataPieces = eachData.split(Constants.DELIMITER);

                        double siteLat = Double.parseDouble(dataPieces[2]);
                        double siteLng = Double.parseDouble(dataPieces[3]);

                        float[] results = new float[1];
                        Location.distanceBetween(lat, lng, siteLat, siteLng, results);
                        float distance = results[0];

                        if(distance <= Constants.siteRange){

                            LatLng latLng = new LatLng(siteLat, siteLng);

                            Marker marker = map.addMarker(new MarkerOptions().position(latLng).title(dataPieces[1]));

                            customizedMarkers.add(marker);
                        }
                    }
                }

                //show the customize site in different color
                for(int index = 0; index < customizedMarkers.size(); index++){

                    LatLng latlng = customizedMarkers.get(0).getPosition();

                    map.addMarker(new MarkerOptions().position(latlng).title(customizedMarkers.get(0).getTitle()))
                            .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }


                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng latLng) {

                        AddPlace.setText("新增地點");

                        if (customizedMarker != null) {
                            customizedMarker.remove();
                        }

                        customizedMarker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(latLng.latitude, latLng.longitude))
                                .draggable(true).visible(true));

                        customizedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                        customizedMarkers.add(customizedMarker);

                        triggerAlertDialog(customizedMarker);
                    }
                });

                map.setOnMarkerClickListener(onMarkerClicked);

                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                try{

                    lat = bundle.getDouble("lat", MinukuStreamManager.getInstance().getLocationDataRecord().getLatitude());
                    lng = bundle.getDouble("lng", MinukuStreamManager.getInstance().getLocationDataRecord().getLongitude());
                } catch (NullPointerException e){

                    //if there are no data corresponding to the session; get the current one.
                    lat = MinukuStreamManager.getInstance().getLocationDataRecord().getLatitude();
                    lng = MinukuStreamManager.getInstance().getLocationDataRecord().getLongitude();
                } catch(Exception e) {

                    Log.e(TAG, "exception", e);
                }

                final double finalLat = lat;
                final double finalLng = lng;
                final CountDownLatch latch = new CountDownLatch(1);

                Thread thread = new Thread() {

                    public void run() {

                        String name = "";
                        String latitude = "";
                        String longitude = "";

                        getJSON(GetUrl.getUrl(finalLat, finalLng));
                        JSONObject jsonObject = null;

                        try {

                            jsonObject = new JSONObject(json);
                            JSONArray results = jsonObject.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {

                                name = results.getJSONObject(i).getString("name");
                                MarkerName.add(name);
                                latitude = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                                MarkerLat.add(latitude);
                                longitude = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
                                MarkerLng.add(longitude);
                                Log.d(TAG, "name: " + name + "latitude: " + latitude + "longitude: " + longitude);
                            }

                            latch.countDown();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();


                try {

                    latch.await();

                    for(int index = 0; index < MarkerLat.size(); index++){

                        LatLng latlng = new LatLng(Double.parseDouble(MarkerLat.get(index)), Double.parseDouble(MarkerLng.get(index)));

                        map.addMarker(new MarkerOptions().position(latlng).title(MarkerName.get(index)));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                LatLng currentLatLng = new LatLng(lat, lng);

                currentLocationMarker = map.addMarker(new MarkerOptions().position(currentLatLng).title(yourSite));

                currentLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

                currentLocationMarker.showInfoWindow();


                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 20));
            }
        });

    }

    public String getJSON(String url) {

        HttpsURLConnection con = null;

        try {
            URL u = new URL(url);
            con = (HttpsURLConnection) u.openConnection();

            con.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            json = sb.toString();
            br.close();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
           return json;
    }

    private GoogleMap.OnMarkerClickListener onMarkerClicked = new GoogleMap.OnMarkerClickListener() {

        @Override
        public boolean onMarkerClick(final Marker marker) {

            if(marker.equals(currentLocationMarker)) {

                triggerAlertDialog(marker);
            }else {

                Log.d(TAG, "marker is not the customized one");

                try {

                    markerTitle = marker.getTitle().toString();
                    markerLocation = marker.getPosition();
                }catch (NullPointerException e){

                    triggerAlertDialog(marker);
                }

                AddPlace.setText("確認");
            }

            return false;
        }
    };

    private void triggerAlertDialog(final Marker marker){

        final LayoutInflater inflater = LayoutInflater.from(PlaceSelection.this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(PlaceSelection.this);
        final View layout = inflater.inflate(R.layout.sitemarker_dialog,null);

        builder.setView(layout)
                .setPositiveButton(R.string.ok, null);

        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialogInterface) {

                Button button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        EditText sitenameInEditText = (EditText) layout.findViewById(R.id.sitename_edittext);

                        String sitename = sitenameInEditText.getText().toString();

                        marker.setTitle(sitename);

                        markerTitle = marker.getTitle().toString();
                        markerLocation = marker.getPosition();

                        DBHelper.insertCustomizedSiteTable(markerTitle, markerLocation);

                        addToConvenientSiteTable();

                        Toast.makeText(PlaceSelection.this, "成功新增地點", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();

                        //After enter the name, jump to the previous page directly.
                        PlaceSelection.this.finish();
                    }
                });
            }
        });

        mAlertDialog.show();
    }

    private Button.OnClickListener onClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {

            final View v = LayoutInflater.from(PlaceSelection.this).inflate(R.layout.addplace, null);

            if(AddPlace.getText().equals("新增地點")){

                triggerAlertDialog(currentLocationMarker);

            }else if(AddPlace.getText().equals("確認")){

                addToConvenientSiteTable();
            }
        }
    };

    private void addToConvenientSiteTable(){

        String sitename = markerTitle;

        DBHelper.insertConvenientSiteTable(sitename, markerLocation);

        Log.d(TAG, "[test add site] fromTimeLineFlag : "+fromTimeLineFlag);

        //addToConvenientSiteTable the functionality of this
        if(!fromTimeLineFlag) {

            //Timer_site is initialized or alive or not.
            Timer_site.data.add(sitename);

            Log.d(TAG, " data : "+ Timer_site.data);
            Log.d(TAG, " dataSize : "+ Timer_site.data.size());
        }else{

            Timeline.selectedSiteName = sitename;
            Timeline.DchoosingSite.setText(Timeline.selectedSiteName);
        }

        PlaceSelection.this.finish();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}