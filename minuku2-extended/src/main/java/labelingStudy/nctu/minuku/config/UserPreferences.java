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

package labelingStudy.nctu.minuku.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by shriti on 7/15/16.
 *
 * Imported code from dstudio
 *
 * Created by neera_000 on 1/28/2016.
 *
 * This class can be used to store the globally shared preferences, e.g. the login details of the
 * user, firebase information etc.
 *
 */
public class UserPreferences {

    private static AtomicInteger numActivities = new AtomicInteger(0);
    private String LOG_TAG = "UserPreferences";
    private static UserPreferences mInstance;
    private Context mContext;
    //
    private SharedPreferences mMyPreferences;
    //private User mUser;

    private UserPreferences(){ }

    public static UserPreferences getInstance(){
        if (mInstance == null) mInstance = new UserPreferences();
        return mInstance;
    }

    public void Initialize(Context ctxt){
        mContext = ctxt;
        mMyPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void writePreference(String key, String value){
        SharedPreferences.Editor e = mMyPreferences.edit();
        e.putString(key, value);
        e.commit();
    }

    public void removePreference(String key) {
        SharedPreferences.Editor e = mMyPreferences.edit();
        e.remove(key);
        e.commit();
    }

    public String getPreference(String key) {
        return mMyPreferences.getString(key, null);
    }

    public Set<String> getPreferenceSet(String key) {
        return mMyPreferences.getStringSet(key, null);
    }

    public void clear() {
        SharedPreferences.Editor e = mMyPreferences.edit();
        e.clear();
        e.commit();
    }

    public void setUser(User user) {
        Log.d(LOG_TAG, "Setting user object." + user.toString());
        writePreference("userobject_firstName", user.getFirstName());
        writePreference("userobject_lastName", user.getLastName());
        writePreference("userobject_userEmail", user.getEmail());
    }

    public User getUser() {
        User storedUser = new User(getPreference("userobject_firstName"),
                getPreference("userobject_lastName"),
                getPreference("userobject_userEmail")
                );
        return storedUser;
    }

    public SharedPreferences getSharedPreferences() {
        return this.mMyPreferences;
    }

}

