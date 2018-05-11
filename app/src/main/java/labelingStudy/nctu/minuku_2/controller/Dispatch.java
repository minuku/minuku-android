package labelingStudy.nctu.minuku_2.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.service.BackgroundService;

/**
 * Created by Lawrence on 2018/3/31.
 */

public class Dispatch extends Activity {

    private final String TAG = "Dispatch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dispatcher);

        MultiDex.install(this);

        SharedPreferences sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        Class<?> activityClass;

        boolean firstStartBackGround = sharedPrefs.getBoolean("firstStartBackGround", true);

        if(firstStartBackGround) {

            startService(new Intent(getBaseContext(), BackgroundService.class));

            sharedPrefs.edit().putBoolean("firstStartBackGround", false).apply();
        }

        try {

            activityClass = Class.forName(
                    sharedPrefs.getString("lastActivity", WelcomeActivity.class.getName()));
        } catch(ClassNotFoundException e) {

            activityClass = WelcomeActivity.class;
        }

        Log.d(TAG, "Going to "+activityClass.getName());

        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        Dispatch.this.finish();
    }

}
