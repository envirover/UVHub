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
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;


/**
 * MAVLinkChannel implementation used to send and receive MAVLink messages to/from server socket.
 *
 */
public class MAVLinkSocket implements MAVLinkChannel {

    private final static Logger logger = Logger.getLogger(MAVLinkSocket.class);

    private final ServerSocket socket;
    private final Object sendLock = new Object();
    private final Object receiveLock = new Object(); 

    private Socket connection = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private int seq = 0;

    /**
     * Constructs instance of MAVLinkSocket.
     * 
     * @param port server socket port number
     * @throws IOException
     */
    public MAVLinkSocket(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    /**
     * Listens for a connection to be made to the socket and accepts it.
     * The method blocks until a connection is made.
     * 
     * @throws IOException
     */
    public synchronized void connect() throws IOException {
        if (!isConnected()) {
            logger.info("Waiting for MAVLink client connection...");

            connection = socket.accept();
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(connection.getOutputStream());

            logger.info(MessageFormat.format("MAVLink client ''{0}'' connected.", connection.getInetAddress()));
        }
    }

    /**
     * Returns true if the socket is connected to a client.
     * 
     * @return true if the socket is connected to a client.
     */
    public synchronized boolean isConnected() {
        return !socket.isClosed() && connection != null && connection.isConnected();
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        Parser parser = new Parser();

        MAVLinkPacket packet = null;

        connect();

        synchronized(receiveLock) {
            try {
                //The maximum size of MAVLink packet is 261 bytes.
                for (int i = 0; i < 263 * 2; i++)
                {
                    try {
                        int c = in.readUnsignedByte();
                        packet = parser.mavlink_parse_char(c);
                    } catch(java.io.EOFException ex) {
                        return null;
                    }

                    if (packet != null) {
                        MAVLinkLogger.log(Level.DEBUG, "<<", packet);
                        return packet;
                    }
                }
            } catch (IOException ex) {
                logger.warn("Failed to receive MAVLink message from socket. " + ex.getMessage());
                closeConnection();
            }
        }

        logger.warn("Failed to parse MAVLink message.");

        return null;
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        if (packet == null)
            return;

        connect();

        if (isConnected()) {
            synchronized(sendLock) {
                try {
                    packet.seq = seq++;

                    byte[] data = packet.encodePacket();

                    out.write(data);
                    out.flush();

                    MAVLinkLogger.log(Level.DEBUG, ">>", packet);
                } catch (IOException ex) {
                    logger.warn("Failed to send MAVLink message to socket. " + ex.getMessage());
                    closeConnection();
                }
            }
        }
    }

    @Override
    public void close() {
        closeConnection();

        try {
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
                in = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger.info("MAVLink client disconnected.");
    }

}
