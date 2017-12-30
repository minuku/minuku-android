package labelingStudy.nctu.minuku_2.dao;

import com.google.common.util.concurrent.SettableFuture;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.config.UserPreferences;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.model.DiabetesLogDataRecord;
import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.dao.DAOException;
import labelingStudy.nctu.minukucore.user.User;

/**
 * Created by shriti on 10/8/16.
 */

public class DiabetesLogDAO implements DAO<DiabetesLogDataRecord> {

    private String TAG = "DiabetesLogDAO";
    private String myUserEmail;
    private UUID uuID;

    public DiabetesLogDAO() {
        myUserEmail = UserPreferences.getInstance().getPreference(Constants.KEY_ENCODED_EMAIL);
    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(DiabetesLogDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding diabetes log data record.");
        /*
        Firebase dataRecordListRef = new Firebase(Constants.FIREBASE_URL_DIABETESLOG)
                .child(myUserEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(new Date()).toString());
        dataRecordListRef.push().setValue(entity);
        */
    }

    @Override
    public void delete(DiabetesLogDataRecord entity) throws DAOException {

    }

    @Override
    public Future<List<DiabetesLogDataRecord>> getAll() throws DAOException {
        final SettableFuture<List<DiabetesLogDataRecord>> settableFuture =
                SettableFuture.create();
        /*
        Firebase dataRecordListRef = new Firebase(Constants.FIREBASE_URL_DIABETESLOG)
                .child(myUserEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(new Date()).toString());
        Log.d(TAG, "Attempting to get information from " + dataRecordListRef);

        dataRecordListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<DiabetesLogDataRecord> values = new ArrayList<>();
                for(DataSnapshot moodEntry:dataSnapshot.getChildren()) {
                    values.add(moodEntry.getValue(DiabetesLogDataRecord.class));
                }
                settableFuture.set(values);
                Log.d(TAG, "Getall: Successfully retrieved information from DB to DAO.");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                settableFuture.set(null);
            }
        });
        */
        return settableFuture;    }

    @Override
    public Future<List<DiabetesLogDataRecord>> getLast(int N) throws DAOException {
        final SettableFuture<List<DiabetesLogDataRecord>> settableFuture = SettableFuture.create();
        /*
        final Date today = new Date();

        final List<DiabetesLogDataRecord> lastNRecords = Collections.synchronizedList(
                new ArrayList<DiabetesLogDataRecord>());

        getLastNValues(N,
                myUserEmail,
                today,
                lastNRecords,
                settableFuture);
*/
        return settableFuture;
    }

    @Override
    public void update(DiabetesLogDataRecord oldEntity, DiabetesLogDataRecord newEntity) throws DAOException {

    }

    private final void getLastNValues(final int N,
                                      final String userEmail,
                                      final Date someDate,
                                      final List<DiabetesLogDataRecord> synchronizedListOfRecords,
                                      final SettableFuture settableFuture) {
        /*
        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL_DIABETESLOG)
//                .child(userEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(someDate).toString());

        Log.d(TAG, "Checking the value of N "+ N);

        if(N <= 0) {
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
                    settableFuture.set(synchronizedListOfRecords);
                    return;
                }

                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    synchronizedListOfRecords.add(snapshot.getValue(DiabetesLogDataRecord.class));
                    newN--;
                }
                Date newDate = new Date(someDate.getTime() - 26 * 60 * 60 * 1000); // -1 Day
                getLastNValues(newN,
                        userEmail,
                        newDate,
                        synchronizedListOfRecords,
                        settableFuture);
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

                // This would mean that the firebase ref does not exist thereby meaning that
                // the number of entries for all dates are over before we could get the last N
                // results
                settableFuture.set(synchronizedListOfRecords);
            }
        });
        */
    }
}
