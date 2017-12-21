package com.envirover.nvi;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_int;
import com.MAVLink.common.msg_command_long;
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
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_set_home_position;
import com.MAVLink.common.msg_set_mode;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_MISSION_RESULT;
import com.MAVLink.enums.MAV_RESULT;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkLogger;
import com.envirover.mavlink.MAVLinkShadow;

/*
 * TCP and WebSocket MAVLink client sessions that handle communications with GCS clients.
 *
 */
public class ClientSession {

    private final static Logger logger = Logger.getLogger(ClientSession.class);
    private static final Config config = Config.getInstance();

    private final Timer heartbeatTimer = new Timer();
    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;

    public ClientSession(MAVLinkChannel src, MAVLinkChannel mtMessageQueue) {
        this.src = src;
        this.dst = mtMessageQueue;
    }

    public void onOpen() {
        TimerTask heartbeatTask = new HeartbeatTask(src, config.getAutopilot(), config.getMavType());
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

    private void handleMissions(MAVLinkPacket packet) throws IOException {
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
     */
    private void handleCommand(MAVLinkPacket packet) throws IOException {
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
            src.sendMessage(command_ack.pack());
        } else if (packet.msgid == msg_command_int.MAVLINK_MSG_ID_COMMAND_INT) {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_command_int msg = (msg_command_int)packet.unpack();
            msg_command_ack command_ack = new msg_command_ack();
            command_ack.command = msg.command;
            command_ack.result = MAV_RESULT.MAV_RESULT_ACCEPTED;
            command_ack.sysid = msg.target_system;
            command_ack.compid = msg.target_component;
            src.sendMessage(command_ack.pack());
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

    private void sendToSource(MAVLinkMessage msg) throws IOException {
        MAVLinkPacket packet = msg.pack();
        packet.sysid = msg.sysid;
        packet.compid = 1;
        src.sendMessage(packet);

        MAVLinkLogger.log(Level.INFO, ">>", packet);
    }

}
