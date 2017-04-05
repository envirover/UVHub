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

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkHandler;
import com.envirover.mavlink.MAVLinkMessageQueue;
import com.envirover.mavlink.MAVLinkSocket;
import com.envirover.rockblock.RockBlockClient;
import com.envirover.rockblock.RockBlockHttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Daemon interface for SPL application.
 */
@SuppressWarnings("restriction")
public class SPLDaemon implements Daemon {

    private final Config config = new Config();
    private MAVLinkChannel socket = null;
    private HttpServer server = null;
    private Thread moMsgPumpThread = null;
    private Thread mtHandlerThread = null;
    private Thread mtMsgPumpThread = null;

    @Override
    public void destroy() {
        if (socket != null)
          socket.close();

        server.removeContext(config.getHttpContext());
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        if (!config.init(context.getArguments()))
            throw new DaemonInitException("Invalid configuration.");

        //Init mobile-originated pipeline
        MAVLinkChannel socket = new MAVLinkSocket(config.getMAVLinkPort());

        MAVLinkMessageQueue moMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());

        server = HttpServer.create(new InetSocketAddress(config.getRockblockPort()), 0);
        server.createContext(config.getHttpContext(), 
                             new RockBlockHttpHandler(moMessageQueue, config.getRockBlockIMEI()));
        server.setExecutor(null);

        MOMessagePump moMsgPump = new MOMessagePump(moMessageQueue, socket, config.getHeartbeatInterval());
        moMsgPumpThread = new Thread(moMsgPump, "mo-message-pump");

        //Init mobile-terminated pipeline
        MAVLinkMessageQueue mtMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());

        MAVLinkHandler mtHandler = new MAVLinkHandler(socket, mtMessageQueue);
        mtHandlerThread = new Thread(mtHandler, "mt-handler");

        RockBlockClient rockblock = new RockBlockClient(config.getRockBlockIMEI(),
                                                        config.getRockBlockUsername(),
                                                        config.getRockBlockPassword(),
                                                        config.getRockBlockURL());

        MTMessagePump mtMsgPump = new MTMessagePump(mtMessageQueue, rockblock);
        mtMsgPumpThread = new Thread(mtMsgPump, "mt-message-pump");
    }

    @Override
    public void start() throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        System.out.printf("Starting RockBLOCK HTTP message handler on http://%s:%d%s...",
                          ip, config.getRockblockPort(), config.getHttpContext());
        System.out.println();

        server.start();
        moMsgPumpThread.start();
        mtHandlerThread.start();
        mtMsgPumpThread.start();
        Thread.sleep(1000);
    }

    @Override
    public void stop() throws Exception {
        mtMsgPumpThread.interrupt();
        mtMsgPumpThread.join(1000);

        mtHandlerThread.interrupt();
        mtHandlerThread.join(1000);

        moMsgPumpThread.interrupt();
        moMsgPumpThread.join(1000);

        server.stop(0);

        Thread.sleep(1000);
    }

}
