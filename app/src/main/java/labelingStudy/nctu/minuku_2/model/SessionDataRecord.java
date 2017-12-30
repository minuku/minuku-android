package labelingStudy.nctu.minuku_2.model;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/12/9.
 */

public class SessionDataRecord implements DataRecord {

    public long creationTime;
    private String sessionid;

    public SessionDataRecord(int sessionid){
        this.creationTime = new Date().getTime();
        this.sessionid = String.valueOf(sessionid);
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getSessionid() {
        return sessionid;
    }
}
