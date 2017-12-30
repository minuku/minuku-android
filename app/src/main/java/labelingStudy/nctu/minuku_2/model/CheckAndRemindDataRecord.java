package labelingStudy.nctu.minuku_2.model;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/10/4.
 */

public class CheckAndRemindDataRecord implements DataRecord {
    public long creationTime;

    public CheckAndRemindDataRecord(){
        this.creationTime = new Date().getTime();

    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

}
