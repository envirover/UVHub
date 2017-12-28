package com.envirover.nvi;

import java.io.IOException;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;

/*
 * MAVLink client sessions that handle TCP communications with RadioRoom clients.
 *
 */
public class RRClientSession {

    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;
    private final Thread thread;

    public RRClientSession(MAVLinkChannel src, MAVLinkChannel dst, MAVLinkChannel mtMessageQueue) {
        this.src = src;
        this.dst = dst;
        this.thread = new Thread(new MTMessagePump(mtMessageQueue, src));
    }

    public void onOpen() {
    	thread.start();
    }

    public void onClose() throws InterruptedException {
    	thread.interrupt();
    	src.close();
    }

    public void onMessage(MAVLinkPacket packet) throws IOException, InterruptedException {
        dst.sendMessage(packet);
    }

}
