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

package com.envirover.nvi;

import java.io.InputStream;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.Logger;
import org.glassfish.tyrus.server.Server;

import com.envirover.mavlink.MAVLinkMessageQueue;
import com.envirover.mavlink.MAVLinkShadow;

/**
 * Daemon interface for NVI application.
 */
public class NVIDaemon implements Daemon {
    private final static String DEFAULT_PARAMS_FILE = "default.params";

    private final static Logger logger = Logger.getLogger(NVIDaemon.class);

    private final Config config = Config.getInstance();
    private GCSTcpServer gcsTcpServer = null;
    private RRTcpServer rrTcpServer = null;
     private Server wsServer;

    @Override
    public void destroy() {
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        if (!config.init(context.getArguments()))
            throw new DaemonInitException("Invalid configuration.");

        ClassLoader loader = NVIDaemon.class.getClassLoader();
        InputStream params = loader.getResourceAsStream(DEFAULT_PARAMS_FILE);
        if (params != null) {
            MAVLinkShadow.getInstance().loadParams(params);
            params.close();
        } else {
            logger.warn("File 'default.params' with initial parameters values not found.");
        }

        MAVLinkMessageQueue mtMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());
        gcsTcpServer = new GCSTcpServer(config.getMAVLinkPort(), mtMessageQueue);

        MAVLinkMessageQueue moMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());

        MOMessageHandler moHandler = new MOMessageHandler(moMessageQueue);

        rrTcpServer = new RRTcpServer(config.getRadioRoomPort(), moHandler, mtMessageQueue);

        WSEndpoint.setMTQueue(mtMessageQueue);
        wsServer = new Server("localhost", config.getWSPort(), "/gcs", WSEndpoint.class);
    }

    @Override
    public void start() throws Exception {
       // String ip = InetAddress.getLocalHost().getHostAddress();

        gcsTcpServer.start();
        wsServer.start();
        rrTcpServer.start();

        Thread.sleep(1000);

        logger.info("NVI Ground Control server started.");
    }

    @Override
    public void stop() throws Exception {

        rrTcpServer.stop();
        gcsTcpServer.stop();
        wsServer.stop();

        Thread.sleep(1000);

        logger.info("NVI Ground Control server stopped.");
    }

}
