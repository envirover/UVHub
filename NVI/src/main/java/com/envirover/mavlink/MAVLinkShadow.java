/*
This file is part of SPLGroundControl application.

SPLGroundControl is a MAVLink proxy server for ArduPilot rovers with
RockBLOCK satellite communication.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLGroundControl is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SPLGroundControl.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.mavlink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.enums.MAV_PARAM_TYPE;

/**
 * Keeps the reported and the desired states of the vehicle.
 * 
 * Ground control client application 'talk' to the shadow. 
 * 
 * The actual state of the vehicle is updated during communication sessions. 
 * 
 */
public class MAVLinkShadow {

    private static int SYS_ID = 1;
    private static int COMP_ID = 1;

    private static String HL_REPORT_PERIOD_PARAM = "HL_REPORT_PERIOD";
    private static float  DEFAULT_HL_REPORT_PERIOD = 300.0F;

    private static MAVLinkShadow instance = null;

    private final msg_high_latency msgHighLatency = new msg_high_latency();
    private ArrayList<msg_param_value> params = new ArrayList<msg_param_value>();
    private ArrayList<msg_mission_item> reportedMissions = new ArrayList<msg_mission_item>();
    private ArrayList<msg_mission_item> desiredMissions = new ArrayList<msg_mission_item>();    
    private int desiredMissionCount = 0;

    protected MAVLinkShadow() {
        msgHighLatency.sysid = SYS_ID;
        msgHighLatency.compid = COMP_ID;
    }

    public static MAVLinkShadow getInstance() {
        if(instance == null) {
           instance = new MAVLinkShadow();
        }

        return instance;
    }

    public void loadParams(InputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Invalid parameters stream.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String str;
        int index = 0;
        boolean hlReportPeriodParamFound = false;
        while ((str = reader.readLine()) != null) {
            if (!str.isEmpty() && !str.startsWith("#")) {
                String[] tokens = str.split("\t");
                if (tokens.length >= 5) { 
                    msg_param_value param = new msg_param_value();
                    param.sysid = Integer.valueOf(tokens[0]);
                    param.compid = Integer.valueOf(tokens[1]);
                    param.setParam_Id(tokens[2].trim());
                    param.param_index = index; 
                    param.param_value = Float.valueOf(tokens[3]);
                    param.param_type = Short.valueOf(tokens[4]);
                    params.add(index, param);
                    index++;
                    if (HL_REPORT_PERIOD_PARAM.equals(param.getParam_Id())) {
                        hlReportPeriodParamFound = true;
                    }
                }
            }
        }

        if (!hlReportPeriodParamFound) {
            // Add HL_REPORT_PERIOD parameter
            msg_param_value param = new msg_param_value();
            param.sysid = 1;
            param.compid = 190;
            param.setParam_Id(HL_REPORT_PERIOD_PARAM);
            param.param_index = index; 
            param.param_value = DEFAULT_HL_REPORT_PERIOD;
            param.param_type = MAV_PARAM_TYPE.MAV_PARAM_TYPE_REAL32;
            params.add(index, param);
            index++;
        }

        // Set param_count for all the parameters.
        for (int i = 0; i < index; i++) {
            params.get(i).param_count = index;
        }
    }

    /**
     * Returns PARAM_VALUE message for the specified parameter.
     *   
     * @param paramid Onboard parameter id, terminated by NULL if the length is less than 16 human-readable chars and WITHOUT null termination (NULL) byte if the length is exactly 16 chars - applications have to provide 16+1 bytes storage if the ID is stored as string
     * @param paramIndex Parameter index. Send -1 to use the paramId field as identifier, else the paramId will be ignored
     * @return MAVLink packet with parameter value or null, if the parameter was not found.
     */
    public msg_param_value getParamValue(String paramId, short paramIndex) {
        if (paramIndex >= 0) {
            return params.get(paramIndex);
        }

        for (msg_param_value param : params) {
            if (param.getParam_Id().equalsIgnoreCase(paramId.trim()))
                return param;
        }

        return null;
    }

    public void setParamValue(String paramId, Float value) {
        for (msg_param_value param : params) {
            if (param.getParam_Id().equalsIgnoreCase(paramId.trim())) {
                param.param_value = value;
                break;
            }
        }
    }
    
    public int getDesiredMissionCount() {
        return desiredMissionCount;
    }

    public void setDesiredMissionCount(int count) {
        desiredMissionCount = count;
        desiredMissions = new ArrayList<msg_mission_item>(count);
    }

    public void setMissionItem(msg_mission_item mission) {
        if (mission.seq >= desiredMissions.size()) {
            desiredMissions.add(mission.seq, mission); 
        } else {
            desiredMissions.set(mission.seq, mission);
        }
    }

    public int getReportedMissionCount() {
    	return reportedMissions.size();
    }
    
    public msg_mission_item getReportedMissionItem(int index) {
        return reportedMissions.get(index);
    }
    
    public void missionAccepted() {
    	reportedMissions = desiredMissions;
    }

    public void updateReportedState(MAVLinkPacket packet) {
        if (packet.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
            msgHighLatency.unpack(packet.payload);
        }
    }

    public msg_high_latency getHighLatencyMessage() {
        return msgHighLatency;
    }
    
    public List<msg_param_value> getParams() {
        return params;
    }

}
