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

package com.envirover.spl;

import java.io.IOException;
import java.util.TimerTask;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_STATE;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkShadow;

/**
 * Sends heartbeats and status messages to the specified channel.  
 * 
 * Heartbeats and high-frequency messages such as SYS_STATUS, GPS_RAW_INT, ATTITUDE, 
 * GLOBAL_POSITION_INT, MISSION_CURRENT, NAV_CONTROLLER_OUTPUT, and VFR_HUD are 
 * derived form the HIGH_LATENCY message and sent to the destination channel.

 * @author pavel
 *
 */
public class HeartbeatTask extends TimerTask {

    private final MAVLinkChannel dst;
    private final Short autopilot;
    private final Short mavType;

    public HeartbeatTask(MAVLinkChannel dst, Short autopilot, Short mavType) {
        this.dst = dst;
        this.autopilot = autopilot;
        this.mavType = mavType;
    }

    @Override
    public void run() {
        try {
            reportState();
        } catch (IOException e) {
            dst.close();
            e.printStackTrace();
        }
    }

    /**
     * Sends heartbeat and other status messages derived 
     * from HIGH_LATENCY message to the specified client channel.
     *
     * @param dst destination channel
     * @throws IOException if a message sending failed
     */
    private void reportState() throws IOException {
        msg_high_latency msgHighLatency = MAVLinkShadow.getInstance().getHighLatencyMessage();

        int sysid = msgHighLatency.sysid;
        int compid = msgHighLatency.compid;

        sendMessage(getHeartbeatMsg(msgHighLatency), sysid, compid);
        sendMessage(getSysStatusMsg(msgHighLatency), sysid, compid);
        sendMessage(getGpsRawIntMsg(msgHighLatency), sysid, compid);
        sendMessage(getAttitudeMsg(msgHighLatency), sysid, compid);
        sendMessage(getGlobalPositionIntMsg(msgHighLatency), sysid, compid);
        sendMessage(getMissionCurrentMsg(msgHighLatency), sysid, compid);
        sendMessage(getNavControllerOutputMsg(msgHighLatency), sysid, compid);
        sendMessage(getVfrHudMsg(msgHighLatency), sysid, compid);
    }

    private void sendMessage(MAVLinkMessage msg, int sysid, int compid) throws IOException {
        MAVLinkPacket packet = msg.pack();
        packet.sysid = sysid;
        packet.compid = compid;
        dst.sendMessage(packet);
    }

    private MAVLinkMessage getHeartbeatMsg(msg_high_latency msgHighLatency) {
        msg_heartbeat msg = new msg_heartbeat();
        msg.base_mode = msgHighLatency.base_mode;
        msg.custom_mode = msgHighLatency.custom_mode;
        msg.system_status = MAV_STATE.MAV_STATE_ACTIVE;
        msg.autopilot = autopilot;
        msg.type = mavType;
        return msg;
    }

    private MAVLinkMessage getSysStatusMsg(msg_high_latency msgHighLatency) {
        msg_sys_status msg = new msg_sys_status();
        msg.battery_remaining = (byte)msgHighLatency.battery_remaining;
        msg.current_battery = 0;
        return msg;
    }

    private MAVLinkMessage getGpsRawIntMsg(msg_high_latency msgHighLatency) {
        msg_gps_raw_int msg = new msg_gps_raw_int();
        msg.fix_type = msgHighLatency.gps_fix_type;
        msg.satellites_visible = msgHighLatency.gps_nsat;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.alt = msgHighLatency.altitude_amsl;
        return msg;
    }

    private MAVLinkMessage getAttitudeMsg(msg_high_latency msgHighLatency) {
        msg_attitude msg = new msg_attitude();
        msg.yaw = (float)Math.toRadians(msgHighLatency.heading / 100.0);
        msg.pitch = (float)Math.toRadians(msgHighLatency.pitch / 100.0);
        msg.roll = (float)Math.toRadians(msgHighLatency.roll / 100.0);
        return msg;
    }

    private MAVLinkMessage getGlobalPositionIntMsg(msg_high_latency msgHighLatency) {
        msg_global_position_int msg = new msg_global_position_int();
        msg.alt = msgHighLatency.altitude_amsl;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.hdg = msgHighLatency.heading;
        msg.relative_alt = msgHighLatency.altitude_sp;
        return msg;
    }

    private MAVLinkMessage getMissionCurrentMsg(msg_high_latency msgHighLatency) {
        msg_mission_current msg = new msg_mission_current();
        msg.seq = msgHighLatency.wp_num;
        return msg;
    }

    private MAVLinkMessage getNavControllerOutputMsg(msg_high_latency msgHighLatency) {
        msg_nav_controller_output msg = new msg_nav_controller_output();
        msg.nav_bearing = (short)(msgHighLatency.heading_sp / 100);
        return msg;
    }
 
    private MAVLinkMessage getVfrHudMsg(msg_high_latency msgHighLatency) {
        msg_vfr_hud msg = new msg_vfr_hud();
        msg.airspeed = msgHighLatency.airspeed;
        msg.alt = msgHighLatency.altitude_amsl;
        msg.climb = msgHighLatency.climb_rate;
        msg.groundspeed = msgHighLatency.groundspeed;
        msg.heading = (short)(msgHighLatency.heading / 100);
        msg.throttle = msgHighLatency.throttle;
        return msg;
    }

}
