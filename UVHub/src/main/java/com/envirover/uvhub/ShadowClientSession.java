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

package com.envirover.uvhub;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_mission_clear_all;
import com.MAVLink.common.msg_mission_count;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_mission_request;
import com.MAVLink.common.msg_mission_request_list;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_MISSION_RESULT;
import com.MAVLink.enums.MAV_STATE;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkLogger;
import com.envirover.mavlink.MAVLinkShadow;

/*
 * TCP and WebSocket MAVLink client sessions that handle communications with GCS clients
 * to update reported states of on-board parameters and missions in the shadow.
 *
 */
public class ShadowClientSession implements ClientSession {

    private final static Logger logger = Logger.getLogger(ShadowClientSession.class);
    private static final Config config = Config.getInstance();

    private final Timer heartbeatTimer = new Timer();
    private final MAVLinkChannel src;

    public ShadowClientSession(MAVLinkChannel src) {
        this.src = src;
    }

    /* (non-Javadoc)
     * @see com.envirover.nvi.ClientSession#onOpen()
     */
    @Override
    public void onOpen() {
        TimerTask heartbeatTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    reportState();
                } catch (IOException | InterruptedException e) {
                }
            }
        };

        heartbeatTimer.schedule(heartbeatTask, 0, config.getHeartbeatInterval());
    }

    /* (non-Javadoc)
     * @see com.envirover.nvi.ClientSession#onClose()
     */
    @Override
    public void onClose() throws InterruptedException {
        heartbeatTimer.cancel();

        if (src != null) {
            src.close();
        }
    }

    /* (non-Javadoc)
     * @see com.envirover.nvi.ClientSession#onMessage(com.MAVLink.MAVLinkPacket)
     */
    @Override
    public void onMessage(MAVLinkPacket packet) throws IOException, InterruptedException {
        handleParams(packet);
        handleMissions(packet);
    }

    private void handleParams(MAVLinkPacket packet) throws IOException, InterruptedException {
        if (packet == null) {
            return;
        }

        MAVLinkShadow shadow = MAVLinkShadow.getInstance();

        switch (packet.msgid) {
            case msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);

                for (msg_param_value param : shadow.getParams()) {
                    sendToSource(param);
                    Thread.sleep(10);
                }

                logger.info(MessageFormat.format("{0} on-board parameters sent to the MAVLink client.", shadow.getParams().size()));
                break;
            }
            case msg_param_request_read.MAVLINK_MSG_ID_PARAM_REQUEST_READ: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);

                msg_param_request_read request = (msg_param_request_read)packet.unpack();
                logger.info(MessageFormat.format("Sending value of parameter ''{0}'' to MAVLink client.", request.getParam_Id()));
                sendToSource(shadow.getParamValue(request.getParam_Id(), request.param_index));
                break;
            }
            case msg_param_set.MAVLINK_MSG_ID_PARAM_SET: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);

                msg_param_set paramSet = (msg_param_set)packet.unpack();
                shadow.setParamValue(paramSet.getParam_Id(), paramSet.param_value);
                sendToSource(shadow.getParamValue(paramSet.getParam_Id(), (short)-1));
                break;
            }
        }
    }

    private void handleMissions(MAVLinkPacket packet) throws IOException, InterruptedException {
        if (packet == null) {
            return;
        }

        MAVLinkShadow shadow = MAVLinkShadow.getInstance();

        switch (packet.msgid) {
            case msg_mission_request_list.MAVLINK_MSG_ID_MISSION_REQUEST_LIST: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                msg_mission_request_list msg = (msg_mission_request_list)packet.unpack();
                msg_mission_count count = new msg_mission_count();
                count.count = shadow.getReportedMissionCount();
                count.sysid = msg.target_system;
                count.compid = msg.target_component;
                count.target_system = (short) packet.sysid;
                count.target_component = (short) packet.compid;
                sendToSource(count);
                break;
            }
            case msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                msg_mission_request msg = (msg_mission_request)packet.unpack();
                msg_mission_item mission = shadow.getReportedMissionItem(msg.seq);
                mission.sysid = msg.target_system;
                mission.compid = msg.target_component;
                sendToSource(mission);
                break;
            }
            case msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                shadow.setDesiredMissionCount(0);
                break;
            }
            case msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                msg_mission_count msg = (msg_mission_count)packet.unpack();
                shadow.setDesiredMissionCount(msg.count);
                msg_mission_request request = new msg_mission_request();
                request.seq = 0;
                request.sysid = msg.target_system;
                request.compid = msg.target_component;
                request.target_system = (short) packet.sysid;
                request.target_component = (short) packet.compid;
                sendToSource(request);
                break;
            }
            case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                msg_mission_item msg = (msg_mission_item)packet.unpack();
                shadow.setMissionItem(msg);
                if (msg.seq + 1 < shadow.getDesiredMissionCount()) {
                    msg_mission_request mission_request = new msg_mission_request();
                    mission_request.seq = msg.seq + 1;
                    mission_request.sysid = msg.target_system;
                    mission_request.compid = msg.target_component;
                    mission_request.target_system = (short) packet.sysid;
                    mission_request.target_component = (short) packet.compid;
                    sendToSource(mission_request);
                } else {
                    msg_mission_ack mission_ack = new msg_mission_ack();
                    mission_ack.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
                    mission_ack.sysid = msg.target_system;
                    mission_ack.compid = msg.target_component;
                    mission_ack.target_system = (short) packet.sysid;
                    mission_ack.target_component = (short) packet.compid;
                    sendToSource(mission_ack);
                    shadow.missionAccepted();
                }
                break;
            }
        }
    }

    private void sendToSource(MAVLinkMessage msg) throws IOException, InterruptedException {
        if (msg == null) {
            return;
        }

        try {
            MAVLinkPacket packet = msg.pack();
            packet.sysid = msg.sysid;
            packet.compid = 1;
            src.sendMessage(packet);
            MAVLinkLogger.log(Level.INFO, ">>", packet);
        } catch (IOException ex) {
            ex.printStackTrace();
            onClose();
            throw ex;
        }
    }

    /**
     * Sends heartbeat and other status messages derived 
     * from HIGH_LATENCY message to the specified client channel.
     *
     * @param dst destination channel
     * @throws IOException if a message sending failed
     * @throws InterruptedException 
     */
    private void reportState() throws IOException, InterruptedException {
        msg_high_latency msgHighLatency = MAVLinkShadow.getInstance().getHighLatencyMessage();

        sendToSource(getHeartbeatMsg(msgHighLatency));
        sendToSource(getSysStatusMsg(msgHighLatency));
        sendToSource(getGpsRawIntMsg(msgHighLatency));
        sendToSource(getAttitudeMsg(msgHighLatency));
        sendToSource(getGlobalPositionIntMsg(msgHighLatency));
        sendToSource(getMissionCurrentMsg(msgHighLatency));
        sendToSource(getNavControllerOutputMsg(msgHighLatency));
        sendToSource(getVfrHudMsg(msgHighLatency));
    }

    private MAVLinkMessage getHeartbeatMsg(msg_high_latency msgHighLatency) {
        msg_heartbeat msg = new msg_heartbeat();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.base_mode = msgHighLatency.base_mode;
        msg.custom_mode = msgHighLatency.custom_mode;
        msg.system_status = MAV_STATE.MAV_STATE_ACTIVE;
        msg.autopilot = config.getAutopilot();
        msg.type = config.getMavType();
        return msg;
    }

    private MAVLinkMessage getSysStatusMsg(msg_high_latency msgHighLatency) {
        msg_sys_status msg = new msg_sys_status();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.battery_remaining = (byte)msgHighLatency.battery_remaining;
        msg.voltage_battery = msgHighLatency.temperature * 1000;
        msg.current_battery = msgHighLatency.temperature_air < 0 ? 
                -1 : (short)(msgHighLatency.temperature_air * 100);
        return msg;
    }

    private MAVLinkMessage getGpsRawIntMsg(msg_high_latency msgHighLatency) {
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

    private MAVLinkMessage getAttitudeMsg(msg_high_latency msgHighLatency) {
        msg_attitude msg = new msg_attitude();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.yaw = (float)Math.toRadians(msgHighLatency.heading / 100.0);
        msg.pitch = (float)Math.toRadians(msgHighLatency.pitch / 100.0);
        msg.roll = (float)Math.toRadians(msgHighLatency.roll / 100.0);
        return msg;
    }

    private MAVLinkMessage getGlobalPositionIntMsg(msg_high_latency msgHighLatency) {
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

    private MAVLinkMessage getMissionCurrentMsg(msg_high_latency msgHighLatency) {
        msg_mission_current msg = new msg_mission_current();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.seq = msgHighLatency.wp_num;
        return msg;
    }

    private MAVLinkMessage getNavControllerOutputMsg(msg_high_latency msgHighLatency) {
        msg_nav_controller_output msg = new msg_nav_controller_output();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.nav_bearing = (short)(msgHighLatency.heading_sp / 100);
        return msg;
    }
 
    private MAVLinkMessage getVfrHudMsg(msg_high_latency msgHighLatency) {
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

}
