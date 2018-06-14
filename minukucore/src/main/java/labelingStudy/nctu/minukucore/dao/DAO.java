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

package labelingStudy.nctu.minukucore.dao;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.user.User;

/**
 * DAO interface expects the DAO class to provide methods for
 * CRUD operations on a DataRecord.
 *
 * Created by Neeraj Kumar on 7/12/2016.
 */
public interface DAO<T extends DataRecord> {

    /**
     * Set the device details and user details for this DAO.
     * Each DataRecord must be associated with a user and device.
     *
     * @param user The {@link labelingStudy.nctu.minukucore.user.User user}
     *             for whom the DataRecords are generated.
     * @param uuid The UUID of the device which the user is on,
     *             when such a DataRecord was generated.
     */
    public void setDevice(User user, UUID uuid);

    /**
     * Add a new entity to persistent data.
     *
     * @param entity
     * @throws DAOException
     */
    public void add(T entity) throws DAOException;

    /**
     * Delete an entity from the persistent data.
     *
     * @param entity
     * @throws DAOException
     */
    public void delete(T entity) throws DAOException;

    /**
     * Get all entities as a list.
     *
     * @return A {@link Future} that promise a list of all
     * entities of this type, related to the user,  in the persistent storage.
     * @throws DAOException
     */
    public Future<List<T>> getAll() throws DAOException;

    /**
     * Get the last {@param N} entities as a list.
     *
     * @param N the number of records to pull from the persistent storage.
     * @return A {@link Future} of the last N DataRecords for this DAO.
     * @throws DAOException
     */
    public Future<List<T>> getLast(int N) throws DAOException;

    /**
     * Update an old entity with the new entity, without modifying it's location in the
     * list.
     *
     * @param oldEntity
     * @param newEntity
     * @throws DAOException
     */
    public void update(T oldEntity, T newEntity) throws DAOException;

}
