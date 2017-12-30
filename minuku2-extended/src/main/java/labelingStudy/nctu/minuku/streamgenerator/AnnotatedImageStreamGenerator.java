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
import labelingStudy.nctu.minuku.dao.AnnotatedImageDataRecordDAO;
import labelingStudy.nctu.minuku.event.DecrementLoadingProcessCountEvent;
import labelingStudy.nctu.minuku.event.IncrementLoadingProcessCountEvent;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.AnnotatedImageDataRecord;
import labelingStudy.nctu.minuku.stream.AnnotatedImageStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.event.NoDataChangeEvent;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by shriti on 7/22/16.
 *
 * AnnotatedImageDataRecordDAO<T extends AnnotatedImageDataRecord> implements
 DAO<T>
 */
public class AnnotatedImageStreamGenerator<T extends AnnotatedImageDataRecord>
        extends AndroidStreamGenerator<T> {

    private AnnotatedImageStream<T> mStream;
    protected String TAG = "AnnotatedImageStreamGenerator";
    private Class<T> mDataRecordType;

    private AnnotatedImageDataRecordDAO mDAO;

    public AnnotatedImageStreamGenerator() {

    }
    /**
     * The AnnotatedImageStreamGenerator class is extended by multiple classes and the type of
     * generic changes for each subclass. The stream generator needs to get the DAO the type of
     * dataRecord which it is created over. As the type is passed in as a generic, the class of the
     * type cannot be determined at runtime. Hence the constructor for AnnotatedImageStreamGenerator
     * needs to take a type of class at runtime.
     * @param applicationContext The context of the application
     * @param dataRecordType The type of data record
     */
    public AnnotatedImageStreamGenerator(Context applicationContext, Class<T> dataRecordType) {
        super(applicationContext);
        this.mStream = new AnnotatedImageStream(Constants.DEFAULT_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(dataRecordType);
        this.mDataRecordType = dataRecordType;
        this.register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with Stream Manager");
        try {
            MinukuStreamManager.getInstance().register(mStream, mDataRecordType, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which ImageDataRecord/ImageStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
            Log.e(TAG, "Another stream which provides ImageDataRecord/ImageStream is already registered.");
        }
    }

    @Override
    public Stream<T> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG,
                "Update Stream called: The update frequency is - \n" +
                        Constants.FOOD_IMAGE_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES);
        MinukuStreamManager.getInstance().handleNoDataChangeEvent(
                new NoDataChangeEvent(mDataRecordType));
        return true;
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
        EventBus.getDefault().post(new IncrementLoadingProcessCountEvent());
        AsyncTask.execute(new Runnable() {
        @Override
        public void run() {
            try
            {
                Log.d(TAG, "Stream " + TAG + "initialized from previous state");
                Future<List<T>> listFuture =
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
            } finally {
                EventBus.getDefault().post(new DecrementLoadingProcessCountEvent());
            }
        }
    });
}

    @Override
    public void offer(T annotatedImageDataRecord) {
        try {
            //add to stream
            Log.d(TAG, "Adding to stream in the offer method");
            mStream.add(annotatedImageDataRecord);
            //add to database
            mDAO.add(annotatedImageDataRecord);
        } catch (DAOException e){
            e.printStackTrace();
        }
    }

}
