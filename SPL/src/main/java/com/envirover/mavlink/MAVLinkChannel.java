/*
This file is part of SPLGroundControl application.

SPLGroundControl is a MAVLink proxy server for ArduPilot rovers with
RockBLOCK satellite communication.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLGroundControl is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Rock7MAVLink.  If not, see <http://www.gnu.org/licenses/>.
*/

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
