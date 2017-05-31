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

import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;

/**
 * Mobile-originated (MO) message pump receives MAVLink messages from the 
 * specified source channel and forwards them to the specified destination
 * channel. 
 */
class MOMessagePump implements Runnable {

    private final static long MO_MESSAGE_PUMP_INTERVAL = 10;

    private final static Logger logger = Logger.getLogger(MOMessagePump.class);

    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;

    /**
     * Constructs MOMessagePump instance. 
     * 
     * @param src source channel
     * @param dst destination channel
     * @param heartbeatInterval heartbeat interval in milliseconds
     */
    public MOMessagePump(MAVLinkChannel src, MAVLinkChannel dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void run() {
        logger.debug("MOMessagePump started.");

        while (true) {
            try {
                MAVLinkPacket packet = src.receiveMessage();

                dst.sendMessage(packet);

                Thread.sleep(MO_MESSAGE_PUMP_INTERVAL);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException e) {
                dst.close();
                logger.debug("MOMessagePump interrupted.");
                return;
            }
        }
    }

}