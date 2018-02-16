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

package com.envirover.nvi;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_int;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_mission_clear_all;
import com.MAVLink.common.msg_mission_count;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.common.msg_mission_request;
import com.MAVLink.common.msg_mission_request_list;
import com.MAVLink.common.msg_mission_set_current;
import com.MAVLink.common.msg_mission_write_partial_list;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_set_home_position;
import com.MAVLink.common.msg_set_mode;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_MISSION_RESULT;
import com.MAVLink.enums.MAV_RESULT;
import com.MAVLink.enums.MAV_STATE;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkLogger;
import com.envirover.mavlink.MAVLinkShadow;

/*
 * TCP and WebSocket MAVLink client sessions that handle communications with GCS clients.
 *
 */
public class GCSClientSession {

    private final static Logger logger = Logger.getLogger(GCSClientSession.class);
    private static final Config config = Config.getInstance();

    private final Timer heartbeatTimer = new Timer();
    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;

    public GCSClientSession(MAVLinkChannel src, MAVLinkChannel mtMessageQueue) {
        this.src = src;
        this.dst = mtMessageQueue;
    }

    public void onOpen() {
        TimerTask heartbeatTask = new HeartbeatTask(config.getAutopilot(), config.getMavType());
        heartbeatTimer.schedule(heartbeatTask, 0, config.getHeartbeatInterval());
    }

    public void onClose() throws InterruptedException {
        heartbeatTimer.cancel();

        if (src != null) {
            src.close();
        }
    }

