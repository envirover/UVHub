package com.envirover.spl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.codec.DecoderException;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkMessageQueue;
import com.envirover.mavlink.MAVLinkWebSocket;

@ServerEndpoint("/ws")
public class WSEndpoint {

    private final Config config = Config.getInstance();

    private Map<String, Timer> timers = new HashMap<String, Timer>();
    private Map<String, Thread> mtHandlerThreads = new HashMap<String, Thread>();
    private Map<String, MAVLinkChannel> mtSessionQueues = new HashMap<String, MAVLinkChannel>();

    private static MAVLinkChannel mtMessageQueue = null;

    public static void setMTQueue(MAVLinkChannel queue) {
        mtMessageQueue = queue;
    }

    public WSEndpoint() {
        System.out.println("class loaded " + this.getClass());
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.printf("Session opened, id: %s%n", session.getId());

        TimerTask heartbeatTask = new HeartbeatTask(new MAVLinkWebSocket(session),
                                                    config.getAutopilot(), 
                                                    config.getMavType());
        Timer heartbeatTimer = new Timer();
        timers.put(session.getId(), heartbeatTimer);
        heartbeatTimer.schedule(heartbeatTask, 0, config.getHeartbeatInterval());

        MAVLinkMessageQueue mtSessionQueue = new MAVLinkMessageQueue(config.getQueueSize());
        mtSessionQueues.put(session.getId(), mtSessionQueue);

        MTMessageHandler mtHandler = new MTMessageHandler(mtSessionQueue, mtMessageQueue);
        Thread mtHandlerThread = new Thread(mtHandler, "mt-handler-ws");
        mtHandlerThread.start();
        mtHandlerThreads.put(session.getId(), mtHandlerThread);
    }

    @OnMessage
    public void onMessage(byte[] message, Session session) {
        System.out.printf("Message received. Session id: %s Message: %s",
            session.getId(), message.toString());

        try {
            MAVLinkChannel mtSessionQueue = mtSessionQueues.get(session.getId());

            if (mtSessionQueue != null) {
                mtSessionQueue.sendMessage(getPacket(message));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) throws InterruptedException {
        Timer timer = timers.get(session.getId());

        if (timer != null) {
            timer.cancel();
            timers.remove(session.getId());
        }

        Thread mtHandlerThread = mtHandlerThreads.get(session.getId());

        if (mtHandlerThread != null) {
            mtHandlerThread.interrupt();
            mtHandlerThread.join(1000);
            mtHandlerThreads.remove(session.getId());
        }

        mtSessionQueues.remove(session.getId());

        System.out.printf("Session closed with id: %s%n", session.getId());
    }
 
    private MAVLinkPacket getPacket(byte[] data) throws DecoderException {
        Parser parser = new Parser();
        MAVLinkPacket packet = null;

        for (byte b : data) {
            packet = parser.mavlink_parse_char(b & 0xFF);
            if (packet != null) {
                return packet;
            }
        }

        return null;
    }

}
