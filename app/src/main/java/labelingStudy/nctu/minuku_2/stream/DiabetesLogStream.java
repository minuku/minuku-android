package labelingStudy.nctu.minuku_2.stream;

import java.util.Arrays;
import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku_2.model.DiabetesLogDataRecord;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.stream.AbstractStreamFromUser;

/**
 * Created by shriti on 10/8/16.
 */

public class DiabetesLogStream extends AbstractStreamFromUser<DiabetesLogDataRecord> {

    public DiabetesLogStream(int maxSize) {
        super(maxSize);
    }

    @Override
    public List<Class <? extends DataRecord>> dependsOnDataRecordType() {
        Class<? extends DataRecord>[] dependsOn = new Class[] {LocationDataRecord.class};
        return Arrays.asList(dependsOn);
    }
}
