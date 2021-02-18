/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE CAMERA_CAPTURE_STATUS PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
 * Information about the status of a capture. Can be requested with a MAV_CMD_REQUEST_MESSAGE command.
 */
public class msg_camera_capture_status extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_CAMERA_CAPTURE_STATUS = 262;
    public static final int MAVLINK_MSG_LENGTH = 22;
    private static final long serialVersionUID = MAVLINK_MSG_ID_CAMERA_CAPTURE_STATUS;

      
    /**
     * Timestamp (time since system boot).
     */
    public long time_boot_ms;
      
    /**
     * Image capture interval
     */
    public float image_interval;
      
    /**
     * Time since recording started
     */
    public long recording_time_ms;
      
    /**
     * Available storage capacity.
     */
    public float available_capacity;
      
    /**
     * Current status of image capturing (0: idle, 1: capture in progress, 2: interval set but idle, 3: interval set and capture in progress)
     */
    public short image_status;
      
    /**
     * Current status of video capturing (0: idle, 1: capture in progress)
     */
    public short video_status;
      
    /**
     * Total number of images captured ('forever', or until reset using MAV_CMD_STORAGE_FORMAT).
     */
    public int image_count;
    

    /**
     * Generates the payload for a mavlink message for a message of this type
     * @return
     */
    @Override
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH,isMavlink2);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_CAMERA_CAPTURE_STATUS;
        
        packet.payload.putUnsignedInt(time_boot_ms);
        packet.payload.putFloat(image_interval);
        packet.payload.putUnsignedInt(recording_time_ms);
        packet.payload.putFloat(available_capacity);
        packet.payload.putUnsignedByte(image_status);
        packet.payload.putUnsignedByte(video_status);
        
        if (isMavlink2) {
             packet.payload.putInt(image_count);
            
        }
        return packet;
    }

    /**
     * Decode a camera_capture_status message into this class fields
     *
     * @param payload The message to decode
     */
    @Override
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        
        this.time_boot_ms = payload.getUnsignedInt();
        this.image_interval = payload.getFloat();
        this.recording_time_ms = payload.getUnsignedInt();
        this.available_capacity = payload.getFloat();
        this.image_status = payload.getUnsignedByte();
        this.video_status = payload.getUnsignedByte();
        
        if (isMavlink2) {
             this.image_count = payload.getInt();
            
        }
    }

    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_camera_capture_status() {
        this.msgid = MAVLINK_MSG_ID_CAMERA_CAPTURE_STATUS;
    }
    
    /**
     * Constructor for a new message, initializes msgid and all payload variables
     */
    public msg_camera_capture_status( long time_boot_ms, float image_interval, long recording_time_ms, float available_capacity, short image_status, short video_status, int image_count) {
        this.msgid = MAVLINK_MSG_ID_CAMERA_CAPTURE_STATUS;

        this.time_boot_ms = time_boot_ms;
        this.image_interval = image_interval;
        this.recording_time_ms = recording_time_ms;
        this.available_capacity = available_capacity;
        this.image_status = image_status;
        this.video_status = video_status;
        this.image_count = image_count;
        
    }
    
    /**
     * Constructor for a new message, initializes everything
     */
    public msg_camera_capture_status( long time_boot_ms, float image_interval, long recording_time_ms, float available_capacity, short image_status, short video_status, int image_count, int sysid, int compid, boolean isMavlink2) {
        this.msgid = MAVLINK_MSG_ID_CAMERA_CAPTURE_STATUS;
        this.sysid = sysid;
        this.compid = compid;
        this.isMavlink2 = isMavlink2;

        this.time_boot_ms = time_boot_ms;
        this.image_interval = image_interval;
        this.recording_time_ms = recording_time_ms;
        this.available_capacity = available_capacity;
        this.image_status = image_status;
        this.video_status = video_status;
        this.image_count = image_count;
        
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     *
     */
    public msg_camera_capture_status(MAVLinkPacket mavLinkPacket) {
        this.msgid = MAVLINK_MSG_ID_CAMERA_CAPTURE_STATUS;
        
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
        return "MAVLINK_MSG_ID_CAMERA_CAPTURE_STATUS - sysid:"+sysid+" compid:"+compid+" time_boot_ms:"+time_boot_ms+" image_interval:"+image_interval+" recording_time_ms:"+recording_time_ms+" available_capacity:"+available_capacity+" image_status:"+image_status+" video_status:"+video_status+" image_count:"+image_count+"";
    }
    
    /**
     * Returns a human-readable string of the name of the message
     */
    @Override
    public String name() {
        return "MAVLINK_MSG_ID_CAMERA_CAPTURE_STATUS";
    }
}
        