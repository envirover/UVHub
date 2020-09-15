/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
