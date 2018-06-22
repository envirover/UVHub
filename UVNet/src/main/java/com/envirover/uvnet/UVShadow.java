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

package com.envirover.uvnet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_value;

/**
 * Stores the reported and the desired states of the vehicle.
 * 
 * Ground control client applications 'talk' to the shadow. 
 * 
 * The shadow stores desired and reported lists of mission items. 
 * Mission items are copied from the desired to the reported state when the 
 * mission is accepted by the vehicle.
 * 
 * The shadow initially stores parameters loaded from a static file. 
 * The parameter values should be updated in the shadow after the
 * parameter changes are acknowledged by the vehicle.
 * 
 * The reported state of the vehicle is stored as HIGH_LATENCY MAVLink message 
 * structure and is updated when mobile-originated messages are received from 
 * the vehicle. 
 * 
 * @author Pavel Bobov
 *
 */
public interface UVShadow {

	/**
	 * Loads parameters from the input stream into the shadow.
	 * 
	 * @param stream input stream in QGroundControl parameters file format.
	 * @throws IOException parameter loading failed. 
	 */
	void loadParams(InputStream stream) throws IOException;

	/**
	 * Returns PARAM_VALUE message for the specified parameter.
	 *   
	 * @param paramId On-board parameter id
	 * @param paramIndex Parameter index. Send -1 to use the paramId field as identifier, else the paramId will be ignored
	 * @return MAVLink packet with parameter value or null, if the parameter was not found.
	 */
	msg_param_value getParamValue(String paramId, short paramIndex);

	/**
	 * Sets value of the specified parameter.
	 * 
	 * @param paramId On-board parameter id
	 * @param value Parameter value
	 */
	void setParamValue(String paramId, Float value);

	/**
	 * Returns list of on-board parameters.
	 * 
	 * @return list of on-board parameters
	 */
	List<msg_param_value> getParams();
	
	/**
	 * Returns number of mission items in the desired state.
	 * 
	 * @return number of mission items in the desired state
	 */
	int getDesiredMissionCount();

	/**
	 * Sets number of mission items in the desired state.
	 * 
	 * @param count number of mission items in the desired state
	 */
	void setDesiredMissionCount(int count);

	/**
	 * Sets mission item in the desired state. 
	 * 
	 * @param mission mision item
	 */
	void setMissionItem(msg_mission_item mission);

	/**
	 * Returns number of mission items in the reported state.
	 * 
	 * @return number of mission items in the reported state
	 */
	int getReportedMissionCount();

	/**
	 * Returns mission item with the specified index from the reported state.
	 * 
	 * @param index mission item index
	 * @return mission item from the reported state
	 */
	msg_mission_item getReportedMissionItem(int index);

	/**
	 * Copies mission items from the desired to the reported state.
	 */
	void missionAccepted();

	/**
	 * Updates reported state of the vehicle with MAVLink message packet received from the vehicle. 
	 * 
	 * @param packet MAVLink message packet received from the vehicle
	 */
	void updateReportedState(MAVLinkPacket packet);

	/**
	 * Returns HIGH_LATENCY MAVLink message that summarizes reported state of the vehicle.
	 * 
	 * @return
	 */
	msg_high_latency getHighLatencyMessage();
}