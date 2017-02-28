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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkHandler;
import com.envirover.mavlink.MAVLinkMessageQueue;
import com.envirover.mavlink.MAVLinkSocket;
import com.envirover.rockblock.RockBlockClient;
import com.envirover.rockblock.RockBlockHttpHandler;
import com.sun.net.httpserver.HttpServer;



@SuppressWarnings("restriction")
public class SPLGroundControl {

    public static void main(String[] args) {
        MAVLinkChannel channel = null;

        try {
            final Config config = new Config();

            if (!config.init(args))
                return;

            MAVLinkMessageQueue moMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());
            MAVLinkMessageQueue mtMessageQueue = new MAVLinkMessageQueue(config.getQueueSize());

            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.printf("Starting RockBLOCK HTTP message handler on http://%s:%d%s...",
                              ip, config.getHttpPort(), config.getHttpContext());
            System.out.println();

            MAVLinkChannel socket = new MAVLinkSocket(config.getMAVLinkPort());

            RockBlockClient rockblock = new RockBlockClient(config.getRockBlockIMEI(),
                                                            config.getRockBlockUsername(),
                                                            config.getRockBlockPassword(),
                                                            config.getRockBlockURL());

            HttpServer server = HttpServer.create(new InetSocketAddress(config.getHttpPort()), 0);
            server.createContext(config.getHttpContext(), 
                                 new RockBlockHttpHandler(moMessageQueue, config.getRockBlockIMEI()));
            server.setExecutor(null);
            server.start();

            // Start pump for mobile-originated messages
            MOMessagePump moMsgPump = new MOMessagePump(moMessageQueue, socket);
            Thread moMsgPumpThread = new Thread(moMsgPump);
            moMsgPumpThread.start();

            // Start handler for mobile-terminated messages
            MAVLinkHandler mtHandler = new MAVLinkHandler(socket, mtMessageQueue);
            Thread mtHandlerThread = new Thread(mtHandler);
            mtHandlerThread.start();

            // Start pump for mobile-terminated messages
            MTMessagePump mtMsgPump = new MTMessagePump(mtMessageQueue, rockblock);
            Thread mtMsgPumpThread = new Thread(mtMsgPump);
            mtMsgPumpThread.start();

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
            moMsgPumpThread.interrupt();
            mtMsgPumpThread.interrupt();
            mtMsgPumpThread.interrupt();

            Thread.sleep(1000);

            System.out.println("Done.");
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channel != null)
                channel.close();
        }
    }

}
