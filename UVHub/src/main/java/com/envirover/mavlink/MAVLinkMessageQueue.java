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
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.MAVLinkPacket;

/**
 * MAVLink message queue channel.
 * 
 * @author Pavel Bobov
 *
 */
public class MAVLinkMessageQueue implements MAVLinkChannel {

    private final static Logger logger = LogManager.getLogger(MAVLinkMessageQueue.class);
    
    private final ConcurrentLinkedQueue<MAVLinkPacket> queue = new ConcurrentLinkedQueue<MAVLinkPacket>(); 
    private final int maxQueueSize;

    /**
     * Constructs instance of MAVLinkMessageQueue.
     * 
     * @param size maximum queue size
     */
    public MAVLinkMessageQueue(int size) {
        this.maxQueueSize = size;
    }

    @Override
    public synchronized MAVLinkPacket receiveMessage() throws IOException {
        return queue.poll();
    }

    @Override
    public synchronized void sendMessage(MAVLinkPacket packet) throws IOException {
        if (queue.size() >= maxQueueSize) {
            queue.poll();
            logger.warn("MAVLink message queue is longer than MaxQueueSize.");
        }

        queue.add(packet);
    }

    @Override
    public void close() {
    }

}
