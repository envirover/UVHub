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
along with SPLGroundControl.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.mavlink;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;

/**
 * MAVLinkChannel implementation used to send and receive MAVLink messages
 * to/from server socket.
 *
 */
public class MAVLinkSocket implements MAVLinkChannel {

    private final static Logger logger = Logger.getLogger(MAVLinkSocket.class);

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    private int seq = 0;

    /**
     * Constructs instance of MAVLinkSocket.
     * 
     * @param socket server socket
     * @throws IOException
     */
    public MAVLinkSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        Parser parser = new Parser();

        MAVLinkPacket packet = null;

        // The maximum size of MAVLink packet is 261 bytes.
        for (int i = 0; i < 263 * 2; i++) {
            try {
                int c = in.readUnsignedByte();
                packet = parser.mavlink_parse_char(c);
            } catch (java.io.EOFException ex) {
                return null;
            }

            if (packet != null) {
                MAVLinkLogger.log(Level.DEBUG, "<<", packet);
                return packet;
            }
        }

        logger.warn("Failed to parse MAVLink message.");

        return null;
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        if (packet == null)
            return;

        packet.seq = seq++;

        byte[] data = packet.encodePacket();

        out.write(data);
        out.flush();

        MAVLinkLogger.log(Level.DEBUG, ">>", packet);
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
