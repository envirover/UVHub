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
package com.envirover.nvi.stream;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class MAVLinkRecord {

    private String deviceId;

    private Date time;

    private Integer msgId;

    private Map<String, Object> packet;

    public MAVLinkRecord(String deviceId, Date time, Integer msgId, Map<String, Object> packet) {
        this.deviceId = deviceId;
        this.time = time;
        this.msgId = msgId;
        this.packet = packet;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Date getTime() {
        return time;
    }

    public Integer getMsgId() {
        return msgId;
    }

    public Double getLatitude() {
        return ((BigDecimal)packet.get("latitude")).doubleValue() / 10000000.0;
    }

    public Double getLongitude() {
        return ((BigDecimal)packet.get("longitude")).doubleValue() / 10000000.0;
    }

    public Double getAltitude() {
        return ((BigDecimal)packet.get("altitude_amsl")).doubleValue() / 1.0;
    }

    public Map<String, Object> getPacket() {
        return packet;
    }

}
