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
 * Mobile-terminated message pump receives MAVLink messages from the specified 
 * source channel and forwards them to the specified destination channel. 
 */
public class MTMessagePump implements Runnable {

    private final static long MT_MESSAGE_PUMP_INTERVAL = 10; //10 milliseconds

    private final static Logger logger = Logger.getLogger(MTMessagePump.class);

    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;

    /**
     * Constructs instance of MTMessagePump
     * 
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
                    dst.sendMessage(packet);
                }

                Thread.sleep(MT_MESSAGE_PUMP_INTERVAL);
            } catch(IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException e) {
                logger.debug("MTMessagePump interrupted.");
                return;
            }
        }
    }

}
