/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.envirover.uvnet.shadow;

import java.io.IOException;
import java.util.Date;
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
    List<StateReport> getReportedStates(int sysId, Date startTime, Date endTime, int top)
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