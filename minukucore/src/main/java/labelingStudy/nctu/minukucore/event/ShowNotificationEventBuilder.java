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

package labelingStudy.nctu.minukucore.event;

import android.util.Log;

import java.util.Map;

public class ShowNotificationEventBuilder {
    private String title;
    private String message;
    private int iconID;
    private int expirationTimeSeconds;
    private Class viewToShow;
    private ShowNotificationEvent.ExpirationAction expirationAction;
    private Map<String, String> params;
    private String category;

    private String TAG = "ShowNotificationEventBuilder";

    public ShowNotificationEventBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public ShowNotificationEventBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public ShowNotificationEventBuilder setIconID(int iconID) {
        this.iconID = iconID;
        return this;
    }

    public ShowNotificationEventBuilder setExpirationTimeSeconds(int expirationTimeSeconds) {
        this.expirationTimeSeconds = expirationTimeSeconds;
        return this;
    }

    public ShowNotificationEventBuilder setViewToShow(Class viewToShow) {
        this.viewToShow = viewToShow;
        return this;
    }

    public ShowNotificationEventBuilder setExpirationAction(ShowNotificationEvent.ExpirationAction expirationAction) {
        this.expirationAction = expirationAction;
        return this;
    }

    public ShowNotificationEventBuilder setCategory(String category) {
        this.category = category;
        return this;
    }

    public ShowNotificationEvent createShowNotificationEvent() {
        Log.d(TAG, "Returing show notification event for " + title);
        return new ShowNotificationEvent(title,
                message,
                iconID,
                expirationTimeSeconds,
                viewToShow,
                expirationAction,
                params,
                category);
    }

    public ShowNotificationEventBuilder setParams(Map<String, String> someParams) {
        this.params = someParams;
        return this;
    }
}