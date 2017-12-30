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

package labelingStudy.nctu.minukucore.manager;

import labelingStudy.nctu.minukucore.event.IsDataExpectedEvent;
import labelingStudy.nctu.minukucore.event.NoDataChangeEvent;
import labelingStudy.nctu.minukucore.event.StateChangeEvent;
import labelingStudy.nctu.minukucore.exception.DataRecordTypeNotFound;
import labelingStudy.nctu.minukucore.model.StreamSnapshot;
import labelingStudy.nctu.minukucore.situation.Situation;

/**
 * Created by shriti on 7/9/16.
 * A registry of all the {@link labelingStudy.nctu.minukucore.situation.Situation} situations
 * contains the logic to call relevant registered situations when a state change event is received
 *
 */
public interface SituationManager {

    /**
     * Called by {@link labelingStudy.nctu.minukucore.manager.StreamManager}.
     * Sends request to appropriate situations
     * Note: does not subscribe to any event on the event bus
     * @param s {@link labelingStudy.nctu.minukucore.model.StreamSnapshot}
     *          a snapshot of the stream with current and previous values
     *          at the time of state change.
     * @param event state change event transferred by StreamManager
     *              contains the type of data for which the state changed
     *              the situations associated with this data type will be called
     */
    public void onStateChange(StreamSnapshot s, StateChangeEvent event);


    /**
     * Called by {@link labelingStudy.nctu.minukucore.manager.StreamManager}.
     * Tells every situation interested in the {@link NoDataChangeEvent#eventType}
     * that an expected data entry did NOT occur.
     * @param aNoDataChangeEvent
     */
    public void onNoDataChange(StreamSnapshot snapshot, NoDataChangeEvent aNoDataChangeEvent);

    /**
     * This method is called by {@link StreamManager}. Tells every Situations interested in the
     * {@link IsDataExpectedEvent#eventType} that a
     * {@link labelingStudy.nctu.minukucore.model.DataRecord} entry is expected.
     * @param aIsDataExpectedEvent
     */
    public void onIsDataExpected(StreamSnapshot s, IsDataExpectedEvent aIsDataExpectedEvent);


    /**
     * Register a situation after checking for the existence of all the streams
     * that the situation depends on. Add situation to the situation manager's registry.
     * Whenever a {@link Situation} calls the register method, this Manager would call the
     * {@link Situation#dependsOnDataRecordType()} method of the Situation trying to register
     *      before it can successfully add that Situation to the registry.
     * @param s the situation requesting to be registered
     * @return true if registration is successful
     * @throws DataRecordTypeNotFound exception when the any of the
     *      {@link labelingStudy.nctu.minukucore.model.DataRecord}s that the Situation depends on
     *      are not registered with the StreamManager.
     */
    public <T extends Situation> boolean register(T s) throws DataRecordTypeNotFound;

    /**
     * Unregister a situation. Remove situation from registry.
     * @param s the situation requesting to be unregistered
     * @return true if the unregistration is successful
     */
    public boolean unregister(Situation s);
}
