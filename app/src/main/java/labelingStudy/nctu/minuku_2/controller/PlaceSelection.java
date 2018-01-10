package labelingStudy.nctu.minuku_2.controller;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import labelingStudy.nctu.minuku.streamgenerator.LocationStreamGenerator;
import labelingStudy.nctu.minuku_2.NearbyPlaces.GetUrl;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.controller.Timeline.DchoosingSite;

public class PlaceSelection extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = "PlaceSelection";

    private SharedPreferences sharedPrefs;

    ArrayList<String> MarkerName = new ArrayList<String>();
    ArrayList<String> MarkerLat = new ArrayList<String>();
    ArrayList<String> MarkerLng = new ArrayList<String>();

    MapView mapView;
    Button AddPlace, SecRes, Muf, Third;
    static String json = "";

    static double lat = 0;
    static double lng = 0;
    static String MarkerFlag = "";
    static int MarkerCount = 0;

    private boolean fromTimeLineFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selection);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE);

    }

    public PlaceSelection(){
        fromTimeLineFlag = true;
    }

    public PlaceSelection(double lat, double lng){
        fromTimeLineFlag = true;
        this.lat = lat;
        this.lng = lng;
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

                if(!fromTimeLineFlag) {
                    timer_site.data.add(sitename);

                    Log.d(TAG, " data : "+ timer_site.data);
                    Log.d(TAG, " dataSize : "+ timer_site.data.size());
                }else{
                    Timeline.selectedSiteName = sitename;
                    DchoosingSite.setText(Timeline.selectedSiteName);
                }

                PlaceSelection.this.finish();
            }else{
                Toast.makeText(PlaceSelection.this,"請點選一個地點" , Toast.LENGTH_LONG).show();
            }

//            sharedPrefs.edit().putString("dataContent" + (timer_site.data.size()-1), sitename);// -1 is because of after add it in.
//            sharedPrefs.edit().putInt("dataSize", timer_site.data.size());
//            sharedPrefs.edit().apply();



            /*if(AddPlace.getText().equals("新增地點")) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlaceSelection.this);
                alertDialog.setTitle("自訂地點");
                alertDialog.setView(v);
                alertDialog.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = (EditText) v.findViewById(R.id.edit_text);
                        String name = editText.getText().toString();
                        if (name.equals("")) {
                            Toast.makeText(PlaceSelection.this, "請輸入地點", Toast.LENGTH_SHORT);
                        }
                        AddPlace.setText("使用\"" + name + "\"為地點");
                    }
                });
                alertDialog.show();
            }else {
                //TODO jump back to home.xml and ready to count the time they staying here;


            }*/
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG,"onResume");

        fromTimeLineFlag = false;

        initPlaceSelection();
    }

    private void initPlaceSelection(){

//        mapView = (MapView) findViewById(R.id.mapView);
//        mapView.onCreate(savedInstanceState);
//        mapView.getMapAsync(PlaceSelection.this);

        ((MapFragment) getFragmentManager().findFragmentById(R.id.Mapfragment)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                map.setOnMarkerClickListener(MarkClick);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

//                float lat = 0;
//                float lng = 0;

                /*if(!fromTimeLineFlag){
                    try{
                        lat = LocationStreamGenerator.latitude.get();
                        lng = LocationStreamGenerator.longitude.get();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    Bundle latlng = getIntent().getExtras();
                    lat = latlng.getDouble("lat");
                    lng = latlng.getDouble("lng");
                }*/

                try{
                    lat = LocationStreamGenerator.latitude.get();
                    lng = LocationStreamGenerator.longitude.get();
                } catch(Exception e) {
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
//                    Log.d(TAG, "MMMMMMMMMMMMMMMMMM: " + MarkerLat.get(1));
                    for(int k = 0; k < MarkerLat.size(); k++){
//                        Log.d(TAG, "Checkkkkkk name: " + MarkerName.get(k));
                        LatLng latandLng = new LatLng(Double.parseDouble(MarkerLat.get(k)), Double.parseDouble(MarkerLng.get(k)));

//                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latandLng, 13));
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
//        SecRes = (Button)findViewById(R.id.btn_secRes);
//        Muf = (Button)findViewById(R.id.btn_muf);
//        Third = (Button)findViewById(R.id.btn_third);


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
//            Log.d(TAG, sb.toString());
            json = sb.toString();
            br.close();
//            return sb.toString();


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
        /*
        float lat = 0;
        float lng = 0;

        try{
            lat = LocationStreamGenerator.toCheckFamiliarOrNotLocationDataRecord.getLatitude();
            lng = LocationStreamGenerator.toCheckFamiliarOrNotLocationDataRecord.getLongitude();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        LatLng latLng = new LatLng(lat, lng);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        map.addMarker(new MarkerOptions().position(latLng).title("Marker"));
        */
    }

   /* @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }*/

    @Override
    protected void onDestroy() {
//        mapView.onDestroy();
        super.onDestroy();
    }

//    @Override
//    public boolean onMarkerClick(Marker marker) {
//            Log.d(TAG, "Clickkkkkkkkkk");
//            Toast.makeText(PlaceSelection.this,marker.getTitle() + " has been clicked " , Toast.LENGTH_SHORT).show();
//
//        return false;
//    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }*/
}
