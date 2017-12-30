package labelingStudy.nctu.minuku.streamgenerator;

import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.AccessibilityDataRecordDAO;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minuku.service.MobileAccessibilityService;
import labelingStudy.nctu.minuku.stream.BatteryStream;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static labelingStudy.nctu.minuku.manager.MinukuStreamManager.getInstance;

/**
 * Created by Lawrence on 2017/9/6.
 */

public class AccessibilityStreamGenerator extends AndroidStreamGenerator<AccessibilityDataRecord> {

    private final String TAG = "AccessibilityStreamGenerator";
    private Stream mStream;
    private Context mContext;
    AccessibilityDataRecordDAO mDAO;
    MobileAccessibilityService mobileAccessibilityService;

    private String pack;
    private String text;
    private String type;
    private String extra;

    public AccessibilityStreamGenerator(Context applicationContext){
        super(applicationContext);
        this.mContext = applicationContext;
        this.mStream = new BatteryStream(Constants.DEFAULT_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(AccessibilityDataRecord.class);

        mobileAccessibilityService = new MobileAccessibilityService(this);

        pack = text = type = extra = "";

        this.register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManage");

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
            mDAO.add(accessibilityDataRecord);

        } catch (DAOException e) {
            e.printStackTrace();
            return false;
        }catch (NullPointerException e){ //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }

        pack = text = type = extra = "";

        return false;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

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
