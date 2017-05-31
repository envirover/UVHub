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
import java.text.MessageFormat;

//import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.enums.MAV_SEVERITY;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkShadow;

/**
 * MOMessageHandler handles mobile-originated MAVLink messages.
 * 
 * The received messages trigger updates of MAVLinkShadow and forwarded to the
 * specified destination channel.
 * 
 * @author envirover
 *
 */
public class MOMessageHandler implements MAVLinkChannel {

    private final MAVLinkChannel dst;

    int seq = 0;
    // private final static Logger logger =
    // Logger.getLogger(MOMessageHandler.class);

    public MOMessageHandler(MAVLinkChannel dst) {
        this.dst = dst;
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

        MAVLinkShadow shadow = MAVLinkShadow.getInstance();

        switch (packet.msgid) {
        case msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY:
            shadow.updateReportedState(packet);
            break;
        case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
            // Replace the COMMAND_ACK message by STATUS_TEXT message.
            sendCommandAck(packet);
            break;
        case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
            msg_param_value paramValue = (msg_param_value) packet.unpack();
            shadow.setParamValue(paramValue.getParam_Id(), paramValue.param_value);
            break;
        case msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK:
            shadow.missionAccepted();
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
        p.seq = seq++;
        p.sysid = packet.sysid;
        p.compid = packet.compid;

        dst.sendMessage(p);
    }

}
