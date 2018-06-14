package labelingStudy.nctu.minuku_2.model;


import java.util.Date;

import labelingStudy.nctu.minukucore.model.LocationBasedDataRecord;

/**
 * Created by shriti on 10/8/16.
 */

public class DiabetesLogDataRecord implements LocationBasedDataRecord {

    public String glucoseImageBase64Data;
    public String foodImageBase64Data;

    public float carbsConsumed;
    public float basalInsulin;
    public float bolusInsulin;

    public String note;

    public int bgNumber;

    public long creationTime;

    /**
     * String representation of location in lat, long or
     * name of a place if semantic location is available.
     */
    public String location;

    public DiabetesLogDataRecord() {

    }

    public DiabetesLogDataRecord(String glucoseImage,
                                 String foodImage,
                                 int bgNumber,
                                 float carbsConsumed,
                                 float basalInsulin,
                                 float bolusInsulin,
                                 String note,
                                 String location) {
        this.glucoseImageBase64Data = glucoseImage;
        this.foodImageBase64Data = foodImage;
        this.bgNumber = bgNumber;
        this.carbsConsumed = carbsConsumed;
        this.basalInsulin = basalInsulin;
        this.bolusInsulin = bolusInsulin;
        this.note = note;
        this.creationTime = new Date().getTime();
        this.location = location;
    }

    public String getGlucoseImageBase64Data() {
        return glucoseImageBase64Data;
    }

    public String getFoodImageBase64Data() {
        return foodImageBase64Data;
    }

    public float getCarbsConsumed() {
        return carbsConsumed;
    }

    public float getBasalInsulin() {
        return basalInsulin;
    }

    public float getBolusInsulin() {
        return bolusInsulin;
    }

    public String getNote() {
        return note;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public String getLocation() {
        return this.location == null ? "" : this.location;
    }

    public int getBgNumber() {
        return bgNumber;
    }

    public void setBgNumber(int bgNumber) {
        this.bgNumber = bgNumber;
    }
}
