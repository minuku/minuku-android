/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minukucore.stream;

import java.util.List;
import java.util.Queue;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by shriti on 7/9/16.
 * A Stream is a collection of DataRecords
 * @see labelingStudy.nctu.minukucore.model.DataRecord
 * @version 1.0
 */
public interface Stream<T extends DataRecord> extends Queue<T> {

    /**
     * Defining stream types
     */
    public enum StreamType{
        FROM_DEVICE, FROM_USER, FROM_QUESTION
    }

    /**
     * Fetch the current value of the stream
     *
     * @return the value of the newest DataRecord (T) in the stream
     */
    public T getCurrentValue();

    /**
     * Fetch the previous value of the stream - the older current value
     *
     * @return the value of the DataRecord right after the newest DataRecord
     */
    public T getPreviousValue();

    /**
     * Fetch a list of DataRecord types that this stream
     * uses as inputs to create a new stream
     *
     * @return the list of DataRecord types
     */
    public List<Class<? extends DataRecord>> dependsOnDataRecordType();

    /**
     * Get the type of stream - from_device, from_user, from_question
     * @return the type of stream
     *         {@link labelingStudy.nctu.minukucore.stream.Stream.StreamType}
     */
    public StreamType getType();

}
