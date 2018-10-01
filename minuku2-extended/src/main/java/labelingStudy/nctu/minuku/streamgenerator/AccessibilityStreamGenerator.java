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
import labelingStudy.nctu.minuku.stream.BatteryStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
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
    MobileAccessibilityService mobileAccessibilityService;

    private String pack;
    private String text;
    private String type;
    private String extra;

    /**
     * Initial constructor
     * @param applicationContext
     */
    public AccessibilityStreamGenerator(Context applicationContext){
        super(applicationContext);
        this.mContext = applicationContext;
        this.mStream = new AccessibilityStream(Constants.DEFAULT_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(AccessibilityDataRecord.class);

        mobileAccessibilityService = new MobileAccessibilityService(this);

        pack = text = type = extra = "";

        this.register();
    }

    /**
     * Register a stream with AccessibilityDataRecord
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
                = new AccessibilityDataRecord(pack, text, type, extra);
        mStream.add(accessibilityDataRecord);
        Log.d(TAG,"pack = "+pack+" text = "+text+" type = "+type+" extra = "+extra);
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
                Log.e(TAG, "pack in db: "+a.getPack());
                Log.e(TAG, "Type in db: "+a.getType());
                Log.e(TAG, "Text in db: "+a.getText());
                Log.e(TAG, "Extra in db: "+a.getExtra());
            }
        }catch (NullPointerException e){ //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }

        // Remove to avoid asyc error
        //pack = text = type = extra = "";

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
    public void setLatestInAppAction(String pack, String text, String type, String extra){

        this.pack = pack;
        this.text = text;
        this.type = type;
        this.extra = extra;

    }

    @Override
    public void onStreamRegistration() {

        activateAccessibilityService();

    }

    @Override
    public void offer(AccessibilityDataRecord dataRecord) {

    }
}
