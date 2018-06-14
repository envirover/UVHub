/*
 * Envirover confidential
 * 
 *  [2018] Envirover
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
import java.util.HashMap;
import java.util.Map;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.spl.stream.MAVLinkOutputStream;
import com.envirover.spl.stream.MAVLinkOutputStreamFactory;

/**
 * Handles MAVLink messages received from RadioRoom TCP client.
 *
 * @author Pavel Bobov
 */
public class RRClientSession implements ClientSession {

    private final MAVLinkChannel dst;
    private final MAVLinkOutputStream stream;
    
    private boolean isOpen = false;

    /**
     * Constructs instance of RRClientSession.
     * 
     * @param dst destination MAVLink message channel (mobile originated message handler).
     */
    public RRClientSession(MAVLinkChannel dst, MAVLinkOutputStream stream) {
        this.dst = dst;
        this.stream = stream;
    }

    @Override
    public void onOpen() throws IOException {
    	isOpen = true;
    }

    @Override
    public void onClose() throws IOException {
    	isOpen = false;
    }

    @Override
    public void onMessage(MAVLinkPacket packet) throws IOException {
        dst.sendMessage(packet);
        
        // Write the message to the persistent store.
        if (stream != null) {
        	Map<String, String> metadata = new HashMap<String, String>();
        	metadata.put("channel", "tcp");
        	stream.writePacket(packet, metadata);
        }
    }

	@Override
	public boolean isOpen() {
		return isOpen;
	}

}
