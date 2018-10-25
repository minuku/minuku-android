package labelingStudy.nctu.minuku.model.DataRecord;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2017/9/6.
 */

public class AccessibilityDataRecord implements DataRecord {

    public long creationTime;
    private String pack;
    private String text;
    private String type;
    private String extra;
    private String sessionid;

    public AccessibilityDataRecord(){
        this.creationTime = new Date().getTime();
    }

    public AccessibilityDataRecord(String pack, String text, String type, String extra, String sessionid){
        this.creationTime = new Date().getTime();

        this.pack = pack;
        this.text = text;
        this.type = type;
        this.extra = extra;
        this.sessionid = sessionid;
    }


    public AccessibilityDataRecord(String pack, String text, String type, String extra, long detectedTime, String sessionid){
        this.creationTime = detectedTime;

        this.pack = pack;
        this.text = text;
        this.type = type;
        this.extra = extra;
        this.sessionid = sessionid;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getPack(){
        return pack;
    }

    public String getText(){
        return text;
    }

    public String getType(){
        return type;
    }

    public String getExtra(){
        return extra;
    }

    public String getSessionid() {
        return sessionid;
    }
}
