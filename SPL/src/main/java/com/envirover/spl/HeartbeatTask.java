/*
This file is part of SPLGroundControl application.

SPLGroundControl is a MAVLink proxy server for ArduPilot rovers with
RockBLOCK satellite communication.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLGroundControl is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Rock7MAVLink.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl;

import java.io.IOException;
import java.util.TimerTask;

import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkShadow;

/**
 * Sends heartbeats and status messages to the specified channel.  
 * 
 * Heartbeats and high-frequency messages such as SYS_STATUS, GPS_RAW_INT, ATTITUDE, 
 * GLOBAL_POSITION_INT, MISSION_CURRENT, NAV_CONTROLLER_OUTPUT, and VFR_HUD are 
 * derived form the HIGH_LATENCY message and sent to the destination channel.

 * @author pavel
 *
 */
public class HeartbeatTask extends TimerTask {

    private final MAVLinkChannel dst;

    public HeartbeatTask(MAVLinkChannel dst) {
        this.dst = dst;
    }

    @Override
    public void run() {
        try {
            MAVLinkShadow.getInstance().reportState(dst);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
