package labelingStudy.nctu.minuku_2.model;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by shriti on 11/1/16.
 */

public class TimelinePatchDataRecord implements DataRecord {

    public long creationTime;
    private String editedNotes;
    private Class associatedDataRecordType;
    private long associatedDtaRecordCreationTime;

    public  TimelinePatchDataRecord() {

    }

    public TimelinePatchDataRecord(String editedNotes, Class associatedDataRecordType, long associatedDtaRecordCreationTime) {
        this.creationTime = new Date().getTime();
        this.editedNotes = editedNotes;
        this.associatedDataRecordType = associatedDataRecordType;
        this.associatedDtaRecordCreationTime = associatedDtaRecordCreationTime;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getEditedNotes() {
        return editedNotes;
    }

    public Class getAssociatedDataRecordType() {
        return associatedDataRecordType;
    }

    public long getAssociatedDtaRecordCreationTime() {
        return associatedDtaRecordCreationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setEditedNotes(String editedNotes) {
        this.editedNotes = editedNotes;
    }

    public void setAssociatedDataRecordType(Class associatedDataRecordType) {
        this.associatedDataRecordType = associatedDataRecordType;
    }

    public void setAssociatedDtaRecordCreationTime(long associatedDtaRecordCreationTime) {
        this.associatedDtaRecordCreationTime = associatedDtaRecordCreationTime;
    }
}
