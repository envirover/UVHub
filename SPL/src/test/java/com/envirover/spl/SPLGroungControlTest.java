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
along with Rock7MAVLink.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl;

import static org.junit.Assert.fail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.common.msg_high_latency;

public class SPLGroungControlTest {
    private static final String[] args = {"-i", "1234567890", "-u", "user", "-p", "password"};
    private static final Config config = new Config();
    private static final SPLDaemon daemon = new SPLDaemon();

    @BeforeClass
    public static void setUpClass() throws Exception {
        //Configure log4j
        ConsoleAppender console = new ConsoleAppender(); 
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(Level.DEBUG);
        console.activateOptions();

        Logger.getRootLogger().addAppender(console);

        System.out.println("SETUP: Starting SPLGroundControl application...");
        config.init(args);

        daemon.init(new SPLGroundControl.SPLDaemonContext(args));
        daemon.start();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        daemon.stop();
        daemon.destroy();
    }

    //Test receiving MO messages from RockBLOCK
    @Test
    public void testMOMessagePipeline() throws URISyntaxException, ClientProtocolException, IOException, InterruptedException {
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

        HttpClient httpclient = HttpClients.createDefault();

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http");
        builder.setHost(InetAddress.getLocalHost().getHostAddress());
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
        System.out.printf("MO TEST: Sending test message to %s", uri.toString());
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

    private MAVLinkPacket getSamplePacket() {
        msg_high_latency msg = new msg_high_latency();
        msg.latitude = 523867;
        msg.longitude = 2938;
        msg.sysid = 1;
        msg.compid = 2;
        return msg.pack();
    }
}
