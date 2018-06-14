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

package com.envirover.uvhub;

import java.io.InputStream;
import java.net.InetSocketAddress;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.Logger;
import org.glassfish.tyrus.server.Server;

import com.envirover.mavlink.MAVLinkMessageQueue;
import com.envirover.mavlink.MAVLinkShadow;

import com.envirover.rockblock.RockBlockClient;
import com.envirover.rockblock.RockBlockHttpHandler;
import com.envirover.spl.stream.ElasticsearchOutputStream;
import com.envirover.spl.stream.MAVLinkOutputStream;
import com.sun.net.httpserver.HttpServer;

/**
 * Daemon interface for UV Hub server.
 * 
 * @author Pavel Bobov
 */
@SuppressWarnings("restriction")
public class UVHubDaemon implements Daemon {
    private final static String DEFAULT_PARAMS_FILE = "default.params";

    private final static Logger logger = Logger.getLogger(UVHubDaemon.class);

    private final Config config = Config.getInstance();
    
    private GCSTcpServer gcsTcpServer = null;
    private RRTcpServer rrTcpServer = null;
    private ShadowTcpServer shadowServer = null;
    private HttpServer httpServer = null;
    private Thread mtMsgPumpThread = null;
    private Server wsServer;

    @Override
    public void destroy() {
    	httpServer.removeContext(config.getHttpContext());
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        if (!config.init(context.getArguments()))
            throw new DaemonInitException("Invalid configuration.");

        // Load on-board parameters from default.params file.
        ClassLoader loader = UVHubDaemon.class.getClassLoader();
        InputStream params = loader.getResourceAsStream(DEFAULT_PARAMS_FILE);
        if (params != null) {
            MAVLinkShadow.getInstance().loadParams(params);
            params.close();
        } else {
            logger.warn("File 'default.params' with initial parameters values not found.");
        }

        // Mobile-terminated queue contains MAVLink messages to be sent to the vehicle.
        MAVLinkMessageQueue mtMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());
        
        // GCS TCP server accepts GCS client connections on port 5760. It handles 
        // MAVLink messages received form the connected clients and forwards some
        // of the messages to the specified mobile-terminated queue.
        gcsTcpServer = new GCSTcpServer(config.getMAVLinkPort(), mtMessageQueue);

        // Shadow TCP server accepts GCS client connections on port 5757. It handles 
        // MAVLink messages received form the connected clients and updates on-board parameters 
        // missions in the vehicle shadow without sending any messages to the vehicle.
        shadowServer = new ShadowTcpServer(config.getShadowPort());

        // Mobile-originated queue contains messages receive form the vehicle.
        MAVLinkMessageQueue moMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());

        // Mobile-originated message handler is an implementation of MAVLinkChannel.
        // Messages sent to MOMessageHandler either used to update the vehicle's shadow, 
        // or pushed to the provided mobile-originated queue. 
        MOMessageHandler moHandler = new MOMessageHandler(moMessageQueue);

        // Init message mersistance stream
        MAVLinkOutputStream stream = new ElasticsearchOutputStream(config.getElasticsearchEndpoint(), 
                config.getElasticsearchPort(),
                config.getElasticsearchProtocol());
        stream.open();

        // RadioRoom TCP server accepts TCP/IP connections on port 5060 from SPL RadioRoom. 
        // MAVLink messages received from RadioRoom are sent to the specified mobile-originated
        // message handler. 
        rrTcpServer = new RRTcpServer(config.getRadioRoomPort(), moHandler, stream);

        // RockBLOCK HTTP handler listens on on port 8080 and sends mobile-originated
        // MAVLink messages received from RockBLOCK to the specified mobile-originated 
        // message handler.
        httpServer = HttpServer.create(new InetSocketAddress(config.getRockblockPort()), 0);
        
        httpServer.createContext(config.getHttpContext(), 
                                 new RockBlockHttpHandler(moHandler, config.getRockBlockIMEI(), stream));
        httpServer.setExecutor(null);

        // RockBLOCK HTTP client sends MAVLink messages to RockBLOCK Web Services.
        RockBlockClient rockblock = null;
       
        if (config.getRockBlockIMEI() != null) {
        	rockblock = new RockBlockClient(config.getRockBlockIMEI(),
                                            config.getRockBlockUsername(),
                                            config.getRockBlockPassword(),
                                            config.getRockBlockURL());
        }
       
        // Mobile-terminated message pump pumps MAVLink messages from the specified
        // mobile-terminated queue to the last connected RadioRoom TCP client or 
        // the specified RockBLOCK HTTP client.
        MTMessagePump mtMsgPump = new MTMessagePump(mtMessageQueue, rrTcpServer, rockblock);
        mtMsgPumpThread = new Thread(mtMsgPump, "mt-message-pump");

        // WebSocket server accepts client connections on port 8000. It handles 
        // MAVLink messages received form the connected clients and forwards some
        // of the messages to the specified mobile-terminated queue.
        WSEndpoint.setMTQueue(mtMessageQueue);
        wsServer = new Server("localhost", config.getWSPort(), "/gcs", WSEndpoint.class);
    }

    @Override
    public void start() throws Exception {
//        String ip = InetAddress.getLocalHost().getHostAddress();
//        System.out.printf("Starting RockBLOCK HTTP message handler on http://%s:%d%s...",
//                          ip, config.getRockblockPort(), config.getHttpContext());
//        System.out.println();

    	// Start all the server threads.
        gcsTcpServer.start();
        shadowServer.start();
        rrTcpServer.start();
    	httpServer.start();
        mtMsgPumpThread.start();
        wsServer.start();

        Thread.sleep(1000);

        logger.info("UV Hub server started.");
    }

    @Override
    public void stop() throws Exception {

    	// Stop all the server threads.
        wsServer.stop();
        mtMsgPumpThread.interrupt();
    	httpServer.stop(0);
        rrTcpServer.stop();
        shadowServer.stop();
        gcsTcpServer.stop();

        Thread.sleep(1000);

        logger.info("UV Hub server stopped.");
    }

}
