package com.envirover.spl.stream;

import java.util.Date;

import com.MAVLink.MAVLinkPacket;

public class MAVLinkRecord {

    private String deviceId;

    private Date time;

    private MAVLinkPacket packet;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public MAVLinkPacket getPacket() {
        return packet;
    }

    public void setPacket(MAVLinkPacket packet) {
        this.packet = packet;
    }

}
