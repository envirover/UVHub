/*
 * Envirover confidential
 * 
 *  [2019] Envirover
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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.envirover.mavlink.MAVLinkSocket;
import com.envirover.uvnet.Config;

/**
 * Integration test for UV Hub.
 */
public class UVHubTest {

    private final static Logger logger = LogManager.getLogger(UVHubTest.class);

    // Test configuration environment variables
    private final static String UVHUB_HOSTNAME = "UVHUB_HOSTNAME";

    private final static String DEFAULT_UVHUB_HOSTNAME = "localhost";

    private final static String HL_REPORT_PERIOD_PARAM = "HL_REPORT_PERIOD";

    private final Config config = Config.getInstance();

    private String getUVHubHostname() {
        String hostname = System.getenv(UVHUB_HOSTNAME);
        return hostname != null ? hostname.trim() : DEFAULT_UVHUB_HOSTNAME;
    }

    // Test receiving MO messages sent from TCP channel
    @Test
    public void testTCPMOMessagePipeline() throws IOException, InterruptedException {
        logger.info("TCP/IP MO TEST: Testing TCP/IP MO message pipeline...");

        Thread.sleep(1000);

        Thread mavlinkThread = new Thread(new Runnable() {
            public void run() {
                try {
                    logger.info(String.format("TCP/IP MO TEST: Connecting to tcp://%s:%d", getUVHubHostname(),
                            config.getMAVLinkPort()));

                    try (Socket socket = new Socket(getUVHubHostname(), config.getMAVLinkPort())) {

                        logger.info(String.format("TCP/IP MO TEST: Connected tcp://%s:%d", getUVHubHostname(),
                                config.getMAVLinkPort()));

                        MAVLinkSocket client = new MAVLinkSocket(socket);

                        while (true) {
                            MAVLinkPacket packet = client.receiveMessage();

                            if (packet != null) {
                                logger.info(String.format("TCP/IP MO TEST: MAVLink message received: msgid = %d", packet.msgid));
                            }

                            Thread.sleep(100);
                        }
                    }
                } catch (InterruptedException ex) {
                    return;
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }
        });

        mavlinkThread.start();

        logger.info(String.format("TCP/IP MO TEST: Connecting to tcp://%s:%d", getUVHubHostname(), config.getRadioRoomPort()));

        try (Socket socket = new Socket(getUVHubHostname(), config.getRadioRoomPort())) {
            logger.info(String.format("TCP/IP MO TEST: Connected to tcp://%s:%d", getUVHubHostname(), config.getRadioRoomPort()));

            MAVLinkSocket client = new MAVLinkSocket(socket);

            MAVLinkPacket packet = getSamplePacket();
            client.sendMessage(packet);

            logger.info(String.format("TCP/IP MO TEST: MAVLink message sent: msgid = %d", packet.msgid));

            Thread.sleep(5000);
        } finally {
            mavlinkThread.interrupt();
        }

        logger.info("TCP/IP MO TEST: Complete.");
    }

    // Test receiving MO messages from RockBLOCK
    @Test
    public void testISBDMOMessagePipeline()
            throws URISyntaxException, ClientProtocolException, IOException, InterruptedException {
        logger.info("ISBD MO TEST: Testing ISBD MO message pipeline...");

        Thread.sleep(1000);

        Thread mavlinkThread = new Thread(new Runnable() {
            public void run() {
                try {
                    try (Socket socket = new Socket(getUVHubHostname(), config.getMAVLinkPort())) {
                        logger.info(String.format("ISBD MO TEST: Connecting to tcp://%s:%d", getUVHubHostname(),
                                config.getMAVLinkPort()));
                        logger.info(String.format("ISBD MO TEST: Connected tcp://%s:%d", getUVHubHostname(),
                                config.getMAVLinkPort()));

                        MAVLinkSocket client = new MAVLinkSocket(socket);
                        while (true) {
                            MAVLinkPacket packet = client.receiveMessage();

                            if (packet != null) {
                                logger.info(String.format("ISBD MO TEST: MAVLink message received: msgid = %d", packet.msgid));
                            }

                            Thread.sleep(100);
                        }
                    }
                } catch (InterruptedException ex) {
                    return;
                } catch (Exception ex) {
                    logger.catching(ex);
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
            logger.info(String.format("ISBD MO TEST: Sending test message to %s", uri.toString()));

            HttpResponse response = httpclient.execute(httppost);

            if (response.getStatusLine().getStatusCode() != 200) {
                fail(String.format("RockBLOCK HTTP message handler status code = %d.",
                        response.getStatusLine().getStatusCode()));
            }

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream responseStream = entity.getContent()) {
                    logger.info("Response: " + IOUtils.toString(responseStream));
                }
            }

            Thread.sleep(1000);
        } catch (Exception ex) {
            logger.catching(ex);
            throw ex;
        } finally {
            mavlinkThread.interrupt();
        }

        logger.info("ISBD MO TEST: Complete.");
    }

    // Test sending MT messages to RockBLOCK
    @Test
    public void testMTMessagePipeline() throws UnknownHostException, IOException, InterruptedException {
        logger.info("MT TEST: Testing MT message pipeline...");
        logger.info(String.format("MT TEST: Connecting to tcp://%s:%d", getUVHubHostname(), config.getMAVLinkPort()));

        try (Socket socket = new Socket(getUVHubHostname(), config.getMAVLinkPort())) {
            logger.info(String.format("MT TEST: Connected to tcp://%s:%d", getUVHubHostname(), config.getMAVLinkPort()));

            MAVLinkSocket client = new MAVLinkSocket(socket);

            MAVLinkPacket packet = getSamplePacket();
            client.sendMessage(packet);

            logger.info(String.format("MT TEST: MAVLink message sent: msgid = %d", packet.msgid));

            Thread.sleep(5000);

            logger.info("MT TEST: Complete.");
        }
    }

    // Test updating parameters and missions in the UV shadow
    @Test
    public void testShadowPort() throws IOException, InterruptedException {
        logger.info("SHADOW PORT TEST: Testing updating parameters and missions in the UV shadow...");
        logger.info(String.format("SHADOW PORT TEST: Connecting to tcp://%s:%d", getUVHubHostname(), config.getShadowPort()));

        try (Socket socket = new Socket(getUVHubHostname(), config.getShadowPort())) {
            logger.info(String.format("SHADOW PORT TEST: Connected to tcp://%s:%d", getUVHubHostname(), config.getShadowPort()));

            MAVLinkSocket client = new MAVLinkSocket(socket);

            msg_param_set msgParamSet = new msg_param_set();
            msgParamSet.setParam_Id(HL_REPORT_PERIOD_PARAM);
            msgParamSet.target_system = 1;
            msgParamSet.param_value = 60.0F;
            msgParamSet.sysid = 1;

            client.sendMessage(msgParamSet.pack());

            logger.info("SHADOW PORT TEST: PARAM_SET MAVLink message sent.");

            for (int i = 0; i < 10; i++) {
                MAVLinkPacket packet = client.receiveMessage();

                logger.info(String.format("SHADOW PORT TEST: MAVLink message received: msgid = %d", packet.msgid));

                if (msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE == packet.msgid) {
                    logger.info("SHADOW PORT TEST: Complete.");
                    return;
                }
            }

            throw new IOException("PARAM_VALUE message not received after PARAM_SET.");
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

}
