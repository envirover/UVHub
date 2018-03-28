/*
This file is part of SPLStream application.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLStrean is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SPLStream.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl.stream;

import java.io.IOException;

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
     * @param imei device Id, such as RockBLOCK IMEI
     * @param momsn TODO
     * @param transmitTime the packet time stamp in UTC time
     * @param iridiumLatitude TODO
     * @param iridiumLongitude TODO
     * @param iridiumCep TODO
     * @param packet MAVLink packet
     * @throws IOException
     */
    void writePacket(String imei, String momsn, String transmitTime, String iridiumLatitude, String iridiumLongitude, String iridiumCep, MAVLinkPacket packet) throws IOException;

}
