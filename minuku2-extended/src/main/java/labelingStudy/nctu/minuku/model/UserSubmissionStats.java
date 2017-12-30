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

package labelingStudy.nctu.minuku.model;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by neerajkumar on 8/22/16.
 */
public class UserSubmissionStats implements DataRecord {

    public int questionCount;
    public int totalImageCount;
    public int glucoseReadingCount;
    public int insulinCount;
    public int foodCount;
    public int othersCount;
    public long creationTime;
    public int totalSubmissionCount;
    public int moodCount;

    public int getMoodCount() {
        return moodCount;
    }

    public void setMoodCount(int moodCount) {
        this.moodCount = moodCount;
    }

    public void incrementMoodCount() {
        this.moodCount++;
        this.totalSubmissionCount++;
    }

    public UserSubmissionStats() {
        this.creationTime = new Date().getTime();
        this.questionCount =0;
        this.moodCount =0;
        this.totalImageCount=0;
        this.totalSubmissionCount=0;
        this.glucoseReadingCount=0;
        this.insulinCount=0;
        this.foodCount=0;
        this.othersCount=0;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public void incrementQuestionCount() {
        this.questionCount++;
        this.totalSubmissionCount++;
    }

    public void incrementTotalImageCount() {
        this.totalImageCount++;
        this.totalSubmissionCount++;
    }

    public void incrementGlucoseReadingCount() {
        this.glucoseReadingCount++;
        this.totalSubmissionCount++;
    }

    public void incrementInsulinCount() {
        this.insulinCount++;
        this.totalSubmissionCount++;
    }

    public void incrementFoodCount() {
        this.foodCount++;
        this.totalSubmissionCount++;
    }

    public void incrementOtherImagesCount() {
        this.othersCount++;
        this.totalSubmissionCount++;
    }


    /**
     *
     * @param creationTime
     */
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public void setTotalImageCount(int totalImageCount) {
        this.totalImageCount = totalImageCount;
    }

    public void setGlucoseReadingCount(int glucoseReadingCount) {
        this.glucoseReadingCount = glucoseReadingCount;
    }

    public void setInsulinCount(int insulinCount) {
        this.insulinCount = insulinCount;
    }

    public void setFoodCount(int foodCount) {
        this.foodCount = foodCount;
    }

    public void setOthersCount(int othersCount) {
        this.othersCount = othersCount;
    }

    public void setTotalSubmissionCount(int totalSubmissionCount) {
        this.totalSubmissionCount = totalSubmissionCount;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public int getTotalImageCount() {
        return totalImageCount;
    }

    public int getGlucoseReadingCount() {
        return glucoseReadingCount;
    }

    public int getInsulinCount() {
        return insulinCount;
    }

    public int getFoodCount() {
        return foodCount;
    }

    public int getOthersCount() {
        return othersCount;
    }

    public int getTotalSubmissionCount() {
        return totalSubmissionCount;
    }
}
