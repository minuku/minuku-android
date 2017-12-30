package labelingStudy.nctu.minuku_2.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.manager.TripManager;
import labelingStudy.nctu.minuku_2.R;

//import edu.ohio.minuku_2.R;

/**
 * Created by Lawrence on 2017/4/22.
 */

public class record extends AppCompatActivity {

    private final String TAG = "record";
    private ListView listview;
    private int Trip_size;
    private Context mContext;

    ArrayList<String> mlocationDataRecords;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);
    }

    public record(Context mContext){
        this.mContext = mContext;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG,"onResume");


    }

    public void initrecord(View view){
        Log.d(TAG,"initrecordinglistohio");

        ArrayList<String> locationDataRecords = null;

        /*
        listview = (ListView) view.findViewById(R.id);
        listview.setEmptyView(view.findViewById(R.id.emptyView));

        try{

            Log.d(TAG,"ListRecordAsyncTask");

//            locationDataRecords = new ListRecordAsyncTask().execute(mReviewMode).get();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                locationDataRecords = new ListRecordAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            else
                locationDataRecords = new ListRecordAsyncTask().execute().get();


            Log.d(TAG,"locationDataRecords = new ListRecordAsyncTask().execute().get();");

            mlocationDataRecords = locationDataRecords;

        }catch(InterruptedException e) {
            Log.d(TAG,"InterruptedException");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d(TAG,"ExecutionException");
            e.printStackTrace();
        }
        */

//        liststr.add(mlocationDataRecords.get());

//        listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);

        /*OhioListAdapter ohioListAdapter = new OhioListAdapter(
                this,
                R.id.recording_list,
                mlocationDataRecords
        );*/

        /*listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,mlocationDataRecords);

        listview.setAdapter(listAdapter);*/

       /* listview.setAdapter(ohioListAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                startAnnotateActivity(position);

//                startActivity(new Intent(recordinglistohio.this, annotateohio.class));

            }
        });*/

    }

    private class ListRecordAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {

        private ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {
            Log.d(TAG,"onPreExecute");

        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            Log.d(TAG, "listRecordAsyncTask going to list recording");

            ArrayList<String> locationDataRecords = new ArrayList<String>();

            try {

                locationDataRecords = TripManager.getTripDatafromSQLite();
//                locationDataRecords = TripManager.getInstance().getTripDatafromSQLite();
//                Trip_size = TripManager.getInstance().getSessionidForTripSize();
                Trip_size = TripManager.getInstance().getTrip_size();

//                Log.d(TAG,"locationDataRecords(0) : " + locationDataRecords.get(0));
//                Log.d(TAG,"locationDataRecords(max) : " + locationDataRecords.get(locationDataRecords.size()-1));

//                if(locationDataRecords.isEmpty())
//                    ;

                Log.d(TAG,"try locationDataRecords");
            }catch (Exception e) {
                locationDataRecords = new ArrayList<String>();
                Log.d(TAG,"Exception");
                e.printStackTrace();
            }
//            return locationDataRecords;

            return locationDataRecords;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<String> result) {

            super.onPostExecute(result);

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
        }

    }

}
