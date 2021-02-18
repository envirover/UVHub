/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE ESC_INFO PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
 * ESC information for lower rate streaming. Recommended streaming rate 1Hz. See ESC_STATUS for higher-rate ESC data.
 */
public class msg_esc_info extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_ESC_INFO = 290;
    public static final int MAVLINK_MSG_LENGTH = 42;
    private static final long serialVersionUID = MAVLINK_MSG_ID_ESC_INFO;

      
    /**
     * Timestamp (UNIX Epoch time or time since system boot). The receiving end can infer timestamp format (since 1.1.1970 or since system boot) by checking for the magnitude the number.
     */
    public long time_usec;
      
    /**
     * Number of reported errors by each ESC since boot.
     */
    public long error_count[] = new long[4];
      
    /**
     * Counter of data packets received.
     */
    public int counter;
      
    /**
     * Bitmap of ESC failure flags.
     */
    public int failure_flags[] = new int[4];
      
    /**
     * Index of the first ESC in this message. minValue = 0, maxValue = 60, increment = 4.
     */
    public short index;
      
    /**
     * Total number of ESCs in all messages of this type. Message fields with an index higher than this should be ignored because they contain invalid data.
     */
    public short count;
      
    /**
     * Connection type protocol for all ESC.
     */
    public short connection_type;
      
    /**
     * Information regarding online/offline status of each ESC.
     */
    public short info;
      
    /**
     * Temperature measured by each ESC. UINT8_MAX if data not supplied by ESC.
     */
    public short temperature[] = new short[4];
    

    /**
     * Generates the payload for a mavlink message for a message of this type
     * @return
     */
    @Override
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH,isMavlink2);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_ESC_INFO;
        
        packet.payload.putUnsignedLong(time_usec);
        
        for (int i = 0; i < error_count.length; i++) {
            packet.payload.putUnsignedInt(error_count[i]);
        }
                    
        packet.payload.putUnsignedShort(counter);
        
        for (int i = 0; i < failure_flags.length; i++) {
            packet.payload.putUnsignedShort(failure_flags[i]);
        }
                    
        packet.payload.putUnsignedByte(index);
        packet.payload.putUnsignedByte(count);
        packet.payload.putUnsignedByte(connection_type);
        packet.payload.putUnsignedByte(info);
        
        for (int i = 0; i < temperature.length; i++) {
            packet.payload.putUnsignedByte(temperature[i]);
        }
                    
        
        if (isMavlink2) {
            
        }
        return packet;
    }

    /**
     * Decode a esc_info message into this class fields
     *
     * @param payload The message to decode
     */
    @Override
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        
        this.time_usec = payload.getUnsignedLong();
         
        for (int i = 0; i < this.error_count.length; i++) {
            this.error_count[i] = payload.getUnsignedInt();
        }
                
        this.counter = payload.getUnsignedShort();
         
        for (int i = 0; i < this.failure_flags.length; i++) {
            this.failure_flags[i] = payload.getUnsignedShort();
        }
                
        this.index = payload.getUnsignedByte();
        this.count = payload.getUnsignedByte();
        this.connection_type = payload.getUnsignedByte();
        this.info = payload.getUnsignedByte();
         
        for (int i = 0; i < this.temperature.length; i++) {
            this.temperature[i] = payload.getUnsignedByte();
        }
                
        
        if (isMavlink2) {
            
        }
    }

    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_esc_info() {
        this.msgid = MAVLINK_MSG_ID_ESC_INFO;
    }
    
    /**
     * Constructor for a new message, initializes msgid and all payload variables
     */
    public msg_esc_info( long time_usec, long[] error_count, int counter, int[] failure_flags, short index, short count, short connection_type, short info, short[] temperature) {
        this.msgid = MAVLINK_MSG_ID_ESC_INFO;

        this.time_usec = time_usec;
        this.error_count = error_count;
        this.counter = counter;
        this.failure_flags = failure_flags;
        this.index = index;
        this.count = count;
        this.connection_type = connection_type;
        this.info = info;
        this.temperature = temperature;
        
    }
    
    /**
     * Constructor for a new message, initializes everything
     */
    public msg_esc_info( long time_usec, long[] error_count, int counter, int[] failure_flags, short index, short count, short connection_type, short info, short[] temperature, int sysid, int compid, boolean isMavlink2) {
        this.msgid = MAVLINK_MSG_ID_ESC_INFO;
        this.sysid = sysid;
        this.compid = compid;
        this.isMavlink2 = isMavlink2;

        this.time_usec = time_usec;
        this.error_count = error_count;
        this.counter = counter;
        this.failure_flags = failure_flags;
        this.index = index;
        this.count = count;
        this.connection_type = connection_type;
        this.info = info;
        this.temperature = temperature;
        
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     *
     */
    public msg_esc_info(MAVLinkPacket mavLinkPacket) {
        this.msgid = MAVLINK_MSG_ID_ESC_INFO;
        
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
        return "MAVLINK_MSG_ID_ESC_INFO - sysid:"+sysid+" compid:"+compid+" time_usec:"+time_usec+" error_count:"+error_count+" counter:"+counter+" failure_flags:"+failure_flags+" index:"+index+" count:"+count+" connection_type:"+connection_type+" info:"+info+" temperature:"+temperature+"";
    }
    
    /**
     * Returns a human-readable string of the name of the message
     */
    @Override
    public String name() {
        return "MAVLINK_MSG_ID_ESC_INFO";
    }
}
        