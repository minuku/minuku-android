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


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.common.util.concurrent.SettableFuture;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.config.UserPreferences;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.model.DataRecord.AnnotatedImageDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by shriti on 7/22/16.
 */
public class AnnotatedImageDataRecordDAO<T extends AnnotatedImageDataRecord> implements
        DAO<T> {

    protected static String TAG = "AnnotatedImageDataRecordDAO";
    protected String myUserEmail;
    //protected String mFirebaseUrl;
    protected UUID uuID;
    protected Class<T> mDataRecordType;
    protected String imageType;

    public AnnotatedImageDataRecordDAO(Class aDataRecordType) {
        myUserEmail = UserPreferences.getInstance().getPreference(Constants.KEY_ENCODED_EMAIL);
        this.mDataRecordType = aDataRecordType;
        //this.mFirebaseUrl = Constants.FIREBASE_URL_IMAGES;
        this.imageType = "DEFAULT";
    }

    public AnnotatedImageDataRecordDAO(Class aDataRecordType, String imageTypeforURL) {
        myUserEmail = UserPreferences.getInstance().getPreference(Constants.KEY_ENCODED_EMAIL);
        this.mDataRecordType = aDataRecordType;
       //this.mFirebaseUrl = Constants.FIREBASE_URL_IMAGES;
        this.imageType = imageTypeforURL;
    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(AnnotatedImageDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding image data record");
/*
            Firebase imageListRef = new Firebase(this.mFirebaseUrl)
                    .child(myUserEmail)
                    .child(new SimpleDateFormat("MMddyyyy").format(new Date()).toString())
                    .child(imageType);
        imageListRef.push().setValue(entity);
*/
    }

    @Override
    public void delete(AnnotatedImageDataRecord entity) throws DAOException {
        //do nothing for now
    }

    @Override
    public Future<List<T>> getAll() throws DAOException {
        final SettableFuture<List<T>> settableFuture =
                SettableFuture.create();
    /*
        Firebase imageListRef = new Firebase(this.mFirebaseUrl)
                .child(myUserEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(new Date()).toString())
                .child(imageType);

        imageListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, T> imageListMap =
                        (HashMap<String,T>) dataSnapshot.getValue();
                List<T> values = (List) imageListMap.values();
                settableFuture.set(values);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                settableFuture.set(null);
            }
        });
        */
        return settableFuture;
    }

    @Override
    public Future<List<T>> getLast(int N) throws DAOException {
        final SettableFuture<List<T>> settableFuture = SettableFuture.create();
      /*
        final Date today = new Date();

        final List<T> lastNRecords = Collections.synchronizedList(
                new ArrayList<T>());

        getLastNValues(N,
                myUserEmail,
                today,
                lastNRecords,
                settableFuture,
                this.mFirebaseUrl);
        */
        return settableFuture;
    }

    @Override
    public void update(AnnotatedImageDataRecord oldEntity, AnnotatedImageDataRecord newEntity) throws DAOException {
    }

    public final void getLastNValues(final int N,
                                     final String userEmail,
                                     final Date someDate,
                                     final List<T> synchronizedListOfRecords,
                                     final SettableFuture settableFuture,
                                     final String databaseURL) {
        Firebase firebaseRef = new Firebase(databaseURL)
                .child(userEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(someDate).toString())
                .child(imageType);

        Log.d(TAG, "Checking the value of N "+ N);

        if(N <= 0) {
            /* TODO(neerajkumar): Get this f***up fixed! */
            // The first element in the list is actually the last in the database.
            // Reverse the list before setting the future with a result.
            Collections.reverse(synchronizedListOfRecords);

            settableFuture.set(synchronizedListOfRecords);
            return;
        }

        firebaseRef.limitToLast(N).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int newN = N;

                // dataSnapshot.exists returns false when the
                // <root>/<datarecord>/<userEmail>/<date> location does not exist.
                // What it means is that no entries were added for this date, i.e.
                // all the historic information has been exhausted.
                if(!dataSnapshot.exists()) {
                    // The first element in the list is actually the last in the database.
                    // Reverse the list before setting the future with a result.
                    Collections.reverse(synchronizedListOfRecords);

                    settableFuture.set(synchronizedListOfRecords);
                    return;
                }

                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    synchronizedListOfRecords.add(snapshot.getValue(mDataRecordType));
                    newN--;
                }
                Date newDate = new Date(someDate.getTime() - 26 * 60 * 60 * 1000); /* -1 Day */
                getLastNValues(newN,
                        userEmail,
                        newDate,
                        synchronizedListOfRecords,
                        settableFuture,
                        databaseURL);
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

                // The first element in the list is actually the last in the database.
                // Reverse the list before setting the future with a result.
                Collections.reverse(synchronizedListOfRecords);


                // This would mean that the firebase ref does not exist thereby meaning that
                // the number of entries for all dates are over before we could get the last N
                // results
                settableFuture.set(synchronizedListOfRecords);
            }
        });
    }
}
