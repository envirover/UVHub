/*
This file is part of SPLGroundControl application.

SPLGroundControl is a ground control proxy station for ArduPilot rovers with
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

package com.envirover.spl;

import java.io.IOException;

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
import com.MAVLink.enums.MAV_MODE_FLAG;

/*
 * Message pump for mobile-originated messages receives MAVLink messages
 * from the specified source channel and sends them to the specified socket
 * if client is listening on the socket.
 * 
 */
class MOMessagePump implements Runnable {
    private final static int HEARTBEAT_INTERVAL = 1000;

    private final MAVLinkChannel src;
    private final int port;
    private final msg_high_latency msgHighLatency = new msg_high_latency();
    private int seq = 0;
    private MAVLinkSocket dst = null;

    public MOMessagePump(MAVLinkChannel src, int port) {
        this.src = src;
        this.port = port;
    }

    @Override
    public void run() {
        MAVLinkPacket packet;

        try {
            dst = new MAVLinkSocket(port);
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        while(true) {
            try {
                while ((packet = src.receiveMessage()) != null) {
                    dst.sendMessage(packet);

                    if (packet.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
                        msgHighLatency.unpack(packet.payload);
                    }
                }

                dst.sendMessage(pack(getHeartbeatMsg()));
                dst.sendMessage(pack(getSysStatusMsg()));
                dst.sendMessage(pack(getGpsRawIntMsg()));
                dst.sendMessage(pack(getAttitudeMsg()));
                dst.sendMessage(pack(getGlobalPositionIntMsg()));
                dst.sendMessage(pack(getMissionCurrentMsg()));
                dst.sendMessage(pack(getNavControllerOutputMsg()));
                dst.sendMessage(pack(getVfrHudMsg()));

                Thread.sleep(HEARTBEAT_INTERVAL);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                try {
                    dst.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
        }
    }

    private MAVLinkMessage getHeartbeatMsg() {
        msg_heartbeat msg = new msg_heartbeat();
        msg.base_mode = MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED;
        return msg;
    }

    // Split high frequency messages from HIGH_LATENCY message
    private MAVLinkMessage getSysStatusMsg() {
        msg_sys_status msg = new msg_sys_status();
        msg.battery_remaining = (byte)msgHighLatency.battery_remaining;
        return msg;
    }

    private MAVLinkMessage getGpsRawIntMsg() {
        msg_gps_raw_int msg = new msg_gps_raw_int();
        msg.fix_type = msgHighLatency.gps_fix_type;
        msg.satellites_visible = msgHighLatency.gps_nsat;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.alt = msgHighLatency.altitude_amsl;
        return msg;
    }

    private MAVLinkMessage getAttitudeMsg() {
        msg_attitude msg = new msg_attitude();
        msg.yaw = msgHighLatency.heading / 100;
        msg.pitch = (float)(msgHighLatency.pitch * Math.PI / 18000.0);
        msg.roll = (float)(msgHighLatency.roll * Math.PI / 18000.0);
        return msg;
    }

    private MAVLinkMessage getGlobalPositionIntMsg() {
        msg_global_position_int msg = new msg_global_position_int();
        msg.alt = msgHighLatency.altitude_amsl;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.hdg = msgHighLatency.heading;
        msg.relative_alt = msgHighLatency.altitude_sp;
        return msg;
    }

    private MAVLinkMessage getMissionCurrentMsg() {
        msg_mission_current msg = new msg_mission_current();
        msg.seq = msgHighLatency.wp_num;
        return msg;
    }

    private MAVLinkMessage getNavControllerOutputMsg() {
        msg_nav_controller_output msg = new msg_nav_controller_output();
        msg.nav_bearing = (short)(msgHighLatency.heading_sp / 100);
        return msg;
    }
 
    private MAVLinkMessage getVfrHudMsg() {
        msg_vfr_hud msg = new msg_vfr_hud();
        msg.airspeed = msgHighLatency.airspeed;
        msg.alt = msgHighLatency.altitude_amsl;
        msg.climb = msgHighLatency.climb_rate;
        msg.groundspeed = msgHighLatency.groundspeed;
        msg.heading = (short)(msgHighLatency.heading / 100);
        msg.throttle = msgHighLatency.throttle;
        return msg;
    }

    private MAVLinkPacket pack(MAVLinkMessage msg) {
        MAVLinkPacket packet = msg.pack();
        packet.sysid = SPLGroundControl.SYSTEM_ID;
        packet.compid = SPLGroundControl.COMP_ID;
        packet.seq = seq++;

        return packet;
    }
}