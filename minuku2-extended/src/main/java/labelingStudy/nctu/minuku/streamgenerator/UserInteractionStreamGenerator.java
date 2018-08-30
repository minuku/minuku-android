package labelingStudy.nctu.minuku.streamgenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.UserInteractionDataRecordDAO;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.UserInteractionDataRecord;
import labelingStudy.nctu.minuku.stream.UserInteractionStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2018/8/29.
 */

public class UserInteractionStreamGenerator extends AndroidStreamGenerator<UserInteractionDataRecord> {

    private String TAG = "UserInteractionStreamGenerator";
    private UserInteractionStream mStream;
    private UserInteractionDataRecordDAO mDAO;

    private static final String STRING_FALSE = "0";
    private static final String STRING_TRUE = "1";

    private String present = STRING_FALSE;
    private String unlock = STRING_FALSE;
    private String background = STRING_FALSE;
    private String foreground = STRING_FALSE;

    public UserInteractionStreamGenerator (Context applicationContext) {

        super(applicationContext);
        this.mStream = new UserInteractionStream(Constants.DEFAULT_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(UserInteractionDataRecord.class);
        this.register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManager");
        try {
            MinukuStreamManager.getInstance().register(mStream, UserInteractionDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "UserInteractionDataRecord/UserInteractionStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " UserInteractionDataRecord/UserInteractionStream is already registered.");
        }
    }

    @Override
    public Stream<UserInteractionDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {

        Log.e(TAG, "Update stream called.");

        UserInteractionDataRecord userInteractionDataRecord
                = new UserInteractionDataRecord(present, unlock, background, foreground);
        mStream.add(userInteractionDataRecord);
        Log.d(TAG, "UserInteractionDataRecord to be sent to event bus" + userInteractionDataRecord);
        // also post an event.
        EventBus.getDefault().post(userInteractionDataRecord);
        try {
            mDAO.add(userInteractionDataRecord);
        } catch (DAOException e) {
            e.printStackTrace();
            return false;
        }catch (NullPointerException e){ //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_USER_UNLOCKED);
        intentFilter.addAction(Intent.ACTION_USER_BACKGROUND);
        intentFilter.addAction(Intent.ACTION_USER_FOREGROUND);
        mApplicationContext.registerReceiver(mBroadcastReceiver, intentFilter);

        CSVHelper.storeToCSV(CSVHelper.CSV_UserInteract, "present", "unlock", "background", "foreground");

        Log.d(TAG, "Stream " + TAG + " registered successfully");

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            present = STRING_FALSE;
            unlock = STRING_FALSE;
            background = STRING_FALSE;
            foreground = STRING_FALSE;

            if (action.equals(Intent.ACTION_USER_PRESENT)) {

                present = STRING_TRUE;
            }

            if (action.equals(Intent.ACTION_USER_UNLOCKED)) {

                unlock = STRING_TRUE;
            }

            if (action.equals(Intent.ACTION_USER_BACKGROUND)) {

                background = STRING_TRUE;
            }

            if (action.equals(Intent.ACTION_USER_FOREGROUND)) {

                foreground = STRING_TRUE;
            }

            Log.d(TAG, "present : "+ present);
            Log.d(TAG, "unlock : "+ unlock);
            Log.d(TAG, "background : "+ background);
            Log.d(TAG, "foreground : "+ foreground);

            CSVHelper.storeToCSV(CSVHelper.CSV_UserInteract, present, unlock, background, foreground);
        }
    };

    @Override
    public void offer(UserInteractionDataRecord dataRecord) {

    }
}
