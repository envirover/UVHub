/*
 * Copyright 2021 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.envirover.rockblock;

import com.MAVLink.MAVLinkPacket;

/**
 * Custom MAVLink codec.
 *
 * HIGH_LATENCY2 message encoded with MAVLinkPacket.encodePacket() takes 54
 * bytes, which requires 2 RockBLOCK credits. With the custom serialization
 * sending HIGH_LATENCY2 takes 50 bytes.
 *
 * For MAVLink 2.0 the custom serialization does not include compatibility flags,
 * incompatibility flags, and the checksum.
 */
public class CustomEncoder {

    /**
     * Encodes MAVLink packet using custom structure.
     *
     * @param packet MAVLink packet
     * @return bytes of encoded message
     */
    public static byte[] encodePacket(MAVLinkPacket packet) {
        if (packet == null) {
            return null;
        }

        final int bufLen;
        final int payloadSize;
        final byte[] buffer;
        int i = 0;

        if (packet.isMavlink2) {
            payloadSize = mavTrimPayload(packet.payload.payload.array());
            bufLen = 8 + payloadSize;

            buffer = new byte[bufLen];

            buffer[i++] = (byte) MAVLinkPacket.MAVLINK_STX_MAVLINK2;
            buffer[i++] = (byte) payloadSize;
            buffer[i++] = (byte) packet.seq;
            buffer[i++] = (byte) packet.sysid;
            buffer[i++] = (byte) packet.compid;
            buffer[i++] = (byte) (packet.msgid & 0XFF);
            buffer[i++] = (byte) ((packet.msgid >>> 8) & 0XFF);
            buffer[i++] = (byte) ((packet.msgid >>> 16) & 0XFF);

            for (int j = 0; j < payloadSize; ++j) {
                buffer[i++] = packet.payload.payload.get(j);
            }
        } else {
            payloadSize = packet.payload.size();
            bufLen = MAVLinkPacket.MAVLINK1_HEADER_LEN + payloadSize + 2;

            buffer = new byte[bufLen];

            buffer[i++] = (byte) MAVLinkPacket.MAVLINK_STX_MAVLINK1;
            buffer[i++] = (byte) payloadSize;
            buffer[i++] = (byte) packet.seq;
            buffer[i++] = (byte) packet.sysid;
            buffer[i++] = (byte) packet.compid;
            buffer[i++] = (byte) packet.msgid;

            for (int j = 0; j < payloadSize; ++j) {
                buffer[i++] = packet.payload.payload.get(j);
            }

            packet.generateCRC(payloadSize);
            buffer[i++] = (byte) (packet.crc.getLSB());
            buffer[i] = (byte) (packet.crc.getMSB());
        }

        return buffer;
    }

    /**
     * Custom decoder of MAVLink packet.
     *
     * @param buffer bytes of encoded message
     * @return decoded MAVLink packet
     */
    public static MAVLinkPacket decodePacket(byte[] buffer) {
        if (buffer == null) {
            return null;
        }

        final boolean isMavlink2 = ((buffer[0] & 0xff) == MAVLinkPacket.MAVLINK_STX_MAVLINK2);
        final int payloadSize = buffer[1] & 0xff;

        MAVLinkPacket packet = new MAVLinkPacket(payloadSize, isMavlink2);

        packet.compatFlags = 0;
        packet.incompatFlags = 0;
        packet.seq = buffer[2] & 0xff;
        packet.sysid = buffer[3] & 0xff;
        packet.compid = buffer[4] & 0xff;

        if (isMavlink2) {
            int b0 = buffer[5] & 0xff;
            int b1 = buffer[6] & 0xff;
            int b2 = buffer[7] & 0xff;
            packet.msgid = b0 | (b1 << 8) | (b2 << 16);

            for (int i = 0; i < payloadSize; i++) {
                packet.payload.add(buffer[8 + i]);
            }
        } else {
            packet.msgid = buffer[5] & 0xff;

            for (int i = 0; i < payloadSize; i++) {
                packet.payload.add(buffer[6 + i]);
            }
        }

        packet.generateCRC(payloadSize);
        return packet;
    }

    /**
     * Return length of actual data after triming zeros at the end.
     *
     * @param payload payload bytes
     * @return minimum length of valid data
     */
    private static int mavTrimPayload(final byte[] payload) {
        int length = payload.length;
        while (length > 1 && payload[length-1] == 0) {
            length--;
        }
        return length;
    }
}
