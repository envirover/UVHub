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

import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;

/**
 * Mobile-originated (MO) message pump receives MAVLink messages from the 
 * specified source channel and forwards them to the specified destination
 * channel. 
 */
class MOMessagePump implements Runnable {

    private final static long MO_MESSAGE_PUMP_INTERVAL = 10; //10 milliseconds

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