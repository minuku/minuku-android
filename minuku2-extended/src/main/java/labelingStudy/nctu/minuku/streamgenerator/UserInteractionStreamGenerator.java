package labelingStudy.nctu.minuku.streamgenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import labelingStudy.nctu.minuku.Data.DBHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.UserInteractionDataRecordDAO;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.UserInteractionDataRecord;
import labelingStudy.nctu.minuku.stream.UserInteractionStream;
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

    private final String ACTION_USERPRESENT = "userPresent";
    private final String ACTION_USERUNLOCK = "userUnlock";
    private final String ACTION_SCREENOFF = "screenOff";

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
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mApplicationContext.registerReceiver(mBroadcastReceiver, intentFilter);

        Log.d(TAG, "Stream " + TAG + " registered successfully");
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_USER_PRESENT)) {

//                present = STRING_TRUE;

                DBHelper.insertActionLogTable(ScheduleAndSampleManager.getCurrentTimeInMillis(), ACTION_USERPRESENT);
            }

            if (action.equals(Intent.ACTION_USER_UNLOCKED)) {

//                unlock = STRING_TRUE;

                DBHelper.insertActionLogTable(ScheduleAndSampleManager.getCurrentTimeInMillis(), ACTION_USERUNLOCK);
            }

            if (action.equals(Intent.ACTION_SCREEN_OFF)) {

                Log.d(TAG, "screen off : "+ ACTION_SCREENOFF);

                DBHelper.insertActionLogTable(ScheduleAndSampleManager.getCurrentTimeInMillis(), ACTION_SCREENOFF);
            }

        }
    };

    @Override
    public void offer(UserInteractionDataRecord dataRecord) {

    }

}
