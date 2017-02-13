/*
This file is part of Rock7MAVLink.

Rock7MAVLink is MAVLink Proxy for RockBLOCK Web Services.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

Rock7MAVLink is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Rock7MAVLink is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Rock7MAVLink.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.net.httpserver.HttpServer;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.enums.MAV_COMPONENT;
import com.MAVLink.enums.MAV_MODE_FLAG;

@SuppressWarnings("restriction")
public class SPLGroundControl {

    final static int SYSTEM_ID = 1;
    final static int COMP_ID = MAV_COMPONENT.MAV_COMP_ID_ALL;

    public static void main(String[] args) {
        final Config config = new Config();

        ServerSocket welcomeSocket = null;

        try {
            config.init();

            welcomeSocket = new ServerSocket(5760);
            Socket connectionSocket = welcomeSocket.accept();
            MAVLinkChannel channel = new MAVLinkSocket(connectionSocket);

            HttpServer server = HttpServer.create(new InetSocketAddress(config.getHttpPort()), 0);
            server.createContext("/test", new RockBLOCKHttpHandler(channel));
            server.setExecutor(null); // creates a default executor
            server.start();

            Thread.sleep(1000);

/*            HttpClient httpclient = HttpClients.createDefault();

            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            builder.setHost("127.0.0.1");
            builder.setPort(httpPort);
            builder.setPath("/test");

            URI uri = builder.build();
            System.out.println(uri.toString());
            HttpPost httppost = new HttpPost(uri);

            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("param-1", "12345"));
            params.add(new BasicNameValuePair("param-2", "Hello!"));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            // Execute and get the response.
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    // do something useful
                } finally {
                    instream.close();
                }
            }
*/
            Thread receiverThread = new Thread(new Receiver(channel));
            receiverThread.start();

            Thread senderThread = new Thread(new Sender(channel));
            senderThread.start();

            System.out.println("Press any key to continue...");
            try {
                System.in.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (welcomeSocket != null)
                    welcomeSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Receiver implements Runnable {

        private final MAVLinkChannel channel;

        public Receiver(MAVLinkChannel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            System.out.println("Receiving messages...");

            while (true) {
                try {
                    MAVLinkPacket packet = channel.receiveMessage();

                    MAVLinkMessage msg = packet.unpack();
                    if (msg != null) {
                        System.out.printf("Message received. msgid = %d\n", msg.msgid);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Sender implements Runnable {

        private static int heartbeat_seq = 0;

        private final MAVLinkChannel channel;

        public Sender(MAVLinkChannel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            System.out.println("Sending messages...");

            while (true) {
                try {
                    msg_heartbeat heartbeat = new msg_heartbeat();
                    heartbeat.base_mode = MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED;

                    MAVLinkPacket packet = heartbeat.pack();
                    packet.sysid = SYSTEM_ID;
                    packet.compid = COMP_ID;
                    packet.seq = heartbeat_seq++;

                    channel.sendMessage(packet);

                    System.out.println("*");

                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
