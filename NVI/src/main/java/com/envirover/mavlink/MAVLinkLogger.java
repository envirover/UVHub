/*
This file is part of NVIGroundControl application.

NVIGroundControl is a MAVLink proxy server for ArduPilot rovers with
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
along with SPLGroundControl.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.mavlink;

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.*;

/**
 * Decodes and logs MAVLink messages to Log4j logger.
 * 
 * @author pavel
 *
 */
public class MAVLinkLogger {

    private final static Logger logger = Logger.getLogger(MAVLinkSocket.class);

    public static void log(Priority priority, String dir, MAVLinkPacket packet) {
        if (packet == null)
            return;

        switch (packet.msgid) {
        case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT: {
            msg_heartbeat msg = (msg_heartbeat) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HEARTBEAT: compid={1}, sysid={2}, type={3}, autopilot={4}, base_mode={5}, custom_mode={6}, system_status={7}, mavlink_version={8}",
                            dir, msg.compid, msg.sysid, msg.type, msg.autopilot, msg.base_mode, msg.custom_mode,
                            msg.system_status, msg.mavlink_version));
            break;
        }
        case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS: {
            msg_sys_status msg = (msg_sys_status) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} SYS_STATUS: compid={1}, sysid={2}, onboard_control_sensors_present={3}, onboard_control_sensors_enabled={4}, onboard_control_sensors_health={5}, load={6}, voltage_battery={7}, current_battery={8}, battery_remaining={9}, drop_rate_comm={10}, errors_comm={11}, errors_count1={12}, errors_count2={13}, errors_count3={14}, errors_count4={15}",
                    dir, msg.compid, msg.sysid, msg.onboard_control_sensors_present,
                    msg.onboard_control_sensors_enabled, msg.onboard_control_sensors_health, msg.load,
                    msg.voltage_battery, msg.current_battery, msg.battery_remaining, msg.drop_rate_comm,
                    msg.errors_comm, msg.errors_count1, msg.errors_count2, msg.errors_count3, msg.errors_count4));
            break;
        }
        case msg_system_time.MAVLINK_MSG_ID_SYSTEM_TIME: {
            msg_system_time msg = (msg_system_time) packet.unpack();
            logger.debug(
                    MessageFormat.format("{0} SYSTEM_TIME: compid={1}, sysid={2}, time_unix_usec={3}, time_boot_ms={4}",
                            dir, msg.compid, msg.sysid, msg.time_unix_usec, msg.time_boot_ms));
            break;
        }
        case msg_ping.MAVLINK_MSG_ID_PING: {
            msg_ping msg = (msg_ping) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} PING: compid={1}, sysid={2}, time_usec={3}, seq={4}, target_system={5}, target_component={6}",
                    dir, msg.compid, msg.sysid, msg.time_usec, msg.seq, msg.target_system, msg.target_component));
            break;
        }
        case msg_change_operator_control.MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL: {
            msg_change_operator_control msg = (msg_change_operator_control) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} CHANGE_OPERATOR_CONTROL: compid={1}, sysid={2}, target_system={3}, control_request={4}, version={5}, passkey={6}",
                    dir, msg.compid, msg.sysid, msg.target_system, msg.control_request, msg.version, msg.passkey));
            break;
        }
        case msg_change_operator_control_ack.MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL_ACK: {
            msg_change_operator_control_ack msg = (msg_change_operator_control_ack) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} CHANGE_OPERATOR_CONTROL_ACK: compid={1}, sysid={2}, gcs_system_id={3}, control_request={4}, ack={5}",
                            dir, msg.compid, msg.sysid, msg.gcs_system_id, msg.control_request, msg.ack));
            break;
        }
        case msg_auth_key.MAVLINK_MSG_ID_AUTH_KEY: {
            msg_auth_key msg = (msg_auth_key) packet.unpack();
            logger.log(priority, MessageFormat.format("{0} AUTH_KEY: compid={1}, sysid={2}, key={3}", dir, msg.compid,
                    msg.sysid, msg.key));
            break;
        }
        case msg_set_mode.MAVLINK_MSG_ID_SET_MODE: {
            msg_set_mode msg = (msg_set_mode) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SET_MODE: compid={1}, sysid={2}, target_system={3}, base_mode={4}, custom_mode={5}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.base_mode, msg.custom_mode));
            break;
        }
        case msg_param_request_read.MAVLINK_MSG_ID_PARAM_REQUEST_READ: {
            msg_param_request_read msg = (msg_param_request_read) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} PARAM_REQUEST_READ: compid={1}, sysid={2}, target_system={3}, target_component={4}, param_id={5}, param_index={6}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.getParam_Id(),
                            msg.param_index));
            break;
        }
        case msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST: {
            msg_param_request_list msg = (msg_param_request_list) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} PARAM_REQUEST_LIST: compid={1}, sysid={2}, target_system={3}, target_component={4}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component));
            break;
        }
        case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE: {
            msg_param_value msg = (msg_param_value) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} PARAM_VALUE: compid={1}, sysid={2}, param_id={3}, param_value={4}, param_type={5}, param_count={6}, param_index={7}",
                            dir, msg.compid, msg.sysid, msg.getParam_Id(), msg.param_value, msg.param_type, msg.param_count,
                            msg.param_index));
            break;
        }
        case msg_param_set.MAVLINK_MSG_ID_PARAM_SET: {
            msg_param_set msg = (msg_param_set) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} PARAM_SET: compid={1}, sysid={2}, target_system={3}, target_component={4}, param_id={5}, param_value={6}, param_type={7}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.getParam_Id(),
                            msg.param_value, msg.param_type));
            break;
        }
        case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT: {
            msg_gps_raw_int msg = (msg_gps_raw_int) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GPS_RAW_INT: compid={1}, sysid={2}, time_usec={3}, fix_type={4}, lat={5}, lon={6}, alt={7}, eph={8}, epv={9}, vel={10}, cog={11}, satellites_visible={12}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.fix_type, msg.lat, msg.lon, msg.alt, msg.eph,
                            msg.epv, msg.vel, msg.cog, msg.satellites_visible));
            break;
        }
        case msg_gps_status.MAVLINK_MSG_ID_GPS_STATUS: {
            msg_gps_status msg = (msg_gps_status) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GPS_STATUS: compid={1}, sysid={2}, satellites_visible={3}, satellite_prn={4}, satellite_used={5}, satellite_elevation={6}, satellite_azimuth={7}, satellite_snr={8}",
                            dir, msg.compid, msg.sysid, msg.satellites_visible, msg.satellite_prn, msg.satellite_used,
                            msg.satellite_elevation, msg.satellite_azimuth, msg.satellite_snr));
            break;
        }
        case msg_scaled_imu.MAVLINK_MSG_ID_SCALED_IMU: {
            msg_scaled_imu msg = (msg_scaled_imu) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SCALED_IMU: compid={1}, sysid={2}, time_boot_ms={3}, xacc={4}, yacc={5}, zacc={6}, xgyro={7}, ygyro={8}, zgyro={9}, xmag={10}, ymag={11}, zmag={12}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.xacc, msg.yacc, msg.zacc, msg.xgyro,
                            msg.ygyro, msg.zgyro, msg.xmag, msg.ymag, msg.zmag));
            break;
        }
        case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU: {
            msg_raw_imu msg = (msg_raw_imu) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} RAW_IMU: compid={1}, sysid={2}, time_usec={3}, xacc={4}, yacc={5}, zacc={6}, xgyro={7}, ygyro={8}, zgyro={9}, xmag={10}, ymag={11}, zmag={12}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.xacc, msg.yacc, msg.zacc, msg.xgyro,
                            msg.ygyro, msg.zgyro, msg.xmag, msg.ymag, msg.zmag));
            break;
        }
        case msg_raw_pressure.MAVLINK_MSG_ID_RAW_PRESSURE: {
            msg_raw_pressure msg = (msg_raw_pressure) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} RAW_PRESSURE: compid={1}, sysid={2}, time_usec={3}, press_abs={4}, press_diff1={5}, press_diff2={6}, temperature={7}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.press_abs, msg.press_diff1, msg.press_diff2,
                            msg.temperature));
            break;
        }
        case msg_scaled_pressure.MAVLINK_MSG_ID_SCALED_PRESSURE: {
            msg_scaled_pressure msg = (msg_scaled_pressure) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} SCALED_PRESSURE: compid={1}, sysid={2}, time_boot_ms={3}, press_abs={4}, press_diff={5}, temperature={6}",
                    dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.press_abs, msg.press_diff, msg.temperature));
            break;
        }
        case msg_attitude.MAVLINK_MSG_ID_ATTITUDE: {
            msg_attitude msg = (msg_attitude) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} ATTITUDE: compid={1}, sysid={2}, time_boot_ms={3}, roll={4}, pitch={5}, yaw={6}, rollspeed={7}, pitchspeed={8}, yawspeed={9}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.roll, msg.pitch, msg.yaw, msg.rollspeed,
                            msg.pitchspeed, msg.yawspeed));
            break;
        }
        case msg_attitude_quaternion.MAVLINK_MSG_ID_ATTITUDE_QUATERNION: {
            msg_attitude_quaternion msg = (msg_attitude_quaternion) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} ATTITUDE_QUATERNION: compid={1}, sysid={2}, time_boot_ms={3}, q1={4}, q2={5}, q3={6}, q4={7}, rollspeed={8}, pitchspeed={9}, yawspeed={10}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.q1, msg.q2, msg.q3, msg.q4, msg.rollspeed,
                            msg.pitchspeed, msg.yawspeed));
            break;
        }
        case msg_local_position_ned.MAVLINK_MSG_ID_LOCAL_POSITION_NED: {
            msg_local_position_ned msg = (msg_local_position_ned) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} LOCAL_POSITION_NED: compid={1}, sysid={2}, time_boot_ms={3}, x={4}, y={5}, z={6}, vx={7}, vy={8}, vz={9}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.x, msg.y, msg.z, msg.vx, msg.vy, msg.vz));
            break;
        }
        case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT: {
            msg_global_position_int msg = (msg_global_position_int) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GLOBAL_POSITION_INT: compid={1}, sysid={2}, time_boot_ms={3}, lat={4}, lon={5}, alt={6}, relative_alt={7}, vx={8}, vy={9}, vz={10}, hdg={11}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.lat, msg.lon, msg.alt, msg.relative_alt,
                            msg.vx, msg.vy, msg.vz, msg.hdg));
            break;
        }
        case msg_rc_channels_scaled.MAVLINK_MSG_ID_RC_CHANNELS_SCALED: {
            msg_rc_channels_scaled msg = (msg_rc_channels_scaled) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} RC_CHANNELS_SCALED: compid={1}, sysid={2}, time_boot_ms={3}, port={4}, chan1_scaled={5}, chan2_scaled={6}, chan3_scaled={7}, chan4_scaled={8}, chan5_scaled={9}, chan6_scaled={10}, chan7_scaled={11}, chan8_scaled={12}, rssi={13}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.port, msg.chan1_scaled, msg.chan2_scaled,
                            msg.chan3_scaled, msg.chan4_scaled, msg.chan5_scaled, msg.chan6_scaled, msg.chan7_scaled,
                            msg.chan8_scaled, msg.rssi));
            break;
        }
        case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW: {
            msg_rc_channels_raw msg = (msg_rc_channels_raw) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} RC_CHANNELS_RAW: compid={1}, sysid={2}, time_boot_ms={3}, port={4}, chan1_raw={5}, chan2_raw={6}, chan3_raw={7}, chan4_raw={8}, chan5_raw={9}, chan6_raw={10}, chan7_raw={11}, chan8_raw={12}, rssi={13}",
                    dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.port, msg.chan1_raw, msg.chan2_raw, msg.chan3_raw,
                    msg.chan4_raw, msg.chan5_raw, msg.chan6_raw, msg.chan7_raw, msg.chan8_raw, msg.rssi));
            break;
        }
        case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW: {
            msg_servo_output_raw msg = (msg_servo_output_raw) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} SERVO_OUTPUT_RAW: compid={1}, sysid={2}, time_usec={3}, port={4}, servo1_raw={5}, servo2_raw={6}, servo3_raw={7}, servo4_raw={8}, servo5_raw={9}, servo6_raw={10}, servo7_raw={11}, servo8_raw={12}",
                    dir, msg.compid, msg.sysid, msg.time_usec, msg.port, msg.servo1_raw, msg.servo2_raw, msg.servo3_raw,
                    msg.servo4_raw, msg.servo5_raw, msg.servo6_raw, msg.servo7_raw, msg.servo8_raw));
            break;
        }
        case msg_mission_request_partial_list.MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST: {
            msg_mission_request_partial_list msg = (msg_mission_request_partial_list) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_REQUEST_PARTIAL_LIST: compid={1}, sysid={2}, target_system={3}, target_component={4}, start_index={5}, end_index={6}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.start_index,
                            msg.end_index));
            break;
        }
        case msg_mission_write_partial_list.MAVLINK_MSG_ID_MISSION_WRITE_PARTIAL_LIST: {
            msg_mission_write_partial_list msg = (msg_mission_write_partial_list) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_WRITE_PARTIAL_LIST: compid={1}, sysid={2}, target_system={3}, target_component={4}, start_index={5}, end_index={6}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.start_index,
                            msg.end_index));
            break;
        }
        case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM: {
            msg_mission_item msg = (msg_mission_item) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_ITEM: compid={1}, sysid={2}, target_system={3}, target_component={4}, seq={5}, frame={6}, command={7}, current={8}, autocontinue={9}, param1={10}, param2={11}, param3={12}, param4={13}, x={14}, y={15}, z={16}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.seq, msg.frame,
                            msg.command, msg.current, msg.autocontinue, msg.param1, msg.param2, msg.param3, msg.param4,
                            msg.x, msg.y, msg.z));
            break;
        }
        case msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST: {
            msg_mission_request msg = (msg_mission_request) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_REQUEST: compid={1}, sysid={2}, target_system={3}, target_component={4}, seq={5}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.seq));
            break;
        }
        case msg_mission_set_current.MAVLINK_MSG_ID_MISSION_SET_CURRENT: {
            msg_mission_set_current msg = (msg_mission_set_current) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_SET_CURRENT: compid={1}, sysid={2}, target_system={3}, target_component={4}, seq={5}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.seq));
            break;
        }
        case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT: {
            msg_mission_current msg = (msg_mission_current) packet.unpack();
            logger.log(priority, MessageFormat.format("{0} MISSION_CURRENT: compid={1}, sysid={2}, seq={3}", dir,
                    msg.compid, msg.sysid, msg.seq));
            break;
        }
        case msg_mission_request_list.MAVLINK_MSG_ID_MISSION_REQUEST_LIST: {
            msg_mission_request_list msg = (msg_mission_request_list) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_REQUEST_LIST: compid={1}, sysid={2}, target_system={3}, target_component={4}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component));
            break;
        }
        case msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT: {
            msg_mission_count msg = (msg_mission_count) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_COUNT: compid={1}, sysid={2}, target_system={3}, target_component={4}, count={5}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.count));
            break;
        }
        case msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL: {
            msg_mission_clear_all msg = (msg_mission_clear_all) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_CLEAR_ALL: compid={1}, sysid={2}, target_system={3}, target_component={4}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component));
            break;
        }
        case msg_mission_item_reached.MAVLINK_MSG_ID_MISSION_ITEM_REACHED: {
            msg_mission_item_reached msg = (msg_mission_item_reached) packet.unpack();
            logger.log(priority, MessageFormat.format("{0} MISSION_ITEM_REACHED: compid={1}, sysid={2}, seq={3}", dir,
                    msg.compid, msg.sysid, msg.seq));
            break;
        }
        case msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK: {
            msg_mission_ack msg = (msg_mission_ack) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_ACK: compid={1}, sysid={2}, target_system={3}, target_component={4}, type={5}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.type));
            break;
        }
        case msg_set_gps_global_origin.MAVLINK_MSG_ID_SET_GPS_GLOBAL_ORIGIN: {
            msg_set_gps_global_origin msg = (msg_set_gps_global_origin) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SET_GPS_GLOBAL_ORIGIN: compid={1}, sysid={2}, target_system={3}, latitude={4}, longitude={5}, altitude={6}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.latitude, msg.longitude, msg.altitude));
            break;
        }
        case msg_gps_global_origin.MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN: {
            msg_gps_global_origin msg = (msg_gps_global_origin) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GPS_GLOBAL_ORIGIN: compid={1}, sysid={2}, latitude={3}, longitude={4}, altitude={5}",
                            dir, msg.compid, msg.sysid, msg.latitude, msg.longitude, msg.altitude));
            break;
        }
        case msg_param_map_rc.MAVLINK_MSG_ID_PARAM_MAP_RC: {
            msg_param_map_rc msg = (msg_param_map_rc) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} PARAM_MAP_RC: compid={1}, sysid={2}, target_system={3}, target_component={4}, param_id={5}, param_index={6}, parameter_rc_channel_index={7}, param_value0={8}, scale={9}, param_value_min={10}, param_value_max={11}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.getParam_Id(),
                            msg.param_index, msg.parameter_rc_channel_index, msg.param_value0, msg.scale,
                            msg.param_value_min, msg.param_value_max));
            break;
        }
        case msg_mission_request_int.MAVLINK_MSG_ID_MISSION_REQUEST_INT: {
            msg_mission_request_int msg = (msg_mission_request_int) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_REQUEST_INT: compid={1}, sysid={2}, target_system={3}, target_component={4}, seq={5}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.seq));
            break;
        }
        case msg_safety_set_allowed_area.MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA: {
            msg_safety_set_allowed_area msg = (msg_safety_set_allowed_area) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SAFETY_SET_ALLOWED_AREA: compid={1}, sysid={2}, target_system={3}, target_component={4}, frame={5}, p1x={6}, p1y={7}, p1z={8}, p2x={9}, p2y={10}, p2z={11}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.frame, msg.p1x,
                            msg.p1y, msg.p1z, msg.p2x, msg.p2y, msg.p2z));
            break;
        }
        case msg_safety_allowed_area.MAVLINK_MSG_ID_SAFETY_ALLOWED_AREA: {
            msg_safety_allowed_area msg = (msg_safety_allowed_area) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} SAFETY_ALLOWED_AREA: compid={1}, sysid={2}, frame={3}, p1x={4}, p1y={5}, p1z={6}, p2x={7}, p2y={8}, p2z={9}",
                    dir, msg.compid, msg.sysid, msg.frame, msg.p1x, msg.p1y, msg.p1z, msg.p2x, msg.p2y, msg.p2z));
            break;
        }
        case msg_attitude_quaternion_cov.MAVLINK_MSG_ID_ATTITUDE_QUATERNION_COV: {
            msg_attitude_quaternion_cov msg = (msg_attitude_quaternion_cov) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} ATTITUDE_QUATERNION_COV: compid={1}, sysid={2}, time_usec={3}, q={4}, rollspeed={5}, pitchspeed={6}, yawspeed={7}, covariance={8}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.q, msg.rollspeed, msg.pitchspeed,
                            msg.yawspeed, msg.covariance));
            break;
        }
        case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT: {
            msg_nav_controller_output msg = (msg_nav_controller_output) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} NAV_CONTROLLER_OUTPUT: compid={1}, sysid={2}, nav_roll={3}, nav_pitch={4}, nav_bearing={5}, target_bearing={6}, wp_dist={7}, alt_error={8}, aspd_error={9}, xtrack_error={10}",
                            dir, msg.compid, msg.sysid, msg.nav_roll, msg.nav_pitch, msg.nav_bearing,
                            msg.target_bearing, msg.wp_dist, msg.alt_error, msg.aspd_error, msg.xtrack_error));
            break;
        }
        case msg_global_position_int_cov.MAVLINK_MSG_ID_GLOBAL_POSITION_INT_COV: {
            msg_global_position_int_cov msg = (msg_global_position_int_cov) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GLOBAL_POSITION_INT_COV: compid={1}, sysid={2}, time_usec={3}, estimator_type={4}, lat={5}, lon={6}, alt={7}, relative_alt={8}, vx={9}, vy={10}, vz={11}, covariance={12}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.estimator_type, msg.lat, msg.lon, msg.alt,
                            msg.relative_alt, msg.vx, msg.vy, msg.vz, msg.covariance));
            break;
        }
        case msg_local_position_ned_cov.MAVLINK_MSG_ID_LOCAL_POSITION_NED_COV: {
            msg_local_position_ned_cov msg = (msg_local_position_ned_cov) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} LOCAL_POSITION_NED_COV: compid={1}, sysid={2}, time_usec={3}, estimator_type={4}, x={5}, y={6}, z={7}, vx={8}, vy={9}, vz={10}, ax={11}, ay={12}, az={13}, covariance={14}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.estimator_type, msg.x, msg.y, msg.z, msg.vx,
                            msg.vy, msg.vz, msg.ax, msg.ay, msg.az, msg.covariance));
            break;
        }
        case msg_rc_channels.MAVLINK_MSG_ID_RC_CHANNELS: {
            msg_rc_channels msg = (msg_rc_channels) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} RC_CHANNELS: compid={1}, sysid={2}, time_boot_ms={3}, chancount={4}, chan1_raw={5}, chan2_raw={6}, chan3_raw={7}, chan4_raw={8}, chan5_raw={9}, chan6_raw={10}, chan7_raw={11}, chan8_raw={12}, chan9_raw={13}, chan10_raw={14}, chan11_raw={15}, chan12_raw={16}, chan13_raw={17}, chan14_raw={18}, chan15_raw={19}, chan16_raw={20}, chan17_raw={21}, chan18_raw={22}, rssi={23}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.chancount, msg.chan1_raw, msg.chan2_raw,
                            msg.chan3_raw, msg.chan4_raw, msg.chan5_raw, msg.chan6_raw, msg.chan7_raw, msg.chan8_raw,
                            msg.chan9_raw, msg.chan10_raw, msg.chan11_raw, msg.chan12_raw, msg.chan13_raw,
                            msg.chan14_raw, msg.chan15_raw, msg.chan16_raw, msg.chan17_raw, msg.chan18_raw, msg.rssi));
            break;
        }
        case msg_request_data_stream.MAVLINK_MSG_ID_REQUEST_DATA_STREAM: {
            msg_request_data_stream msg = (msg_request_data_stream) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} REQUEST_DATA_STREAM: compid={1}, sysid={2}, target_system={3}, target_component={4}, req_stream_id={5}, req_message_rate={6}, start_stop={7}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.req_stream_id,
                            msg.req_message_rate, msg.start_stop));
            break;
        }
        case msg_data_stream.MAVLINK_MSG_ID_DATA_STREAM: {
            msg_data_stream msg = (msg_data_stream) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} DATA_STREAM: compid={1}, sysid={2}, stream_id={3}, message_rate={4}, on_off={5}", dir,
                            msg.compid, msg.sysid, msg.stream_id, msg.message_rate, msg.on_off));
            break;
        }
        case msg_manual_control.MAVLINK_MSG_ID_MANUAL_CONTROL: {
            msg_manual_control msg = (msg_manual_control) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MANUAL_CONTROL: compid={1}, sysid={2}, target={3}, x={4}, y={5}, z={6}, r={7}, buttons={8}",
                            dir, msg.compid, msg.sysid, msg.target, msg.x, msg.y, msg.z, msg.r, msg.buttons));
            break;
        }
        case msg_rc_channels_override.MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE: {
            msg_rc_channels_override msg = (msg_rc_channels_override) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} RC_CHANNELS_OVERRIDE: compid={1}, sysid={2}, target_system={3}, target_component={4}, chan1_raw={5}, chan2_raw={6}, chan3_raw={7}, chan4_raw={8}, chan5_raw={9}, chan6_raw={10}, chan7_raw={11}, chan8_raw={12}",
                    dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.chan1_raw, msg.chan2_raw,
                    msg.chan3_raw, msg.chan4_raw, msg.chan5_raw, msg.chan6_raw, msg.chan7_raw, msg.chan8_raw));
            break;
        }
        case msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT: {
            msg_mission_item_int msg = (msg_mission_item_int) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MISSION_ITEM_INT: compid={1}, sysid={2}, target_system={3}, target_component={4}, seq={5}, frame={6}, command={7}, current={8}, autocontinue={9}, param1={10}, param2={11}, param3={12}, param4={13}, x={14}, y={15}, z={16}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.seq, msg.frame,
                            msg.command, msg.current, msg.autocontinue, msg.param1, msg.param2, msg.param3, msg.param4,
                            msg.x, msg.y, msg.z));
            break;
        }
        case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD: {
            msg_vfr_hud msg = (msg_vfr_hud) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} VFR_HUD: compid={1}, sysid={2}, airspeed={3}, groundspeed={4}, heading={5}, throttle={6}, alt={7}, climb={8}",
                            dir, msg.compid, msg.sysid, msg.airspeed, msg.groundspeed, msg.heading, msg.throttle,
                            msg.alt, msg.climb));
            break;
        }
        case msg_command_int.MAVLINK_MSG_ID_COMMAND_INT: {
            msg_command_int msg = (msg_command_int) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} COMMAND_INT: compid={1}, sysid={2}, target_system={3}, target_component={4}, frame={5}, command={6}, current={7}, autocontinue={8}, param1={9}, param2={10}, param3={11}, param4={12}, x={13}, y={14}, z={15}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.frame, msg.command,
                            msg.current, msg.autocontinue, msg.param1, msg.param2, msg.param3, msg.param4, msg.x, msg.y,
                            msg.z));
            break;
        }
        case msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG: {
            msg_command_long msg = (msg_command_long) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} COMMAND_LONG: compid={1}, sysid={2}, target_system={3}, target_component={4}, command={5}, confirmation={6}, param1={7}, param2={8}, param3={9}, param4={10}, param5={11}, param6={12}, param7={13}",
                    dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.command, msg.confirmation,
                    msg.param1, msg.param2, msg.param3, msg.param4, msg.param5, msg.param6, msg.param7));
            break;
        }
        case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK: {
            msg_command_ack msg = (msg_command_ack) packet.unpack();
            logger.log(priority, MessageFormat.format("{0} COMMAND_ACK: compid={1}, sysid={2}, command={3}, result={4}",
                    dir, msg.compid, msg.sysid, msg.command, msg.result));
            break;
        }
        case msg_manual_setpoint.MAVLINK_MSG_ID_MANUAL_SETPOINT: {
            msg_manual_setpoint msg = (msg_manual_setpoint) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MANUAL_SETPOINT: compid={1}, sysid={2}, time_boot_ms={3}, roll={4}, pitch={5}, yaw={6}, thrust={7}, mode_switch={8}, manual_override_switch={9}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.roll, msg.pitch, msg.yaw, msg.thrust,
                            msg.mode_switch, msg.manual_override_switch));
            break;
        }
        case msg_set_attitude_target.MAVLINK_MSG_ID_SET_ATTITUDE_TARGET: {
            msg_set_attitude_target msg = (msg_set_attitude_target) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} SET_ATTITUDE_TARGET: compid={1}, sysid={2}, time_boot_ms={3}, target_system={4}, target_component={5}, type_mask={6}, q={7}, body_roll_rate={8}, body_pitch_rate={9}, body_yaw_rate={10}, thrust={11}",
                    dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.target_system, msg.target_component,
                    msg.type_mask, msg.q, msg.body_roll_rate, msg.body_pitch_rate, msg.body_yaw_rate, msg.thrust));
            break;
        }
        case msg_attitude_target.MAVLINK_MSG_ID_ATTITUDE_TARGET: {
            msg_attitude_target msg = (msg_attitude_target) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} ATTITUDE_TARGET: compid={1}, sysid={2}, time_boot_ms={3}, type_mask={4}, q={5}, body_roll_rate={6}, body_pitch_rate={7}, body_yaw_rate={8}, thrust={9}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.type_mask, msg.q, msg.body_roll_rate,
                            msg.body_pitch_rate, msg.body_yaw_rate, msg.thrust));
            break;
        }
        case msg_set_position_target_local_ned.MAVLINK_MSG_ID_SET_POSITION_TARGET_LOCAL_NED: {
            msg_set_position_target_local_ned msg = (msg_set_position_target_local_ned) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SET_POSITION_TARGET_LOCAL_NED: compid={1}, sysid={2}, time_boot_ms={3}, target_system={4}, target_component={5}, coordinate_frame={6}, type_mask={7}, x={8}, y={9}, z={10}, vx={11}, vy={12}, vz={13}, afx={14}, afy={15}, afz={16}, yaw={17}, yaw_rate={18}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.target_system, msg.target_component,
                            msg.coordinate_frame, msg.type_mask, msg.x, msg.y, msg.z, msg.vx, msg.vy, msg.vz, msg.afx,
                            msg.afy, msg.afz, msg.yaw, msg.yaw_rate));
            break;
        }
        case msg_position_target_local_ned.MAVLINK_MSG_ID_POSITION_TARGET_LOCAL_NED: {
            msg_position_target_local_ned msg = (msg_position_target_local_ned) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} POSITION_TARGET_LOCAL_NED: compid={1}, sysid={2}, time_boot_ms={3}, coordinate_frame={4}, type_mask={5}, x={6}, y={7}, z={8}, vx={9}, vy={10}, vz={11}, afx={12}, afy={13}, afz={14}, yaw={15}, yaw_rate={16}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.coordinate_frame, msg.type_mask, msg.x,
                            msg.y, msg.z, msg.vx, msg.vy, msg.vz, msg.afx, msg.afy, msg.afz, msg.yaw, msg.yaw_rate));
            break;
        }
        case msg_set_position_target_global_int.MAVLINK_MSG_ID_SET_POSITION_TARGET_GLOBAL_INT: {
            msg_set_position_target_global_int msg = (msg_set_position_target_global_int) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SET_POSITION_TARGET_GLOBAL_INT: compid={1}, sysid={2}, time_boot_ms={3}, target_system={4}, target_component={5}, coordinate_frame={6}, type_mask={7}, lat_int={8}, lon_int={9}, alt={10}, vx={11}, vy={12}, vz={13}, afx={14}, afy={15}, afz={16}, yaw={17}, yaw_rate={18}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.target_system, msg.target_component,
                            msg.coordinate_frame, msg.type_mask, msg.lat_int, msg.lon_int, msg.alt, msg.vx, msg.vy,
                            msg.vz, msg.afx, msg.afy, msg.afz, msg.yaw, msg.yaw_rate));
            break;
        }
        case msg_position_target_global_int.MAVLINK_MSG_ID_POSITION_TARGET_GLOBAL_INT: {
            msg_position_target_global_int msg = (msg_position_target_global_int) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} POSITION_TARGET_GLOBAL_INT: compid={1}, sysid={2}, time_boot_ms={3}, coordinate_frame={4}, type_mask={5}, lat_int={6}, lon_int={7}, alt={8}, vx={9}, vy={10}, vz={11}, afx={12}, afy={13}, afz={14}, yaw={15}, yaw_rate={16}",
                    dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.coordinate_frame, msg.type_mask, msg.lat_int,
                    msg.lon_int, msg.alt, msg.vx, msg.vy, msg.vz, msg.afx, msg.afy, msg.afz, msg.yaw, msg.yaw_rate));
            break;
        }
        case msg_local_position_ned_system_global_offset.MAVLINK_MSG_ID_LOCAL_POSITION_NED_SYSTEM_GLOBAL_OFFSET: {
            msg_local_position_ned_system_global_offset msg = (msg_local_position_ned_system_global_offset) packet
                    .unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} LOCAL_POSITION_NED_SYSTEM_GLOBAL_OFFSET: compid={1}, sysid={2}, time_boot_ms={3}, x={4}, y={5}, z={6}, roll={7}, pitch={8}, yaw={9}",
                    dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.x, msg.y, msg.z, msg.roll, msg.pitch, msg.yaw));
            break;
        }
        case msg_hil_state.MAVLINK_MSG_ID_HIL_STATE: {
            msg_hil_state msg = (msg_hil_state) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HIL_STATE: compid={1}, sysid={2}, time_usec={3}, roll={4}, pitch={5}, yaw={6}, rollspeed={7}, pitchspeed={8}, yawspeed={9}, lat={10}, lon={11}, alt={12}, vx={13}, vy={14}, vz={15}, xacc={16}, yacc={17}, zacc={18}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.roll, msg.pitch, msg.yaw, msg.rollspeed,
                            msg.pitchspeed, msg.yawspeed, msg.lat, msg.lon, msg.alt, msg.vx, msg.vy, msg.vz, msg.xacc,
                            msg.yacc, msg.zacc));
            break;
        }
        case msg_hil_controls.MAVLINK_MSG_ID_HIL_CONTROLS: {
            msg_hil_controls msg = (msg_hil_controls) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} HIL_CONTROLS: compid={1}, sysid={2}, time_usec={3}, roll_ailerons={4}, pitch_elevator={5}, yaw_rudder={6}, throttle={7}, aux1={8}, aux2={9}, aux3={10}, aux4={11}, mode={12}, nav_mode={13}",
                    dir, msg.compid, msg.sysid, msg.time_usec, msg.roll_ailerons, msg.pitch_elevator, msg.yaw_rudder,
                    msg.throttle, msg.aux1, msg.aux2, msg.aux3, msg.aux4, msg.mode, msg.nav_mode));
            break;
        }
        case msg_hil_rc_inputs_raw.MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW: {
            msg_hil_rc_inputs_raw msg = (msg_hil_rc_inputs_raw) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HIL_RC_INPUTS_RAW: compid={1}, sysid={2}, time_usec={3}, chan1_raw={4}, chan2_raw={5}, chan3_raw={6}, chan4_raw={7}, chan5_raw={8}, chan6_raw={9}, chan7_raw={10}, chan8_raw={11}, chan9_raw={12}, chan10_raw={13}, chan11_raw={14}, chan12_raw={15}, rssi={16}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.chan1_raw, msg.chan2_raw, msg.chan3_raw,
                            msg.chan4_raw, msg.chan5_raw, msg.chan6_raw, msg.chan7_raw, msg.chan8_raw, msg.chan9_raw,
                            msg.chan10_raw, msg.chan11_raw, msg.chan12_raw, msg.rssi));
            break;
        }
        case msg_hil_actuator_controls.MAVLINK_MSG_ID_HIL_ACTUATOR_CONTROLS: {
            msg_hil_actuator_controls msg = (msg_hil_actuator_controls) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HIL_ACTUATOR_CONTROLS: compid={1}, sysid={2}, time_usec={3}, controls={4}, mode={5}, flags={6}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.controls, msg.mode, msg.flags));
            break;
        }
        case msg_optical_flow.MAVLINK_MSG_ID_OPTICAL_FLOW: {
            msg_optical_flow msg = (msg_optical_flow) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} OPTICAL_FLOW: compid={1}, sysid={2}, time_usec={3}, sensor_id={4}, flow_x={5}, flow_y={6}, flow_comp_m_x={7}, flow_comp_m_y={8}, quality={9}, ground_distance={10}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.sensor_id, msg.flow_x, msg.flow_y,
                            msg.flow_comp_m_x, msg.flow_comp_m_y, msg.quality, msg.ground_distance));
            break;
        }
        case msg_global_vision_position_estimate.MAVLINK_MSG_ID_GLOBAL_VISION_POSITION_ESTIMATE: {
            msg_global_vision_position_estimate msg = (msg_global_vision_position_estimate) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GLOBAL_VISION_POSITION_ESTIMATE: compid={1}, sysid={2}, usec={3}, x={4}, y={5}, z={6}, roll={7}, pitch={8}, yaw={9}",
                            dir, msg.compid, msg.sysid, msg.usec, msg.x, msg.y, msg.z, msg.roll, msg.pitch, msg.yaw));
            break;
        }
        case msg_vision_position_estimate.MAVLINK_MSG_ID_VISION_POSITION_ESTIMATE: {
            msg_vision_position_estimate msg = (msg_vision_position_estimate) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} VISION_POSITION_ESTIMATE: compid={1}, sysid={2}, usec={3}, x={4}, y={5}, z={6}, roll={7}, pitch={8}, yaw={9}",
                            dir, msg.compid, msg.sysid, msg.usec, msg.x, msg.y, msg.z, msg.roll, msg.pitch, msg.yaw));
            break;
        }
        case msg_vision_speed_estimate.MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE: {
            msg_vision_speed_estimate msg = (msg_vision_speed_estimate) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} VISION_SPEED_ESTIMATE: compid={1}, sysid={2}, usec={3}, x={4}, y={5}, z={6}", dir,
                            msg.compid, msg.sysid, msg.usec, msg.x, msg.y, msg.z));
            break;
        }
        case msg_vicon_position_estimate.MAVLINK_MSG_ID_VICON_POSITION_ESTIMATE: {
            msg_vicon_position_estimate msg = (msg_vicon_position_estimate) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} VICON_POSITION_ESTIMATE: compid={1}, sysid={2}, usec={3}, x={4}, y={5}, z={6}, roll={7}, pitch={8}, yaw={9}",
                            dir, msg.compid, msg.sysid, msg.usec, msg.x, msg.y, msg.z, msg.roll, msg.pitch, msg.yaw));
            break;
        }
        case msg_highres_imu.MAVLINK_MSG_ID_HIGHRES_IMU: {
            msg_highres_imu msg = (msg_highres_imu) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HIGHRES_IMU: compid={1}, sysid={2}, time_usec={3}, xacc={4}, yacc={5}, zacc={6}, xgyro={7}, ygyro={8}, zgyro={9}, xmag={10}, ymag={11}, zmag={12}, abs_pressure={13}, diff_pressure={14}, pressure_alt={15}, temperature={16}, fields_updated={17}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.xacc, msg.yacc, msg.zacc, msg.xgyro,
                            msg.ygyro, msg.zgyro, msg.xmag, msg.ymag, msg.zmag, msg.abs_pressure, msg.diff_pressure,
                            msg.pressure_alt, msg.temperature, msg.fields_updated));
            break;
        }
        case msg_optical_flow_rad.MAVLINK_MSG_ID_OPTICAL_FLOW_RAD: {
            msg_optical_flow_rad msg = (msg_optical_flow_rad) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} OPTICAL_FLOW_RAD: compid={1}, sysid={2}, time_usec={3}, sensor_id={4}, integration_time_us={5}, integrated_x={6}, integrated_y={7}, integrated_xgyro={8}, integrated_ygyro={9}, integrated_zgyro={10}, temperature={11}, quality={12}, time_delta_distance_us={13}, distance={14}",
                    dir, msg.compid, msg.sysid, msg.time_usec, msg.sensor_id, msg.integration_time_us, msg.integrated_x,
                    msg.integrated_y, msg.integrated_xgyro, msg.integrated_ygyro, msg.integrated_zgyro, msg.temperature,
                    msg.quality, msg.time_delta_distance_us, msg.distance));
            break;
        }
        case msg_hil_sensor.MAVLINK_MSG_ID_HIL_SENSOR: {
            msg_hil_sensor msg = (msg_hil_sensor) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HIL_SENSOR: compid={1}, sysid={2}, time_usec={3}, xacc={4}, yacc={5}, zacc={6}, xgyro={7}, ygyro={8}, zgyro={9}, xmag={10}, ymag={11}, zmag={12}, abs_pressure={13}, diff_pressure={14}, pressure_alt={15}, temperature={16}, fields_updated={17}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.xacc, msg.yacc, msg.zacc, msg.xgyro,
                            msg.ygyro, msg.zgyro, msg.xmag, msg.ymag, msg.zmag, msg.abs_pressure, msg.diff_pressure,
                            msg.pressure_alt, msg.temperature, msg.fields_updated));
            break;
        }
        case msg_sim_state.MAVLINK_MSG_ID_SIM_STATE: {
            msg_sim_state msg = (msg_sim_state) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SIM_STATE: compid={1}, sysid={2}, q1={3}, q2={4}, q3={5}, q4={6}, roll={7}, pitch={8}, yaw={9}, xacc={10}, yacc={11}, zacc={12}, xgyro={13}, ygyro={14}, zgyro={15}, lat={16}, lon={17}, alt={18}, std_dev_horz={19}, std_dev_vert={20}, vn={21}, ve={22}, vd={23}",
                            dir, msg.compid, msg.sysid, msg.q1, msg.q2, msg.q3, msg.q4, msg.roll, msg.pitch, msg.yaw,
                            msg.xacc, msg.yacc, msg.zacc, msg.xgyro, msg.ygyro, msg.zgyro, msg.lat, msg.lon, msg.alt,
                            msg.std_dev_horz, msg.std_dev_vert, msg.vn, msg.ve, msg.vd));
            break;
        }
        case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS: {
            msg_radio_status msg = (msg_radio_status) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} RADIO_STATUS: compid={1}, sysid={2}, rssi={3}, remrssi={4}, txbuf={5}, noise={6}, remnoise={7}, rxerrors={8}, fixed={9}",
                            dir, msg.compid, msg.sysid, msg.rssi, msg.remrssi, msg.txbuf, msg.noise, msg.remnoise,
                            msg.rxerrors, msg.fixed));
            break;
        }
        case msg_file_transfer_protocol.MAVLINK_MSG_ID_FILE_TRANSFER_PROTOCOL: {
            msg_file_transfer_protocol msg = (msg_file_transfer_protocol) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} FILE_TRANSFER_PROTOCOL: compid={1}, sysid={2}, target_network={3}, target_system={4}, target_component={5}, payload={6}",
                            dir, msg.compid, msg.sysid, msg.target_network, msg.target_system, msg.target_component,
                            msg.payload));
            break;
        }
        case msg_timesync.MAVLINK_MSG_ID_TIMESYNC: {
            msg_timesync msg = (msg_timesync) packet.unpack();
            logger.log(priority, MessageFormat.format("{0} TIMESYNC: compid={1}, sysid={2}, tc1={3}, ts1={4}", dir,
                    msg.compid, msg.sysid, msg.tc1, msg.ts1));
            break;
        }
        case msg_camera_trigger.MAVLINK_MSG_ID_CAMERA_TRIGGER: {
            msg_camera_trigger msg = (msg_camera_trigger) packet.unpack();
            logger.log(priority,
                    MessageFormat.format("{0} CAMERA_TRIGGER: compid={1}, sysid={2}, time_usec={3}, seq={4}", dir,
                            msg.compid, msg.sysid, msg.time_usec, msg.seq));
            break;
        }
        case msg_hil_gps.MAVLINK_MSG_ID_HIL_GPS: {
            msg_hil_gps msg = (msg_hil_gps) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HIL_GPS: compid={1}, sysid={2}, time_usec={3}, fix_type={4}, lat={5}, lon={6}, alt={7}, eph={8}, epv={9}, vel={10}, vn={11}, ve={12}, vd={13}, cog={14}, satellites_visible={15}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.fix_type, msg.lat, msg.lon, msg.alt, msg.eph,
                            msg.epv, msg.vel, msg.vn, msg.ve, msg.vd, msg.cog, msg.satellites_visible));
            break;
        }
        case msg_hil_optical_flow.MAVLINK_MSG_ID_HIL_OPTICAL_FLOW: {
            msg_hil_optical_flow msg = (msg_hil_optical_flow) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} HIL_OPTICAL_FLOW: compid={1}, sysid={2}, time_usec={3}, sensor_id={4}, integration_time_us={5}, integrated_x={6}, integrated_y={7}, integrated_xgyro={8}, integrated_ygyro={9}, integrated_zgyro={10}, temperature={11}, quality={12}, time_delta_distance_us={13}, distance={14}",
                    dir, msg.compid, msg.sysid, msg.time_usec, msg.sensor_id, msg.integration_time_us, msg.integrated_x,
                    msg.integrated_y, msg.integrated_xgyro, msg.integrated_ygyro, msg.integrated_zgyro, msg.temperature,
                    msg.quality, msg.time_delta_distance_us, msg.distance));
            break;
        }
        case msg_hil_state_quaternion.MAVLINK_MSG_ID_HIL_STATE_QUATERNION: {
            msg_hil_state_quaternion msg = (msg_hil_state_quaternion) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HIL_STATE_QUATERNION: compid={1}, sysid={2}, time_usec={3}, attitude_quaternion={4}, rollspeed={5}, pitchspeed={6}, yawspeed={7}, lat={8}, lon={9}, alt={10}, vx={11}, vy={12}, vz={13}, ind_airspeed={14}, true_airspeed={15}, xacc={16}, yacc={17}, zacc={18}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.attitude_quaternion, msg.rollspeed,
                            msg.pitchspeed, msg.yawspeed, msg.lat, msg.lon, msg.alt, msg.vx, msg.vy, msg.vz,
                            msg.ind_airspeed, msg.true_airspeed, msg.xacc, msg.yacc, msg.zacc));
            break;
        }
        case msg_scaled_imu2.MAVLINK_MSG_ID_SCALED_IMU2: {
            msg_scaled_imu2 msg = (msg_scaled_imu2) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SCALED_IMU2: compid={1}, sysid={2}, time_boot_ms={3}, xacc={4}, yacc={5}, zacc={6}, xgyro={7}, ygyro={8}, zgyro={9}, xmag={10}, ymag={11}, zmag={12}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.xacc, msg.yacc, msg.zacc, msg.xgyro,
                            msg.ygyro, msg.zgyro, msg.xmag, msg.ymag, msg.zmag));
            break;
        }
        case msg_log_request_list.MAVLINK_MSG_ID_LOG_REQUEST_LIST: {
            msg_log_request_list msg = (msg_log_request_list) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} LOG_REQUEST_LIST: compid={1}, sysid={2}, target_system={3}, target_component={4}, start={5}, end={6}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.start, msg.end));
            break;
        }
        case msg_log_entry.MAVLINK_MSG_ID_LOG_ENTRY: {
            msg_log_entry msg = (msg_log_entry) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} LOG_ENTRY: compid={1}, sysid={2}, id={3}, num_logs={4}, last_log_num={5}, time_utc={6}, size={7}",
                    dir, msg.compid, msg.sysid, msg.id, msg.num_logs, msg.last_log_num, msg.time_utc, msg.size));
            break;
        }
        case msg_log_request_data.MAVLINK_MSG_ID_LOG_REQUEST_DATA: {
            msg_log_request_data msg = (msg_log_request_data) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} LOG_REQUEST_DATA: compid={1}, sysid={2}, target_system={3}, target_component={4}, id={5}, ofs={6}, count={7}",
                    dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.id, msg.ofs, msg.count));
            break;
        }
        case msg_log_data.MAVLINK_MSG_ID_LOG_DATA: {
            msg_log_data msg = (msg_log_data) packet.unpack();
            logger.debug(
                    MessageFormat.format("{0} LOG_DATA: compid={1}, sysid={2}, id={3}, ofs={4}, count={5}, data={6}",
                            dir, msg.compid, msg.sysid, msg.id, msg.ofs, msg.count, msg.data));
            break;
        }
        case msg_log_erase.MAVLINK_MSG_ID_LOG_ERASE: {
            msg_log_erase msg = (msg_log_erase) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} LOG_ERASE: compid={1}, sysid={2}, target_system={3}, target_component={4}", dir,
                            msg.compid, msg.sysid, msg.target_system, msg.target_component));
            break;
        }
        case msg_log_request_end.MAVLINK_MSG_ID_LOG_REQUEST_END: {
            msg_log_request_end msg = (msg_log_request_end) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} LOG_REQUEST_END: compid={1}, sysid={2}, target_system={3}, target_component={4}", dir,
                            msg.compid, msg.sysid, msg.target_system, msg.target_component));
            break;
        }
        case msg_gps_inject_data.MAVLINK_MSG_ID_GPS_INJECT_DATA: {
            msg_gps_inject_data msg = (msg_gps_inject_data) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GPS_INJECT_DATA: compid={1}, sysid={2}, target_system={3}, target_component={4}, len={5}, data={6}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.target_component, msg.len, msg.data));
            break;
        }
        case msg_gps2_raw.MAVLINK_MSG_ID_GPS2_RAW: {
            msg_gps2_raw msg = (msg_gps2_raw) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GPS2_RAW: compid={1}, sysid={2}, time_usec={3}, fix_type={4}, lat={5}, lon={6}, alt={7}, eph={8}, epv={9}, vel={10}, cog={11}, satellites_visible={12}, dgps_numch={13}, dgps_age={14}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.fix_type, msg.lat, msg.lon, msg.alt, msg.eph,
                            msg.epv, msg.vel, msg.cog, msg.satellites_visible, msg.dgps_numch, msg.dgps_age));
            break;
        }
        case msg_power_status.MAVLINK_MSG_ID_POWER_STATUS: {
            msg_power_status msg = (msg_power_status) packet.unpack();
            logger.log(priority,
                    MessageFormat.format("{0} POWER_STATUS: compid={1}, sysid={2}, Vcc={3}, Vservo={4}, flags={5}", dir,
                            msg.compid, msg.sysid, msg.Vcc, msg.Vservo, msg.flags));
            break;
        }
        case msg_serial_control.MAVLINK_MSG_ID_SERIAL_CONTROL: {
            msg_serial_control msg = (msg_serial_control) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} SERIAL_CONTROL: compid={1}, sysid={2}, device={3}, flags={4}, timeout={5}, baudrate={6}, count={7}, data={8}",
                    dir, msg.compid, msg.sysid, msg.device, msg.flags, msg.timeout, msg.baudrate, msg.count, msg.data));
            break;
        }
        case msg_gps_rtk.MAVLINK_MSG_ID_GPS_RTK: {
            msg_gps_rtk msg = (msg_gps_rtk) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GPS_RTK: compid={1}, sysid={2}, time_last_baseline_ms={3}, rtk_receiver_id={4}, wn={5}, tow={6}, rtk_health={7}, rtk_rate={8}, nsats={9}, baseline_coords_type={10}, baseline_a_mm={11}, baseline_b_mm={12}, baseline_c_mm={13}, accuracy={14}, iar_num_hypotheses={15}",
                            dir, msg.compid, msg.sysid, msg.time_last_baseline_ms, msg.rtk_receiver_id, msg.wn, msg.tow,
                            msg.rtk_health, msg.rtk_rate, msg.nsats, msg.baseline_coords_type, msg.baseline_a_mm,
                            msg.baseline_b_mm, msg.baseline_c_mm, msg.accuracy, msg.iar_num_hypotheses));
            break;
        }
        case msg_gps2_rtk.MAVLINK_MSG_ID_GPS2_RTK: {
            msg_gps2_rtk msg = (msg_gps2_rtk) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GPS2_RTK: compid={1}, sysid={2}, time_last_baseline_ms={3}, rtk_receiver_id={4}, wn={5}, tow={6}, rtk_health={7}, rtk_rate={8}, nsats={9}, baseline_coords_type={10}, baseline_a_mm={11}, baseline_b_mm={12}, baseline_c_mm={13}, accuracy={14}, iar_num_hypotheses={15}",
                            dir, msg.compid, msg.sysid, msg.time_last_baseline_ms, msg.rtk_receiver_id, msg.wn, msg.tow,
                            msg.rtk_health, msg.rtk_rate, msg.nsats, msg.baseline_coords_type, msg.baseline_a_mm,
                            msg.baseline_b_mm, msg.baseline_c_mm, msg.accuracy, msg.iar_num_hypotheses));
            break;
        }
        case msg_scaled_imu3.MAVLINK_MSG_ID_SCALED_IMU3: {
            msg_scaled_imu3 msg = (msg_scaled_imu3) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SCALED_IMU3: compid={1}, sysid={2}, time_boot_ms={3}, xacc={4}, yacc={5}, zacc={6}, xgyro={7}, ygyro={8}, zgyro={9}, xmag={10}, ymag={11}, zmag={12}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.xacc, msg.yacc, msg.zacc, msg.xgyro,
                            msg.ygyro, msg.zgyro, msg.xmag, msg.ymag, msg.zmag));
            break;
        }
        case msg_data_transmission_handshake.MAVLINK_MSG_ID_DATA_TRANSMISSION_HANDSHAKE: {
            msg_data_transmission_handshake msg = (msg_data_transmission_handshake) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} DATA_TRANSMISSION_HANDSHAKE: compid={1}, sysid={2}, type={3}, size={4}, width={5}, height={6}, packets={7}, payload={8}, jpg_quality={9}",
                            dir, msg.compid, msg.sysid, msg.type, msg.size, msg.width, msg.height, msg.packets,
                            msg.payload, msg.jpg_quality));
            break;
        }
        case msg_encapsulated_data.MAVLINK_MSG_ID_ENCAPSULATED_DATA: {
            msg_encapsulated_data msg = (msg_encapsulated_data) packet.unpack();
            logger.log(priority,
                    MessageFormat.format("{0} ENCAPSULATED_DATA: compid={1}, sysid={2}, seqnr={3}, data={4}", dir,
                            msg.compid, msg.sysid, msg.seqnr, msg.data));
            break;
        }
        case msg_distance_sensor.MAVLINK_MSG_ID_DISTANCE_SENSOR: {
            msg_distance_sensor msg = (msg_distance_sensor) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} DISTANCE_SENSOR: compid={1}, sysid={2}, time_boot_ms={3}, min_distance={4}, max_distance={5}, current_distance={6}, type={7}, id={8}, orientation={9}, covariance={10}",
                            dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.min_distance, msg.max_distance,
                            msg.current_distance, msg.type, msg.id, msg.orientation, msg.covariance));
            break;
        }
        case msg_terrain_request.MAVLINK_MSG_ID_TERRAIN_REQUEST: {
            msg_terrain_request msg = (msg_terrain_request) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} TERRAIN_REQUEST: compid={1}, sysid={2}, lat={3}, lon={4}, grid_spacing={5}, mask={6}",
                            dir, msg.compid, msg.sysid, msg.lat, msg.lon, msg.grid_spacing, msg.mask));
            break;
        }
        case msg_terrain_data.MAVLINK_MSG_ID_TERRAIN_DATA: {
            msg_terrain_data msg = (msg_terrain_data) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} TERRAIN_DATA: compid={1}, sysid={2}, lat={3}, lon={4}, grid_spacing={5}, gridbit={6}, data={7}",
                            dir, msg.compid, msg.sysid, msg.lat, msg.lon, msg.grid_spacing, msg.gridbit, msg.data));
            break;
        }
        case msg_terrain_check.MAVLINK_MSG_ID_TERRAIN_CHECK: {
            msg_terrain_check msg = (msg_terrain_check) packet.unpack();
            logger.log(priority, MessageFormat.format("{0} TERRAIN_CHECK: compid={1}, sysid={2}, lat={3}, lon={4}", dir,
                    msg.compid, msg.sysid, msg.lat, msg.lon));
            break;
        }
        case msg_terrain_report.MAVLINK_MSG_ID_TERRAIN_REPORT: {
            msg_terrain_report msg = (msg_terrain_report) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} TERRAIN_REPORT: compid={1}, sysid={2}, lat={3}, lon={4}, spacing={5}, terrain_height={6}, current_height={7}, pending={8}, loaded={9}",
                            dir, msg.compid, msg.sysid, msg.lat, msg.lon, msg.spacing, msg.terrain_height,
                            msg.current_height, msg.pending, msg.loaded));
            break;
        }
        case msg_scaled_pressure2.MAVLINK_MSG_ID_SCALED_PRESSURE2: {
            msg_scaled_pressure2 msg = (msg_scaled_pressure2) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} SCALED_PRESSURE2: compid={1}, sysid={2}, time_boot_ms={3}, press_abs={4}, press_diff={5}, temperature={6}",
                    dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.press_abs, msg.press_diff, msg.temperature));
            break;
        }
        case msg_att_pos_mocap.MAVLINK_MSG_ID_ATT_POS_MOCAP: {
            msg_att_pos_mocap msg = (msg_att_pos_mocap) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} ATT_POS_MOCAP: compid={1}, sysid={2}, time_usec={3}, q={4}, x={5}, y={6}, z={7}", dir,
                            msg.compid, msg.sysid, msg.time_usec, msg.q, msg.x, msg.y, msg.z));
            break;
        }
        case msg_set_actuator_control_target.MAVLINK_MSG_ID_SET_ACTUATOR_CONTROL_TARGET: {
            msg_set_actuator_control_target msg = (msg_set_actuator_control_target) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SET_ACTUATOR_CONTROL_TARGET: compid={1}, sysid={2}, time_usec={3}, group_mlx={4}, target_system={5}, target_component={6}, controls={7}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.group_mlx, msg.target_system,
                            msg.target_component, msg.controls));
            break;
        }
        case msg_actuator_control_target.MAVLINK_MSG_ID_ACTUATOR_CONTROL_TARGET: {
            msg_actuator_control_target msg = (msg_actuator_control_target) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} ACTUATOR_CONTROL_TARGET: compid={1}, sysid={2}, time_usec={3}, group_mlx={4}, controls={5}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.group_mlx, msg.controls));
            break;
        }
        case msg_altitude.MAVLINK_MSG_ID_ALTITUDE: {
            msg_altitude msg = (msg_altitude) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} ALTITUDE: compid={1}, sysid={2}, time_usec={3}, altitude_monotonic={4}, altitude_amsl={5}, altitude_local={6}, altitude_relative={7}, altitude_terrain={8}, bottom_clearance={9}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.altitude_monotonic, msg.altitude_amsl,
                            msg.altitude_local, msg.altitude_relative, msg.altitude_terrain, msg.bottom_clearance));
            break;
        }
        case msg_resource_request.MAVLINK_MSG_ID_RESOURCE_REQUEST: {
            msg_resource_request msg = (msg_resource_request) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} RESOURCE_REQUEST: compid={1}, sysid={2}, request_id={3}, uri_type={4}, uri={5}, transfer_type={6}, storage={7}",
                    dir, msg.compid, msg.sysid, msg.request_id, msg.uri_type, msg.uri, msg.transfer_type, msg.storage));
            break;
        }
        case msg_scaled_pressure3.MAVLINK_MSG_ID_SCALED_PRESSURE3: {
            msg_scaled_pressure3 msg = (msg_scaled_pressure3) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} SCALED_PRESSURE3: compid={1}, sysid={2}, time_boot_ms={3}, press_abs={4}, press_diff={5}, temperature={6}",
                    dir, msg.compid, msg.sysid, msg.time_boot_ms, msg.press_abs, msg.press_diff, msg.temperature));
            break;
        }
        case msg_follow_target.MAVLINK_MSG_ID_FOLLOW_TARGET: {
            msg_follow_target msg = (msg_follow_target) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} FOLLOW_TARGET: compid={1}, sysid={2}, timestamp={3}, est_capabilities={4}, lat={5}, lon={6}, alt={7}, vel={8}, acc={9}, attitude_q={10}, rates={11}, position_cov={12}, custom_state={13}",
                            dir, msg.compid, msg.sysid, msg.timestamp, msg.est_capabilities, msg.lat, msg.lon, msg.alt,
                            msg.vel, msg.acc, msg.attitude_q, msg.rates, msg.position_cov, msg.custom_state));
            break;
        }
        case msg_control_system_state.MAVLINK_MSG_ID_CONTROL_SYSTEM_STATE: {
            msg_control_system_state msg = (msg_control_system_state) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} CONTROL_SYSTEM_STATE: compid={1}, sysid={2}, time_usec={3}, x_acc={4}, y_acc={5}, z_acc={6}, x_vel={7}, y_vel={8}, z_vel={9}, x_pos={10}, y_pos={11}, z_pos={12}, airspeed={13}, vel_variance={14}, pos_variance={15}, q={16}, roll_rate={17}, pitch_rate={18}, yaw_rate={19}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.x_acc, msg.y_acc, msg.z_acc, msg.x_vel,
                            msg.y_vel, msg.z_vel, msg.x_pos, msg.y_pos, msg.z_pos, msg.airspeed, msg.vel_variance,
                            msg.pos_variance, msg.q, msg.roll_rate, msg.pitch_rate, msg.yaw_rate));
            break;
        }
        case msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS: {
            msg_battery_status msg = (msg_battery_status) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} BATTERY_STATUS: compid={1}, sysid={2}, id={3}, battery_function={4}, type={5}, temperature={6}, voltages={7}, current_battery={8}, current_consumed={9}, energy_consumed={10}, battery_remaining={11}",
                    dir, msg.compid, msg.sysid, msg.id, msg.battery_function, msg.type, msg.temperature, msg.voltages,
                    msg.current_battery, msg.current_consumed, msg.energy_consumed, msg.battery_remaining));
            break;
        }
        case msg_autopilot_version.MAVLINK_MSG_ID_AUTOPILOT_VERSION: {
            msg_autopilot_version msg = (msg_autopilot_version) packet.unpack();
            logger.log(priority, MessageFormat.format(
                    "{0} AUTOPILOT_VERSION: compid={1}, sysid={2}, capabilities={3}, flight_sw_version={4}, middleware_sw_version={5}, os_sw_version={6}, board_version={7}, flight_custom_version={8}, middleware_custom_version={9}, os_custom_version={10}, vendor_id={11}, product_id={12}, uid={13}",
                    dir, msg.compid, msg.sysid, msg.capabilities, msg.flight_sw_version, msg.middleware_sw_version,
                    msg.os_sw_version, msg.board_version, msg.flight_custom_version, msg.middleware_custom_version,
                    msg.os_custom_version, msg.vendor_id, msg.product_id, msg.uid));
            break;
        }
        case msg_landing_target.MAVLINK_MSG_ID_LANDING_TARGET: {
            msg_landing_target msg = (msg_landing_target) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} LANDING_TARGET: compid={1}, sysid={2}, time_usec={3}, target_num={4}, frame={5}, angle_x={6}, angle_y={7}, distance={8}, size_x={9}, size_y={10}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.target_num, msg.frame, msg.angle_x,
                            msg.angle_y, msg.distance, msg.size_x, msg.size_y));
            break;
        }
        case msg_estimator_status.MAVLINK_MSG_ID_ESTIMATOR_STATUS: {
            msg_estimator_status msg = (msg_estimator_status) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} ESTIMATOR_STATUS: compid={1}, sysid={2}, time_usec={3}, flags={4}, vel_ratio={5}, pos_horiz_ratio={6}, pos_vert_ratio={7}, mag_ratio={8}, hagl_ratio={9}, tas_ratio={10}, pos_horiz_accuracy={11}, pos_vert_accuracy={12}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.flags, msg.vel_ratio, msg.pos_horiz_ratio,
                            msg.pos_vert_ratio, msg.mag_ratio, msg.hagl_ratio, msg.tas_ratio, msg.pos_horiz_accuracy,
                            msg.pos_vert_accuracy));
            break;
        }
        case msg_wind_cov.MAVLINK_MSG_ID_WIND_COV: {
            msg_wind_cov msg = (msg_wind_cov) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} WIND_COV: compid={1}, sysid={2}, time_usec={3}, wind_x={4}, wind_y={5}, wind_z={6}, var_horiz={7}, var_vert={8}, wind_alt={9}, horiz_accuracy={10}, vert_accuracy={11}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.wind_x, msg.wind_y, msg.wind_z,
                            msg.var_horiz, msg.var_vert, msg.wind_alt, msg.horiz_accuracy, msg.vert_accuracy));
            break;
        }
        case msg_gps_input.MAVLINK_MSG_ID_GPS_INPUT: {
            msg_gps_input msg = (msg_gps_input) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} GPS_INPUT: compid={1}, sysid={2}, time_usec={3}, gps_id={4}, ignore_flags={5}, time_week_ms={6}, time_week={7}, fix_type={8}, lat={9}, lon={10}, alt={11}, hdop={12}, vdop={13}, vn={14}, ve={15}, vd={16}, speed_accuracy={17}, horiz_accuracy={18}, vert_accuracy={19}, satellites_visible={20}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.gps_id, msg.ignore_flags, msg.time_week_ms,
                            msg.time_week, msg.fix_type, msg.lat, msg.lon, msg.alt, msg.hdop, msg.vdop, msg.vn, msg.ve,
                            msg.vd, msg.speed_accuracy, msg.horiz_accuracy, msg.vert_accuracy, msg.satellites_visible));
            break;
        }
        case msg_gps_rtcm_data.MAVLINK_MSG_ID_GPS_RTCM_DATA: {
            msg_gps_rtcm_data msg = (msg_gps_rtcm_data) packet.unpack();
            logger.log(priority,
                    MessageFormat.format("{0} GPS_RTCM_DATA: compid={1}, sysid={2}, flags={3}, len={4}, data={5}", dir,
                            msg.compid, msg.sysid, msg.flags, msg.len, msg.data));
            break;
        }
        case msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY: {
            msg_high_latency msg = (msg_high_latency) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HIGH_LATENCY: compid={1}, sysid={2}, base_mode={3}, custom_mode={4}, landed_state={5}, roll={6}, pitch={7}, heading={8}, throttle={9}, heading_sp={10}, latitude={11}, longitude={12}, altitude_amsl={13}, altitude_sp={14}, airspeed={15}, airspeed_sp={16}, groundspeed={17}, climb_rate={18}, gps_nsat={19}, gps_fix_type={20}, battery_remaining={21}, temperature={22}, temperature_air={23}, failsafe={24}, wp_num={25}, wp_distance={26}",
                            dir, msg.compid, msg.sysid, msg.base_mode, msg.custom_mode, msg.landed_state, msg.roll,
                            msg.pitch, msg.heading, msg.throttle, msg.heading_sp, msg.latitude, msg.longitude,
                            msg.altitude_amsl, msg.altitude_sp, msg.airspeed, msg.airspeed_sp, msg.groundspeed,
                            msg.climb_rate, msg.gps_nsat, msg.gps_fix_type, msg.battery_remaining, msg.temperature,
                            msg.temperature_air, msg.failsafe, msg.wp_num, msg.wp_distance));
            break;
        }
        case msg_vibration.MAVLINK_MSG_ID_VIBRATION: {
            msg_vibration msg = (msg_vibration) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} VIBRATION: compid={1}, sysid={2}, time_usec={3}, vibration_x={4}, vibration_y={5}, vibration_z={6}, clipping_0={7}, clipping_1={8}, clipping_2={9}",
                            dir, msg.compid, msg.sysid, msg.time_usec, msg.vibration_x, msg.vibration_y,
                            msg.vibration_z, msg.clipping_0, msg.clipping_1, msg.clipping_2));
            break;
        }
        case msg_home_position.MAVLINK_MSG_ID_HOME_POSITION: {
            msg_home_position msg = (msg_home_position) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} HOME_POSITION: compid={1}, sysid={2}, latitude={3}, longitude={4}, altitude={5}, x={6}, y={7}, z={8}, q={9}, approach_x={10}, approach_y={11}, approach_z={12}",
                            dir, msg.compid, msg.sysid, msg.latitude, msg.longitude, msg.altitude, msg.x, msg.y, msg.z,
                            msg.q, msg.approach_x, msg.approach_y, msg.approach_z));
            break;
        }
        case msg_set_home_position.MAVLINK_MSG_ID_SET_HOME_POSITION: {
            msg_set_home_position msg = (msg_set_home_position) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} SET_HOME_POSITION: compid={1}, sysid={2}, target_system={3}, latitude={4}, longitude={5}, altitude={6}, x={7}, y={8}, z={9}, q={10}, approach_x={11}, approach_y={12}, approach_z={13}",
                            dir, msg.compid, msg.sysid, msg.target_system, msg.latitude, msg.longitude, msg.altitude,
                            msg.x, msg.y, msg.z, msg.q, msg.approach_x, msg.approach_y, msg.approach_z));
            break;
        }
        case msg_message_interval.MAVLINK_MSG_ID_MESSAGE_INTERVAL: {
            msg_message_interval msg = (msg_message_interval) packet.unpack();
            logger.debug(
                    MessageFormat.format("{0} MESSAGE_INTERVAL: compid={1}, sysid={2}, message_id={3}, interval_us={4}",
                            dir, msg.compid, msg.sysid, msg.message_id, msg.interval_us));
            break;
        }
        case msg_extended_sys_state.MAVLINK_MSG_ID_EXTENDED_SYS_STATE: {
            msg_extended_sys_state msg = (msg_extended_sys_state) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} EXTENDED_SYS_STATE: compid={1}, sysid={2}, vtol_state={3}, landed_state={4}", dir,
                            msg.compid, msg.sysid, msg.vtol_state, msg.landed_state));
            break;
        }
        case msg_adsb_vehicle.MAVLINK_MSG_ID_ADSB_VEHICLE: {
            msg_adsb_vehicle msg = (msg_adsb_vehicle) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} ADSB_VEHICLE: compid={1}, sysid={2}, ICAO_address={3}, lat={4}, lon={5}, altitude_type={6}, altitude={7}, heading={8}, hor_velocity={9}, ver_velocity={10}, callsign={11}, emitter_type={12}, tslc={13}, flags={14}, squawk={15}",
                            dir, msg.compid, msg.sysid, msg.ICAO_address, msg.lat, msg.lon, msg.altitude_type,
                            msg.altitude, msg.heading, msg.hor_velocity, msg.ver_velocity, msg.callsign,
                            msg.emitter_type, msg.tslc, msg.flags, msg.squawk));
            break;
        }
        case msg_collision.MAVLINK_MSG_ID_COLLISION: {
            msg_collision msg = (msg_collision) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} COLLISION: compid={1}, sysid={2}, src={3}, id={4}, action={5}, threat_level={6}, time_to_minimum_delta={7}, altitude_minimum_delta={8}, horizontal_minimum_delta={9}",
                            dir, msg.compid, msg.sysid, msg.src, msg.id, msg.action, msg.threat_level,
                            msg.time_to_minimum_delta, msg.altitude_minimum_delta, msg.horizontal_minimum_delta));
            break;
        }
        case msg_v2_extension.MAVLINK_MSG_ID_V2_EXTENSION: {
            msg_v2_extension msg = (msg_v2_extension) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} V2_EXTENSION: compid={1}, sysid={2}, target_network={3}, target_system={4}, target_component={5}, message_type={6}, payload={7}",
                            dir, msg.compid, msg.sysid, msg.target_network, msg.target_system, msg.target_component,
                            msg.message_type, msg.payload));
            break;
        }
        case msg_memory_vect.MAVLINK_MSG_ID_MEMORY_VECT: {
            msg_memory_vect msg = (msg_memory_vect) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} MEMORY_VECT: compid={1}, sysid={2}, address={3}, ver={4}, type={5}, value={6}", dir,
                            msg.compid, msg.sysid, msg.address, msg.ver, msg.type, msg.value));
            break;
        }
        case msg_debug_vect.MAVLINK_MSG_ID_DEBUG_VECT: {
            msg_debug_vect msg = (msg_debug_vect) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} DEBUG_VECT: compid={1}, sysid={2}, name={3}, time_usec={4}, x={5}, y={6}, z={7}", dir,
                            msg.compid, msg.sysid, msg.name, msg.time_usec, msg.x, msg.y, msg.z));
            break;
        }
        case msg_named_value_float.MAVLINK_MSG_ID_NAMED_VALUE_FLOAT: {
            msg_named_value_float msg = (msg_named_value_float) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} NAMED_VALUE_FLOAT: compid={1}, sysid={2}, time_boot_ms={3}, name={4}, value={5}", dir,
                            msg.compid, msg.sysid, msg.time_boot_ms, msg.name, msg.value));
            break;
        }
        case msg_named_value_int.MAVLINK_MSG_ID_NAMED_VALUE_INT: {
            msg_named_value_int msg = (msg_named_value_int) packet.unpack();
            logger.log(priority,
                    MessageFormat.format(
                            "{0} NAMED_VALUE_INT: compid={1}, sysid={2}, time_boot_ms={3}, name={4}, value={5}", dir,
                            msg.compid, msg.sysid, msg.time_boot_ms, msg.name, msg.value));
            break;
        }
        case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT: {
            msg_statustext msg = (msg_statustext) packet.unpack();
            logger.log(priority, MessageFormat.format("{0} STATUSTEXT: compid={1}, sysid={2}, severity={3}, text={4}",
                    dir, msg.compid, msg.sysid, msg.severity, new String(msg.text)));
            break;
        }
        case msg_debug.MAVLINK_MSG_ID_DEBUG: {
            msg_debug msg = (msg_debug) packet.unpack();
            logger.log(priority,
                    MessageFormat.format("{0} DEBUG: compid={1}, sysid={2}, time_boot_ms={3}, ind={4}, value={5}", dir,
                            msg.compid, msg.sysid, msg.time_boot_ms, msg.ind, msg.value));
            break;
        }
        default: {
            logger.log(priority, MessageFormat.format("{0} msgid={1}: compid={2}, sysid={3}",
                                                      dir, packet.msgid, packet.compid, packet.sysid));
        }
      }
    }

}
