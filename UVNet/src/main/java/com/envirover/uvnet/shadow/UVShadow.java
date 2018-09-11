/*
 * Envirover confidential
 * 
 *  [2018] Envirover
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

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_log_entry;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;

/**
 * UVShadow stores the reported state of the vehicle.
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
	 * @param sysId system id
	 * @param params values of all on-board parameters
	 * @throws IOException parameter loading failed 
	 */
	void setParams(int sysId, List<msg_param_value> params) throws IOException;

	/**
	 * Returns PARAM_VALUE MAVLink message for the specified parameter.
	 *   
	 * @param sysId system id
	 * @param paramId On-board parameter id
	 * @param paramIndex Parameter index. Send -1 to use the paramId field as identifier, else the paramId will be ignored
	 * @return MAVLink packet with parameter value or null, if the parameter was not found.
	 * @throws IOException I/O operation failed
	 */
	msg_param_value getParamValue(int sysId, String paramId, short paramIndex) throws IOException;

	/**
	 * Sets value of the specified parameter.
	 * 
	 * @param sysId system id
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
	 * @param sysId system id
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
	 * Updates reported state of the vehicle with MAVLink message received from the vehicle. 
	 * 
	 * @param msg MAVLink message packet received from the vehicle
	 * @param timestamp Unix Epoch report time 
	 * @throws IOException I/O operation failed
	 */
	void updateReportedState(MAVLinkMessage msg, long timestamp) throws IOException;

	/**
	 * Returns last reported MAVLink message of the specified type from the specified system.
	 * 
	 * @param sysId system ID
	 * @param msgId MAVLink message ID
	 * @return HIGH_LATENCY MAVLink message that summarizes reported state of the vehicle
	 * @throws IOException I/O operation failed
	 */
	MAVLinkMessage getLastMessage(int sysId, int msgId) throws IOException;
	
	/**
	 * Returns list of available logs.
	 * 
	 * @param sysId system ID
	 * @return list of available logs
	 * @throws IOException I/O operation failed
	 */
	List<msg_log_entry> getLogs(int sysId) throws IOException;
	
	/**
	 * Erases all logs.
	 * 
	 * @param sysId system ID
	 * @throws IOException I/O error
	 */
	void eraseLogs(int sysId) throws IOException;
	
}