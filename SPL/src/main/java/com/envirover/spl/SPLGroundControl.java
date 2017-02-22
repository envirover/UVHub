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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

import com.sun.net.httpserver.HttpServer;
import com.MAVLink.enums.MAV_COMPONENT;

@SuppressWarnings("restriction")
public class SPLGroundControl {

    private final static int MAX_MGS_QUEUE_SIZE = 10;

    final static int SYSTEM_ID = 1;
    final static int COMP_ID = MAV_COMPONENT.MAV_COMP_ID_ALL;
    final static MAVLinkMessageQueue messageQueue = new MAVLinkMessageQueue(MAX_MGS_QUEUE_SIZE);
    final static Config config = new Config();

    public static void main(String[] args) {
        MAVLinkChannel channel = null;

        try {
            config.init(args);

            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.printf("Starting RockBLOCK HTTP message handler on http://%s:%d%s...",
                              ip, config.getHttpPort(), config.getHtppContext());
            System.out.println();

            HttpServer server = HttpServer.create(new InetSocketAddress(config.getHttpPort()), 0);
            server.createContext(config.getHtppContext(), new RockBLOCKHttpHandler(messageQueue));
            server.setExecutor(null);
            server.start();

            // Start message pump for mobile-originated messages
            MOMessagePump msgPump = new MOMessagePump(messageQueue, config.getMAVLinkPort());
            Thread msgPumpThread = new Thread(msgPump);
            msgPumpThread.start();

            Thread.sleep(1000);

            System.out.println("Enter 'exit' to exit the program.");

            Scanner scanner = new Scanner(System.in);
            String str;
            while (!(str = scanner.next()).equalsIgnoreCase("exit")) {
                //Just echo the user input for now.
                System.out.println(str);
            }

            System.out.println("Exiting...");
            scanner.close();
            server.stop(0);
            msgPumpThread.interrupt();
            Thread.sleep(1000);
            System.out.println("Done.");
            System.exit(0);
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
