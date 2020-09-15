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

package com.envirover.mavlink;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;

/**
 * MAVLinkChannel implementation used to send and receive MAVLink messages
 * to/from server socket.
 *
 * @author Pavel Bobov
 */
public class MAVLinkSocket implements MAVLinkChannel {

    private final static Logger logger = LogManager.getLogger(MAVLinkSocket.class);

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    private int seq = 0;
    private AtomicBoolean isOpen = new AtomicBoolean();

    /**
     * Constructs instance of MAVLinkSocket.
     * 
     * @param socket server socket
     * @throws IOException if instance construction failed.
     */
    public MAVLinkSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.isOpen.set(true);
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        if (!isOpen.get()) {
            throw new IOException("Failed to receive message. The socket is closed.");
        }

        synchronized (in) {
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
        }

        logger.warn("Failed to parse MAVLink message.");

        return null;
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        if (!isOpen.get()) {
            throw new IOException("Failed to send message. The socket is closed.");
        }

        if (packet == null)
            return;

        synchronized (out) {
            packet.seq = seq++;

            byte[] data = packet.encodePacket();

            out.write(data);
            out.flush();
        }

        MAVLinkLogger.log(Level.DEBUG, ">>", packet);
    }

    @Override
    public void close() {
        if (isOpen.getAndSet(false)) {
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

}
