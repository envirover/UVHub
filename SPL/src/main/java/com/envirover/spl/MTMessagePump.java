/*
This file is part of SPLGroundControl application.

SPLGroundControl is a ground control proxy station for ArduPilot rovers with
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
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_request_data_stream;
import com.envirover.mavlink.MAVLinkChannel;

/*
 * Mobile-terminated message pump.
 */
public class MTMessagePump implements Runnable {

    private final static Logger logger = Logger.getLogger(MTMessagePump.class);

    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;

    /*
     * @param src source messages channel
     * @param dst destination messages channel
     */
    public MTMessagePump(MAVLinkChannel src, MAVLinkChannel dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void run() {
        logger.debug("MTMessagePump started.");

        while(true) {
            try {
                MAVLinkPacket packet = src.receiveMessage();

                if (packet != null) {
                    logger.debug(MessageFormat.format("MT message received (msgid = {0}).", packet.msgid));

                    //TODO: filter out high frequency messages
                    if (packet.msgid != msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT &&
                        packet.msgid != msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST &&
                        packet.msgid != msg_request_data_stream.MAVLINK_MSG_ID_REQUEST_DATA_STREAM)
                    dst.sendMessage(packet);
                }

                Thread.sleep(10);
            } catch(IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException e) {
                logger.debug("MTMessagePump interrupted.");
                return;
            }
        }
    }

}
