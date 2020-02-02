
/*
 * Envirover confidential
 * 
 *  [2020] Envirover
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains the property of 
 * Envirover and its suppliers, if any.  The intellectual and technical concepts
 * contained herein are proprietary to Envirover and its suppliers and may be 
 * covered by U.S. and Foreign Patents, patents in process, and are protected
 * by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Envirover.
 */

package com.envirover.uvnet.shadow;

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
import com.MAVLink.enums.MAV_MODE;
import com.MAVLink.enums.MAV_STATE;

/**
 * Reported state of the vehicle at specific time.
 * 
 * HIGH_LATENCY MAVlink message is used to store state of the vehicle.
 */
public class StateReport {
    private final Long time;
    private final msg_high_latency state;

    /**
     * Constructs StateReport instance.
     * 
     * @param time  Unix Epoch time of the report
     * @param state state of the vehicle
     */
    public StateReport(Long time, msg_high_latency state) {
        this.time = time;
        this.state = state;
    }

    /**
     * Returns Unix Epoch time of the report.
     * 
     * @return Unix Epoch time of the report
     */
    public Long getTime() {
        return time;
    }

    /**
     * Returns state of the vehicle.
     * 
     * @return state of the vehicle.
     */
    public msg_high_latency getState() {
        return state;
    }

    public MAVLinkMessage getHeartbeatMsg(int sysid, short autopilot, short mavType) {
        msg_heartbeat msg = new msg_heartbeat();

        if (state != null) {
            msg.sysid = state.sysid;
            msg.compid = state.compid;
            msg.base_mode = state.base_mode;
            msg.custom_mode = state.custom_mode;
        } else {
            msg.sysid = sysid;
            msg.compid = 0;
            msg.base_mode = MAV_MODE.MAV_MODE_PREFLIGHT;
            msg.custom_mode = 0;
        }

        msg.system_status = MAV_STATE.MAV_STATE_ACTIVE;
        msg.autopilot = autopilot;
        msg.type = mavType;

        return msg;
    }

    public MAVLinkMessage getSysStatusMsg() {
        if (state == null) {
            return null;
        }

        msg_sys_status msg = new msg_sys_status();
        msg.sysid = state.sysid;
        msg.battery_remaining = (byte) state.battery_remaining;
        msg.voltage_battery = state.temperature * 1000;
        msg.current_battery = state.temperature_air < 0 ? -1 : (short) (state.temperature_air * 100);
        return msg;
    }

    public MAVLinkMessage getGpsRawIntMsg() {
        if (state == null) {
            return null;
        }

        msg_gps_raw_int msg = new msg_gps_raw_int();
        msg.sysid = state.sysid;
        msg.fix_type = state.gps_fix_type;
        msg.satellites_visible = state.gps_nsat;
        msg.lat = state.latitude;
        msg.lon = state.longitude;
        msg.alt = state.altitude_amsl * 1000;
        return msg;
    }

    public MAVLinkMessage getAttitudeMsg() {
        if (state == null) {
            return null;
        }

        msg_attitude msg = new msg_attitude();
        msg.sysid = state.sysid;
        msg.yaw = (float) Math.toRadians(state.heading / 100.0);
        msg.pitch = (float) Math.toRadians(state.pitch / 100.0);
        msg.roll = (float) Math.toRadians(state.roll / 100.0);
        return msg;
    }

    public MAVLinkMessage getGlobalPositionIntMsg() {
        if (state == null) {
            return null;
        }

        msg_global_position_int msg = new msg_global_position_int();
        msg.sysid = state.sysid;
        msg.alt = state.altitude_amsl * 1000;
        msg.lat = state.latitude;
        msg.lon = state.longitude;
        msg.hdg = state.heading;
        msg.relative_alt = state.altitude_sp * 1000;
        return msg;
    }

    public MAVLinkMessage getMissionCurrentMsg() {
        if (state == null) {
            return null;
        }

        msg_mission_current msg = new msg_mission_current();
        msg.sysid = state.sysid;
        msg.seq = state.wp_num;
        return msg;
    }

    public MAVLinkMessage getNavControllerOutputMsg() {
        if (state == null) {
            return null;
        }

        msg_nav_controller_output msg = new msg_nav_controller_output();
        msg.sysid = state.sysid;
        msg.nav_bearing = (short) (state.heading_sp / 100);
        return msg;
    }

    public MAVLinkMessage getVfrHudMsg() {
        if (state == null) {
            return null;
        }

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

    public MAVLinkMessage getBattery2Msg() {
        if (state == null) {
            return null;
        }

        msg_battery2 msg = new msg_battery2();
        msg.sysid = state.sysid;
        msg.current_battery = -1;
        msg.voltage = state.temperature_air * 1000;
        return msg;
    }

}