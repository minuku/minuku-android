package labelingStudy.nctu.minuku.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.R;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.streamgenerator.NotificationStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;

/**
 * Created by chiaenchiang on 18/11/2018.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListenService extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    private NotificationManager mManager;

    //    private String deviceId;
    private String title;
    private String text;
    private String subText;
    private String tickerText;
    public String pack;
    private SharedPreferences sharedPrefs;
    ArrayList<String> NotiInfo = new ArrayList<String>();
    ArrayList<String> NotiInfoForNon = new ArrayList<String>();
//    private String app;
//    private Boolean send_form;
//    private String  last_title;
//    private Boolean skip_form;
//    private Intent intent;

    // JSONObject dataInJson = new JSONObject();
    private static NotificationStreamGenerator notificationStreamGenerator;
    String fileName = "notitaskcontent.csv";
    public NotificationListenService(NotificationStreamGenerator notiStreamGenerator){
        try {
            Log.d(TAG,"call notificationlistener service2");
            this.notificationStreamGenerator = (NotificationStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(NotificationDataRecord.class);
        } catch (StreamNotFoundException e) {
            this.notificationStreamGenerator = notiStreamGenerator;
            Log.d(TAG,"call notificationlistener service3");}
    }

    public NotificationListenService(){
        super();
    }
    boolean is_mobile_crowdsource_task = false;
    public class repeatTask {
        Handler mHandler = new Handler();
        int interval = 100; // 1000 * 30
        int countDown;

        Runnable mHandlerTask = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (countDown > 0) {
                    Log.d(TAG,"CountDown in if = "+countDown);
                    SharedPreferences pref = getSharedPreferences("edu.nctu.minuku", MODE_PRIVATE);
                    Integer nhandle_or_dismiss = pref.getInt("nhandle_or_dismiss", -1);
                    //已經handle 而且超過十分鐘 停止偵測
                    if(nhandle_or_dismiss == 4 ||nhandle_or_dismiss==5) {
                        stopRepeatingTask();
                    }
                    countDown -= interval;
                    mHandler.postDelayed(mHandlerTask, interval);
                }else{   //十分鐘之後沒有按
                    // trigger notification 為什麼沒有做
                    // 送的時候當下有沒有亮和unlock
                    Log.d(TAG,"CountDown in else = "+countDown);
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//                    COMMENT3
//                    if (MobileCrowdsourceRecognitionService.ifScreenLight(pm)) {
//                        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//                        if (!MobileCrowdsourceRecognitionService.ifScreenLock(myKM)) {
//                            // trigger notification
//                            //triggerNotifications(pack,"noti_true_user_false");
//                            stopRepeatingTask();
//                        }
//                    }else{  //要送noti時沒有亮，就放棄送ESM
//                        stopRepeatingTask();
//                    }

                }
            }
        };

        void startRepeatingTask() {
            countDown = 2000;//ten minutes // 10*60*1000
            Log.d(TAG,"startCountdown");
            mHandlerTask.run();
        }

        void stopRepeatingTask() {
            mHandler.removeCallbacks(mHandlerTask);
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id ");
        // Crashlytics.log(Log.INFO, "SilentModeService", "Requested new filter. StartId: " + startId + ": " + intent);

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Notification bind");

        return super.onBind(intent);
    }






    @TargetApi(Build.VERSION_CODES.O)


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SharedPreferences pref = getSharedPreferences("edu.nctu.minuku", MODE_PRIVATE);
        Log.d(TAG,"in posted");
        pref.edit()
                .putLong("state_notification_listen", System.currentTimeMillis() / 1000L)
                .apply();

        //    long unixTime = System.currentTimeMillis() / 1000L;

        Log.d(TAG, "Notification received: "+sbn.getPackageName()+":"+sbn.getNotification().tickerText);

        Long last_form_done_time = getSharedPreferences("edu.nctu.minuku", MODE_PRIVATE)
                .getLong("last_form_done_time", 0 );

        Notification notification = sbn.getNotification();

        ContentValues values = new ContentValues();

        try {
            title = notification.extras.get("android.title").toString();
        } catch (Exception e){
            title = "";
        }
        try {
            text = notification.extras.get("android.text").toString();
        } catch (Exception e){
            text = "";
        }

        try {
            subText = notification.extras.get("android.subText").toString();
        } catch (Exception e){
            subText = "";
        }

        try {
            tickerText = notification.tickerText.toString();
        } catch (Exception e){
            tickerText = "";
        }
        try {
            pack = sbn.getPackageName();
        } catch (Exception e){
            pack = "";
        }
        String extra = "";
        for (String key : notification.extras.keySet()) {
            Object value = notification.extras.get(key);
            try {
                Log.d(TAG, String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
                extra += value.toString();
            } catch (Exception e) {

            }
        }
        String postedNotiInfo = title+" "+text+" "+subText+" "+tickerText+" "+extra;
        Log.d(TAG,title+" "+text+" "+subText+" "+tickerText+" "+extra);
        try {
            this.notificationStreamGenerator = (NotificationStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(NotificationDataRecord.class);
        } catch (StreamNotFoundException e) {
            e.printStackTrace();
        }
//COMMENT3
//        int notificationCode = MobileCrowdsourceRecognitionService.matchAppCode(sbn.getPackageName());
        try {
            notificationStreamGenerator.setNotificationDataRecord(title, text, subText, tickerText, pack, -1);
            notificationStreamGenerator.updateStream();
        }catch (Exception e) {
            e.printStackTrace();
        }

        // 判斷是否為map or mobile crowdosurce 且判斷是否螢幕亮著
//        if((text.contains("測試測試"))) {
//            //  if((notificationCode==4)||(notificationCode==5)){
//            is_mobile_crowdsource_task = MobileCrowdsourceRecognitionService.ifMobileCrowdsourceTask(this,postedNotiInfo);
//            pref.edit().putBoolean("is_noti_mobile_crowdsource",is_mobile_crowdsource_task).apply();
//            // if(is_mobile_crowdsource_task) {
//            repeatTask rep = new repeatTask();
//            rep.startRepeatingTask();
//            // checkifNoticeinTenMinutes
//            triggerNotifications(pack,"noti_true_user_false");
//            // }else{
//
//            // }
//
//        }

//        Calendar c = Calendar.getInstance();
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String formattedDate = df.format(c.getTime());






//        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
//        try {
//            resultIntent.setData(Uri.parse("https://nctucommunication.qualtrics.com/jfe/form/SV_78KPI6cbgtRFHp3?app="+app+"&title="+ URLEncoder.encode(title, "UTF-8") +"&text="+URLEncoder.encode(text, "UTF-8")+"&created_at="+unixTime*1000+"&user="+deviceId+"&time="+URLEncoder.encode(formattedDate, "UTF-8")));
//        } catch (java.io.UnsupportedEncodingException e){
//            resultIntent.setData(Uri.parse("https://nctucommunication.qualtrics.com/jfe/form/SV_78KPI6cbgtRFHp3?app="+app+"&title="+title+"&text="+text+"&created_at="+unixTime*1000+"&user="+deviceId+"&time="+formattedDate));
//        }
//
//        Intent notificationIntent = new Intent(getApplicationContext(),  ResultActivity.class);
//        try{
//            notificationIntent.putExtra("URL", "https://nctucommunication.qualtrics.com/jfe/form/SV_78KPI6cbgtRFHp3?app="+app+"&title="+ URLEncoder.encode(title, "UTF-8") +"&text="+URLEncoder.encode(text, "UTF-8")+"&created_at="+unixTime*1000+"&user="+deviceId+"&time="+URLEncoder.encode(formattedDate, "UTF-8"));
//
//        } catch (java.io.UnsupportedEncodingException e){
//            notificationIntent.putExtra("URL", "https://nctucommunication.qualtrics.com/jfe/form/SV_78KPI6cbgtRFHp3?app="+app+"&title="+title+"&text="+text+"&created_at="+unixTime*1000+"&user="+deviceId+"&time="+formattedDate);
//        }
//        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_CLEAR_TASK);


//        Long last_form_notification_sent_time = getSharedPreferences("edu.nctu.minuku", MODE_PRIVATE)
//                .getLong("last_form_notification_sent_time", 1);
//
//
//
////        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
////                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////
////        PendingIntent formIntent = PendingIntent.getActivity(this, UUID.randomUUID().hashCode(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        Intent snoozeIntent = new Intent(this, labelingStudy.nctu.minuku.receiver.SnoozeReceiver.class);
//        snoozeIntent.setAction("ACTION_SNOOZE");
//
//        PendingIntent btPendingIntent = PendingIntent.getBroadcast(this, UUID.randomUUID().hashCode(), snoozeIntent,0);
//
//
//        mManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//        // notification channel
//        int notifyID = 1;
//        String CHANNEL_ID = "my_channel_01";// The id of the channel.
//        CharSequence name = "firstChannel";// The user-visible name of the channel.
//        int importance = NotificationManager.IMPORTANCE_HIGH;
//        @SuppressLint({"NewApi", "LocalSuppress"}) NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mManager.createNotificationChannel(mChannel);
//        }
//
//
//
//
//
//        Log.d(TAG,"notificaitonCode : "+notificationCode);
//
//        if(notificationCode == InterceptedNotificationCode.OTHER_KIND_OF_NOTIFICATION){
//            Log.d(TAG,"inOtherKind");
//            if((text.contains("測試測試"))) {
//                pref.edit()
//                        .putLong("state_notification_sent_esm", System.currentTimeMillis() / 1000L)
//                        .apply();
////                try {
////                    SQLiteDatabase db = DBManager.getInstance().openDatabase();
////                    values.put(DBHelper.TIME, new Date().getTime());
////                    values.put(DBHelper.title_col, title);
////                    values.put(DBHelper.n_text_col, text);
////                    values.put(DBHelper.subText_col, subText);
////                    values.put(DBHelper.tickerText_col, tickerText);
////                    values.put(DBHelper.app_col, sbn.getPackageName());
////                    values.put(DBHelper.sendForm_col, Boolean.TRUE);
////                    values.put(DBHelper.longitude_col, (float)LocationStreamGenerator.longitude.get());
////                    values.put(DBHelper.latitude_col, (float)LocationStreamGenerator.latitude.get());
////
////                    db.insert(DBHelper.notification_table, null, values);
////
////                } catch (NullPointerException e) {
////                    e.printStackTrace();
//////                    Amplitude.getInstance().logEvent("SAVE_NOTIFICATION_FAILED");
////                } finally {
////                    values.clear();
////                    DBManager.getInstance().closeDatabase();
////                }
////                Amplitude.getInstance().logEvent("SUCCESS_SEND_FORM");
//                pref.edit()
//                        .putLong("last_form_notification_sent_time", unixTime)
//                        .apply();
//                String type = pref.getString("type","NA");
//                Log.d(TAG,"type : "+type);
//
//
//
//
//                Log.d(TAG,"ready to sent questionnaire");
//
//
//                Intent nIntent = new Intent(Intent.ACTION_VIEW);
//
//
//
//                app = "googleMap";
//                nIntent.setData(Uri.parse("https://nctucommunication.qualtrics.com/jfe/form/SV_ezVodMgyxCpbe7j?app="+app));
//                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, nIntent, 0);
//// Create a notification and set the notification channel.
//                Notification noti = new Notification.Builder(this)
//                        .setContentTitle("New Message")
//                        .setContentText("請填寫問卷")
//                        .setSmallIcon(R.drawable.self_reflection)
//                       // .setWhen(System.currentTimeMillis()+5000)
//                        .setContentIntent(contentIntent)
//                        .setChannelId(CHANNEL_ID)
//                        .setAutoCancel(true)
//                        .setOngoing(true)
//
//                        .build();
//                mManager.notify(notifyID , noti);
//
//
//
//                new Thread(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    Thread.sleep(600*1000);
//                                } catch (InterruptedException e) {
//                                    Log.d(TAG, "sleep failure");
//                                }
//
//                                mManager.cancel(0);
//                            }
//                        }
//                ).start();
//
//                Handler h = new Handler();
//                long delayInMilliseconds = 600*1000;
//                h.postDelayed(new Runnable() {
//                    public void run() {
//                        mManager.cancel(0);
//                    }
//                }, delayInMilliseconds);
//            }
//
//
//
//        }


    }


//COMMENT2
//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn) {
//        Log.d(TAG, "Notification handle or dismiss: "+sbn.getPackageName()+":"+sbn.getNotification().tickerText);
//        int notificationCode = MobileCrowdsourceRecognitionService.matchAppCode(sbn.getPackageName());
//        SharedPreferences pref = getSharedPreferences("edu.nctu.minuku", MODE_PRIVATE);
//
//        // double check : 是否內容為mobile crowdsource
//        if(notificationCode==4||notificationCode==5) {
//            String removedNotiinfo = null;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                removedNotiinfo = getRemovedNotiInfo(sbn);
//            }
//            boolean is_removed_mobile_crowdsource_task = MobileCrowdsourceRecognitionService.ifMobileCrowdsourceTask(this, removedNotiinfo);
//
//            pref.edit().putBoolean("is_noti_mobile_crowdsource", is_removed_mobile_crowdsource_task).apply();
//
//            pref.edit()
//                    .putInt("nhandle_or_dismiss", notificationCode)
//                    .apply();
//            saveArrayList(NotiInfo, "NotiInfo", pref);
//            NotiInfo.clear();
//        }


//        else if(notificationCode==1||notificationCode==2||notificationCode==3){ //message
//////            String removedNotiinfo = null;
//////            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//////                removedNotiinfo = getRemovedNotiInfo(sbn);
//////            }
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                getRemovedNotiInfo(sbn);
//            }
//            pref.edit()
//                    .putInt("nhandle_or_dismiss", notificationCode)
//                    .apply();
//            saveArrayList(NotiInfo, "NotiInfo", pref);
//            NotiInfo.clear();
//        }
//
//    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getRemovedNotiInfo(StatusBarNotification sbn){
        String title,text,subText,tickerText,pack = " ";
        try {
            title = sbn.getNotification().extras.get("android.title").toString();
        } catch (Exception e){
            title = "";
        }
        try {
            text = sbn.getNotification().extras.get("android.text").toString();
        } catch (Exception e){
            text = "";
        }

        try {
            subText = sbn.getNotification().extras.get("android.subText").toString();
        } catch (Exception e){
            subText = "";
        }

        try {
            tickerText = sbn.getNotification().tickerText.toString();
        } catch (Exception e){
            tickerText = "";
        }
        try {
            pack = sbn.getPackageName();
        } catch (Exception e){
            pack = "";
        }

        NotiInfo.add(title);
        NotiInfo.add(text);
        NotiInfo.add(subText);
        NotiInfo.add(tickerText);
        NotiInfo.add(pack);



        String allString = title+" "+text+" "+subText+" "+tickerText+" "+pack;
        return allString;
    }

//COMMENT1
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void triggerNotifications(String app, String type){
//        SharedPreferences pref = getSharedPreferences("edu.nctu.minuku", MODE_PRIVATE);
//        Log.d(TAG," notificaitons");
//        final NotificationManager mManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        // notification channel
//        int notifyID = 1;
//        String CHANNEL_ID = "my_channel_01";// The id of the channel.
//        CharSequence name = "firstChannel";// The user-visible name of the channel.
//        int importance = NotificationManager.IMPORTANCE_HIGH;
//        @SuppressLint({"NewApi", "LocalSuppress"}) NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mManager.createNotificationChannel(mChannel);
//        }
//        //print notificaiton send time
//
//
//
//        Log.d(TAG,"ready to sent questionnaire");
//
//        Intent nIntent = new Intent(Intent.ACTION_VIEW);
//
//        //   nIntent.putExtra("URL", "https://nctucommunication.qualtrics.com/jfe/form/SV_78KPI6cbgtRFHp3?app="+app);//"&title="+ URLEncoder.encode(title, "UTF-8") +"&text="+URLEncoder.encode(text, "UTF-8")+"&created_at="+unixTime*1000+"&user="+deviceId+"&time="+URLEncoder.encode(formattedDate, "UTF-8"
//        try {
//            nIntent = new Intent(this,Class.forName("mobilecrowdsourceStudy.nctu.minuku_2.MainActivity"));
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        pref.edit().putString("appNameForQ",app).apply();
//        pref.edit().putString("usertaskTypeForQ",type).apply();
//        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);
//        sharedPrefs.edit().putBoolean("canFillQuestionnaire",true).apply();
//        sharedPrefs.edit().putInt("relatedIdForQ",-1).apply();
//
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, nIntent, 0);
//        // Create a notification and set the notification channel.
//        @SuppressLint({"NewApi", "LocalSuppress"}) Notification noti = new Notification.Builder(this)
//                .setContentTitle("New Message")
//                .setContentText("為什麼沒執行請填寫問卷: "+type)
//                .setSmallIcon(R.drawable.hand_shake_noti)
//                // .setWhen(System.currentTimeMillis()+5000)
//                .setContentIntent(contentIntent)
//                .setChannelId(CHANNEL_ID)
//                .setAutoCancel(true)
//                .setOngoing(true)
//
//                .build();
//        mManager.notify(notifyID , noti);
//
//
//
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(600*1000);
//                        } catch (InterruptedException e) {
//                            Log.d(TAG, "sleep failure");
//                        }
//
//                        mManager.cancel(0);
//                    }
//                }
//        ).start();
//
//
//    }

    public void saveArrayList(ArrayList<String> list, String key, SharedPreferences prefs){

        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }







}
