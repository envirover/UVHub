/*
 * Envirover confidential
 * 
 *  [2018] Envirover
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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;

/**
 * Mobile-terminated message pump receives MAVLink messages from the specified 
 * source channel and forwards them to the specified destination channels. 
 * 
 * If RadioRoom client is connected, the TCP channel is used. If the TCP client
 * is not connected, or sending message to the TCP channel failed, the message
 * is sent to RockBLOCK.
 * 
 * @author Pavel Bobov
 */
public class MTMessagePump implements Runnable {

    private final static long MT_MESSAGE_PUMP_INTERVAL = 10; //10 milliseconds

    private final static Logger logger = LogManager.getLogger(MTMessagePump.class);

    private final MAVLinkChannel src;
    private final RRTcpServer    tcpServer;
    private final MAVLinkChannel rockblock;

    /**
     * Constructs instance of MTMessagePump.
     * 
     * @param src source messages channel
     * @param tcpServer RadioRoom TCP server
     * @param rockblock RockBLOCK messages channel
     */
    public MTMessagePump(MAVLinkChannel src, RRTcpServer tcpServer, MAVLinkChannel rockblock) {
        this.src = src;
        this.tcpServer = tcpServer;
        this.rockblock = rockblock;
    }

    @Override
    public void run() {
        logger.debug("MTMessagePump started.");

        while(true) {
            try {
                MAVLinkPacket packet = src.receiveMessage();

                if (packet != null) {
                	boolean sent = false;
                	
                	if (tcpServer != null) {
	                	MAVLinkChannel tcpSocket = tcpServer.getMAVLinkSocket();
	                	
	                	// Try the TCP channel first if it's active.
	                	if (tcpSocket != null) {
	                		try {
	                			tcpSocket.sendMessage(packet);
	                			sent = true;
	                		} catch(IOException ex) {
	                			logger.error(ex.getMessage());
	                		}
	                	}
                	}
                	
                	// Send the message to the secondary RockBLOCK channel if
                	// If the TCP channel is not active, or sending message to
                	// the TCP channel failed.
                	if (!sent && rockblock != null) {
                		rockblock.sendMessage(packet);
                	}
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
