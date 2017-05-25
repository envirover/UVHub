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

//import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_param_value;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkShadow;

/**
 * MOMessageHandler handles mobile-originated MAVLink messages.
 * 
 * The received messages either trigger updates of MAVLinkShadow or 
 * forwarded to the specified destination channel.
 * 
 * @author envirover
 *
 */
public class MOMessageHandler implements MAVLinkChannel {
	
	private final MAVLinkChannel dst;
	
	//private final static Logger logger = Logger.getLogger(MOMessageHandler.class);
	
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

        shadow.updateReportedState(packet);

        switch(packet.msgid) {
            case msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY:
                // Do not forward HIGH_LATENCY messages
                break;
            case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
                // Replace the COMMAND_ACK message by STATUS_TEXT message.
                shadow.sendCommandAck(packet, dst);
                break;
            case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
                //TODO: Update the actual param value in MAVLinkShadow.
                break;
            case msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK:
            	//TODO: Update actual missions in MAVLinkShadow.
            	break;
            default:
                dst.sendMessage(packet);
        }		
	}

	@Override
	public void close() {
	}

}
