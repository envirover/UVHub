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

package com.envirover.nvi;

import java.io.IOException;

import com.MAVLink.MAVLinkPacket;


/*
 * TCP and WebSocket MAVLink client sessions that handle communications with GCS clients.
 *
 */
public interface ClientSession {

    /**
     * Called when client is connected.
     */
    void onOpen();

    /**
     * Called when client is disconnected.
     * 
     * @throws InterruptedException
     */
    void onClose() throws InterruptedException;

    /**
     * Called when MAVLink message is received from the client.
     * 
     * @param packet MAVLink packet 
     * @throws IOException thrown in case of I/O errors.
     * @throws InterruptedException
     */
    void onMessage(MAVLinkPacket packet) throws IOException, InterruptedException;

}