package com.envirover.mavlink;

import java.io.IOException;

import com.MAVLink.MAVLinkPacket;

/**
 * Interface for two-way MAVLink message channels.
 *
 */
public interface MAVLinkChannel {

    /**
     * Receives MAVLink message from the channel.
     * 
     * @return MAVLink message packet
     * @throws IOException
     */
    MAVLinkPacket receiveMessage() throws IOException;

    /**
     * Sends MAVLink message to the channel.
     * 
     * @param packet MAVLink message packet to sent.
     * @throws IOException
     */
    void sendMessage(MAVLinkPacket packet) throws IOException;

    /**
     * Closes the channel.
     */
    void close();
}
