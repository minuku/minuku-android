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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.config.UserPreferences;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuSituationManager;
import labelingStudy.nctu.minuku.model.DataRecord.MoodDataRecord;
import labelingStudy.nctu.minuku_2.event.MoodDataExpectedActionEvent;
import labelingStudy.nctu.minukucore.event.ActionEvent;
import labelingStudy.nctu.minukucore.event.IsDataExpectedEvent;
import labelingStudy.nctu.minukucore.event.MinukuEvent;
import labelingStudy.nctu.minukucore.exception.DataRecordTypeNotFound;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.model.StreamSnapshot;
import labelingStudy.nctu.minukucore.situation.Situation;

/**
 * Created by neerajkumar on 7/30/16.
 */
public class MoodDataExpectedSituation implements Situation {

    private static final String TAG = "MoodDataExpectedSituat";

    public MoodDataExpectedSituation() {
        try {
            MinukuSituationManager.getInstance().register(this);
            Log.d(TAG, "Registered successfully.");
        } catch (DataRecordTypeNotFound dataRecordTypeNotFound) {
            Log.e(TAG, "Registration failed.");
            dataRecordTypeNotFound.printStackTrace();
        }
    }

    @Override
    public <T extends ActionEvent> T assertSituation(StreamSnapshot snapshot,
                                                     MinukuEvent aMinukuEvent) {
        List<DataRecord> dataRecords = new ArrayList<>();
        if(aMinukuEvent instanceof IsDataExpectedEvent) {
            Log.d(TAG, "MinukuEvent is instance of data expected event. Checking if I should show" +
                    "notification.");
            if(shouldShowNotification()) {
                Log.d(TAG, "Should show notification returned true. Sending ActionEvent.");
                return (T) new MoodDataExpectedActionEvent("TIME_FOR_MOOD_RECORDING", dataRecords);
            }

        }
        return null;
    }

    @Override
    public List<Class<? extends DataRecord>> dependsOnDataRecordType() throws DataRecordTypeNotFound {
        List<Class<? extends  DataRecord>> dependsOn = new ArrayList<>();
        dependsOn.add(MoodDataRecord.class);
        return dependsOn;
    }

    /**
     * As per the UserPreferences for wake up and sleep time, select three times during the day
     * at which the user will be shown a prompt to register their mood.
     * @return Array of integers, where each entry in the array is the number of seconds from
     * midnight at which the mood prompt should be generated.
     */
    private int[] getTimesForNotification() {
        String startTime = UserPreferences.getInstance().getPreference("Trip_startTime");
        String endTime = UserPreferences.getInstance().getPreference("Trip_endTime");
        if(startTime == null || endTime == null) {
            return new int[0];
        }
        int[] timesForNotification = new int[3];

        int startTimeInSeconds = convertHHMMtoSeconds(startTime);
        int endTimeInSeconds = convertHHMMtoSeconds(endTime);
        int newEndTimeInSeconds = endTimeInSeconds - 140*60; //140 mins less than end time
        endTimeInSeconds = newEndTimeInSeconds;

        int partitionWindow = (endTimeInSeconds - startTimeInSeconds) / 3;
        //timesForNotification[0] = startTimeInSeconds += partitionWindow;
        /*Random randomGenerator = new Random();
        int min = partitionWindow/4;
        int max = partitionWindow;
        int randomeNumberOne = randomGenerator.nextInt(max - min) + min;
        int randomeNumberTwo = randomGenerator.nextInt(max - min) + min;
        int randomeNumberThree = randomGenerator.nextInt(max - min) + min;

        Log.d(TAG, "partition: " + partitionWindow +
                "Start time: " + startTimeInSeconds +
                "End time: " + endTimeInSeconds +
                "New end time: " + newEndTimeInSeconds +
                " #1: " + randomeNumberOne +
                " #2: " + randomeNumberTwo +
                " #3: " + randomeNumberThree);

        int randomTimeOne = startTimeInSeconds+randomeNumberOne;
        int randomTimeTwo = startTimeInSeconds+partitionWindow +randomeNumberTwo;
        int randomTimeThree = startTimeInSeconds+partitionWindow+partitionWindow+randomeNumberThree;
        Log.d(TAG, "new random times for mood:" +
                randomTimeOne + "----" +
                randomTimeTwo + "----" +
                randomTimeThree);*/

        timesForNotification[0] = startTimeInSeconds + partitionWindow/2;
        //timesForNotification[0] = randomTimeOne;
        timesForNotification[1] = startTimeInSeconds + partitionWindow + partitionWindow/2;
        //timesForNotification[1] = randomTimeTwo;
        timesForNotification[2] = startTimeInSeconds + partitionWindow + partitionWindow + partitionWindow/2;
        //timesForNotification[2] = randomTimeThree;

        Log.d(TAG, "Mood data expected at: " + timesForNotification[0] +
                ", " + timesForNotification[1] +
        ", " + timesForNotification[2]);

        return timesForNotification;
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

    /**
     * Gets all the times at which a notification needs to be shown, and check if the current time
     * is within a window of 15 minutes from that time. Returns true if that is the case, false
     * otherwise.
     * @return true if current time is within a 15 minutes window of a time at which a notification
     * needs to be shown. False otherwise.
     */
    private boolean shouldShowNotification() {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long passed = now - c.getTimeInMillis();
        long secondsPassed = passed / 1000;

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
        }
        Log.d(TAG, "Time now is in the range of Trip_startTime and Trip_endTime for user");

        for(int i:getTimesForNotification()) {
            Log.d(TAG, "Seconds passed: " + secondsPassed + "; Time: " + i);
            if(secondsPassed - i >= 0 && secondsPassed - i < Constants.MOOD_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES*60) {
                Log.d(TAG, "Situation returning true");
                return true;
            }
        }
        Log.d(TAG, "Situation returning false");
        return false;
    }
}
