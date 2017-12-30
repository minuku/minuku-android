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

package labelingStudy.nctu.minuku_2.situation;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import labelingStudy.nctu.minuku.config.UserPreferences;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuSituationManager;
import labelingStudy.nctu.minuku.model.UserSubmissionStats;
import labelingStudy.nctu.minuku_2.event.MissedReportsActionEvent;
import labelingStudy.nctu.minuku_2.manager.InstanceManager;
import labelingStudy.nctu.minuku_2.model.DiabetesLogDataRecord;
import labelingStudy.nctu.minukucore.event.ActionEvent;
import labelingStudy.nctu.minukucore.event.MinukuEvent;
import labelingStudy.nctu.minukucore.event.NoDataChangeEvent;
import labelingStudy.nctu.minukucore.exception.DataRecordTypeNotFound;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.model.StreamSnapshot;
import labelingStudy.nctu.minukucore.situation.Situation;

/**
 * Created by shriti on 8/12/16.
 */
public class MissedReportsSituation implements Situation {

    private String TAG = "MissedReportsSituation";
    private Context mContext;

    public MissedReportsSituation(Context context) {
        try {
            MinukuSituationManager.getInstance().register(this);
            this.mContext = context;
            Log.d(TAG, "Registered successfully.");
        } catch (DataRecordTypeNotFound dataRecordTypeNotFound) {
            Log.e(TAG, "Registration failed.");
            dataRecordTypeNotFound.printStackTrace();
        }
    }

    @Override
    public <T extends ActionEvent> T assertSituation(StreamSnapshot snapshot, MinukuEvent aMinukuEvent) {
        Log.d(TAG, "The type of minuku event received is:" + aMinukuEvent.getStreamSourceClass().getSimpleName() );
        if(!aMinukuEvent.getStreamSourceClass().equals(DiabetesLogDataRecord.class)) {
            Log.e(TAG, "Something is fu**ed up. Expected type :" +
                    " DiabetesLogDataRecord , Received:" + aMinukuEvent.getStreamSourceClass() );
        }
        List<DataRecord> dataRecords = new ArrayList<>();
        dataRecords.add(snapshot.getCurrentValue(DiabetesLogDataRecord.class));
        if (aMinukuEvent instanceof NoDataChangeEvent) {
            Log.d(TAG, "MinukuEvent is instance of no data change event. Checking if I should check" +
                    "time passed from last data reported to check for missing reports");
            if (checkLastDataReport(snapshot)) {
                Log.d(TAG, "Should show questionnaire if returned true. Sending ActionEvent.");
                return (T) new MissedReportsActionEvent("MISSED_DATA_FOOD", dataRecords);
            }

        }
        return null;
    }

    @Override
    public List<Class<? extends DataRecord>> dependsOnDataRecordType() throws DataRecordTypeNotFound {
        List<Class<? extends  DataRecord>> dependsOn = new ArrayList<>();
        dependsOn.add(DiabetesLogDataRecord.class);
        return dependsOn;
    }

    private boolean checkLastDataReport(StreamSnapshot snapshot) {

        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long passed = now - c.getTimeInMillis();
        long secondsPassed = passed / 1000; /*time now*/

        //compare now with start and end time
        String endTime = UserPreferences.getInstance().getPreference("Trip_endTime");
        Log.d(TAG, "end time " + endTime);
        String startTime = UserPreferences.getInstance().getPreference("Trip_startTime");
        Log.d(TAG, "start time " + startTime);
        if(endTime!=null && startTime!=null) {
            int endTimeInSeconds = convertHHMMtoSeconds(endTime);
            Log.d(TAG, "end time in seconds " + endTimeInSeconds);
            int startTimeInSeconds = convertHHMMtoSeconds(startTime);
            Log.d(TAG, "start time in seconds " + startTimeInSeconds);

            if (secondsPassed > endTimeInSeconds || secondsPassed < startTimeInSeconds) {
                Log.d(TAG, "Situation returning false because time now is beyond start or end time" +
                        "for the user");
                return false;
            }
            //now check if diff between time now and end time is more than 3 hours
            if((endTimeInSeconds-secondsPassed>3*3600) || (endTimeInSeconds-secondsPassed<=1*3600) ) {
                return false;
            }

        }

        Log.d(TAG, "Time now is in the range of Trip_startTime and Trip_endTime for user and the diff" +
                "between time now and end time is less than or equal to 3 hours");

        //if the number of reports submitted until now is less than a specific number
        UserSubmissionStats userSubmissionStats = InstanceManager
                .getInstance(mContext)
                .getUserSubmissionStats();
        //get the total count for the day (assuming the stats is for the day)
        if(userSubmissionStats!=null) {
            int relevantStats = userSubmissionStats.getFoodCount() + userSubmissionStats.getGlucoseReadingCount() +
                    userSubmissionStats.getInsulinCount();
            if (relevantStats < 6) {
                Log.d(TAG, "Situation returning true because relevant stats are less than needed");
                return true;
            } else {
                Log.d(TAG, "Situation returning false because relevant stats are as needed");
                return false;
            }
        }
        Log.d(TAG, "User Submission stats is null");
        return false;
    }

    /**
     * Given a String in the format HH:MM, returns the number of seconds from midnight.
     * @param aTime
     * @return
     */
    private int convertHHMMtoSeconds(String aTime) {
        //atime example: "23:55" , length =5 0-1, 3-4
        int timeInseconds = 0;
        String hour =null;
        String minutes = null;
        if(aTime!=null) {
            String[] time = aTime.split(":");
            if(time.length>0) {
                hour = time[0];
                timeInseconds = timeInseconds + Integer.valueOf(hour)*3600;
            }
            if(time.length>1) {
                minutes = time[1];
                timeInseconds = timeInseconds + Integer.valueOf(minutes)*60;
            }
            Log.d(TAG, "hour: " + hour + "minutes: " + minutes);
        }
        return timeInseconds;
    }
}