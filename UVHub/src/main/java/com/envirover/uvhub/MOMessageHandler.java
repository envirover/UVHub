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
import java.util.Date;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.enums.MAV_SEVERITY;
import com.envirover.mavlink.MAVLinkChannel;
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
public class MOMessageHandler implements MAVLinkChannel {

    private final static Logger logger = LogManager.getLogger(MOMessageHandler.class);

    private final MAVLinkChannel dst;
    private final UVShadow shadow;
    private final UVLogbook logbook;

    private long last_report_time = 0L;

    /**
     * Constructs instance of MOMessageHandler.
     * 
     * @param dst    destination channel for mobile-originated messages.
     * @param shadow vehicle shadow
     */
    public MOMessageHandler(MAVLinkChannel dst, UVShadow shadow, UVLogbook logbook) {
        this.dst = dst;
        this.shadow = shadow;
        this.logbook = logbook;
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        return null;
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        if (packet == null) {
            return;
        }

        if (packet.sysid != Config.getInstance().getMavSystemId()) {
            logger.warn(
                    MessageFormat.format("System ID of the autopilot ({0}) does not match system ID of UV Hub ({1}).",
                            packet.sysid, Config.getInstance().getMavSystemId()));
        }

        switch (packet.msgid) {
        case msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY:
            // Milliseconds elapsed since the last report
            long time = new Date().getTime();
            float elapsed = time - last_report_time;

            shadow.updateReportedState((msg_high_latency) packet.unpack(), time);

            // Log the report if HL_REPORT_PERIOD parameter value seconds elapsed
            if (elapsed >= getReportPeriod(packet.sysid) * 1000.0) {
                logbook.addReportedState((msg_high_latency) packet.unpack(), time);
                last_report_time = time;
            }

            break;
        case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
            // Replace the COMMAND_ACK message by STATUS_TEXT message.
            sendCommandAck(packet);
            break;
        case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
            msg_param_value param_value = (msg_param_value) packet.unpack();
            msg_param_set param = new msg_param_set();
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
            dst.sendMessage(packet);
        }
    }

    @Override
    public void close() {
    }

    private void sendCommandAck(MAVLinkPacket packet) throws IOException {
        // Replace COMMAND_ACK by STATUSTEXT message
        msg_command_ack ack = (msg_command_ack) packet.unpack();

        String text = MessageFormat.format("ACK: comand={0}, result={1}", ack.command, ack.result);

        msg_statustext msg = new msg_statustext();
        msg.severity = MAV_SEVERITY.MAV_SEVERITY_INFO;
        msg.setText(text);

        MAVLinkPacket p = msg.pack();
        // p.seq = seq++;
        p.sysid = packet.sysid;
        p.compid = packet.compid;

        dst.sendMessage(p);
    }

    private float getReportPeriod(int sysId) throws IOException {
        msg_param_value period = shadow.getParamValue(sysId, OnBoardParams.HL_REPORT_PERIOD_PARAM, (short) -1);
        if (period != null) {
            return period.param_value;
        }

        return Config.getInstance().getDefaultHLReportPeriod();
    }

}
