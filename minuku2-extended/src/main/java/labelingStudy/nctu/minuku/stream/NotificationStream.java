package labelingStudy.nctu.minuku.stream;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.stream.AbstractStreamFromDevice;

/**
 * Created by chiaenchiang on 27/10/2018.
 */

public class NotificationStream extends AbstractStreamFromDevice<NotificationDataRecord> {
    public NotificationStream(int maxSize) {
        super(maxSize);
    }

    @Override
    public List<Class<? extends DataRecord>> dependsOnDataRecordType() {
        return new ArrayList<>();
    }

    @Override
    public void replaceAll(UnaryOperator<NotificationDataRecord> operator) {

    }

    @Override
    public void sort(Comparator<? super NotificationDataRecord> c) {

    }

    @Override
    public boolean removeIf(Predicate<? super NotificationDataRecord> filter) {
        return false;
    }

    @Override
    public Stream<NotificationDataRecord> stream() {
        return null;
    }

    @Override
    public Stream<NotificationDataRecord> parallelStream() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super NotificationDataRecord> action) {

    }
}
