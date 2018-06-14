package labelingStudy.nctu.minuku.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.TimeZone;

import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;

/**
 * Created by chiaenchiang on 08/03/2018.
 */

public class MobileAccessibilityService extends AccessibilityService {

    private final String TAG="MobileAccessibilityService";

    private static labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator AccessibilityStreamGenerator;

    public MobileAccessibilityService(){}
    public MobileAccessibilityService(labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator accessibilityStreamGenerator){
        super();

        try {
            this.AccessibilityStreamGenerator = (labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(AccessibilityDataRecord.class);
        } catch (StreamNotFoundException e){
            this.AccessibilityStreamGenerator = accessibilityStreamGenerator;
            e.printStackTrace();
        }

    }

    @Override
    protected void onServiceConnected() {
        Log.d("in access", "config success!");
        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        accessibilityServiceInfo.notificationTimeout = 1000;
        setServiceInfo(accessibilityServiceInfo);

    }

    @SuppressLint("LongLogTag")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        // TODO Auto-generated method stub
        int eventType = accessibilityEvent.getEventType();
        String pack = "";
        String text = "";
        String type = "";
        String extra = "";
        long time = -1;

        Log.d(TAG,"onAccessibilityEvent");

        if(accessibilityEvent.getPackageName()!=null){
            pack=accessibilityEvent.getPackageName().toString();
            Log.d(TAG,"pack : "+ pack);
        }

        if (accessibilityEvent.getClassName()!=null ) {
            text = accessibilityEvent.getClassName().toString();
        }
        if(accessibilityEvent.getText()!=null){
            text += ":" + accessibilityEvent.getText().toString();
            //TODO testing the attribute.
            Log.d(TAG,"text : "+ text);
        }
        if (accessibilityEvent.getContentDescription()!=null) {
            extra = accessibilityEvent.getContentDescription().toString();
            Log.d(TAG,"extra : "+ extra);
        }
        time = getCurrentTimeInMillis();

//        Log.d(TAG,"event : " + accessibilityEvent.toString());

        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                type="TYPE_VIEW_CLICKED";
                Log.d(TAG,type);
                break;

            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                type="TYPE_VIEW_LONG_CLICKED";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                type="TYPE_VIEW_SCROLLED";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                type="TYPE_VIEW_SELECTED";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                type="TYPE_VIEW_TEXT_CHANGED";
                Log.d(TAG,type);
                break;
        }

        Log.d(TAG,"pack = "+pack+" text = "+text+" type = "+type+" extra = "+extra);

        AccessibilityStreamGenerator.setLatestInAppAction(pack, text, type, extra);

    }

    @Override
    public void onInterrupt() {

    }

    public static long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        java.util.Calendar cal = java.util.Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }
}

