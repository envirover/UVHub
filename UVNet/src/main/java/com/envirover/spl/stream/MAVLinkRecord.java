/*
This file is part of SPLTracks application.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLStrean is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SPLStream.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl.stream;

import java.util.Date;
import java.util.Map;

public class MAVLinkRecord {

    private String deviceId;

    private Date time;

    private Integer msgId;

    private Map<String, Object> message;
    
    public MAVLinkRecord(String deviceId, Date time, Integer msgId, Map<String, Object> packet) {
        this.deviceId = deviceId;
        this.time = time;
        this.msgId = msgId;
        this.message = packet;
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
        return ((Integer) message.get("latitude")) / 10000000.0;
    }

    public Double getLongitude() {
        return ((Integer) message.get("longitude")) / 10000000.0;
    }

    public Double getAltitude() {
        return ((Integer) message.get("altitude_amsl")) / 1.0;
    }

    public Map<String, Object> getPacket() {
        return message;
    }
}
