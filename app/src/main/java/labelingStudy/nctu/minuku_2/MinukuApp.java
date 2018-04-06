/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku_2;

import android.content.Context;

//import com.bugfender.sdk.Bugfender;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.firebase.client.Config;
import com.instabug.library.IBGInvocationEvent;
import com.instabug.library.Instabug;

import io.fabric.sdk.android.Fabric;
import labelingStudy.nctu.minuku.config.UserPreferences;

/**
 * Created by neerajkumar on 7/18/16.
 */
public class MinukuApp extends android.app.Application {

    private static MinukuApp instance;
    private static Context mContext;

    public static MinukuApp getInstance() {
        return instance;
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        Config mConfig = new Config();
        mConfig.setPersistenceEnabled(true);
        long cacheSizeOfHundredMB = 100 * 1024 * 1024;
        mConfig.setPersistenceCacheSizeBytes(cacheSizeOfHundredMB);
        mConfig.setPersistenceEnabled(true);
        mConfig.setAndroidContext(this);
        /*
        Firebase.setDefaultConfig(mConfig);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        */
        UserPreferences.getInstance().Initialize(getApplicationContext());

        //Bugfender.init(this, "N7pdXEGbmKhK9k8YtpFPyXORtsAwgZa5", false);
        //Bugfender.setForceEnabled(true);

        new Instabug.Builder(this, "2be6d236d601237a17e9c6314455930a")
                .setInvocationEvent(IBGInvocationEvent.IBGInvocationEventShake)
                .build();
    }
}
