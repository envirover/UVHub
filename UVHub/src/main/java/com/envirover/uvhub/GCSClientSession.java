/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.envirover.uvhub;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.MAVLink.common.*;
import com.MAVLink.enums.MAV_STATE;
import com.MAVLink.minimal.msg_heartbeat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_MISSION_RESULT;
import com.MAVLink.enums.MAV_RESULT;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkLogger;
import com.envirover.uvnet.shadow.StateReport;
import com.envirover.uvnet.shadow.UVShadow;

/**
 * TCP MAVLink client session that handle communications with GCS clients.
 * 
 * @author Pavel Bobov
 */
public class GCSClientSession implements ClientSession {

    private final static Logger logger = LogManager.getLogger(GCSClientSession.class);

    private static final Config config = Config.getInstance();

    private final ScheduledExecutorService heartbeatTimer;
    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;
    private final UVShadow shadow;

    private final AtomicBoolean isOpen = new AtomicBoolean(false);
    private int desiredMissionCount = 0;
    private List<msg_mission_item> reportedMission = new ArrayList<>();

    public GCSClientSession(MAVLinkChannel src, MAVLinkChannel mtMessageQueue, UVShadow shadow) {
        this.heartbeatTimer = Executors.newScheduledThreadPool(2);
        this.src = src;
        this.dst = mtMessageQueue;
        this.shadow = shadow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.envirover.nvi.ClientSession#onOpen()
     */
    @Override
    public void onOpen() {
        Runnable heartbeatTask = new Runnable() {
            @Override
            public void run() {
                try {
                    reportState();
                } catch (IOException ex) {
                    logger.warn("Failed to send message to GCS client. " + ex.getMessage(), ex);

                    if (ex.getMessage() == null)
                        logger.warn(ex);

                    logger.debug(ex.getMessage(), ex);
                    try {
                        onClose();
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                } catch (Exception ex) {
                    logger.warn("Failed to send message to GCS client. " + ex.getMessage(), ex);

                    if (ex.getMessage() == null)
                        logger.warn(ex);

                    logger.debug(ex);
                }
            }
        };

        heartbeatTimer.scheduleAtFixedRate(heartbeatTask, 0, config.getHeartbeatInterval(), TimeUnit.MILLISECONDS);

        isOpen.set(true);

        logger.info("GCS client session opened.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.envirover.nvi.ClientSession#onClose()
     */
    @Override
    public void onClose() throws IOException {
        if (isOpen.getAndSet(false)) {
            heartbeatTimer.shutdownNow();

            if (src != null) {
                src.close();
            }

            logger.info("GCS client session closed.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.envirover.nvi.ClientSession#onMessage(com.MAVLink.MAVLinkPacket)
     */
    @Override
    public void onMessage(MAVLinkPacket packet) throws IOException {
        handleParams(packet);
        handleMissions(packet);
        handleCommand(packet);

        if (filter(packet)) {
            dst.sendMessage(packet);
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    private void handleParams(MAVLinkPacket packet) throws IOException {
        if (packet == null) {
            return;
        }

        switch (packet.msgid) {
        case msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);

            List<msg_param_value> params = shadow.getParams(Config.getInstance().getMavSystemId());
            for (msg_param_value param : params) {
                sendToSource(param, false);
            }

            logger.info(MessageFormat.format("{0} on-board parameters sent to the MAVLink client.", params.size()));
            break;
        }
        case msg_param_request_read.MAVLINK_MSG_ID_PARAM_REQUEST_READ: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);

            msg_param_request_read request = (msg_param_request_read) packet.unpack();
            // logger.info(MessageFormat.format("Sending value of parameter ''{0}'' to
            // MAVLink client.", request.param_index));
            msg_param_value paramValue = shadow.getParamValue(
                    request.target_system,
                    request.getParam_Id(),
                    request.param_index);
            sendToSource(paramValue, true);
            break;
        }
        case msg_param_set.MAVLINK_MSG_ID_PARAM_SET: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);

            msg_param_set paramSet = (msg_param_set) packet.unpack();

            msg_param_value paramValue = shadow.getParamValue(
                    paramSet.target_system,
                    paramSet.getParam_Id(),
                    (short) -1);

            if (paramValue != null) {
                paramValue.param_value = paramSet.param_value;
                sendToSource(paramValue, true);
            }
            break;
        }
        }
    }

    /**
     * Handles MAVLink mission protocol communications between GCS and UV as
     * described at https://mavlink.io/en/protocol/mission.html
     * 
     * @param packet MAVLink packet
     * @throws IOException I/O exception
     */
    private void handleMissions(MAVLinkPacket packet) throws IOException {
        if (packet == null) {
            return;
        }

        switch (packet.msgid) {
        case msg_mission_request_list.MAVLINK_MSG_ID_MISSION_REQUEST_LIST: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_mission_request_list msg = (msg_mission_request_list) packet.unpack();
            reportedMission = shadow.getMission(msg.target_system);
            msg_mission_count count = new msg_mission_count();
            count.count = reportedMission != null ? reportedMission.size() : 0;
            count.sysid = msg.target_system;
            count.compid = msg.target_component;
            count.target_system = (short) packet.sysid;
            count.target_component = (short) packet.compid;
            sendToSource(count, true);
            break;
        }
        case msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_mission_request msg = (msg_mission_request) packet.unpack();
            if (reportedMission != null && msg.seq < reportedMission.size()) {
                msg_mission_item mission = reportedMission.get(msg.seq);
                mission.sysid = msg.target_system;
                mission.compid = msg.target_component;
                sendToSource(mission, true);
            }
            break;
        }
        case msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            shadow.getDesiredMission().clear();
            desiredMissionCount = 0;
            break;
        }
        case msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_mission_count msg = (msg_mission_count) packet.unpack();
            shadow.getDesiredMission().clear();
            desiredMissionCount = msg.count;
            msg_mission_request request = new msg_mission_request();
            request.seq = 0;
            request.sysid = msg.target_system;
            request.compid = msg.target_component;
            request.target_system = (short) packet.sysid;
            request.target_component = (short) packet.compid;
            sendToSource(request, true);
            break;
        }
        case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_mission_item msg = (msg_mission_item) packet.unpack();
            shadow.getDesiredMission().add(msg);
            if (msg.seq + 1 < desiredMissionCount) {
                msg_mission_request mission_request = new msg_mission_request();
                mission_request.seq = msg.seq + 1;
                mission_request.sysid = msg.target_system;
                mission_request.compid = msg.target_component;
                mission_request.target_system = (short) packet.sysid;
                mission_request.target_component = (short) packet.compid;
                sendToSource(mission_request, true);
            } else {
                msg_mission_ack mission_ack = new msg_mission_ack();
                mission_ack.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
                mission_ack.sysid = msg.target_system;
                mission_ack.compid = msg.target_component;
                mission_ack.target_system = (short) packet.sysid;
                mission_ack.target_component = (short) packet.compid;
                sendToSource(mission_ack, true);
            }
            break;
        }
        }
    }

    /**
     * Immediately return COMMAND_ACK for COMMAND_LONG and COMMAND_INT.
     * 
     * @param packet mobile-terminated MAVLink packet
     * @throws IOException if sending ack to the GCS client failed
     */
    private synchronized void handleCommand(MAVLinkPacket packet) throws IOException {
        if (packet == null) {
            return;
        }

        if (packet.msgid == msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG) {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_command_long msg = (msg_command_long) packet.unpack();
            msg_command_ack command_ack = new msg_command_ack();
            command_ack.command = msg.command;
            command_ack.result = MAV_RESULT.MAV_RESULT_ACCEPTED;
            command_ack.sysid = msg.target_system;
            command_ack.compid = msg.target_component;
            sendToSource(command_ack, true);
        } else if (packet.msgid == msg_command_int.MAVLINK_MSG_ID_COMMAND_INT) {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_command_int msg = (msg_command_int) packet.unpack();
            msg_command_ack command_ack = new msg_command_ack();
            command_ack.command = msg.command;
            command_ack.result = MAV_RESULT.MAV_RESULT_ACCEPTED;
            command_ack.sysid = msg.target_system;
            command_ack.compid = msg.target_component;
            sendToSource(command_ack, true);
        }
    }

    // White list message filter
    private static boolean filter(MAVLinkPacket packet) {
        if (packet == null) {
            return false;
        }

        if (packet.msgid == msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG) {
            int command = ((msg_command_long) packet.unpack()).command;
            return command != MAV_CMD.MAV_CMD_REQUEST_AUTOPILOT_CAPABILITIES
                    && command != 519 /* MAV_CMD_REQUEST_PROTOCOL_VERSION */;
        } else if (packet.msgid == msg_param_set.MAVLINK_MSG_ID_PARAM_SET) {
            msg_param_set paramSet = (msg_param_set) packet.unpack();
            return !OnBoardParams.getReadOnlyParamIds().contains(paramSet.getParam_Id());
        }

        return packet.msgid == msg_set_mode.MAVLINK_MSG_ID_SET_MODE
                || packet.msgid == msg_mission_write_partial_list.MAVLINK_MSG_ID_MISSION_WRITE_PARTIAL_LIST
                || packet.msgid == msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM
                || packet.msgid == msg_mission_set_current.MAVLINK_MSG_ID_MISSION_SET_CURRENT
                || packet.msgid == msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT
                || packet.msgid == msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT
                || packet.msgid == msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL
                || packet.msgid == msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT
                || packet.msgid == msg_command_int.MAVLINK_MSG_ID_COMMAND_INT
                || packet.msgid == msg_set_home_position.MAVLINK_MSG_ID_SET_HOME_POSITION;
    }

    private void sendToSource(MAVLinkMessage msg, boolean log) throws IOException {
        if (msg == null) {
            return;
        }

        MAVLinkPacket packet = msg.pack();
        packet.sysid = msg.sysid;
        packet.compid = 1;
        src.sendMessage(packet);

        if (log) {
            MAVLinkLogger.log(Level.INFO, ">>", packet);
        }
    }

    /**
     * Sends heartbeat and other status messages derived from HIGH_LATENCY message
     * to the specified client channel.
     *
     * @throws IOException          if a message sending failed
     */
    private synchronized void reportState() throws IOException {
        StateReport stateReport = shadow.getLastReportedState(Config.getInstance().getMavSystemId());

        if (stateReport != null) {
            List<MAVLinkMessage> messages = StateCodec.getMessages(stateReport);
            for (MAVLinkMessage msg : messages) {
                sendToSource(msg, false);
            }
        } else {
            // send only heartbeat
            msg_heartbeat msg = new msg_heartbeat();

            msg.sysid = Config.getInstance().getMavSystemId();
            msg.compid = 0;
            msg.base_mode = 0;
            msg.custom_mode = 0;
            msg.system_status = MAV_STATE.MAV_STATE_POWEROFF;
            msg.autopilot = Config.getInstance().getAutopilot();
            msg.type = Config.getInstance().getMavType();

            sendToSource(msg, false);
        }
    }

}
