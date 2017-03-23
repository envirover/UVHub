package com.envirover.mavlink;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_command_int;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_mission_clear_all;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.common.msg_mission_request;
import com.MAVLink.common.msg_mission_request_int;
import com.MAVLink.common.msg_mission_request_list;
import com.MAVLink.common.msg_mission_request_partial_list;
import com.MAVLink.common.msg_mission_set_current;
import com.MAVLink.common.msg_mission_write_partial_list;
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_request_data_stream;
import com.MAVLink.common.msg_set_home_position;
import com.MAVLink.common.msg_set_mode;

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

    //White list message filter
    private static boolean filter(MAVLinkPacket packet) {
        return packet != null &&
              (packet.msgid == msg_set_mode.MAVLINK_MSG_ID_SET_MODE || 
               packet.msgid == msg_param_set.MAVLINK_MSG_ID_PARAM_SET ||
               packet.msgid == msg_mission_request_partial_list.MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST ||
               packet.msgid == msg_mission_write_partial_list.MAVLINK_MSG_ID_MISSION_WRITE_PARTIAL_LIST ||
               packet.msgid == msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM ||
               packet.msgid == msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST ||
               packet.msgid == msg_mission_set_current.MAVLINK_MSG_ID_MISSION_SET_CURRENT ||
               packet.msgid == msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT ||
               packet.msgid == msg_mission_request_list.MAVLINK_MSG_ID_MISSION_REQUEST_LIST ||
               packet.msgid == msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL ||
               packet.msgid == msg_mission_request_int.MAVLINK_MSG_ID_MISSION_REQUEST_INT ||
               packet.msgid == msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT ||
               packet.msgid == msg_command_int.MAVLINK_MSG_ID_COMMAND_INT ||
               packet.msgid == msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG ||
               packet.msgid == msg_set_home_position.MAVLINK_MSG_ID_SET_HOME_POSITION);
    }
    
    /* (blacklist filter)
    // Filter out high frequency messages 
    private static boolean filter(MAVLinkPacket packet) {
        return packet != null &&
               packet.msgid != msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT &&
               packet.msgid != msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST &&
               packet.msgid != msg_request_data_stream.MAVLINK_MSG_ID_REQUEST_DATA_STREAM;
    }
    */
    
}
