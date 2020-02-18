
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

import java.util.Date;

import com.MAVLink.common.msg_high_latency;

/**
 * Reported state of the vehicle at specific time.
 * 
 * HIGH_LATENCY MAVlink message is used to store state of the vehicle.
 */
public class StateReport {
    private Date time = new Date();
    private msg_high_latency state = new msg_high_latency();

    /**
     * Default constructor of StateReport used by deserialization.
     */
    public StateReport() {
    }

    /**
     * Constructs StateReport instance.
     * 
     * @param time time of the report
     * @param state state of the vehicle
     */
    public StateReport(Date time, msg_high_latency state) {
        this.time = time;
        this.state = state;
    }

    /**
     * Returns time of the report.
     * 
     * @return time of the report
     */
    public Date getTime() {
        return time;
    }

    /**
     * Sets time of the report.
     * 
     * @param time time of the report
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * Returns state of the vehicle.
     * 
     * @return state of the vehicle.
     */
    public msg_high_latency getState() {
        return state;
    }

    /**
     * Sets state of the vehicle.
     * 
     * @param state state of the vehicle
     */
    public void setState(msg_high_latency state) {
        this.state = state;
    }

}