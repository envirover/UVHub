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
package com.envirover.nvi.stream;

import java.io.IOException;
import java.util.Date;

import com.MAVLink.MAVLinkPacket;

/**
 * Output stream of MAVLink packets.
 *
 */
public interface MAVLinkOutputStream {

    /**
     * Opens the stream.
     * 
     * @throws IOException
     */
    void open() throws IOException;

    /**
     * Closes the stream.
     * 
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * Writes MAVLink packet to the stream. 
     * 
     * @param deviceId device Id, such as RockBLOCK IMEI
     * @param time the packet time stamp in UTC time
     * @param packet MAVLink packet
     * @throws IOException
     */
    void writePacket(String deviceId, Date time, MAVLinkPacket packet) throws IOException;

}
