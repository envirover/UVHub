package com.envirover.spl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;

public class MAVLinkSocket implements MAVLinkChannel {

    private final Parser parser = new Parser();
    private final ServerSocket socket;

    private Socket connection;
    private DataInputStream in;
    private DataOutputStream out;

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

    public boolean isConnected() {
        return !socket.isClosed() && connection != null && connection.isConnected();
    }

    @Override
    public synchronized MAVLinkPacket receiveMessage() throws IOException {
        MAVLinkPacket packet = null;

        connect();

        if (isConnected()) {
            do {
                int c = in.readUnsignedByte();
                packet = parser.mavlink_parse_char(c);
            } while (packet == null);
        }

        return packet;
    }

    @Override
    public synchronized void sendMessage(MAVLinkPacket packet) throws IOException {
        connect();

        if (isConnected()) {
            byte[] data = packet.encodePacket();
            out.write(data);
            out.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }

        if (out != null) {
            out.close();
        }

        if (connection != null) {
            connection.close();
        }

        socket.close();
    }

}
