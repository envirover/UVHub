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

import com.MAVLink.MAVLinkPacket;


/**
 * Client sessions that handle communications with TCP MAVLink clients.
 * 
 * @author Pavel Bobov
 *
 */
public interface ClientSession {

    /**
     * Called when client is connected.
     * 
     * @throws IOException if the session start failed.
     */
    void onOpen() throws IOException;

    /**
     * Called when client is disconnected.
     * 
     * @throws IOException if closing the session failed.
     */
    void onClose() throws IOException;

    /**
     * Called when MAVLink message is received from the client.
     * 
     * @param packet MAVLink packet 
     * @throws IOException thrown in case of I/O errors.
     */
    void onMessage(MAVLinkPacket packet) throws IOException;
    
    /**
     * Returns true if the client session is open.
     * 
     * @return true if the client session is open.
     */
    boolean isOpen();

}