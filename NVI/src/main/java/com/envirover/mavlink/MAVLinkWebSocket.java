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

package com.envirover.mavlink;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.Session;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;

public class MAVLinkWebSocket implements MAVLinkChannel {

    private final static Logger logger = Logger.getLogger(MAVLinkWebSocket.class);
    private final Session session;
    private final Object sendLock = new Object();

    private int seq = 0;

    public MAVLinkWebSocket(Session session) {
        this.session = session;
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        return null;
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        if (packet == null)
            return;

        if (session.isOpen()) {
            synchronized(sendLock) {
                try {
                    packet.seq = seq++;

                    byte[] data = packet.encodePacket();

                    session.getBasicRemote().sendBinary(ByteBuffer.wrap(data));

                    MAVLinkLogger.log(Level.DEBUG, ">>", packet);
                } catch (IOException ex) {
                    logger.warn("Failed to send MAVLink message to socket. " + ex.getMessage());
                }
            }
        }

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
