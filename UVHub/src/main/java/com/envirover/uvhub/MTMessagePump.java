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

package com.envirover.uvhub;

import java.io.IOException;

import com.envirover.rockblock.RockBlockHttpHandler;
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

    private final static long MT_MESSAGE_PUMP_INTERVAL = 10; // 10 milliseconds

    private final static Logger logger = LogManager.getLogger(MTMessagePump.class);

    private final MAVLinkChannel src;
    private final RRTcpServer tcpServer;
    private final MAVLinkChannel rockblock;
    private final MOMessageHandler moMessageHandler;

    /**
     * Constructs instance of MTMessagePump.
     * 
     * @param src source messages channel
     * @param tcpServer RadioRoom TCP server
     * @param rockblock RockBLOCK messages channel
     */
    public MTMessagePump(MAVLinkChannel src, RRTcpServer tcpServer, MAVLinkChannel rockblock, MOMessageHandler moMessageHandler) {
        this.src = src;
        this.tcpServer = tcpServer;
        this.rockblock = rockblock;
        this.moMessageHandler = moMessageHandler;
    }

    /**
     * Returns the active channel.
     *
     * @return active channel
     */
    private MAVLinkChannel getActiveChannel() {
        if (RockBlockHttpHandler.CHANNEL_NAME.equals(moMessageHandler.getActiveChannelName())) {
            return rockblock;
        }

        return tcpServer.getClientSocket();
    }

    @Override
    public void run() {
        logger.debug("MTMessagePump started.");

        while (true) {
            try {
                MAVLinkPacket packet = src.receiveMessage();

                if (packet != null) {
                    MAVLinkChannel channel = getActiveChannel();
                    if (channel != null) {
                        channel.sendMessage(packet);
                    }
                }

                Thread.sleep(MT_MESSAGE_PUMP_INTERVAL);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException e) {
                logger.debug("MTMessagePump interrupted.");
                return;
            }
        }
    }

}
