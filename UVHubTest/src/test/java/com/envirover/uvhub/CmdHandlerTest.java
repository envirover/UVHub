/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.envirover.uvhub;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;
import com.envirover.mavlink.MAVLinkSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

public class CmdHandlerTest {

    private final static String UVHUB_HOSTNAME = "54.203.120.37";
    private final static Logger logger = LogManager.getLogger(CmdHandlerTest.class);
    private final static Config config = Config.getInstance();

    @Test
    public void testVideoStartCapture() throws IOException {
        logger.info("CmdHandlerTest: Test MAV_CMD_VIDEO_START_CAPTURE...");

        msg_command_long msg = new msg_command_long();
        msg.command = MAV_CMD.MAV_CMD_VIDEO_START_CAPTURE;
        msg.compid = 0;
        MAVLinkPacket packet = msg.pack();
        packet.sysid = Config.getInstance().getMavSystemId();

        sendMessageToUVHub(packet);
    }

    @Test
    public void testVideoStopCapture() throws IOException {
        logger.info("CmdHandlerTest: Test MAV_CMD_VIDEO_STOP_CAPTURE...");

        msg_command_long msg = new msg_command_long();
        msg.command = MAV_CMD.MAV_CMD_VIDEO_STOP_CAPTURE;
        msg.compid = 0;
        MAVLinkPacket packet = msg.pack();
        packet.sysid = Config.getInstance().getMavSystemId();

        sendMessageToUVHub(packet);
    }

    private void sendMessageToUVHub(MAVLinkPacket packet) throws IOException {
        logger.info(String.format("CmdHandlerTest: Connecting to tcp://%s:%d", UVHUB_HOSTNAME, config.getMAVLinkPort()));

        try (Socket socket = new Socket(UVHUB_HOSTNAME, config.getMAVLinkPort())) {
            logger.info(String.format("CmdHandlerTest: Connected to tcp://%s:%d", UVHUB_HOSTNAME, config.getMAVLinkPort()));

            MAVLinkSocket client = new MAVLinkSocket(socket);

            client.sendMessage(packet);

            logger.info(String.format("CmdHandlerTest: MAVLink message sent: msgid = %d", packet.msgid));

            logger.info("CmdHandlerTest: Complete.");
        }
    }
}
