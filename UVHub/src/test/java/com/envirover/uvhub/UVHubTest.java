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

import static org.junit.Assert.fail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.common.msg_high_latency;
import com.envirover.mavlink.MAVLinkLogger;
import com.envirover.uvhub.Config;
import com.envirover.uvhub.UVHub;
import com.envirover.uvhub.UVHubDaemon;

public class UVHubTest {
    private static final String[] args = {};
    private static final Config config = Config.getInstance();
    private static final UVHubDaemon daemon = new UVHubDaemon();

    @BeforeClass
    public static void setUpClass() throws Exception {
        //Configure log4j
        ConsoleAppender console = new ConsoleAppender(); 
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(Level.DEBUG);
        console.activateOptions();

        Logger.getRootLogger().addAppender(console);

        System.out.println("SETUP: Starting UVHub...");
        config.init(args);

        daemon.init(new UVHub.NVIDaemonContext(args));
        daemon.start();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        daemon.stop();
        daemon.destroy();
        System.out.println("TEAR DOWN: UVHub stopped.");
    }

    //Test receiving MO messages 
    @Test
    public void testMOMessagePipeline() throws IOException, InterruptedException {
        System.out.println("MO TEST: Testing MO message pipeline...");

        Thread.sleep(1000);

        Thread mavlinkThread = new Thread(new Runnable() {
            public void run() {
                Socket client = null;

                try {
                    System.out.printf("MO TEST: Connecting to tcp://%s:%d", 
                                      InetAddress.getLocalHost().getHostAddress(), 
                                      config.getMAVLinkPort());
                    System.out.println();
    
                    client = new Socket(InetAddress.getLocalHost().getHostAddress(), 
                                               config.getMAVLinkPort());

                    System.out.printf("MO TEST: Connected tcp://%s:%d", 
                                      InetAddress.getLocalHost().getHostAddress(), 
                                      config.getMAVLinkPort());
                    System.out.println();

                    Parser parser = new Parser();
                    DataInputStream in = new DataInputStream(client.getInputStream());
                    while (true) {
                        MAVLinkPacket packet;
                        do {
                            int c = in.readUnsignedByte();
                            packet = parser.mavlink_parse_char(c);
                        } while (packet == null);

                        System.out.printf("MO TEST: MAVLink message received: msgid = %d", packet.msgid);
                        System.out.println();

                        Thread.sleep(100);
                    }
                } catch(InterruptedException ex) {
                    return;
                }  catch(Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mavlinkThread.start();

        
        Socket client = null;
        DataOutputStream out = null;

        try {
            System.out.printf("MO TEST: Connecting to tcp://%s:%d",
                              InetAddress.getLocalHost().getHostAddress(),
                              config.getRadioRoomPort());
            System.out.println();

            client = new Socket(InetAddress.getLocalHost().getHostAddress(), 
                                config.getRadioRoomPort());

            System.out.printf("MO TEST: Connected to tcp://%s:%d", 
                              InetAddress.getLocalHost().getHostAddress(), 
                              config.getRadioRoomPort());
            System.out.println();

            out = new DataOutputStream(client.getOutputStream());

            MAVLinkPacket packet = getSamplePacket();
            out.write(packet.encodePacket());
            out.flush();

            System.out.printf("MO TEST: MAVLink message sent: msgid = %d", packet.msgid);
            System.out.println();

            Thread.sleep(5000);
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) 
                    out.close();

                if (client != null) 
                    client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
              
        Thread.sleep(1000);

        mavlinkThread.interrupt();
        System.out.println("MO TEST: Complete.");
    }

    //Test sending MT messages to RockBLOCK
    @Test
    public void testMTMessagePipeline() {
        System.out.println("MT TEST: Testing MT message pipeline...");

        Socket client = null;
        DataOutputStream out = null;

        try {
            System.out.printf("MT TEST: Connecting to tcp://%s:%d",
                              InetAddress.getLocalHost().getHostAddress(),
                              config.getMAVLinkPort());
            System.out.println();

            client = new Socket(InetAddress.getLocalHost().getHostAddress(), 
                                config.getMAVLinkPort());

            System.out.printf("MT TEST: Connected to tcp://%s:%d", 
                              InetAddress.getLocalHost().getHostAddress(), 
                              config.getMAVLinkPort());
            System.out.println();

            out = new DataOutputStream(client.getOutputStream());

            MAVLinkPacket packet = getSamplePacket();
            out.write(packet.encodePacket());
            out.flush();

            System.out.printf("MT TEST: MAVLink message sent: msgid = %d", packet.msgid);
            System.out.println();

            Thread.sleep(5000);

            System.out.println("MT TEST: Complete.");
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) 
                    out.close();

                if (client != null) 
                    client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testMessageParser() throws DecoderException {
        MAVLinkPacket packet = getPacket("fe28010101ea04000000242c4f14d4f32cbac1fe78fe527d6829d301d4e5010081001700000000090400000000018099");

        int[] MAVLINK_MESSAGE_CRCS = {50, 124, 137, 0, 237, 217, 104, 119, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 214, 159, 220, 168, 24, 23, 170, 144, 67, 115, 39, 246, 185, 104, 237, 244, 222, 212, 9, 254, 230, 28, 28, 132, 221, 232, 11, 153, 41, 39, 78, 196, 0, 0, 15, 3, 0, 0, 0, 0, 0, 167, 183, 119, 191, 118, 148, 21, 0, 243, 124, 0, 0, 38, 20, 158, 152, 143, 0, 0, 0, 106, 49, 22, 143, 140, 5, 150, 0, 231, 183, 63, 54, 47, 0, 0, 0, 0, 0, 0, 175, 102, 158, 208, 56, 93, 138, 108, 32, 185, 84, 34, 174, 124, 237, 4, 76, 128, 56, 116, 134, 237, 203, 250, 87, 203, 220, 25, 226, 46, 29, 223, 85, 6, 229, 203, 1, 195, 109, 168, 181, 47, 72, 131, 127, 0, 103, 154, 178, 200, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 163, 105, 151, 35, 150, 0, 0, 0, 0, 0, 0, 90, 104, 85, 95, 130, 184, 81, 8, 204, 49, 170, 44, 83, 46, 0};

        System.out.println(MAVLINK_MESSAGE_CRCS[47]);

        if (packet == null) {
            fail("MAVLink message parse failed.");
        }
    }

    //Testing web socket service endpoint
    @Test
    public void testWSClient() throws InterruptedException, DeploymentException, IOException  {
        System.out.println("WS TEST: Testing WebSocket endpoint...");

        String endpoint = String.format("ws://%s:%d/gcs/ws", 
                          InetAddress.getLocalHost().getHostAddress(), 
                          config.getWSPort());

        System.out.printf("WS TEST: Connecting to %s", endpoint);
        System.out.println();

        ClientManager client = ClientManager.createClient();

        Session session = client.connectToServer(WSClient.class, URI.create(endpoint)); 

        System.out.printf("WS TEST: Connected to %s", endpoint);
        System.out.println();

        MAVLinkPacket packet = getSamplePacket();
        session.getBasicRemote().sendBinary(ByteBuffer.wrap(packet.encodePacket()));

        Thread.sleep(10000);

        System.out.println("WS TEST: Complete.");

        session.close();
    }

    private MAVLinkPacket getSamplePacket() {
        msg_high_latency msg = new msg_high_latency();
        msg.latitude = 523867;
        msg.longitude = 2938;
        msg.sysid = 1;
        msg.compid = 2;
        return msg.pack();
    }

    private MAVLinkPacket getPacket(String data) throws DecoderException {
        Parser parser = new Parser();
        MAVLinkPacket packet = null;

        for (byte b : Hex.decodeHex(data.toCharArray())) {
            packet = parser.mavlink_parse_char(b & 0xFF);
        }

        return packet;
    }

    @ClientEndpoint
    public static class WSClient {

        @OnOpen
        public void onOpen(Session session) {
        }

        @OnMessage
        public void onMessage(byte[] message, Session session) throws DecoderException {
            MAVLinkLogger.log(Level.INFO, "WS >>", getPacket(message));
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
