package com.envirover.spl;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.MAVLink.MAVLinkPacket;

public class MAVLinkMessageQueue implements MAVLinkChannel {

    private final ConcurrentLinkedQueue<MAVLinkPacket> queue = new ConcurrentLinkedQueue<MAVLinkPacket>(); 

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        return queue.poll();
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        queue.add(packet);
    }

    @Override
    public void close() throws IOException {
    }

}
