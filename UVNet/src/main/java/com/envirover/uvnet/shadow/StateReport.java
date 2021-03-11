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

package com.envirover.uvnet.shadow;

import java.util.Date;

import com.MAVLink.common.msg_high_latency2;

/**
 * Reported state of the vehicle at specific time.
 * 
 * HIGH_LATENCY MAVlink message is used to store state of the vehicle.
 */
public class StateReport {
    private Date time = new Date();
    private msg_high_latency2 state = new msg_high_latency2();

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
    public StateReport(Date time, msg_high_latency2 state) {
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
    public msg_high_latency2 getState() {
        return state;
    }

    /**
     * Sets state of the vehicle.
     * 
     * @param state state of the vehicle
     */
    public void setState(msg_high_latency2 state) {
        this.state = state;
    }

}