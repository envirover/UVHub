/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE DEVICE_OP_WRITE_REPLY PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
 * Write registers reply.
 */
public class msg_device_op_write_reply extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_DEVICE_OP_WRITE_REPLY = 11003;
    public static final int MAVLINK_MSG_LENGTH = 5;
    private static final long serialVersionUID = MAVLINK_MSG_ID_DEVICE_OP_WRITE_REPLY;

      
    /**
     * Request ID - copied from request.
     */
    public long request_id;
      
    /**
     * 0 for success, anything else is failure code.
     */
    public short result;
    

    /**
     * Generates the payload for a mavlink message for a message of this type
     * @return
     */
    @Override
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH,isMavlink2);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_DEVICE_OP_WRITE_REPLY;
        
        packet.payload.putUnsignedInt(request_id);
        packet.payload.putUnsignedByte(result);
        
        if (isMavlink2) {
            
        }
        return packet;
    }

    /**
     * Decode a device_op_write_reply message into this class fields
     *
     * @param payload The message to decode
     */
    @Override
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        
        this.request_id = payload.getUnsignedInt();
        this.result = payload.getUnsignedByte();
        
        if (isMavlink2) {
            
        }
    }

    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_device_op_write_reply() {
        this.msgid = MAVLINK_MSG_ID_DEVICE_OP_WRITE_REPLY;
    }
    
    /**
     * Constructor for a new message, initializes msgid and all payload variables
     */
    public msg_device_op_write_reply( long request_id, short result) {
        this.msgid = MAVLINK_MSG_ID_DEVICE_OP_WRITE_REPLY;

        this.request_id = request_id;
        this.result = result;
        
    }
    
    /**
     * Constructor for a new message, initializes everything
     */
    public msg_device_op_write_reply( long request_id, short result, int sysid, int compid, boolean isMavlink2) {
        this.msgid = MAVLINK_MSG_ID_DEVICE_OP_WRITE_REPLY;
        this.sysid = sysid;
        this.compid = compid;
        this.isMavlink2 = isMavlink2;

        this.request_id = request_id;
        this.result = result;
        
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     *
     */
    public msg_device_op_write_reply(MAVLinkPacket mavLinkPacket) {
        this.msgid = MAVLINK_MSG_ID_DEVICE_OP_WRITE_REPLY;
        
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.isMavlink2 = mavLinkPacket.isMavlink2;
        unpack(mavLinkPacket.payload);
    }

        
    /**
     * Returns a string with the MSG name and data
     */
    @Override
    public String toString() {
        return "MAVLINK_MSG_ID_DEVICE_OP_WRITE_REPLY - sysid:"+sysid+" compid:"+compid+" request_id:"+request_id+" result:"+result+"";
    }
    
    /**
     * Returns a human-readable string of the name of the message
     */
    @Override
    public String name() {
        return "MAVLINK_MSG_ID_DEVICE_OP_WRITE_REPLY";
    }
}
        