/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE FLIGHT_INFORMATION PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
 * Information about flight since last arming.
 */
public class msg_flight_information extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_FLIGHT_INFORMATION = 264;
    public static final int MAVLINK_MSG_LENGTH = 28;
    private static final long serialVersionUID = MAVLINK_MSG_ID_FLIGHT_INFORMATION;

      
    /**
     * Timestamp at arming (time since UNIX epoch) in UTC, 0 for unknown
     */
    public long arming_time_utc;
      
    /**
     * Timestamp at takeoff (time since UNIX epoch) in UTC, 0 for unknown
     */
    public long takeoff_time_utc;
      
    /**
     * Universally unique identifier (UUID) of flight, should correspond to name of log files
     */
    public long flight_uuid;
      
    /**
     * Timestamp (time since system boot).
     */
    public long time_boot_ms;
    

    /**
     * Generates the payload for a mavlink message for a message of this type
     * @return
     */
    @Override
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH,isMavlink2);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_FLIGHT_INFORMATION;
        
        packet.payload.putUnsignedLong(arming_time_utc);
        packet.payload.putUnsignedLong(takeoff_time_utc);
        packet.payload.putUnsignedLong(flight_uuid);
        packet.payload.putUnsignedInt(time_boot_ms);
        
        if (isMavlink2) {
            
        }
        return packet;
    }

    /**
     * Decode a flight_information message into this class fields
     *
     * @param payload The message to decode
     */
    @Override
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        
        this.arming_time_utc = payload.getUnsignedLong();
        this.takeoff_time_utc = payload.getUnsignedLong();
        this.flight_uuid = payload.getUnsignedLong();
        this.time_boot_ms = payload.getUnsignedInt();
        
        if (isMavlink2) {
            
        }
    }

    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_flight_information() {
        this.msgid = MAVLINK_MSG_ID_FLIGHT_INFORMATION;
    }
    
    /**
     * Constructor for a new message, initializes msgid and all payload variables
     */
    public msg_flight_information( long arming_time_utc, long takeoff_time_utc, long flight_uuid, long time_boot_ms) {
        this.msgid = MAVLINK_MSG_ID_FLIGHT_INFORMATION;

        this.arming_time_utc = arming_time_utc;
        this.takeoff_time_utc = takeoff_time_utc;
        this.flight_uuid = flight_uuid;
        this.time_boot_ms = time_boot_ms;
        
    }
    
    /**
     * Constructor for a new message, initializes everything
     */
    public msg_flight_information( long arming_time_utc, long takeoff_time_utc, long flight_uuid, long time_boot_ms, int sysid, int compid, boolean isMavlink2) {
        this.msgid = MAVLINK_MSG_ID_FLIGHT_INFORMATION;
        this.sysid = sysid;
        this.compid = compid;
        this.isMavlink2 = isMavlink2;

        this.arming_time_utc = arming_time_utc;
        this.takeoff_time_utc = takeoff_time_utc;
        this.flight_uuid = flight_uuid;
        this.time_boot_ms = time_boot_ms;
        
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     *
     */
    public msg_flight_information(MAVLinkPacket mavLinkPacket) {
        this.msgid = MAVLINK_MSG_ID_FLIGHT_INFORMATION;
        
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
        return "MAVLINK_MSG_ID_FLIGHT_INFORMATION - sysid:"+sysid+" compid:"+compid+" arming_time_utc:"+arming_time_utc+" takeoff_time_utc:"+takeoff_time_utc+" flight_uuid:"+flight_uuid+" time_boot_ms:"+time_boot_ms+"";
    }
    
    /**
     * Returns a human-readable string of the name of the message
     */
    @Override
    public String name() {
        return "MAVLINK_MSG_ID_FLIGHT_INFORMATION";
    }
}
        