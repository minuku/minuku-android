package labelingStudy.nctu.minuku_2.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;

/**
 * Created by Lawrence on 2018/3/31.
 */

public class Dispatch extends Activity {

    private final String TAG = "Dispatch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dispatcher);

        Class<?> activityClass;

        try {

            SharedPreferences sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);
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
