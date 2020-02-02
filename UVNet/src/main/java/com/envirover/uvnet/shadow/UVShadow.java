/*
 * Envirover confidential
 * 
 *  [2020] Envirover
 *  All Rights Reserved.
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
import java.util.Map.Entry;

import com.MAVLink.common.msg_high_latency;
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
     * @param msg   state report received from the vehicle
     * @param times Unix Epoch report time
     * @throws IOException I/O operation failed
     */
    void updateReportedState(msg_high_latency msg, long time) throws IOException;

    /**
     * Returns the last reported state.
     * 
     * @param sysId system id
     * @return report time and the last reported state pair or null if there were no
     *         state reports.
     * @throws IOException I/O operation failed
     */
    Entry<Long, msg_high_latency> getLastReportedState(int sysId) throws IOException;
}