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

package labelingStudy.nctu.minuku.model.DataRecord;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.LocationBasedDataRecord;

/**
 * Created by shriti on 7/21/16.
 * Code reused from intel's mood map
 *
 * see: https://github.com/ohmage/mood-map-android
 */
public class MoodDataRecord implements LocationBasedDataRecord {

    /**
     * X axis to draw
     */
    public float x = -50f;
    /**
     * Y axis to draw
     */
    public float y = -50f;
    /**
     * Checking flag while drag the mood
     */
    public boolean isSelected = false;
    /**
     * Checking flag while Inserting and Posting the mood
     */
    public boolean isCreated = false;

    //Actual values, X and Y range from -10 to +10, X-Mood, Y-Energy
    /**
     * X Possition on 10/10 graph
     */
    public float moodLevel = -50;
    /**
     * Y Possition on 10/10 graph
     */
    public float energyLevel = -50;

    public long creationTime;

    /**
     * String representation of location in lat, long or
     * name of a place if semantic location is available.
     */
    public String location;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public boolean getIsCreated() {
        return isCreated;
    }

    public float getMoodLevel() {
        return moodLevel;
    }

    public float getEnergyLevel() {
        return energyLevel;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime =creationTime;
    }

    public MoodDataRecord(){

    }

    public MoodDataRecord(float x, float y, boolean isSelected, boolean isCreated, float moodLevel,
                          float energyLevel, String location) {
        this.x = x;
        this.y = y;
        this.isSelected = isSelected;
        this.isCreated = isCreated;
        this.moodLevel = moodLevel;
        this.energyLevel = energyLevel;
        this.creationTime = new Date().getTime();
        this.location = location;
    }

    @Override
    public String getLocation() {
        return this.location == null ? "" : this.location;
    }

    /**todo: a) add a simple annotation after selecting mood, OR
     b)compare with last saved mood and ask why did your mood change this way?, OR
     c)if it is a negative mood that is sellected, ask what happened?
     Put this as a situation??**/
}
