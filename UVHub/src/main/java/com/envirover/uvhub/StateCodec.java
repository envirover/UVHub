package com.envirover.uvhub;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_battery2;
import com.MAVLink.ardupilotmega.msg_wind;
import com.MAVLink.common.*;
import com.MAVLink.enums.GPS_FIX_TYPE;
import com.MAVLink.enums.HL_FAILURE_FLAG;
import com.MAVLink.enums.MAV_STATE;
import com.MAVLink.enums.MAV_SYS_STATUS_SENSOR;
import com.MAVLink.minimal.msg_heartbeat;
import com.envirover.uvnet.shadow.StateReport;

/**
 * Encodes and decodes vehicle state report to/from multiple MAVLink messages
 * recognized by GCS.
 */
class StateCodec {

    /**
     * Returns list of MAVLink messages that contains data from the vehicle
     * state report delivered in HIGH_LATENCY2 message.
     */
    public static List<MAVLinkMessage> getMessages(StateReport report) {
        List<MAVLinkMessage> messages = new ArrayList<>();

        msg_high_latency2 state = report.getState();
        if (state != null) {
            messages.add(getHeartbeatMsg(state));
            messages.add(getSysStatusMsg(state));
            messages.add(getGpsRawIntMsg(state));
            messages.add(getAttitudeMsg(state));
            messages.add(getGlobalPositionIntMsg(state));
            messages.add(getMissionCurrentMsg(state));
            messages.add(getNavControllerOutputMsg(state));
            messages.add(getVfrHudMsg(state));
            messages.add(getBattery2Msg(state));
            messages.add(getWindMsg(state));
            messages.add(getScaledPressure(state));
        }

        return messages;
    }

    /**
     * Updates the reported state with data from the specified MAVLink message.
     * 
     * Currently only HIGH_LATENCY2 message type is supported. Other message
     * types are ignored.
     * 
     * @param report state report
     * @param msg MAVLink message
     * @return true if the state was updated
     */
    public static boolean update(StateReport report, MAVLinkMessage msg) {
        if (msg.msgid == msg_high_latency2.MAVLINK_MSG_ID_HIGH_LATENCY2) {
            report.setState((msg_high_latency2) msg);
            return true;
        }

        return false;
    }

    private static MAVLinkMessage getHeartbeatMsg(msg_high_latency2 state) {
        msg_heartbeat msg = new msg_heartbeat();

        msg.sysid = state.sysid;
        msg.compid = state.compid;
        msg.base_mode = (short)(state.custom_mode & 0xFF);
        msg.custom_mode = state.custom_mode >> 8;
        msg.system_status = MAV_STATE.MAV_STATE_ACTIVE;
        msg.autopilot = state.autopilot;
        msg.type = state.type;
 
        return msg;
    }

    private static MAVLinkMessage getSysStatusMsg(msg_high_latency2 state) {
        msg_sys_status msg = new msg_sys_status();
        msg.sysid = state.sysid;
        msg.onboard_control_sensors_health = getHealth(state.failure_flags);
        msg.battery_remaining = state.battery;
        msg.voltage_battery = state.custom0 * 1000;
        return msg;
    }

    private static MAVLinkMessage getGpsRawIntMsg(msg_high_latency2 state) {
        msg_gps_raw_int msg = new msg_gps_raw_int();
        msg.sysid = state.sysid;
        msg.lat = state.latitude;
        msg.lon = state.longitude;
        msg.alt = state.altitude * 1000;
        msg.eph = state.eph;
        msg.epv = state.epv;
        //msg.satellites_visible = 4;
        msg.fix_type = GPS_FIX_TYPE.GPS_FIX_TYPE_3D_FIX;
        return msg;
    }

    private static MAVLinkMessage getAttitudeMsg(msg_high_latency2 state) {
        msg_attitude msg = new msg_attitude();
        msg.sysid = state.sysid;
        msg.yaw = (float) Math.toRadians(state.heading * 2);
        msg.pitch = 0;
        msg.roll = 0;
        return msg;
    }

    private static MAVLinkMessage getGlobalPositionIntMsg(msg_high_latency2 state) {
        msg_global_position_int msg = new msg_global_position_int();
        msg.sysid = state.sysid;
        msg.time_boot_ms = state.timestamp;
        msg.alt = state.altitude * 1000;
        msg.lat = state.latitude;
        msg.lon = state.longitude;
        msg.hdg = state.heading;
        msg.relative_alt = state.target_altitude * 1000;
        return msg;
    }

    private static MAVLinkMessage getMissionCurrentMsg(msg_high_latency2 state) {
        msg_mission_current msg = new msg_mission_current();
        msg.sysid = state.sysid;
        msg.seq = state.wp_num;
        return msg;
    }

    private static MAVLinkMessage getNavControllerOutputMsg(msg_high_latency2 state) {
        msg_nav_controller_output msg = new msg_nav_controller_output();
        msg.sysid = state.sysid;
        msg.nav_bearing = (short) (state.target_heading * 2);
        msg.wp_dist = state.target_distance;
        return msg;
    }

    private static MAVLinkMessage getVfrHudMsg(msg_high_latency2 state) {
        msg_vfr_hud msg = new msg_vfr_hud();
        msg.sysid = state.sysid;
        msg.airspeed = state.airspeed / 5;
        msg.alt = state.altitude;
        msg.climb = state.climb_rate / 10;
        msg.groundspeed = state.groundspeed / 5;
        msg.heading = (short)(state.heading * 2);
        msg.throttle = state.throttle;
        return msg;
    }

    private static MAVLinkMessage getBattery2Msg(msg_high_latency2 state) {
        msg_battery2 msg = new msg_battery2();
        msg.sysid = state.sysid;
        msg.current_battery = -1;
        msg.voltage = state.custom1 * 1000;
        return msg;
    }

    private static MAVLinkMessage getWindMsg(msg_high_latency2 state) {
        msg_wind msg = new msg_wind();
        msg.sysid = state.sysid;
        msg.direction = state.wind_heading * 2;
        msg.speed = (float)(state.windspeed / 5.0);
        return msg;
    }

    private static MAVLinkMessage getScaledPressure(msg_high_latency2 state) {
        msg_scaled_pressure msg = new msg_scaled_pressure();
        msg.sysid = state.sysid;
        msg.temperature = state.temperature_air;
        return msg;
    }

    private static long getHealth(int failure_flags) {
        long health = 0;

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_GPS) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_SENSOR_GPS;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_DIFFERENTIAL_PRESSURE) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_SENSOR_DIFFERENTIAL_PRESSURE;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_ABSOLUTE_PRESSURE) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_SENSOR_ABSOLUTE_PRESSURE;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_3D_ACCEL) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_SENSOR_3D_ACCEL;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_3D_GYRO) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_SENSOR_3D_GYRO;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_3D_MAG) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_SENSOR_3D_MAG;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_TERRAIN) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_TERRAIN;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_BATTERY) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_SENSOR_BATTERY;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_RC_RECEIVER) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_SENSOR_RC_RECEIVER;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_ENGINE) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_SENSOR_MOTOR_OUTPUTS;
        }

        if ((failure_flags & HL_FAILURE_FLAG.HL_FAILURE_FLAG_GEOFENCE) != 0) {
            health |= MAV_SYS_STATUS_SENSOR.MAV_SYS_STATUS_GEOFENCE;
        }

        return health;
    }
}