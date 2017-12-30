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

package labelingStudy.nctu.minuku.dao;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.config.UserPreferences;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.event.ShowNotificationEvent;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by neerajkumar on 8/21/16.
 */
public class NotificationDAO implements DAO<ShowNotificationEvent> {

    public static String TAG = "NotificationDAO";
    private String myUserEmail;

    public NotificationDAO() {
        myUserEmail = UserPreferences.getInstance().getPreference(Constants.KEY_ENCODED_EMAIL);
    }

    @Override
    public void setDevice(User user, UUID uuid) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void add(ShowNotificationEvent entity) throws DAOException {
        Log.d(TAG, "Adding notification data record.");
        /*
        Firebase notificationListRef = new Firebase(Constants.FIREBASE_URL_NOTIFICATIONS)
                .child(myUserEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(new Date()).toString());
        notificationListRef.push().setValue(entity);
        */
    }

    @Override
    public void delete(ShowNotificationEvent entity) throws DAOException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Future<List<ShowNotificationEvent>> getAll() throws DAOException {
        throw new DAOException();

    }

    @Override
    public Future<List<ShowNotificationEvent>> getLast(int N) throws DAOException {
        throw new DAOException();
    }

    @Override
    public void update(ShowNotificationEvent oldEntity, ShowNotificationEvent newEntity) throws DAOException {
        throw new RuntimeException("Not implemented");
    }
}
