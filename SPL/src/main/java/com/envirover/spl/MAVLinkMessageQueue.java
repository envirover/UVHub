/*
This file is part of SPLGroundControl application.

SPLGroundControl is a ground control proxy station for ArduPilot rovers with
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
import java.util.concurrent.ConcurrentLinkedQueue;

import com.MAVLink.MAVLinkPacket;

public class MAVLinkMessageQueue implements MAVLinkChannel {

    private final ConcurrentLinkedQueue<MAVLinkPacket> queue = new ConcurrentLinkedQueue<MAVLinkPacket>(); 
    private final int maxQueueSize;

    public MAVLinkMessageQueue(int size) {
        this.maxQueueSize = size;
    }

    @Override
    public synchronized MAVLinkPacket receiveMessage() throws IOException {
        return queue.poll();
    }

    @Override
    public synchronized void sendMessage(MAVLinkPacket packet) throws IOException {
        if (queue.size() >= maxQueueSize) {
            queue.poll();
        }

        queue.add(packet);
    }

    @Override
    public void close() throws IOException {
    }

}
