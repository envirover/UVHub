package com.envirover.spl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;

public class MAVLinkSocket implements MAVLinkChannel {

    private final DataInputStream in;
    private final DataOutputStream out;
    private final Parser parser = new Parser();
    private final ServerSocket socket;

    public MAVLinkSocket(int port) throws IOException {
        socket = new ServerSocket(port);
        Socket connectionSocket = socket.accept();

        in = new DataInputStream(connectionSocket.getInputStream());
        out = new DataOutputStream(connectionSocket.getOutputStream());
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        MAVLinkPacket packet;

        do {
            int c = in.readUnsignedByte();
            packet = parser.mavlink_parse_char(c);
        } while (packet == null);

        return packet;
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        byte[] data = packet.encodePacket();
        out.write(data);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}
