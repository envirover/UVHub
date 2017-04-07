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

package com.envirover.spl;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_SEVERITY;
import com.MAVLink.enums.MAV_TYPE;
import com.envirover.mavlink.MAVLinkChannel;

/**
 * Mobile-originated (MO) message pump receives MAVLink messages from the 
 * specified source channel and forwards them to the specified destination
 * channel.
 * 
 * Messages of HIGH_LATENCY type are not forwarded by stored in memory. 
 * Heartbeats and high-frequency messages such as SYS_STATUS, GPS_RAW_INT, ATTITUDE, 
 * GLOBAL_POSITION_INT, MISSION_CURRENT, NAV_CONTROLLER_OUTPUT, and VFR_HUD are 
 * derived form the HIGH_LATENCY message and periodically sent to the destination channel.
 */
class MOMessagePump implements Runnable {

    private final static Logger logger = Logger.getLogger(MOMessagePump.class);

    private final msg_high_latency msgHighLatency = new msg_high_latency();

    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;
    private final Integer heartbeatInterval;

    private int seq = 0;

    /**
     * Constructs MOMessagePump instance. 
     * 
     * @param src source channel
     * @param dst destination channel
     * @param heartbeatInterval heartbeat interval in milliseconds
     */
    public MOMessagePump(MAVLinkChannel src, MAVLinkChannel dst, Integer heartbeatInterval) {
        this.src = src;
        this.dst = dst;
        this.heartbeatInterval = heartbeatInterval;
        msgHighLatency.sysid = 1;
        msgHighLatency.compid = 1;
    }

    @Override
    public void run() {
        logger.debug("MOMessagePump started.");

        while (true) {
            try {
                MAVLinkPacket packet;

                while ((packet = src.receiveMessage()) != null) {
                    if (packet.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
                        msgHighLatency.unpack(packet.payload);
                    } else if (packet.msgid == msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK) {
                        //Replace COMMAND_ACK by STATUSTEXT message
                        msg_command_ack ack = (msg_command_ack)packet.unpack();

                        String text = MessageFormat.format("ACK: comand={0}, result={1}",
                                                           ack.command, ack.result);

                        msg_statustext msg = new msg_statustext();
                        msg.compid = packet.compid;
                        msg.sysid = packet.sysid;
                        msg.severity = MAV_SEVERITY.MAV_SEVERITY_INFO;
                        msg.text = text.getBytes();

                        dst.sendMessage(pack(msg));
                    } else {
                        dst.sendMessage(packet);
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

                Thread.sleep(heartbeatInterval);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException e) {
                dst.close();
                logger.debug("MOMessagePump interrupted.");
                return;
            }
        }
    }

    private MAVLinkMessage getHeartbeatMsg() {
        msg_heartbeat msg = new msg_heartbeat();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.base_mode = 0;//MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED;
        msg.type = MAV_TYPE.MAV_TYPE_GROUND_ROVER;
        return msg;
    }

    // Split high frequency messages from HIGH_LATENCY message
    private MAVLinkMessage getSysStatusMsg() {
        msg_sys_status msg = new msg_sys_status();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.battery_remaining = (byte)msgHighLatency.battery_remaining;
        return msg;
    }

    private MAVLinkMessage getGpsRawIntMsg() {
        msg_gps_raw_int msg = new msg_gps_raw_int();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.fix_type = msgHighLatency.gps_fix_type;
        msg.satellites_visible = msgHighLatency.gps_nsat;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.alt = msgHighLatency.altitude_amsl;
        return msg;
    }

    private MAVLinkMessage getAttitudeMsg() {
        msg_attitude msg = new msg_attitude();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.yaw = msgHighLatency.heading / 100;
        msg.pitch = (float)(msgHighLatency.pitch * Math.PI / 18000.0);
        msg.roll = (float)(msgHighLatency.roll * Math.PI / 18000.0);
        return msg;
    }

    private MAVLinkMessage getGlobalPositionIntMsg() {
        msg_global_position_int msg = new msg_global_position_int();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.alt = msgHighLatency.altitude_amsl;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.hdg = msgHighLatency.heading;
        msg.relative_alt = msgHighLatency.altitude_sp;
        return msg;
    }

    private MAVLinkMessage getMissionCurrentMsg() {
        msg_mission_current msg = new msg_mission_current();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.seq = msgHighLatency.wp_num;
        return msg;
    }

    private MAVLinkMessage getNavControllerOutputMsg() {
        msg_nav_controller_output msg = new msg_nav_controller_output();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.nav_bearing = (short)(msgHighLatency.heading_sp / 100);
        return msg;
    }
 
    private MAVLinkMessage getVfrHudMsg() {
        msg_vfr_hud msg = new msg_vfr_hud();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
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
        packet.seq = seq++;
        return packet;
    }
}