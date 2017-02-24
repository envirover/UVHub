package com.envirover.mavlink;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;

public class MAVLinkSocket implements MAVLinkChannel {
    private final ServerSocket socket;

    private Socket connection = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    public MAVLinkSocket(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    public synchronized void connect() throws IOException {
        if (!isConnected()) {
            System.out.printf("Waiting for MAVLink client connection on tcp://%s:%d...",
                    socket.getInetAddress().getHostAddress(), socket.getLocalPort());
            System.out.println();

            connection = socket.accept();
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(connection.getOutputStream());

            System.out.println("MAVLink client connected.");
        }
    }

    public synchronized boolean isConnected() {
        return !socket.isClosed() && connection != null && connection.isConnected();
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        final Parser parser = new Parser();

        MAVLinkPacket packet = null;

        connect();

        try {
            if (isConnected()) {
                do {
                    try {
                        int c = in.readUnsignedByte();
                        packet = parser.mavlink_parse_char(c);
                    } catch(java.io.EOFException ex) {
                    }
                } while (packet == null);
            }
        } catch (SocketException ex) {
            System.out.println("MAVLink client disconnected.");
            closeConnection();
        }

        return packet;
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        connect();

        try {
            if (isConnected()) {
                byte[] data = packet.encodePacket();
                out.write(data);
                out.flush();
            }
        } catch (SocketException ex) {
            System.out.println("MAVLink client disconnected.");
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
