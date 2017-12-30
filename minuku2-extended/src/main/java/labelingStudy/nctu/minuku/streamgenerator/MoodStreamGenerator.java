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
import android.os.AsyncTask;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.config.UserPreferences;
import labelingStudy.nctu.minuku.dao.MoodDataRecordDAO;
import labelingStudy.nctu.minuku.event.DecrementLoadingProcessCountEvent;
import labelingStudy.nctu.minuku.event.IncrementLoadingProcessCountEvent;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.MoodDataRecord;
import labelingStudy.nctu.minuku.stream.ImageStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.event.IsDataExpectedEvent;
import labelingStudy.nctu.minukucore.event.StateChangeEvent;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by shriti on 7/21/16.
 */
public class MoodStreamGenerator extends AndroidStreamGenerator<MoodDataRecord> {

    private Stream mStream;
    private String TAG = "MoodStreamGenerator";
    private MoodDataRecordDAO mDAO;

    public MoodStreamGenerator(Context applicationContext) {
        super(applicationContext);
        this.mStream = new ImageStream(Constants.DEFAULT_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(MoodDataRecord.class);
        this.register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with Stream Manager");
        try {
            MinukuStreamManager.getInstance().register(mStream, MoodDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    " MoodDataRecord/MoodStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " MoodDataRecord/MoodStream is already registered.");
        }
    }

    @Override
    public Stream<MoodDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG,
                "Update Stream called: The preference values are - \n" +
                        UserPreferences.getInstance().getPreference("Trip_startTime") + "\n" +
                        UserPreferences.getInstance().getPreference("Trip_endTime"));
        MinukuStreamManager.getInstance().handleIsDataExpectedEvent(
                new IsDataExpectedEvent(MoodDataRecord.class));
        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return Constants.MOOD_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES;
    }

    @Override
    public void sendStateChangeEvent() {
        EventBus.getDefault().post(new StateChangeEvent(MoodDataRecord.class));
    }

    @Override
    public void onStreamRegistration() {

        Log.d(TAG, "Stream " + TAG + " registered successfully");
        EventBus.getDefault().post(new IncrementLoadingProcessCountEvent());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Log.d(TAG, "Stream " + TAG + "initialized from previous state");
                    Future<List<MoodDataRecord>> listFuture =
                            mDAO.getLast(Constants.MOOD_QUEUE_SIZE);
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
                } finally {
                    EventBus.getDefault().post(new DecrementLoadingProcessCountEvent());
                }
            }
        });
    }

    @Override
    public void offer(MoodDataRecord aMoodDataRecord) {
        mStream.add(aMoodDataRecord);
        try {
            mDAO.add(aMoodDataRecord);
            StateChangeEvent moodStateChangeEvent = new StateChangeEvent(MoodDataRecord.class);
            MinukuStreamManager.getInstance().handleStateChangeEvent(moodStateChangeEvent);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

}
