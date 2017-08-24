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
along with SPLGroundControl.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.Logger;
import org.glassfish.tyrus.server.Server;

import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkMessageQueue;
import com.envirover.mavlink.MAVLinkShadow;
import com.envirover.mavlink.MAVLinkSocket;
import com.envirover.rockblock.RockBlockClient;
import com.envirover.rockblock.RockBlockHttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Daemon interface for SPL application.
 */
@SuppressWarnings("restriction")
public class SPLDaemon implements Daemon {
    private final static String DEFAULT_PARAMS_FILE = "default.params";

    private final static Logger logger = Logger.getLogger(SPLDaemon.class);
    
    private final Config config = Config.getInstance();
    private MAVLinkChannel socket = null;
    private HttpServer server = null;
    private Thread moMsgPumpThread = null;
    private Thread mtHandlerThread = null;
    private Thread mtMsgPumpThread = null;
    private Timer  reportStateTimer = null;
    private TimerTask reportStateTask = null;
    private Server wsServer;

    @Override
    public void destroy() {
        if (socket != null) {
          socket.close();
          socket = null;
        }

        server.removeContext(config.getHttpContext());
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        if (!config.init(context.getArguments()))
            throw new DaemonInitException("Invalid configuration.");

        ClassLoader loader = SPLDaemon.class.getClassLoader();
        InputStream params = loader.getResourceAsStream(DEFAULT_PARAMS_FILE);
        if (params != null) {
            MAVLinkShadow.getInstance().loadParams(params);
            params.close();
        } else {
            logger.warn("File 'default.params' with initial parameters values not found.");
        }

        //Init mobile-originated pipeline
        socket = new MAVLinkSocket(config.getMAVLinkPort());

        MAVLinkMessageQueue moMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());

        MOMessageHandler moHandler = new MOMessageHandler(moMessageQueue);

        server = HttpServer.create(new InetSocketAddress(config.getRockblockPort()), 0);
        server.createContext(config.getHttpContext(), 
                             new RockBlockHttpHandler(moHandler, config.getRockBlockIMEI()));
        server.setExecutor(null);

        MOMessagePump moMsgPump = new MOMessagePump(moMessageQueue, socket);
        moMsgPumpThread = new Thread(moMsgPump, "mo-message-pump");

        //Init mobile-terminated pipeline
        MAVLinkMessageQueue mtMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());

        MTMessageHandler mtHandler = new MTMessageHandler(socket, mtMessageQueue);
        mtHandlerThread = new Thread(mtHandler, "mt-handler");

        RockBlockClient rockblock = new RockBlockClient(config.getRockBlockIMEI(),
                                                        config.getRockBlockUsername(),
                                                        config.getRockBlockPassword(),
                                                        config.getRockBlockURL());

        MTMessagePump mtMsgPump = new MTMessagePump(mtMessageQueue, rockblock);
        mtMsgPumpThread = new Thread(mtMsgPump, "mt-message-pump");

        reportStateTimer = new Timer("report-state-timer", false);
        reportStateTask = new HeartbeatTask(socket, config.getAutopilot(), config.getMavType());

        WSEndpoint.setMTQueue(mtMessageQueue);
        wsServer = new Server("localhost", config.getWSPort(), "/gcs", WSEndpoint.class);
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
        reportStateTimer.schedule(reportStateTask, 0, config.getHeartbeatInterval());
        wsServer.start();

        Thread.sleep(1000);

        logger.info("SPL Ground Control server started.");
    }

    @Override
    public void stop() throws Exception {
        reportStateTimer.cancel();
        mtMsgPumpThread.interrupt();
        mtMsgPumpThread.join(1000);

        mtHandlerThread.interrupt();
        mtHandlerThread.join(1000);

        moMsgPumpThread.interrupt();
        moMsgPumpThread.join(1000);

        server.stop(0);

        wsServer.stop();

        Thread.sleep(1000);

        logger.info("SPL Ground Control server stopped.");
    }

}
