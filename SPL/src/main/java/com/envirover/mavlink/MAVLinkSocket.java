package com.envirover.mavlink;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;


/**
 * MAVLinkChannel implementation used to sends/receives messages to/from server sockets.
 *
 */
public class MAVLinkSocket implements MAVLinkChannel {

    private final static Logger logger = Logger.getLogger(MAVLinkSocket.class);

    private final ServerSocket socket;

    private Socket connection = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

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
            System.out.printf("Waiting for MAVLink client connection on tcp://%s:%d...",
                    socket.getInetAddress().getHostAddress(), socket.getLocalPort());
            System.out.println();

            connection = socket.accept();
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(connection.getOutputStream());

            logger.info("MAVLink client connected.");
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

        try {
            do {
                try {
                    int c = in.readUnsignedByte();
                    packet = parser.mavlink_parse_char(c);
                } catch(java.io.EOFException ex) {
                    return null;
                }
            } while (packet == null);
        } catch (SocketException ex) {
            logger.info("MAVLink client disconnected.");
            closeConnection();
        }

        if (packet != null) {
            logger.debug(MessageFormat.format("MAVLink message received: msgid = {0}, seq={1}", packet.msgid, packet.seq));
        }

        return packet;
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        if (packet == null)
            return;

        connect();

        try {
            if (isConnected()) {
                byte[] data = packet.encodePacket();
                out.write(data);
                out.flush();

                logger.debug(MessageFormat.format("MAVLink message sent: msgid = {0}, seq={1}", packet.msgid, packet.seq));
            }
        } catch (SocketException ex) {
            logger.info("MAVLink client disconnected.");
            closeConnection();
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
    }

}
