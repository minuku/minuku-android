package labelingStudy.nctu.minuku.streamgenerator;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.AccessibilityDataRecordDAO;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minuku.service.MobileAccessibilityService;
import labelingStudy.nctu.minuku.stream.AccessibilityStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static labelingStudy.nctu.minuku.manager.MinukuStreamManager.getInstance;

/**
 * Created by Lawrence on 2017/9/6.
 */

/**
 * AccessibilityStreamGenerator collects data about events happen in the user interface that get from AccessibilityEvent
 */

public class AccessibilityStreamGenerator extends AndroidStreamGenerator<AccessibilityDataRecord> {

    private final String TAG = "AccessibilityStreamGenerator";

    private AccessibilityStream mStream;
    private Context mContext;
    AccessibilityDataRecordDAO mDAO;
    MobileAccessibilityService mMobileAccessibilityService;

    private String mPack;
    private String mText;
    private String mType;
    private String mExtra;

    /**
     * Initial constructor
     * @param applicationContext
     */
    public AccessibilityStreamGenerator(Context applicationContext) {
        super(applicationContext);
        mContext = applicationContext;
        mStream = new AccessibilityStream(Constants.DEFAULT_QUEUE_SIZE);
        mDAO = MinukuDAOManager.getInstance().getDaoFor(AccessibilityDataRecord.class);

        mMobileAccessibilityService = new MobileAccessibilityService(this);

        mPack = mText = mType = mExtra = "";

        this.register();
    }

    /**
     * Register to MinukuStreamManager with a AccessibilityDataRecord stream
     */
    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManager");

        try {
            getInstance().register(mStream, AccessibilityDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "AccessibilityDataRecord/AccessibilityStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " AccessibilityDataRecord/AccessibilityStream is already registered.");
        }
    }

    private void activateAccessibilityService() {

        Log.d(TAG, "testing logging task and requested activateAccessibilityService");
        Intent intent = new Intent(mContext, MobileAccessibilityService.class);
        mContext.startService(intent);
    }


    @Override
    public Stream<AccessibilityDataRecord> generateNewStream() {
        return mStream;
    }

    /**
     * Send data as AccessibilityDataRecord to database.
     * @return
     */
    @Override
    public boolean updateStream() {

        Log.d(TAG, "updateStream called");

        AccessibilityDataRecord accessibilityDataRecord
                = new AccessibilityDataRecord(mPack, mText, mType, mExtra);
        mStream.add(accessibilityDataRecord);
        Log.d(TAG,"pack = " + mPack + " text = " + mText + " type = " + mType + " extra = " + mExtra);
        Log.d(TAG, "Accessibility to be sent to event bus" + accessibilityDataRecord);
        // also post an event.
        EventBus.getDefault().post(accessibilityDataRecord);
        try {
            appDatabase db;
            db = Room.databaseBuilder(mContext,appDatabase.class,"dataCollection")
                    .allowMainThreadQueries()
                    .build();

            db.accessibilityDataRecordDao().insertAll(accessibilityDataRecord);
            List<AccessibilityDataRecord> accessibilityDataRecords = db.accessibilityDataRecordDao().getAll();

            for (AccessibilityDataRecord a : accessibilityDataRecords) {
                Log.e(TAG, "mPack in db: " + a.getPack());
                Log.e(TAG, "Type in db: " + a.getType());
                Log.e(TAG, "Text in db: " + a.getText());
                Log.e(TAG, "Extra in db: " + a.getExtra());
            }
        } catch (NullPointerException e) { //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }

        // Remove to avoid asyc error
        //mPack = mText = mType = mExtra = "";

        return false;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    /**
     * Update Accessibility data from MobileAccessibilityService
     */
    public void setLatestInAppAction(String pack, String text, String type, String extra) {

        this.mPack = pack;
        this.mText = text;
        this.mType = type;
        this.mExtra = extra;

    }

    @Override
    public void onStreamRegistration() {
        activateAccessibilityService();
    }

    @Override
    public void offer(AccessibilityDataRecord dataRecord) {

    }
}
