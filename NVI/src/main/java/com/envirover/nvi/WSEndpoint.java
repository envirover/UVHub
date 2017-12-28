package com.envirover.nvi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import com.envirover.mavlink.MAVLinkWebSocket;

@ServerEndpoint("/ws")
public class WSEndpoint {

    private static Map<String, GCSClientSession> sessions = new HashMap<String, GCSClientSession>();
    private static MAVLinkChannel mtMessageQueue = null;

    public static void setMTQueue(MAVLinkChannel queue) {
        mtMessageQueue = queue;
    }

    public WSEndpoint() {
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.printf("WebSocket session opened, id: %s%n", session.getId());

        GCSClientSession clientSession = new GCSClientSession(new MAVLinkWebSocket(session), mtMessageQueue);
        clientSession.onOpen();
        sessions.put(session.getId(), clientSession);
    }

    @OnMessage
    public void onMessage(byte[] message, Session session) throws IOException, InterruptedException, DecoderException {
        System.out.printf("Message received. Session id: %s Message: %s", session.getId(), message.toString());

        GCSClientSession clientSession = sessions.get(session.getId());

        if (clientSession != null) {
            clientSession.onMessage(getPacket(message));
        }
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) throws InterruptedException {
        GCSClientSession clientSession = sessions.get(session.getId());

        if (clientSession != null) {
            clientSession.onClose();
            sessions.remove(session.getId());
        }

        System.out.printf("webSocket %s session closed.", session.getId());
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
