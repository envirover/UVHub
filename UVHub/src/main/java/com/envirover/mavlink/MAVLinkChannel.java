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

import com.MAVLink.MAVLinkPacket;

/**
 * Interface for two-way MAVLink message channels.
 * 
 * @author Pavel Bobov
 *
 */
public interface MAVLinkChannel {

    /**
     * Receives MAVLink message from the channel.
     * 
     * @return MAVLink message packet or null.
     * @throws IOException if receiving message failed.
     */
    MAVLinkPacket receiveMessage() throws IOException;

    /**
     * Sends MAVLink message to the channel.
     * 
     * @param packet MAVLink message packet to sent.
     * @throws IOException if sending message failed.
     */
    void sendMessage(MAVLinkPacket packet) throws IOException;

    /**
     * Closes the channel.
     */
    void close();
}
