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

import android.util.Log;

import java.util.Collection;
import java.util.LinkedList;

import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * Created by Neeraj Kumar on 7/17/16.
 *
 * AbstractStream which also acts as an evicting queue with a maxSize.
 * The evicting queue implementation has a maxSize. Once the queue reaches this maxSize, any new
 * elements added to the queue lead to elements being removed(evicted) from the front of the queue.
 */
public abstract class AbstractStream<T extends DataRecord>
        extends LinkedList<T>
        implements Stream<T> {

    protected int maxSize;

    public AbstractStream(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(T object) {
        if (this.size() == maxSize) {
            this.removeFirst();
        }
        return super.add(object);
    }

    @Override
    public void add(int location, T object) {
        if (this.size() == maxSize) {
            this.removeFirst();
        }
        super.add(location, object);
    }

    @Override
    public boolean addAll(Collection<? extends T> objects) {
        final int neededSize = size() + objects.size();
        final int overflowSize = neededSize - maxSize;
        if (overflowSize > 0) {
            removeRange(0, overflowSize);
        }
        return super.addAll(objects);
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> objects) {
        Log.d("AbstractStream", "Add all called on location :" + location);
        return super.addAll(location, objects);
    }

    @Override
    public void addFirst(T object) {
        Log.e("AbstractStream", "Cannot add to the starting of the queue in abstract stream.");
        throw new UnsupportedOperationException(
                "Cannot add to the starting of the queue in abstract stream.");
    }

    @Override
    public void addLast(T object) {
        this.add(object);
    }

    @Override
    public T getCurrentValue() {
        return (this.size() >= 2 ? this.getLast() : null);
    }

    @Override
    public T getPreviousValue() {
        return this.size() > 1 ? this.get(this.size() - 2): null;
    }

}
