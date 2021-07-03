package com.envirover.rockblock;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_high_latency2;
import org.junit.Test;

public class CustomEncoderTest {

    @Test
    public void testMAVLink2Encoding() {
        msg_high_latency2 msg = new msg_high_latency2();
        msg.isMavlink2 = true;
        msg.airspeed = 1;
        msg.custom2 = 1;
        MAVLinkPacket originalPacket = msg.pack();
        originalPacket.generateCRC(originalPacket.payload.size());
        byte[] buffer = CustomEncoder.encodePacket(originalPacket);
        MAVLinkPacket decodedPacket = CustomEncoder.decodePacket(buffer);
        assert(originalPacket.msgid == decodedPacket.msgid);
        assert(originalPacket.incompatFlags == decodedPacket.incompatFlags);
        assert(originalPacket.compatFlags == decodedPacket.compatFlags);
        assert(originalPacket.compid == decodedPacket.compid);
        assert(originalPacket.sysid == decodedPacket.sysid);
        assert(originalPacket.seq == decodedPacket.seq);
        assert(originalPacket.len == decodedPacket.len);
        assert(originalPacket.crc.getLSB() == decodedPacket.crc.getLSB());
        assert(originalPacket.crc.getMSB() == decodedPacket.crc.getMSB());
    }

    @Test
    public void testMAVLink1Encoding() {
        msg_high_latency msg = new msg_high_latency();
        msg.isMavlink2 = false;
        msg.airspeed = 1;
        msg.wp_num = 1;
        MAVLinkPacket originalPacket = msg.pack();
        originalPacket.generateCRC(originalPacket.payload.size());
        byte[] buffer = CustomEncoder.encodePacket(originalPacket);
        MAVLinkPacket decodedPacket = CustomEncoder.decodePacket(buffer);
        assert(originalPacket.msgid == decodedPacket.msgid);
        assert(originalPacket.incompatFlags == decodedPacket.incompatFlags);
        assert(originalPacket.compatFlags == decodedPacket.compatFlags);
        assert(originalPacket.compid == decodedPacket.compid);
        assert(originalPacket.sysid == decodedPacket.sysid);
        assert(originalPacket.seq == decodedPacket.seq);
        assert(originalPacket.len == decodedPacket.len);
        assert(originalPacket.crc.getLSB() == decodedPacket.crc.getLSB());
        assert(originalPacket.crc.getMSB() == decodedPacket.crc.getMSB());
    }
}
