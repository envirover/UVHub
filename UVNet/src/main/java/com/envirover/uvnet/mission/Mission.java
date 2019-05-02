/*
 * Envirover confidential
 * 
 *  [2018] Envirover
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
