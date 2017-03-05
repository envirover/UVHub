package com.envirover.mavlink;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_request_data_stream;

/**
 * Receives MAVLink message packets from a source channel, such as MAVLinkSocket,
 * filters out high frequency messages, and sends the packets to the destination
 * channel, such as MAVLinkMessageQueue.
 */
public class MAVLinkHandler implements Runnable {

    private final static Logger logger = Logger.getLogger(MAVLinkHandler.class);

    private final MAVLinkChannel src;
    private final MAVLinkChannel dst;

    /**
     * Constructs instance of MAVLinkHandler.
     * 
     * @param src source message channel, such as MAVLinkSocket.
     * @param dst destination message channel, such as MAVLinkMessageQueue.
     */
    public MAVLinkHandler(MAVLinkChannel src, MAVLinkChannel dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void run() {
        logger.debug("MAVLinkHandler started.");

        while (true) {
            try {
                MAVLinkPacket packet = src.receiveMessage();

                if (filter(packet))
                    dst.sendMessage(packet);

                Thread.sleep(10);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                logger.debug("MAVLinkHandler interrupted.");
                return;
            }
        }
    }

    // Filter out high frequency messages
    private static boolean filter(MAVLinkPacket packet) {
        return packet != null &&
               packet.msgid != msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT &&
               packet.msgid != msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST &&
               packet.msgid != msg_request_data_stream.MAVLINK_MSG_ID_REQUEST_DATA_STREAM;
    }
    
}
