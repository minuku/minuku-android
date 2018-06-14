package labelingStudy.nctu.minuku_2.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.R;
//import edu.ohio.minuku_2.R;

/**
 * Created by Lawrence on 2017/6/5.
 */

public class QuestionnaireManager {

    Context context;
    private final ScheduledExecutorService mScheduledExecutorService;
    public static final int BACKGROUND_RECORDING_INITIAL_DELAY = 0;
    public static final int REFRESH_FREQUENCY = 10; //10s, 10000ms

    private final String link = "https://qtrial2017q2az1.az1.qualtrics.com/jfe/form/SV_0VA9kDhoEeWHuYd";
    private String NotificationText = "Please click to fill the questionnaire";

    private final String TAG = "QuestionnaireManager";

    public QuestionnaireManager(){

        mScheduledExecutorService = Executors.newScheduledThreadPool(REFRESH_FREQUENCY);

    }

    public QuestionnaireManager(Context context){

        this.context = context;
        mScheduledExecutorService = Executors.newScheduledThreadPool(REFRESH_FREQUENCY);

        runningtheWork();
    }

    private void runningtheWork(){

        mScheduledExecutorService.scheduleAtFixedRate(
                QuestionnaireRunnable,
                BACKGROUND_RECORDING_INITIAL_DELAY,
                REFRESH_FREQUENCY,
                TimeUnit.SECONDS);
    }

    Runnable QuestionnaireRunnable = new Runnable() {
        @Override
        public void run() {
            doTheWork();
            Log.e(TAG,"doingTheWork");
        }
    };

    private void doTheWork(){
        Notification.Builder note  = new Notification.Builder(context)
                .setContentTitle(Constants.APP_NAME)
                .setContentText(NotificationText)
                .setSmallIcon(R.drawable.self_reflection)
                .setAutoCancel(true);
        //note.flags |= Notification.FLAG_NO_CLEAR;
        //startForeground( 42, note );

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // pending implicit intent to view url
        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setData(Uri.parse(link));

        PendingIntent pending = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        note.setContentIntent(pending);

        // using the same tag and Id causes the new notification to replace an existing one
        mNotificationManager.notify(String.valueOf(System.currentTimeMillis()), 0, note.build());

    }

}
