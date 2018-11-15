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

package labelingStudy.nctu.minuku.streamgenerator;

import android.content.Context;

import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.streamgenerator.StreamGenerator;

/**
 * Created by Neeraj Kumar on 7/17/16.
 *
 * This is an Android OS specific interface which extends the StreamGenerator interface. Application
 * Context would be an important requirement for any StreamGenerator which generates
 * a {@link labelingStudy.nctu.minukucore.stream.Stream} of type
 * {@link labelingStudy.nctu.minukucore.stream.Stream.StreamType#FROM_DEVICE}
 */
public abstract class AndroidStreamGenerator<T extends DataRecord>
        implements StreamGenerator<T> {

    protected Context mApplicationContext;

    public AndroidStreamGenerator(Context aApplicationContext) {
        this.mApplicationContext = aApplicationContext;
    }

    public AndroidStreamGenerator() {

    }
}
