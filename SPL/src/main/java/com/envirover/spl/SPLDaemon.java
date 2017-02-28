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

        MAVLinkMessageQueue moMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());
        MAVLinkMessageQueue mtMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());

        MAVLinkChannel socket = new MAVLinkSocket(config.getMAVLinkPort());

        RockBlockClient rockblock = new RockBlockClient(config.getRockBlockIMEI(),
                                                        config.getRockBlockUsername(),
                                                        config.getRockBlockPassword(),
                                                        config.getRockBlockURL());

        server = HttpServer.create(new InetSocketAddress(config.getHttpPort()), 0);
        server.createContext(config.getHttpContext(), 
                             new RockBlockHttpHandler(moMessageQueue, config.getRockBlockIMEI()));
        server.setExecutor(null);

        // Pump for mobile-originated messages
        MOMessagePump moMsgPump = new MOMessagePump(moMessageQueue, socket);
        moMsgPumpThread = new Thread(moMsgPump, "mo-message-pump");

        // Handler for mobile-terminated messages
        MAVLinkHandler mtHandler = new MAVLinkHandler(socket, mtMessageQueue);
        mtHandlerThread = new Thread(mtHandler, "mt-handler");

        // Pump for mobile-terminated messages
        MTMessagePump mtMsgPump = new MTMessagePump(mtMessageQueue, rockblock);
        mtMsgPumpThread = new Thread(mtMsgPump, "mt-message-pump");
    }

    @Override
    public void start() throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        System.out.printf("Starting RockBLOCK HTTP message handler on http://%s:%d%s...",
                          ip, config.getHttpPort(), config.getHttpContext());
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
