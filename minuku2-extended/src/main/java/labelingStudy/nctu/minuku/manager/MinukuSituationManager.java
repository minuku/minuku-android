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

package labelingStudy.nctu.minuku.manager;


import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minukucore.event.ActionEvent;
import labelingStudy.nctu.minukucore.event.IsDataExpectedEvent;
import labelingStudy.nctu.minukucore.event.NoDataChangeEvent;
import labelingStudy.nctu.minukucore.event.StateChangeEvent;
import labelingStudy.nctu.minukucore.exception.DataRecordTypeNotFound;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.manager.SituationManager;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.model.StreamSnapshot;
import labelingStudy.nctu.minukucore.situation.Situation;
/**
 * Created by neerajkumar on 7/23/16.
 */
public class MinukuSituationManager implements SituationManager {

    private static final String TAG = "MinikuSituationManager";
    private Map<Class<? extends DataRecord>, HashSet<Situation>> registeredSituationMap;
    private static MinukuSituationManager instance;

    private MinukuSituationManager() {
        registeredSituationMap = new HashMap<>();
    }

    public static MinukuSituationManager getInstance() {
        if(instance == null) {
            instance = new MinukuSituationManager();
        }
        return instance;
    }

    @Override
    public void onStateChange(StreamSnapshot snapshot, StateChangeEvent aStateChangeEvent) {
        Log.d(TAG, "Calling is state change event on situation manager "
                + "Type:" + aStateChangeEvent.getType());
        if(!registeredSituationMap.containsKey(aStateChangeEvent.getType())) {
            Log.d(TAG, "Situation list for (state change)" +
                    aStateChangeEvent.getType() + " is null. Returning.");
            return;
        }
        for(Situation situation: registeredSituationMap.get(aStateChangeEvent.getType())) {
            ActionEvent actionEvent = situation.assertSituation(snapshot, aStateChangeEvent);
            if(actionEvent!=null) {
                EventBus.getDefault().post(actionEvent);
            }
        }

    }

    @Override
    public void onNoDataChange(StreamSnapshot snapshot, NoDataChangeEvent aNoDataChangeEvent) {
        Log.d(TAG, "Calling no data change event on situation manager "
                + "Type:" + aNoDataChangeEvent.getType());
        if(!registeredSituationMap.containsKey(aNoDataChangeEvent.getType())) {
            Log.d(TAG, "Situation list for (no data change)" +
                    aNoDataChangeEvent.getType() + " is null. Returning.");
            return;
        }
        for(Situation situation: registeredSituationMap.get(aNoDataChangeEvent.getType())) {
            Log.d(TAG, "Creation action event for situation " + situation.getClass());
            ActionEvent actionEvent = situation.assertSituation(snapshot, aNoDataChangeEvent);
            if(actionEvent != null) {
                EventBus.getDefault().post(actionEvent);
            }
        }
    }

    @Override
    public void onIsDataExpected(StreamSnapshot snapshot,
                                 IsDataExpectedEvent aIsDataExpectedEvent) {
        Log.d(TAG, "Calling is data expected event on situation manager "
                + "Type:" + aIsDataExpectedEvent.getType());
        if(!registeredSituationMap.containsKey(aIsDataExpectedEvent.getType())) {
            Log.d(TAG, "Situation list for (data expected)"
                    + aIsDataExpectedEvent.getType() + " is null. Returning.");
            return;
        }
        for(Situation situation: registeredSituationMap.get(aIsDataExpectedEvent.getType())) {
            ActionEvent actionEvent = situation.assertSituation(snapshot, aIsDataExpectedEvent);
            if(actionEvent != null) {
                EventBus.getDefault().post(actionEvent);
            }
        }
    }


    @Override
    public <T extends Situation> boolean register(T s) throws DataRecordTypeNotFound {

        Log.d(TAG, "Registering situation " + s.getClass().getSimpleName().toString());

        for(Class<? extends DataRecord> type: s.dependsOnDataRecordType()) {
            try {
                MinukuStreamManager.getInstance().getStreamFor(type);
                Log.d(TAG, "Registered situation successfully: " +
                        s.getClass().getSimpleName().toString());
                if(!registeredSituationMap.containsKey(type)) {
                    registeredSituationMap.put(type, new HashSet<Situation>());
                }
                return registeredSituationMap.get(type).add(s);
            } catch (StreamNotFoundException e) {
                e.printStackTrace();
                throw new DataRecordTypeNotFound();
            }
        }
        return true;
    }

    @Override
    public boolean unregister(Situation s) {
        boolean successful = true;
        for(Map.Entry<Class<? extends DataRecord>, HashSet<Situation>> entry:
                registeredSituationMap.entrySet()) {
            successful = successful && entry.getValue().remove(s);
        }
        return successful;
    }
}
