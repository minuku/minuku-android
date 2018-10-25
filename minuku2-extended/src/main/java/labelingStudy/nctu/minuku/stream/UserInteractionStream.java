package labelingStudy.nctu.minuku.stream;

import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.UserInteractionDataRecord;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.stream.AbstractStreamFromDevice;

/**
 * Created by Lawrence on 2018/8/29.
 */

public class UserInteractionStream extends AbstractStreamFromDevice<UserInteractionDataRecord> {
    public UserInteractionStream(int maxSize) {
        super(maxSize);
    }

    @Override
    public List<Class<? extends DataRecord>> dependsOnDataRecordType() {
        return new ArrayList<>();
    }
}
