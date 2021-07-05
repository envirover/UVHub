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
import java.util.Date;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.envirover.uvnet.shadow.StateReport;
import com.envirover.uvnet.shadow.UVLogbook;
import com.envirover.uvnet.shadow.UVShadow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MOMessageHandler handles mobile-originated MAVLink messages.
 * 
 * The received messages trigger updates of MAVLinkShadow and forwarded to the
 * specified destination channel.
 * 
 * @author Pavel Bobov
 *
 */
public class MOMessageHandler {

    private final static Logger logger = LogManager.getLogger(MOMessageHandler.class);

    private final UVShadow shadow;
    private final UVLogbook logbook;
    private final StateReport report = new StateReport();

    private Date lastReportTime = new Date(0L);
    private String activeChannelName = null;

    /**
     * Constructs instance of MOMessageHandler.
     * 
     * @param shadow vehicle shadow
     * @param  logbook vehicle logbook
     */
    public MOMessageHandler(UVShadow shadow, UVLogbook logbook) {

        this.shadow = shadow;
        this.logbook = logbook;
    }

    /**
     * Returns name of the channel from that the last message was received.
     *
     * @return name of the active channel.
     */
    public String getActiveChannelName() {
        return activeChannelName;
    }

    /**
     * Handles mobile-originated message.
     *
     * @param packet mobile-originated MAVLink message packet
     * @param channelName name of the channel from which the message was received.
     * @throws IOException if message handling failed because of an I/O error.
     */
    public void handleMessage(MAVLinkPacket packet, String channelName) throws IOException {
        if (packet == null) {
            return;
        }

        activeChannelName = channelName;

        if (packet.sysid != Config.getInstance().getMavSystemId()) {
            logger.warn(MessageFormat.format("System ID of the autopilot ({0}) does not match system ID of UV Hub ({1}).",
                            packet.sysid, Config.getInstance().getMavSystemId()));
        }

        switch (packet.msgid) {
        // case msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY:
        // break;
        case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
            // Replace the COMMAND_ACK message by STATUS_TEXT message.
            //sendCommandAck(packet);
            break;
        case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
            msg_param_value param_value = (msg_param_value) packet.unpack();
            msg_param_set param = new msg_param_set();
            param.isMavlink2 = true;
            param.sysid = param_value.sysid;
            param.compid = param_value.compid;
            param.setParam_Id(param_value.getParam_Id());
            param.param_type = param_value.param_type;
            param.param_value = param_value.param_value;
            shadow.setParam(param.sysid, param);
            break;
        case msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK:
            msg_mission_ack mission_ack = (msg_mission_ack) packet.unpack();
            shadow.setMission(mission_ack.sysid, shadow.getDesiredMission());
            break;
        default:
            // Update the reported state
            if (StateCodec.update(report, packet.unpack())) {
                // Milliseconds elapsed since the last report
                Date time = new Date();
                report.setTime(time);
                shadow.updateReportedState(report);

                // Log the report if HL_REPORT_PERIOD parameter value seconds elapsed
                float elapsed = time.getTime() - lastReportTime.getTime();
                if (elapsed >= getReportPeriod(packet.sysid) * 1000.0) {
                    logbook.addReportedState(report);
                    lastReportTime = time;
                }
            }
        }
    }

//    private void sendCommandAck(MAVLinkPacket packet) throws IOException {
//        // Replace COMMAND_ACK by STATUSTEXT message
//        msg_command_ack ack = (msg_command_ack) packet.unpack();
//
//        String text = MessageFormat.format("ACK: comand={0}, result={1}", ack.command, ack.result);
//
//        msg_statustext msg = new msg_statustext();
//        msg.severity = MAV_SEVERITY.MAV_SEVERITY_INFO;
//        msg.setText(text);
//
//        MAVLinkPacket p = msg.pack();
//        // p.seq = seq++;
//        p.sysid = packet.sysid;
//        p.compid = packet.compid;
//
//        dst.sendMessage(p);
//    }

    private float getReportPeriod(int sysId) throws IOException {
        msg_param_value period = shadow.getParamValue(sysId, OnBoardParams.HL_REPORT_PERIOD_PARAM, (short) -1);
        if (period != null) {
            return period.param_value;
        }

        return Config.getInstance().getDefaultHLReportPeriod();
    }

}
