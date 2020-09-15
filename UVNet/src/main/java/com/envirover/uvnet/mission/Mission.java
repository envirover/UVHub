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

package com.envirover.uvnet.mission;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_TYPE;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Mission waypoints
 * 
 * @see GeoFence
 * @see RallyPoints
 * 
 * @author Pavel Bobov
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mission {

    private int cruiseSpeed;
    private int firmwareType = MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA;
    private int hoverSpeed;
    private List<MissionItem> items = new ArrayList<MissionItem>();
    private List<Double> plannedHomePosition = new ArrayList<Double>();
    private int vehicleType = MAV_TYPE.MAV_TYPE_GENERIC;
    private final int version = 2;

    public int getCruiseSpeed() {
        return cruiseSpeed;
    }

    public void setCruiseSpeed(int cruiseSpeed) {
        this.cruiseSpeed = cruiseSpeed;
    }

    public int getFirmwareType() {
        return firmwareType;
    }

    public void setFirmwareType(int firmwareType) {
        this.firmwareType = firmwareType;
    }

    public int getHoverSpeed() {
        return hoverSpeed;
    }

    public void setHoverSpeed(int hoverSpeed) {
        this.hoverSpeed = hoverSpeed;
    }

    public List<MissionItem> getItems() {
        return items;
    }

    public void setItems(List<MissionItem> items) {
        this.items = items;
    }

    public List<Double> getPlannedHomePosition() {
        return plannedHomePosition;
    }

    public void setPlannedHomePosition(List<Double> plannedHomePosition) {
        this.plannedHomePosition = plannedHomePosition;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(int vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getVersion() {
        return version;
    }

}
