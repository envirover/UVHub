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

        // The maximum size of MAVLink packet is 263 bytes.
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
