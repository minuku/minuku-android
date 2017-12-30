package labelingStudy.nctu.minuku_2.stream;

import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku_2.model.CheckAndRemindDataRecord;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.stream.AbstractStreamFromDevice;

/**
 * Created by Lawrence on 2017/10/4.
 */

public class CheckAndRemindStream extends AbstractStreamFromDevice<CheckAndRemindDataRecord> {

    public CheckAndRemindStream(int maxSize) {
        super(maxSize);
    }

    @Override
    public List<Class<? extends DataRecord>> dependsOnDataRecordType() {
        return new ArrayList<>();
    }
}
