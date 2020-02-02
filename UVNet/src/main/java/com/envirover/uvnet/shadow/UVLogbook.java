/*
 * Envirover confidential
 * 
 * [2020] Envirover
 * All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains the property of 
 * Envirover and its suppliers, if any.  The intellectual and technical concepts
 * contained herein are proprietary to Envirover and its suppliers and may be 
 * covered by U.S. and Foreign Patents, patents in process, and are protected
 * by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Envirover.
 */

package com.envirover.uvnet.shadow;

import java.io.IOException;
import java.util.List;

import com.MAVLink.common.msg_log_entry;

/**
 * Vehicle's state report log operations.
 */
public interface UVLogbook {
    /**
     * Adds the report to the state report log.
     * 
     * @param state state report received from the vehicle
     * @param times Unix Epoch report time
     * @throws IOException I/O operation failed
     */
    void addReportedState(StateReport state) throws IOException;

    /**
     * Retrieves state reports for the specified system.
     * 
     * @param sysId     MAVLink system id
     * @param startTime minimum reported time. No minimum limit if 'null'.
     * @param endTime   maximum reported time. No maximum time limit if 'null'.
     * @param top       maximum number of reported points returned
     * @return List of <timestamp, state report> pairs
     * @throws IOException in case of I/O exception
     */
    List<StateReport> getReportedStates(int sysId, Long startTime, Long endTime, int top)
            throws IOException;

    /**
     * Returns list of available state report logs for the specified system.
     * 
     * @param sysId system ID
     * @return list of available logs
     * @throws IOException I/O operation failed
     */
    List<msg_log_entry> getLogs(int sysId) throws IOException;

    /**
     * Erases all state report logs for the specified system.
     * 
     * @param sysId system ID
     * @throws IOException I/O error
     */
    void eraseLogs(int sysId) throws IOException;
}