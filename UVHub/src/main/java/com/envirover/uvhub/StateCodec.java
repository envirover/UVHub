package com.envirover.uvhub;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_battery2;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_STATE;
import com.envirover.uvnet.shadow.StateReport;

/**
 * Encodes and decodes vehicle state report to/from multiple MAVLink messages
 * recognized by GCS.
 */
class StateCodec {

    private static final Config config = Config.getInstance();

    /**
     * Returns list of MAVLink messages that contains data from the vehicle
     * state report delivered in HIGH_LATENCY message.
     */
    public static List<MAVLinkMessage> getMessages(StateReport report) {
        List<MAVLinkMessage> messages = new ArrayList<>();

        msg_high_latency state = report.getState();
        if (state != null) {
            messages.add(getHeartbeatMsg(state, config.getMavSystemId(), config.getAutopilot(), config.getMavType()));
            messages.add(getSysStatusMsg(state));
            messages.add(getGpsRawIntMsg(state));
            messages.add(getAttitudeMsg(state));
            messages.add(getGlobalPositionIntMsg(state));
            messages.add(getMissionCurrentMsg(state));
            messages.add(getNavControllerOutputMsg(state));
            messages.add(getVfrHudMsg(state));
            messages.add(getBattery2Msg(state));
        }

        return messages;
    }

    /**
     * Updates the reported state with data from the specified MAVLink message.
     * 
     * Currently only HIGH_LATENCY message type is supported. Other message 
     * types are ignored.
     * 
     * @param report state report
     * @param msg MAVLink message
     * @return true if the state was updated
     */
    public static boolean update(StateReport report, MAVLinkMessage msg) {
        if (msg.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
            report.setState((msg_high_latency) msg);
            return true;
        }

        return false;
    }

    private static MAVLinkMessage getHeartbeatMsg(msg_high_latency state, int sysid, short autopilot, short mavType) {
        msg_heartbeat msg = new msg_heartbeat();

        msg.sysid = state.sysid;
        msg.compid = state.compid;
        msg.base_mode = state.base_mode;
        msg.custom_mode = state.custom_mode;
        msg.system_status = MAV_STATE.MAV_STATE_ACTIVE;
        msg.autopilot = autopilot;
        msg.type = mavType;

        return msg;
    }

    private static MAVLinkMessage getSysStatusMsg(msg_high_latency state) {
        msg_sys_status msg = new msg_sys_status();
        msg.sysid = state.sysid;
        msg.battery_remaining = (byte) state.battery_remaining;
        msg.voltage_battery = state.temperature * 1000;
        msg.current_battery = state.temperature_air < 0 ? -1 : (short) (state.temperature_air * 100);
        return msg;
    }

    private static MAVLinkMessage getGpsRawIntMsg(msg_high_latency state) {
        msg_gps_raw_int msg = new msg_gps_raw_int();
        msg.sysid = state.sysid;
        msg.fix_type = state.gps_fix_type;
        msg.satellites_visible = state.gps_nsat;
        msg.lat = state.latitude;
        msg.lon = state.longitude;
        msg.alt = state.altitude_amsl * 1000;
        return msg;
    }

    private static MAVLinkMessage getAttitudeMsg(msg_high_latency state) {
        msg_attitude msg = new msg_attitude();
        msg.sysid = state.sysid;
        msg.yaw = (float) Math.toRadians(state.heading / 100.0);
        msg.pitch = (float) Math.toRadians(state.pitch / 100.0);
        msg.roll = (float) Math.toRadians(state.roll / 100.0);
        return msg;
    }

    private static MAVLinkMessage getGlobalPositionIntMsg(msg_high_latency state) {
        msg_global_position_int msg = new msg_global_position_int();
        msg.sysid = state.sysid;
        msg.alt = state.altitude_amsl * 1000;
        msg.lat = state.latitude;
        msg.lon = state.longitude;
        msg.hdg = state.heading;
        msg.relative_alt = state.altitude_sp * 1000;
        return msg;
    }

    private static MAVLinkMessage getMissionCurrentMsg(msg_high_latency state) {
        msg_mission_current msg = new msg_mission_current();
        msg.sysid = state.sysid;
        msg.seq = state.wp_num;
        return msg;
    }

    private static MAVLinkMessage getNavControllerOutputMsg(msg_high_latency state) {
        msg_nav_controller_output msg = new msg_nav_controller_output();
        msg.sysid = state.sysid;
        msg.nav_bearing = (short) (state.heading_sp / 100);
        return msg;
    }

    private static MAVLinkMessage getVfrHudMsg(msg_high_latency state) {
        msg_vfr_hud msg = new msg_vfr_hud();
        msg.sysid = state.sysid;
        msg.airspeed = state.airspeed;
        msg.alt = state.altitude_amsl;
        msg.climb = state.climb_rate;
        msg.groundspeed = state.groundspeed;
        msg.heading = (short) (state.heading / 100);
        msg.throttle = state.throttle;
        return msg;
    }

    private static MAVLinkMessage getBattery2Msg(msg_high_latency state) {
        msg_battery2 msg = new msg_battery2();
        msg.sysid = state.sysid;
        msg.current_battery = -1;
        msg.voltage = state.temperature_air * 1000;
        return msg;
    }

}