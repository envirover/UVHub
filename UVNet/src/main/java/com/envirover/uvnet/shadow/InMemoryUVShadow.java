/*
 * Envirover confidential
 * 
 *  [2017] Envirover
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
import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_log_entry;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;

/**
 * Keeps the vehicle shadow in memory.
 * 
 * @author Pavel Bobov
 * 
 */
public class InMemoryUVShadow implements UVShadow {

    private msg_high_latency msgHighLatency = new msg_high_latency();
    private List<msg_param_value> params = new ArrayList<msg_param_value>();
    private ArrayList<msg_mission_item> mission = new ArrayList<msg_mission_item>();
    private ArrayList<msg_mission_item> desiredMission = new ArrayList<msg_mission_item>();

    protected InMemoryUVShadow() {
    }

    @Override
	public List<msg_param_value> getParams(int sysId) throws IOException {
        return params;
    }

    @Override
	public void setParams(int sysId, List<msg_param_value> params) throws IOException {
    	this.params = params;
    }

    @Override
	public msg_param_value getParamValue(int sysId, String paramId, short paramIndex)  throws IOException{
        if (paramIndex >= 0) {
            if (paramIndex >= params.size()) {
                return null;
            }

            return params.get(paramIndex);
        }

        for (msg_param_value param : params) {
            if (param.getParam_Id().equalsIgnoreCase(paramId.trim()))
                return param;
        }

        return null;
    }

    @Override
	public void setParam(int sysId, msg_param_set parameter) throws IOException{
    	for (msg_param_value param : params) {
            if (param.getParam_Id().equalsIgnoreCase(parameter.getParam_Id().trim())) {
                param.param_value = parameter.param_value;
                param.param_type = parameter.param_type;
                return;
            }
        }
        
    	// New parameter
    	msg_param_value param_value = new msg_param_value();
    	param_value.setParam_Id(parameter.getParam_Id());
        param_value.param_value = parameter.param_value;
        param_value.param_index = params.size();
        param_value.param_type = parameter.param_type;
        params.add(param_value);
        
        // Set param_count to match the actual number of parameters
        for (msg_param_value param : params) {
        	param.param_count = params.size();
        }
    }
    
    @Override
    public List<msg_mission_item> getDesiredMission() throws IOException {
    	return desiredMission;
    }
    
    @Override
	public List<msg_mission_item> getMission(int sysId) throws IOException {
    	return mission;
    }
    

    @Override
	public void setMission(int sysId, List<msg_mission_item> mission) throws IOException {
    	this.mission = new ArrayList<msg_mission_item>(mission);
    }

    @Override
	public void updateReportedState(MAVLinkMessage msg, long timestamp) throws IOException {
        if (msg.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
            msgHighLatency = (msg_high_latency)msg;
        }
    }

    @Override
	public MAVLinkMessage getLastMessage(int sysId, int msgId) throws IOException {
        return msgHighLatency;
    }

	@Override
	public List<msg_log_entry> getLogs(int sysId) throws IOException {
		return null;
	}

	@Override
	public void eraseLogs(int sysId) throws IOException {
	}
    
}
