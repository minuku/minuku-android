package labelingStudy.nctu.minuku_2.controller;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import labelingStudy.nctu.minuku.manager.DBManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku_2.NearbyPlaces.GetUrl;
import labelingStudy.nctu.minuku_2.R;

public class PlaceSelection extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = "PlaceSelection";

    private SharedPreferences sharedPrefs;

    private ArrayList<String> MarkerName = new ArrayList<String>();
    private ArrayList<String> MarkerLat = new ArrayList<String>();
    private ArrayList<String> MarkerLng = new ArrayList<String>();

    private MapView mapView;
    private Button AddPlace, SecRes, Muf, Third;
    private static String json = "";

    private static double lat = 0;
    private static double lng = 0;
    public static String MarkerFlag = "";
    public static int MarkerCount = 0;

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

    private Button.OnClickListener onClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            final View v = LayoutInflater.from(PlaceSelection.this).inflate(R.layout.addplace, null);


            if(!MarkerFlag.equals("")){
                String sitename = MarkerFlag;

                ContentValues values = new ContentValues();

                try {
                    SQLiteDatabase db = DBManager.getInstance().openDatabase();

                    values.put(DBHelper.customsitename_col, sitename);

                    db.insert(DBHelper.customsite_table, null, values);
                }
                catch(NullPointerException e){
                    e.printStackTrace();
                }
                finally {
                    values.clear();
                    DBManager.getInstance().closeDatabase(); // Closing database connection
                }

                Log.d(TAG, "[test add site] fromTimeLineFlag : "+fromTimeLineFlag);

                //TODO confirm the functionality of this
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
            }else{
                Toast.makeText(PlaceSelection.this,"請點選一個地點" , Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG,"onResume");

        try {
            fromTimeLineFlag = bundle.getBoolean("fromTimeLineFlag", false);
        }catch (NullPointerException e){
            e.printStackTrace();
            fromTimeLineFlag = false;
            Log.d(TAG, "no bundle be sent");
        }
        initPlaceSelection();
    }

    private void initPlaceSelection(){

        ((MapFragment) getFragmentManager().findFragmentById(R.id.Mapfragment)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                map.setOnMarkerClickListener(MarkClick);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                try{

                    lat = bundle.getDouble("lat", MinukuStreamManager.getInstance().getLocationDataRecord().getLatitude());
                    lng = bundle.getDouble("lng", MinukuStreamManager.getInstance().getLocationDataRecord().getLongitude());
//                    lat = MinukuStreamManager.getInstance().getLocationDataRecord().getLatitude();
//                    lng = MinukuStreamManager.getInstance().getLocationDataRecord().getLongitude();
                }catch (NullPointerException e){
                    Log.d(TAG, "NullPointerException");
                    Log.d(TAG, "no bundle be sent");

                    lat = MinukuStreamManager.getInstance().getLocationDataRecord().getLatitude();
                    lng = MinukuStreamManager.getInstance().getLocationDataRecord().getLongitude();
                }
                catch(Exception e) {
                    e.printStackTrace();
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
                    for(int k = 0; k < MarkerLat.size(); k++){
                        LatLng latandLng = new LatLng(Double.parseDouble(MarkerLat.get(k)), Double.parseDouble(MarkerLng.get(k)));

                        map.addMarker(new MarkerOptions().position(latandLng).title(MarkerName.get(k)));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                LatLng latLng = new LatLng(lat, lng);

                map.addMarker(new MarkerOptions().position(latLng).title("您的位置"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

            }


        });

        AddPlace = (Button)findViewById(R.id.btn_addplace);

        AddPlace.setOnClickListener(onClick);
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


    private GoogleMap.OnMarkerClickListener MarkClick = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
                MarkerFlag = marker.getTitle().toString();
            return false;
        }
    };

    @Override
    public void onMapReady(GoogleMap map) {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
