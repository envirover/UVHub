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

package com.envirover.uvhub;

import java.io.IOException;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;

/*
 * MAVLink client sessions that handle TCP communications with RadioRoom clients.
 *
 */
public class RRClientSession implements ClientSession {

    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;
    private final Thread thread;

    public RRClientSession(MAVLinkChannel src, MAVLinkChannel dst, MAVLinkChannel mtMessageQueue) {
        this.src = src;
        this.dst = dst;
        this.thread = new Thread(new MTMessagePump(mtMessageQueue, src));
    }

    @Override
    public void onOpen() {
    	thread.start();
    }

    @Override
    public void onClose() throws InterruptedException {
    	thread.interrupt();
    	src.close();
    }

    @Override
    public void onMessage(MAVLinkPacket packet) throws IOException, InterruptedException {
        dst.sendMessage(packet);
    }

}
