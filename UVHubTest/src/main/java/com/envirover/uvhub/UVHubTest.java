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
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import org.apache.logging.log4j.Level;

import org.glassfish.tyrus.client.ClientManager;
import org.junit.Test;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.common.msg_high_latency;
import com.envirover.mavlink.MAVLinkLogger;
import com.envirover.uvnet.Config;

public class UVHubTest {

    // Test configuration environment variables
    private final static String UVHUB_HOSTNAME = "UVHUB_HOSTNAME";

    private final static String DEFAULT_UVHUB_HOSTNAME = "localhost";

    private final Config config = Config.getInstance();

    private String getUVHubHostname() {
        String hostname = System.getenv(UVHUB_HOSTNAME);
        return hostname != null ? hostname : DEFAULT_UVHUB_HOSTNAME;
    }

    // Test receiving MO messages sent from TCP channel
    @Test
    public void testTCPMOMessagePipeline() throws IOException, InterruptedException {
        System.out.println("TCP/IP MO TEST: Testing TCP/IP MO message pipeline...");

        Thread.sleep(1000);

        Thread mavlinkThread = new Thread(new Runnable() {
            public void run() {
                Socket client = null;

                try {
                    System.out.printf("TCP/IP MO TEST: Connecting to tcp://%s:%d", getUVHubHostname(),
                            config.getMAVLinkPort());
                    System.out.println();

                    client = new Socket(getUVHubHostname(), config.getMAVLinkPort());

                    System.out.printf("TCP/IP MO TEST: Connected tcp://%s:%d", getUVHubHostname(),
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

                        System.out.printf("TCP/IP MO TEST: MAVLink message received: msgid = %d", packet.msgid);
                        System.out.println();

                        Thread.sleep(100);
                    }
                } catch (InterruptedException ex) {
                    return;
                } catch (Exception ex) {
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
            System.out.printf("TCP/IP MO TEST: Connecting to tcp://%s:%d", getUVHubHostname(),
                    config.getRadioRoomPort());
            System.out.println();

            client = new Socket(getUVHubHostname(), config.getRadioRoomPort());

            System.out.printf("TCP/IP MO TEST: Connected to tcp://%s:%d", getUVHubHostname(),
                    config.getRadioRoomPort());
            System.out.println();

            out = new DataOutputStream(client.getOutputStream());

            MAVLinkPacket packet = getSamplePacket();
            out.write(packet.encodePacket());
            out.flush();

            System.out.printf("TCP/IP MO TEST: MAVLink message sent: msgid = %d", packet.msgid);
            System.out.println();

            Thread.sleep(5000);
        } catch (Exception ex) {
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
        System.out.println("TCP/IP MO TEST: Complete.");
    }

    // Test receiving MO messages from RockBLOCK
    @Test
    public void testISBDMOMessagePipeline()
            throws URISyntaxException, ClientProtocolException, IOException, InterruptedException {
        System.out.println("ISBD MO TEST: Testing ISBD MO message pipeline...");

        Thread.sleep(1000);

        Thread mavlinkThread = new Thread(new Runnable() {
            public void run() {
                Socket client = null;

                try {
                    System.out.printf("ISBD MO TEST: Connecting to tcp://%s:%d", getUVHubHostname(),
                            config.getMAVLinkPort());
                    System.out.println();

                    client = new Socket(getUVHubHostname(), config.getMAVLinkPort());

                    System.out.printf("ISBD MO TEST: Connected tcp://%s:%d", getUVHubHostname(),
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

                        System.out.printf("ISBD MO TEST: MAVLink message received: msgid = %d", packet.msgid);
                        System.out.println();

                        Thread.sleep(100);
                    }
                } catch (InterruptedException ex) {
                    return;
                } catch (Exception ex) {
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

        try {
            HttpClient httpclient = HttpClients.createDefault();

            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            builder.setHost(getUVHubHostname());
            builder.setPort(config.getRockblockPort());
            builder.setPath(config.getHttpContext());

            URI uri = builder.build();
            HttpPost httppost = new HttpPost(uri);

            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("imei", config.getRockBlockIMEI()));
            params.add(new BasicNameValuePair("momsn", "12345"));
            params.add(new BasicNameValuePair("transmit_time", "12-10-10 10:41:50"));
            params.add(new BasicNameValuePair("iridium_latitude", "52.3867"));
            params.add(new BasicNameValuePair("iridium_longitude", "0.2938"));
            params.add(new BasicNameValuePair("iridium_cep", "9"));
            params.add(new BasicNameValuePair("data", Hex.encodeHexString(getSamplePacket().encodePacket())));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            // Execute and get the response.
            System.out.printf("ISBD MO TEST: Sending test message to %s", uri.toString());
            System.out.println();

            HttpResponse response = httpclient.execute(httppost);

            if (response.getStatusLine().getStatusCode() != 200) {
                fail(String.format("RockBLOCK HTTP message handler status code = %d.",
                        response.getStatusLine().getStatusCode()));
            }

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream responseStream = entity.getContent();
                try {
                    String responseString = IOUtils.toString(responseStream);
                    System.out.println(responseString);
                } finally {
                    responseStream.close();
                }
            }

            Thread.sleep(1000);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            mavlinkThread.interrupt();
        }

        System.out.println("ISBD MO TEST: Complete.");
    }

    // Test sending MT messages to RockBLOCK
    @Test
    public void testMTMessagePipeline() {
        System.out.println("MT TEST: Testing MT message pipeline...");

        Socket client = null;
        DataOutputStream out = null;

        try {
            System.out.printf("MT TEST: Connecting to tcp://%s:%d", getUVHubHostname(), config.getMAVLinkPort());
            System.out.println();

            client = new Socket(getUVHubHostname(), config.getMAVLinkPort());

            System.out.printf("MT TEST: Connected to tcp://%s:%d", getUVHubHostname(), config.getMAVLinkPort());
            System.out.println();

            out = new DataOutputStream(client.getOutputStream());

            MAVLinkPacket packet = getSamplePacket();
            out.write(packet.encodePacket());
            out.flush();

            System.out.printf("MT TEST: MAVLink message sent: msgid = %d", packet.msgid);
            System.out.println();

            Thread.sleep(5000);

            System.out.println("MT TEST: Complete.");
        } catch (Exception ex) {
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
        MAVLinkPacket packet = getPacket(
                "fe28010101ea04000000242c4f14d4f32cbac1fe78fe527d6829d301d4e5010081001700000000090400000000018099");

        int[] MAVLINK_MESSAGE_CRCS = { 50, 124, 137, 0, 237, 217, 104, 119, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 214,
                159, 220, 168, 24, 23, 170, 144, 67, 115, 39, 246, 185, 104, 237, 244, 222, 212, 9, 254, 230, 28, 28,
                132, 221, 232, 11, 153, 41, 39, 78, 196, 0, 0, 15, 3, 0, 0, 0, 0, 0, 167, 183, 119, 191, 118, 148, 21,
                0, 243, 124, 0, 0, 38, 20, 158, 152, 143, 0, 0, 0, 106, 49, 22, 143, 140, 5, 150, 0, 231, 183, 63, 54,
                47, 0, 0, 0, 0, 0, 0, 175, 102, 158, 208, 56, 93, 138, 108, 32, 185, 84, 34, 174, 124, 237, 4, 76, 128,
                56, 116, 134, 237, 203, 250, 87, 203, 220, 25, 226, 46, 29, 223, 85, 6, 229, 203, 1, 195, 109, 168, 181,
                47, 72, 131, 127, 0, 103, 154, 178, 200, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 163, 105, 151, 35, 150, 0, 0, 0,
                0, 0, 0, 90, 104, 85, 95, 130, 184, 81, 8, 204, 49, 170, 44, 83, 46, 0 };

        System.out.println(MAVLINK_MESSAGE_CRCS[47]);

        if (packet == null) {
            fail("MAVLink message parse failed.");
        }
    }

    // Testing web socket service endpoint
    @Test
    public void testWSClient() throws InterruptedException, DeploymentException, IOException {
        try {
            System.out.println("WS TEST: Testing WebSocket endpoint...");

            String endpoint = String.format("ws://%s:%d/gcs/ws", getUVHubHostname(), config.getWSPort());

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
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
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
