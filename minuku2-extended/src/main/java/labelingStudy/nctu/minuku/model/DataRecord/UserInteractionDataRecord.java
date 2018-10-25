package labelingStudy.nctu.minuku.model.DataRecord;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Lawrence on 2018/8/29.
 */

public class UserInteractionDataRecord implements DataRecord {

    public long creationTime;

    private String present;
    private String unlock;
    private String background;
    private String foreground;

    public UserInteractionDataRecord(String present, String unlock, String background, String foreground){
        this.creationTime = new Date().getTime();

        this.present = present;
        this.unlock = unlock;
        this.background = background;
        this.foreground = foreground;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getUnlock() {
        return unlock;
    }

    public void setUnlock(String unlock) {
        this.unlock = unlock;
    }

    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getForeground() {
        return foreground;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }



}
