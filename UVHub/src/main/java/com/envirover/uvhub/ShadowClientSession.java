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
import com.MAVLink.enums.MAV_MISSION_RESULT;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkLogger;
import com.envirover.uvnet.shadow.StateReport;
import com.envirover.uvnet.shadow.UVLogbook;
import com.envirover.uvnet.shadow.UVShadow;

/**
 * TCP MAVLink client sessions that handle communications with GCS clients to
 * update reported states of on-board parameters and missions in the shadow.
 * 
 * @author Pavel Bobov
 */
public class ShadowClientSession implements ClientSession {

    private final static Logger logger = LogManager.getLogger(ShadowClientSession.class);
    private static final Config config = Config.getInstance();

    private final ScheduledExecutorService heartbeatTimer;

    private final MAVLinkChannel src;
    private final UVShadow shadow;
    private final UVLogbook logbook;

    private AtomicBoolean isOpen = new AtomicBoolean(false);
    private List<msg_mission_item> desiredMission = new ArrayList<>();
    private int desiredMissionCount = 0;
    private List<msg_mission_item> reportedMission = new ArrayList<>();

    public ShadowClientSession(MAVLinkChannel src, UVShadow shadow, UVLogbook logbook) {
        this.heartbeatTimer = Executors.newScheduledThreadPool(2);
        this.src = src;
        this.shadow = shadow;
        this.logbook = logbook;
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
                    logger.warn("Failed to send message to GCS client. " + ex.getMessage());
                    logger.debug(ex);
                    try {
                        onClose();
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                } catch (Exception ex) {
                    logger.warn("Failed to send message to GCS client. " + ex.getMessage());
                    logger.debug(ex);
                }
            }
        };

        heartbeatTimer.scheduleAtFixedRate(heartbeatTask, 0, config.getHeartbeatInterval(), TimeUnit.MILLISECONDS);

        isOpen.set(true);

        logger.info("Shadow client session opened.");
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

            logger.info("Shadow client session closed.");
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
        handleLogs(packet);
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
            msg_param_request_list msg = (msg_param_request_list) packet.unpack();
            List<msg_param_value> params = shadow.getParams(msg.target_system);

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
            // MAVLink client.", request.getParam_Id()));
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

            if (!OnBoardParams.getReadOnlyParamIds().contains(paramSet.getParam_Id())) {
                shadow.setParam(paramSet.target_system, paramSet);
            }

            msg_param_value paramValue = shadow.getParamValue(
                    paramSet.target_system,
                    paramSet.getParam_Id(),
                    (short) -1);
            sendToSource(paramValue, true);
            break;
        }
        }
    }

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
            desiredMission.clear();
            break;
        }
        case msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_mission_count msg = (msg_mission_count) packet.unpack();
            desiredMission = new ArrayList<>(msg.count);
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
        case msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            break;
        }
        case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM: {
            MAVLinkLogger.log(Level.INFO, "<<", packet);
            msg_mission_item msg = (msg_mission_item) packet.unpack();
            try {
                // desiredMission.set(msg.seq, msg);
                desiredMission.add(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (msg.seq + 1 != desiredMissionCount) {
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
                shadow.setMission(msg.target_system, desiredMission);
            }
            break;
        }
        }
    }

    private void handleLogs(MAVLinkPacket packet) throws IOException {
        if (packet == null) {
            return;
        }

        switch (packet.msgid) {
        case msg_log_request_list.MAVLINK_MSG_ID_LOG_REQUEST_LIST:
            MAVLinkLogger.log(Level.INFO, "<<", packet);

            msg_log_request_list log_request_list = (msg_log_request_list) packet.unpack();

            List<msg_log_entry> logs = logbook.getLogs(log_request_list.target_system);

            if (logs != null) {
                for (msg_log_entry log_entry : logs) {
                    sendToSource(log_entry, true);
                }
            }

            break;
        case msg_log_erase.MAVLINK_MSG_ID_LOG_ERASE:
            MAVLinkLogger.log(Level.INFO, "<<", packet);

            msg_log_erase log_erase = (msg_log_erase) packet.unpack();

            logbook.eraseLogs(log_erase.target_system);

            logger.info("Messages log erased.");

            break;
        }
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
    private void reportState() throws IOException {
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
