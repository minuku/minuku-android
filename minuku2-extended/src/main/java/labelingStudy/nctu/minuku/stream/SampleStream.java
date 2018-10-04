package labelingStudy.nctu.minuku.stream;

import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.SampleDataRecord;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.stream.AbstractStreamFromDevice;

/**
 * Created by Marvin on 2018/09/26.
 * If a developer desires to collect data which is aside from the existent data record, the developer might have to program a new streamGenerator
 * to make a streamGenerator work properly, the relative stream and dataRecord are necessary, while sometimes need to program manager classes as well if the desired data is complicated
 * Stream can be view as a container also collections of relative dataRecord.
 */
public class SampleStream extends AbstractStreamFromDevice<SampleDataRecord> {

    /**
     * Define a constructor with initializing mMaxSize
     * @param maxSize
     */
    public SampleStream(int maxSize) {
        super(maxSize);
    }

    /**
     * Override the function
     * @return
     */
    @Override
    public List<Class<? extends DataRecord>> dependsOnDataRecordType() {
        return new ArrayList<>();
    }
}

