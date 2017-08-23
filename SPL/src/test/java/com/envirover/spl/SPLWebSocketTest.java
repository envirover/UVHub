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
along with SPLGroundControl.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.apache.commons.codec.DecoderException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.common.msg_high_latency;
import com.envirover.mavlink.MAVLinkLogger;

public class SPLWebSocketTest {
    private static final String[] args = {"-i", "1234567890", "-u", "user", "-p", "password"};
    private static final Config config = Config.getInstance();
    private static final SPLDaemon daemon = new SPLDaemon();

    @Before
    public void setUpClass() throws Exception {
        //Configure log4j
        ConsoleAppender console = new ConsoleAppender(); 
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(Level.INFO);
        console.activateOptions();

        Logger.getRootLogger().addAppender(console);

        System.out.println("SET UP CLASS: Starting SPLGroundControl application...");
        config.init(args);

        daemon.init(new SPLGroundControl.SPLDaemonContext(args));
        daemon.start();
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("TEAR DOWN: Stopping SPLGroundControl application...");
        daemon.stop();
        daemon.destroy();
    }

    //Testing web socket service endpoint
    @Test
    public void testWSClient() throws InterruptedException, DeploymentException, IOException  {
        System.out.println("WS TEST: Testing WebSocket endpoint...");

        Thread.sleep(1000);

        String endpoint = String.format("ws://%s:%d/gcs/ws", 
                          InetAddress.getLocalHost().getHostAddress(), 
                          config.getWSPort());

        System.out.printf("WS TEST: Connecting to %s", endpoint);
        System.out.println();

        ClientManager client = ClientManager.createClient();


        Session session = client.connectToServer(WSClient.class, URI.create(endpoint)); 

        System.out.printf("WS TEST: Connected %s", endpoint);
        System.out.println();

        MAVLinkPacket packet = getSamplePacket();
        session.getBasicRemote().sendBinary(ByteBuffer.wrap(packet.encodePacket()));

        Thread.sleep(10000);

        System.out.println("WS TEST: Complete.");
    }

    private MAVLinkPacket getSamplePacket() {
        msg_high_latency msg = new msg_high_latency();
        msg.latitude = 523867;
        msg.longitude = 2938;
        msg.sysid = 1;
        msg.compid = 2;
        return msg.pack();
    }

    @ClientEndpoint
    public static class WSClient {

        @OnOpen
        public void onOpen(Session session) {
        }

        @OnMessage
        public void onMessage(byte[] message, Session session) throws DecoderException {
            MAVLinkLogger.log(Level.INFO, "WS <<", getPacket(message));
            //System.out.println("Received msg: " + message.toString());
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

}
