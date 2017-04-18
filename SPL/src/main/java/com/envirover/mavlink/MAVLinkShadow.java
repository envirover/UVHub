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
along with Rock7MAVLink.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.mavlink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_SEVERITY;
import com.MAVLink.enums.MAV_TYPE;

/**
 * Keeps the reported and the desired states of the vehicle.
 * 
 * Ground control client application 'talk' to the shadow. 
 * 
 * The actual state of the vehicle is updated during communication sessions. 
 * 
 */
public class MAVLinkShadow {

    private static MAVLinkShadow instance = null;

    private final msg_high_latency msgHighLatency = new msg_high_latency();
    private ArrayList<msg_param_value> params = new ArrayList<msg_param_value>();
    private ArrayList<msg_mission_item> missions = new ArrayList<msg_mission_item>();
    private int missionCount = 0;

    private int seq = 0;

    protected MAVLinkShadow() {
        msgHighLatency.sysid = 1;
        msgHighLatency.compid = 1;
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
                }
            }
        }

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
    public MAVLinkPacket getParamValue(String paramId, short paramIndex) {
        if (paramIndex >= 0) {
            return pack(params.get(paramIndex));
        }

        for (msg_param_value param : params) {
            if (param.getParam_Id().equalsIgnoreCase(paramId.trim()))
                return pack(param);
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
    
    public int getMissionCount() {
        return missionCount;
    }

    public void setMissionCount(int count) {
        missionCount = count;
        missions = new ArrayList<msg_mission_item>(count);
    }

    public void setMissionItem(msg_mission_item mission) {
        if (mission.seq >= missions.size()) {
            missions.add(mission.seq, mission); 
        } else {
            missions.set(mission.seq, mission);
        }
    }

    public MAVLinkPacket getMissionItem(int index) {
        msg_mission_item mission  = missions.get(index);
        return mission != null ? pack(mission) : null;
    }

    public void updateReportedState(MAVLinkPacket packet) {
        if (packet.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
            msgHighLatency.unpack(packet.payload);
        }
    }

    public void updateDesiredState(MAVLinkPacket packet) {
        
    }

    /**
     * Sends heartbeat and other status messages derived 
     * from HIGH_LATENCY message to the specified client channel.
     *
     * @param dst destination channel
     * @throws IOException if a message sending failed
     */
    public void reportState(MAVLinkChannel dst) throws IOException {
        dst.sendMessage(getHeartbeatMsg());
        dst.sendMessage(getSysStatusMsg());
        dst.sendMessage(getGpsRawIntMsg());
        dst.sendMessage(getAttitudeMsg());
        dst.sendMessage(getGlobalPositionIntMsg());
        dst.sendMessage(getMissionCurrentMsg());
        dst.sendMessage(getNavControllerOutputMsg());
        dst.sendMessage(getVfrHudMsg());
    }

    public void reportParams(MAVLinkChannel dst) throws IOException, InterruptedException {
        for (msg_param_value param : params) {
            dst.sendMessage(pack(param));
            Thread.sleep(10);
        }
    }

    public void sendCommandAck(MAVLinkPacket packet, MAVLinkChannel dst) throws IOException {
        //Replace COMMAND_ACK by STATUSTEXT message
        msg_command_ack ack = (msg_command_ack)packet.unpack();

        String text = MessageFormat.format("ACK: comand={0}, result={1}",
                                           ack.command, ack.result);

        msg_statustext msg = new msg_statustext();
        msg.severity = MAV_SEVERITY.MAV_SEVERITY_INFO;
        msg.setText(text);

        dst.sendMessage(pack(msg));
    }

    private MAVLinkPacket pack(MAVLinkMessage msg) {
        MAVLinkPacket packet = msg.pack();
        packet.seq = seq++;
        packet.sysid = msgHighLatency.sysid;
        packet.compid = msgHighLatency.compid;
        return packet;
    }

    private MAVLinkPacket getHeartbeatMsg() {
        msg_heartbeat msg = new msg_heartbeat();
        msg.base_mode = msgHighLatency.base_mode;
        msg.type = MAV_TYPE.MAV_TYPE_GROUND_ROVER;
        return pack(msg);
    }

    private MAVLinkPacket getSysStatusMsg() {
        msg_sys_status msg = new msg_sys_status();
        msg.battery_remaining = (byte)msgHighLatency.battery_remaining;
        return pack(msg);
    }

    private MAVLinkPacket getGpsRawIntMsg() {
        msg_gps_raw_int msg = new msg_gps_raw_int();
        msg.fix_type = msgHighLatency.gps_fix_type;
        msg.satellites_visible = msgHighLatency.gps_nsat;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.alt = msgHighLatency.altitude_amsl;
        return pack(msg);
    }

    private MAVLinkPacket getAttitudeMsg() {
        msg_attitude msg = new msg_attitude();
        msg.yaw = msgHighLatency.heading / 100;
        msg.pitch = (float)(msgHighLatency.pitch * Math.PI / 18000.0);
        msg.roll = (float)(msgHighLatency.roll * Math.PI / 18000.0);
        return pack(msg);
    }

    private MAVLinkPacket getGlobalPositionIntMsg() {
        msg_global_position_int msg = new msg_global_position_int();
        msg.alt = msgHighLatency.altitude_amsl;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.hdg = msgHighLatency.heading;
        msg.relative_alt = msgHighLatency.altitude_sp;
        return pack(msg);
    }

    private MAVLinkPacket getMissionCurrentMsg() {
        msg_mission_current msg = new msg_mission_current();
        msg.seq = msgHighLatency.wp_num;
        return pack(msg);
    }

    private MAVLinkPacket getNavControllerOutputMsg() {
        msg_nav_controller_output msg = new msg_nav_controller_output();
        msg.nav_bearing = (short)(msgHighLatency.heading_sp / 100);
        return pack(msg);
    }
 
    private MAVLinkPacket getVfrHudMsg() {
        msg_vfr_hud msg = new msg_vfr_hud();
        msg.airspeed = msgHighLatency.airspeed;
        msg.alt = msgHighLatency.altitude_amsl;
        msg.climb = msgHighLatency.climb_rate;
        msg.groundspeed = msgHighLatency.groundspeed;
        msg.heading = (short)(msgHighLatency.heading / 100);
        msg.throttle = msgHighLatency.throttle;
        return pack(msg);
    }

}
