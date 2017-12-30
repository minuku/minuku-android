package labelingStudy.nctu.minuku.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.TimeZone;

import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;

/**
 * Created by Lawrence on 2017/9/3.
 */

public class MobileAccessibilityService extends AccessibilityService {

    private final String TAG="MobileAccessibilityService";

    private static AccessibilityStreamGenerator accessibilityStreamGenerator;

    public MobileAccessibilityService(){
        super();
    }

    public MobileAccessibilityService(AccessibilityStreamGenerator accessibilityStreamGenerator){
        super();

        try {
            this.accessibilityStreamGenerator = (AccessibilityStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(AccessibilityDataRecord.class);
        }catch (StreamNotFoundException e){
            this.accessibilityStreamGenerator = accessibilityStreamGenerator;
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
             /*
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                type = "TYPE_WINDOW_STATE_CHANGED";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                type = "TYPE_WINDOW_STATE_CHANGED";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                type = "TYPE_WINDOW_STATE_CHANGED";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                eventText = "TYPE_WINDOW_CONTENT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                eventText = "TYPE_WINDOWS_CHANGED";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                type="TYPE_TOUCH_EXPLORATION_GESTURE_START";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                type="TYPE_TOUCH_EXPLORATION_GESTURE_END";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                type="TYPE_TOUCH_INTERACTION_END";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                type="TYPE_TOUCH_INTERACTION_END";
                Log.d(TAG,type);
                break;

            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                type="TYPE_TOUCH_INTERACTION_END";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                type="TYPE_TOUCH_INTERACTION_START";
                Log.d(TAG,type);
                break;

            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                type="TYPE_VIEW_FOCUSED";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                type="TYPE_VIEW_HOVER_ENTER";
                Log.d(TAG,type);
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                type="TYPE_VIEW_HOVER_EXIT";
                Log.d(TAG,type);
                break;
            */
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                type="TYPE_VIEW_CLICKED";
                Log.d(TAG,type);
                break;
//            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
//                type="TYPE_VIEW_CONTEXT_CLICKED";
//                Log.d(TAG,type);
//                break;
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

//        Log.d(TAG,"pack = "+pack+" text = "+text+" type = "+type+" extra = "+extra);

        accessibilityStreamGenerator.setLatestInAppAction(pack, text, type, extra);

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
