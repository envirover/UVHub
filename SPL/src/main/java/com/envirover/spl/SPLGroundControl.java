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

import com.sun.net.httpserver.HttpServer;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.enums.MAV_COMPONENT;
import com.MAVLink.enums.MAV_MODE_FLAG;

@SuppressWarnings("restriction")
public class SPLGroundControl {

    final static int SYSTEM_ID = 1;
    final static int COMP_ID = MAV_COMPONENT.MAV_COMP_ID_ALL;

    public static void main(String[] args) {
        final Config config = new Config();
        MAVLinkChannel channel = null;

        try {
            config.init();

            MAVLinkMessageQueue messageQueue = new MAVLinkMessageQueue();

            HttpServer server = HttpServer.create(new InetSocketAddress(config.getHttpPort()), 0);
            server.createContext("/test", new RockBLOCKHttpHandler(messageQueue));
            server.setExecutor(null);
            server.start();

            channel = new MAVLinkSocket(config.getMAVLinkPort());

            Thread.sleep(1000);

            // Message pump for mobile-originated messages
            Thread msgPumpThread = new Thread(new MessagePump(messageQueue, channel));
            msgPumpThread.start();

            /*
            Thread senderThread = new Thread(new Sender(channel));
            senderThread.start();
            */

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
                if (channel != null)
                    channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class MessagePump implements Runnable {
        private final static int HEARTBEAT_INTERVAL = 1000;

        private final MAVLinkChannel src;
        private final MAVLinkChannel dst;
        private msg_high_latency msgHighLatency = null;
        private int heartbeat_seq = 0;

        public MessagePump(MAVLinkChannel src, MAVLinkChannel dst) {
            this.src = src;
            this.dst = dst;
        }

        @Override
        public void run() {
            MAVLinkPacket packet;
            while(true) {
                try {
                    while ((packet = src.receiveMessage()) != null) {
                        dst.sendMessage(packet);

                        MAVLinkMessage msg = packet.unpack();
                        if (msg.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
                            msgHighLatency = (msg_high_latency)msg;
                        }
                    }

                    heartbeat();

                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch(IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private void heartbeat() throws IOException {
            msg_heartbeat heartbeat = new msg_heartbeat();
            heartbeat.base_mode = MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED;

            MAVLinkPacket packet = heartbeat.pack();
            packet.sysid = SYSTEM_ID;
            packet.compid = COMP_ID;
            packet.seq = heartbeat_seq++;

            dst.sendMessage(packet);

            //TODO: Derive high frequency messages from HIGH_LATENCY message
        }
    }

    /*
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
    */
}
