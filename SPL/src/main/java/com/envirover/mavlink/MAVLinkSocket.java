/*
This file is part of SPLGroundControl application.

SPLGroundControl is a MAVLink proxy server for ArduPilot rovers with
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

package com.envirover.mavlink;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_int;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_request_data_stream;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;


/**
 * MAVLinkChannel implementation used to sends/receives messages to/from server sockets.
 *
 */
public class MAVLinkSocket implements MAVLinkChannel {

    private final static Logger logger = Logger.getLogger(MAVLinkSocket.class);

    private final ServerSocket socket;

    private Socket connection = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    /**
     * Constructs instance of MAVLinkSocket.
     * 
     * @param port server socket port number
     * @throws IOException
     */
    public MAVLinkSocket(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    /**
     * Listens for a connection to be made to the socket and accepts it.
     * The method blocks until a connection is made.
     * 
     * @throws IOException
     */
    public synchronized void connect() throws IOException {
        if (!isConnected()) {
            System.out.printf("Waiting for MAVLink client connection on tcp://%s:%d...",
                    socket.getInetAddress().getHostAddress(), socket.getLocalPort());
            System.out.println();

            connection = socket.accept();
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(connection.getOutputStream());

            logger.info("MAVLink client connected.");
        }
    }

    /**
     * Returns true if the socket is connected to a client.
     * 
     * @return true if the socket is connected to a client.
     */
    public synchronized boolean isConnected() {
        return !socket.isClosed() && connection != null && connection.isConnected();
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        Parser parser = new Parser();

        MAVLinkPacket packet = null;

        connect();

        try {
            do {
                try {
                    int c = in.readUnsignedByte();
                    packet = parser.mavlink_parse_char(c);
                } catch(java.io.EOFException ex) {
                    return null;
                }
            } while (packet == null);
        } catch (SocketException ex) {
            logger.info("MAVLink client disconnected.");
            closeConnection();
        }

        debugPrint("received", packet);

        return packet;
    }

    @Override
    public void sendMessage(MAVLinkPacket packet) throws IOException {
        if (packet == null)
            return;

        connect();

        try {
            if (isConnected()) {
                byte[] data = packet.encodePacket();
                out.write(data);
                out.flush();

                debugPrint("sent", packet);
            }
        } catch (SocketException ex) {
            logger.info("MAVLink client disconnected.");
            closeConnection();
        }
    }

    @Override
    public void close() {
        closeConnection();

        try {
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
                in = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void debugPrint(String dir, MAVLinkPacket packet) {
        if (packet == null)
            return;

        switch(packet.msgid) {
            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
            {
                msg_heartbeat msg = (msg_heartbeat)packet.unpack();
                logger.debug(MessageFormat.format("{0} HEARTBEAT: compid={1}, sysid={2}, autopilot={3}, base_mode={4}, custom_mode={5}, mavlink_version={6}, system_status={7}, type={8}",
                       dir, msg.compid, msg.sysid, msg.autopilot, msg.base_mode, msg.custom_mode, msg.mavlink_version, msg.system_status, msg.type));
                break;
            }
            case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
            {
                msg_sys_status msg = (msg_sys_status)packet.unpack();
                logger.debug(MessageFormat.format("{0} SYS_STATUS: compid={1}, sysid={2}, voltage_battery={3}, msg.battery_remaining={4}, current_battery={5}, drop_rate_comm={6}, " +
                       "errors_comm={7}, errors_count1={8}, errors_count2={9}, errors_count3={10}, errors_count4={11}, load={12}, " +
                       "onboard_control_sensors_enabled={13}, onboard_control_sensors_health={14}, onboard_control_sensors_present={15}",
                       dir, msg.compid, msg.sysid, msg.voltage_battery, msg.battery_remaining, msg.current_battery, msg.drop_rate_comm, 
                       msg.errors_comm, msg.errors_count1, msg.errors_count2, msg.errors_count3, msg.errors_count4, msg.load,
                       msg.onboard_control_sensors_enabled, msg.onboard_control_sensors_health, msg.onboard_control_sensors_present));
                break;
            }
            case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
            {
                msg_gps_raw_int msg = (msg_gps_raw_int)packet.unpack();
                logger.debug(MessageFormat.format("{0} GPS_RAW_INT: compid={1}, sysid={2}, " +
                       "alt={3}, cog={4}, eph={5}, epv={6}, fix_type={7}, lat={8}, lon={9}",
                       dir, msg.compid, msg.sysid, msg.alt, msg.cog, msg.eph, msg.epv, msg.fix_type, msg.lat, msg.lon));
                break;
            }
            case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
            {
                msg_attitude msg = (msg_attitude)packet.unpack();
                logger.debug(MessageFormat.format("{0} ATTITUDE: compid={1}, sysid={2}, " +
                       "pitch={3}, pitchspeed={4}, roll={5}, rollspeed={6}, yaw={7}, yawspeed={8}, time_boot_ms={9}",
                       dir, msg.compid, msg.sysid, msg.pitch, msg.pitchspeed, msg.roll, msg.rollspeed, msg.yaw, msg.yawspeed, msg.time_boot_ms));
                break;
            }
            case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
            {
                msg_global_position_int msg = (msg_global_position_int)packet.unpack();
                logger.debug(MessageFormat.format("{0} ATTITUDE: compid={1}, sysid={2}, " +
                       "alt={3}, hdg={4}, lat={5}, lon={6}, relative_alt={7}, time_boot_ms={8}, vx={9}, vy={10}, vz={11}",
                       dir, msg.compid, msg.sysid, msg.alt, msg.hdg, msg.lat, msg.lon, msg.relative_alt, msg.time_boot_ms, msg.vx, msg.vy, msg.vz));
                break;
            }
            case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
            {
                msg_mission_current msg = (msg_mission_current)packet.unpack();
                logger.debug(MessageFormat.format("{0} ATTITUDE: compid={1}, sysid={2}, seq={3}",
                       dir, msg.compid, msg.sysid, msg.seq));
                break;
            }
            case msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK:
            {
                msg_mission_ack msg = (msg_mission_ack)packet.unpack();
                logger.debug(MessageFormat.format("{0} ATTITUDE: compid={1}, sysid={2}, target_system={3}, target_component={4}, type={5}",
                        dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.type));
            }
            case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
            {
                msg_nav_controller_output msg = (msg_nav_controller_output)packet.unpack();
                logger.debug(MessageFormat.format("{0} ATTITUDE: compid={1}, sysid={2}, alt_error={3}, " +
                       "aspd_error={4}, nav_bearing={5}, nav_pitch={6}, nav_roll={7}, terget_bearing={8}, wp_dist={9}, xtrack_error={10}",
                       dir, msg.compid, msg.sysid, msg.alt_error, msg.aspd_error, msg.nav_bearing,
                       msg.nav_pitch, msg.nav_roll, msg.target_bearing, msg.wp_dist, msg.xtrack_error));
                break;
            }
            case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
            {
                msg_vfr_hud msg = (msg_vfr_hud)packet.unpack();
                logger.debug(MessageFormat.format("{0} ATTITUDE: compid={1}, sysid={2}, airspeed={3}," +
                       "alt={4}, climb={5}, groundspeed={6}, heading={7}, throttle={8}",
                       dir, msg.compid, msg.sysid, msg.airspeed, msg.alt, msg.climb, msg.groundspeed,
                       msg.heading, msg.throttle));
                 break;
            }
            case msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST:
            {
                msg_param_request_list msg = (msg_param_request_list)packet.unpack();
                logger.debug(MessageFormat.format("{0} PARAM_REQUEST_LIST: compid={1}, sysid={2}, target_component={3}, target_system={4}",
                       dir, msg.compid, msg.sysid, msg.target_component, msg.target_system));
                break;
            }
            case msg_request_data_stream.MAVLINK_MSG_ID_REQUEST_DATA_STREAM:
            {
                msg_request_data_stream msg = (msg_request_data_stream)packet.unpack();
                logger.debug(MessageFormat.format("{0} REQUEST_DATA_STREAM: compid={1}, sysid={2}, req_message_rate={3}, req_stream_id={4}, start_stop={5}, target_system={6}, target_component={7}", 
                       dir, msg.compid, msg.sysid, msg.req_message_rate, msg.req_stream_id, msg.start_stop, msg.target_system, msg.target_component));
                break;
            }
            case msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG:
            {
                msg_command_long msg = (msg_command_long)packet.unpack();
                logger.debug(MessageFormat.format("{0} COMMAND_LONG: compid={1}, sysid={2}, command={3}, confirmation={4}, param1={5}, param2={6}, param3={7}, param4={8}, param5={9}, param6={10}, param7={11}", 
                       dir, msg.compid, msg.sysid, msg.command, msg.confirmation, msg.param1, msg.param2, msg.param3, msg.param4, msg.param5, msg.param6, msg.param7));
                break;
            }
            case msg_command_int.MAVLINK_MSG_ID_COMMAND_INT:
            {
                msg_command_int msg = (msg_command_int)packet.unpack();
                logger.debug(MessageFormat.format("{0} COMMAND_LONG: compid={1}, sysid={2}, command={3}, frame={4}, current={5}, autocontinue={6}, param1={7}, param2={8}, param3={9}, param4={10}, x={11}, y={12}, z={13}",
                       dir, msg.compid, msg.sysid, msg.command, msg.frame, msg.current, msg.autocontinue, msg.param1, msg.param2, msg.param3, msg.param4, msg.x, msg.y, msg.z));
                break;
            }
            case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
            {
                msg_command_ack msg = (msg_command_ack)packet.unpack();
                logger.debug(MessageFormat.format("{0} COMMAND_ACK: compid={1}, sysid={2}, command={3}, result={4}",
                       dir, msg.compid, msg.sysid, msg.command, msg.result));
            }
            default:
            {
                MAVLinkMessage msg = packet.unpack();
                logger.debug(MessageFormat.format("{0} {1}: compid={2}, sysid={3}",
                       dir, msg.msgid, msg.compid, msg.sysid));
            }
        }
    }

}
