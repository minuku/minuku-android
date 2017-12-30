package labelingStudy.nctu.minuku_2.stream;

import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku_2.model.SessionDataRecord;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.stream.AbstractStreamFromDevice;

/**
 * Created by Lawrence on 2017/12/9.
 */

public class SessionStream extends AbstractStreamFromDevice<SessionDataRecord> {

    public SessionStream(int maxSize) {
        super(maxSize);
    }

    @Override
    public List<Class<? extends DataRecord>> dependsOnDataRecordType() {
        return new ArrayList<>();
    }
}
