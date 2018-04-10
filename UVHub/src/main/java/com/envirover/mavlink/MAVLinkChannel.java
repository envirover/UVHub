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