    public void onMessage(MAVLinkPacket packet) throws IOException, InterruptedException {
        handleParams(packet);
        handleMissions(packet);
        handleCommand(packet);

        if (filter(packet)) {
            dst.sendMessage(packet);
        }
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
                }
                break;
            }
        }
    }

    /**
     * Immediately return COMMAND_ACK for COMMAND_LONG and and COMMAND_INT.
     * 
     * @param packet
     * @throws IOException
     * @throws InterruptedException 
     */
    private void handleCommand(MAVLinkPacket packet) throws IOException, InterruptedException {
        if (packet == null) {
            return;
        }

        if (packet.msgid == msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG) {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_command_long msg = (msg_command_long)packet.unpack();
            msg_command_ack command_ack = new msg_command_ack();
            command_ack.command = msg.command;
            command_ack.result = MAV_RESULT.MAV_RESULT_ACCEPTED;
            command_ack.sysid = msg.target_system;
            command_ack.compid = msg.target_component;
            sendToSource(command_ack);
        } else if (packet.msgid == msg_command_int.MAVLINK_MSG_ID_COMMAND_INT) {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_command_int msg = (msg_command_int)packet.unpack();
            msg_command_ack command_ack = new msg_command_ack();
            command_ack.command = msg.command;
            command_ack.result = MAV_RESULT.MAV_RESULT_ACCEPTED;
            command_ack.sysid = msg.target_system;
            command_ack.compid = msg.target_component;
            sendToSource(command_ack);
        }
    }

    //White list message filter
    private static boolean filter(MAVLinkPacket packet) {
        if (packet.msgid == msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG) {
            int command = ((msg_command_long)packet.unpack()).command;
            return command != MAV_CMD.MAV_CMD_REQUEST_AUTOPILOT_CAPABILITIES &&
                   command != 519  /* MAV_CMD_REQUEST_PROTOCOL_VERSION */;
        }

        return packet != null &&
              (packet.msgid == msg_set_mode.MAVLINK_MSG_ID_SET_MODE || 
               packet.msgid == msg_param_set.MAVLINK_MSG_ID_PARAM_SET ||
               packet.msgid == msg_mission_write_partial_list.MAVLINK_MSG_ID_MISSION_WRITE_PARTIAL_LIST ||
               packet.msgid == msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM ||
               packet.msgid == msg_mission_set_current.MAVLINK_MSG_ID_MISSION_SET_CURRENT ||
               packet.msgid == msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT ||
               packet.msgid == msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT ||
               packet.msgid == msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL ||
               packet.msgid == msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT ||
               packet.msgid == msg_command_int.MAVLINK_MSG_ID_COMMAND_INT ||
               packet.msgid == msg_set_home_position.MAVLINK_MSG_ID_SET_HOME_POSITION);
    }

    private void sendToSource(MAVLinkMessage msg) throws IOException, InterruptedException {
    	if (msg == null) {
    		return;
    	}
    	
        MAVLinkPacket packet = msg.pack();
        packet.sysid = msg.sysid;
        packet.compid = 1;
        try {
        	src.sendMessage(packet);
            MAVLinkLogger.log(Level.INFO, ">>", packet);
        } catch(IOException ex) {
      		ex.printStackTrace();
			onClose();
			throw ex;
        }

    }

    /**
     * Sends heartbeats and status messages to the specified channel.  
     * 
     * Heartbeats and high-frequency messages such as SYS_STATUS, GPS_RAW_INT, ATTITUDE, 
     * GLOBAL_POSITION_INT, MISSION_CURRENT, NAV_CONTROLLER_OUTPUT, and VFR_HUD are 
     * derived form the HIGH_LATENCY message and sent to the destination channel.

     * @author pavel
     *
     */
    class HeartbeatTask extends TimerTask {

        private final Short autopilot;
        private final Short mavType;

        public HeartbeatTask(Short autopilot, Short mavType) {
            this.autopilot = autopilot;
            this.mavType = mavType;
        }

        @Override
        public void run() {
            try {
                reportState();
            } catch (IOException | InterruptedException e) {
                dst.close();
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
            msg.base_mode = msgHighLatency.base_mode;
            msg.custom_mode = msgHighLatency.custom_mode;
            msg.system_status = MAV_STATE.MAV_STATE_ACTIVE;
            msg.autopilot = autopilot;
            msg.type = mavType;
            return msg;
        }

        private MAVLinkMessage getSysStatusMsg(msg_high_latency msgHighLatency) {
            msg_sys_status msg = new msg_sys_status();
            msg.sysid = msgHighLatency.sysid;
            msg.battery_remaining = (byte)msgHighLatency.battery_remaining;
            msg.voltage_battery = msgHighLatency.temperature * 1000;
            msg.current_battery = (short)(msgHighLatency.temperature_air * 100);
            return msg;
        }

        private MAVLinkMessage getGpsRawIntMsg(msg_high_latency msgHighLatency) {
            msg_gps_raw_int msg = new msg_gps_raw_int();
            msg.sysid = msgHighLatency.sysid;
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
            msg.yaw = (float)Math.toRadians(msgHighLatency.heading / 100.0);
            msg.pitch = (float)Math.toRadians(msgHighLatency.pitch / 100.0);
            msg.roll = (float)Math.toRadians(msgHighLatency.roll / 100.0);
            return msg;
        }

        private MAVLinkMessage getGlobalPositionIntMsg(msg_high_latency msgHighLatency) {
            msg_global_position_int msg = new msg_global_position_int();
            msg.sysid = msgHighLatency.sysid;
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
            msg.seq = msgHighLatency.wp_num;
            return msg;
        }

        private MAVLinkMessage getNavControllerOutputMsg(msg_high_latency msgHighLatency) {
            msg_nav_controller_output msg = new msg_nav_controller_output();
            msg.sysid = msgHighLatency.sysid;
            msg.nav_bearing = (short)(msgHighLatency.heading_sp / 100);
            return msg;
        }
     
        private MAVLinkMessage getVfrHudMsg(msg_high_latency msgHighLatency) {
            msg_vfr_hud msg = new msg_vfr_hud();
            msg.sysid = msgHighLatency.sysid;
            msg.airspeed = msgHighLatency.airspeed;
            msg.alt = msgHighLatency.altitude_amsl;
            msg.climb = msgHighLatency.climb_rate;
            msg.groundspeed = msgHighLatency.groundspeed;
            msg.heading = (short)(msgHighLatency.heading / 100);
            msg.throttle = msgHighLatency.throttle;
            return msg;
        }

    }
}
