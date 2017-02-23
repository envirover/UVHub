package com.envirover.spl;

import java.io.IOException;

import com.MAVLink.MAVLinkPacket;

public interface MAVLinkChannel {
    MAVLinkPacket receiveMessage() throws IOException;

    void sendMessage(MAVLinkPacket packet) throws IOException;

    void close();
}
