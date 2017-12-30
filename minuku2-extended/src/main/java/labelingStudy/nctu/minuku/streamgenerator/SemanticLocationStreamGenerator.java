/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku.streamgenerator;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.config.LocationPreference;
import labelingStudy.nctu.minuku.config.SelectedLocation;
import labelingStudy.nctu.minuku.dao.SemanticLocationDataRecordDAO;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SemanticLocationDataRecord;
import labelingStudy.nctu.minuku.stream.SemanticLocationStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by neerajkumar on 7/21/16.
 */
public class SemanticLocationStreamGenerator
        extends AndroidStreamGenerator<SemanticLocationDataRecord> {

    private SemanticLocationStream mStream;
    private String TAG = "SemanticLocationStreamGenerator";
    private SemanticLocationDataRecordDAO mDAO;


    public SemanticLocationStreamGenerator(Context applicationContext) {
        super(applicationContext);
        mStream = new SemanticLocationStream(Constants.LOCATION_QUEUE_SIZE);
        mDAO = MinukuDAOManager.getInstance().getDaoFor(SemanticLocationDataRecord.class);

        // A potential subscriber must register to the event bus before calling subscribe
        // on it.
        EventBus.getDefault().register(this);
        this.register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, SemanticLocationDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which SemanticLocationDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides SemanticLocationDataRecord is already registered.");
        }
    }

    @Override
    public Stream<SemanticLocationDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        return false;
    }

    @Override
    public long getUpdateFrequency() {
        return -1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {
        Log.d(TAG, "Stream " + TAG + " registered successfully");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Log.d(TAG, "Stream " + TAG + "initialized from previous state");
                    Future<List<SemanticLocationDataRecord>> listFuture =
                            mDAO.getLast(Constants.DEFAULT_QUEUE_SIZE);
                    while(!listFuture.isDone()) {
                        Thread.sleep(1000);
                    }
                    Log.d(TAG, "Received data from Future for " + TAG);
                    mStream.addAll(new LinkedList<>(listFuture.get()));
                } catch (DAOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void offer(SemanticLocationDataRecord dataRecord) {
        // do nothing. The only way data records can be added to semantic location is via
        // the onLocationDataChangeEvent
    }

    // This is how one streamgenerator listens to another stream generators events
    // The event bus is class-agnostic, i.e. any POJO can be passed as an event object.
    @Subscribe
    public void onLocationDataChangeEvent(LocationDataRecord d) {
        try {
            SemanticLocationDataRecord  semanticLocationDataRecord = convertToSemanticLocation(d);
            mDAO.add(semanticLocationDataRecord);
            mStream.add(semanticLocationDataRecord);
        } catch (DAOException e) {
            e.printStackTrace();
            Log.e(TAG, "There was an error adding the semantic location data record.");

        }
    }

    private SemanticLocationDataRecord convertToSemanticLocation(
            LocationDataRecord aLocationDataRecord) {
        for(SelectedLocation selectedLocation: LocationPreference.getInstance().getLocations()) {
            Location loc1 = new Location("");
            loc1.setLatitude(aLocationDataRecord.getLatitude());
            loc1.setLongitude(aLocationDataRecord.getLongitude());

            Location loc2 = new Location("");
            loc2.setLatitude(selectedLocation.getLatitude());
            loc2.setLongitude(selectedLocation.getLongitude());

            // Due to accuracy settings we have used, this can actually be upto 80 meters.
            Log.d(TAG, "Location 1 " + loc1.getLatitude() + ", " + loc1.getLongitude() +
                    ",     Location 2 " + loc2.getLatitude() + ", " + loc2.getLongitude());
            Log.d(TAG, "Label for Location 2: " + selectedLocation.getLabel());

            if(loc1.distanceTo(loc2) < 150) {
                Log.d(TAG, "Found matching location " + selectedLocation.getAddress());
                return new SemanticLocationDataRecord(selectedLocation.getLabel());
            } else {
                Log.d(TAG, "Distance was " + loc1.distanceTo(loc2));
            }
        }
        return new SemanticLocationDataRecord("unknown");
    }

    private String convertLocToString(Location l) {
        return "" + l.getLongitude() + " : " + l.getLatitude();
    }
}
