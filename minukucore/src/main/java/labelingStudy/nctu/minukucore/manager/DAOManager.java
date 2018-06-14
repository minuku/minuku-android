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

import labelingStudy.nctu.minukucore.dao.DAO;
import labelingStudy.nctu.minukucore.model.DataRecord;

/**
 * The DAOManager keeps a mapping of {@link DataRecord DataRecord} type to
 * {@link DAO DataAccessObject} responsible for CRUD operations on the former.
 *
 * Created by neerajkumar on 7/13/16.
 */
public interface DAOManager {
    /**
     * Given the class of a {@link DataRecord}, returns the DAO associated with it.
     *
     * @param dataRecordType Class of {@link DataRecord}
     * @param <T> A subclass of {@link DAO}
     * @param <D> A subclass of {@link DataRecord}
     * @return
     */
    public <T extends DAO<D>, D extends DataRecord> T getDaoFor(Class<D> dataRecordType);

    /**
     * Register a DAO for a specific DataRecord type.
     *
     * @param dataRecordType Class of {@link DataRecord} with which a specific DAO is associated and
     *                       responsible for.
     * @param dao {@link DAO} object that is responsible for CRUD operations on a DataRecord class.
     * @param <T> A subclass of {@link DAO}
     * @param <D> A subclass of {@link DataRecord}
     */
    public <T extends DAO<D>, D extends DataRecord> void registerDaoFor(Class<D> dataRecordType,
                                                                     T dao);
}
