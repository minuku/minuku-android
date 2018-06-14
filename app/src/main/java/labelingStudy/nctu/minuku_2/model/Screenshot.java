package labelingStudy.nctu.minuku_2.model;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by shriti on 12/9/16.
 */

public class Screenshot implements DataRecord{

    public long creationTime;
    public String screenshotBase64;

    public Screenshot() {
    }

    public Screenshot(String screenshotBase64) {
        this.creationTime = new Date().getTime();
        this.screenshotBase64 = screenshotBase64;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getScreenshotBase64() {
        return screenshotBase64;
    }

    public void setScreenshotBase64(String screenshotBase64) {
        this.screenshotBase64 = screenshotBase64;
    }
}
