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
import java.util.List;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;

/**
 * UVShadow stores the reported or desired state of the vehicle.
 * 
 * Ground control client applications 'talk' to the shadow.
 * 
 * @author Pavel Bobov
 *
 */
public interface UVShadow {

    /**
     * Resets values of all parameters.
     * 
     * @param sysId  system id
     * @param params values of all on-board parameters
     * @throws IOException parameter loading failed
     */
    void setParams(int sysId, List<msg_param_value> params) throws IOException;

    /**
     * Returns PARAM_VALUE MAVLink message for the specified parameter.
     * 
     * @param sysId      system id
     * @param paramId    On-board parameter id
     * @param paramIndex Parameter index. Send -1 to use the paramId field as
     *                   identifier, else the paramId will be ignored
     * @return MAVLink packet with parameter value or null, if the parameter was not
     *         found.
     * @throws IOException I/O operation failed
     */
    msg_param_value getParamValue(int sysId, String paramId, short paramIndex) throws IOException;

    /**
     * Sets value of the specified parameter.
     * 
     * @param sysId     system id
     * @param parameter On-board parameter
     * @throws IOException I/O operation failed
     */
    void setParam(int sysId, msg_param_set parameter) throws IOException;

    /**
     * Returns list of on-board parameters.
     * 
     * @param sysId system id
     * @return list of on-board parameters
     * @throws IOException I/O operation failed
     */
    List<msg_param_value> getParams(int sysId) throws IOException;

    /**
     * Returns desired mission items.
     * 
     * @return desired mission items
     * @throws IOException
     */
    List<msg_mission_item> getDesiredMission() throws IOException;

    /**
     * Sets the specified mission.
     * 
     * @param sysId   system id
     * @param mission list of mission items
     * @throws IOException I/O operation failed
     */
    void setMission(int sysId, List<msg_mission_item> mission) throws IOException;

    /**
     * Returns mission items.
     * 
     * @param sysId system id
     * @return mission items in the reported state
     * @throws IOException I/O operation failed
     */
    List<msg_mission_item> getMission(int sysId) throws IOException;

    /**
     * Updates the reported state of the vehicle.
     * 
     * @param state reported state of the vehicle
     * @throws IOException I/O operation failed
     */
    void updateReportedState(StateReport state) throws IOException;

    /**
     * Returns the last reported state.
     * 
     * @param sysId system id
     * @return the last reported state or null if there were no state reports.
     * @throws IOException I/O operation failed
     */
    StateReport getLastReportedState(int sysId) throws IOException;
}