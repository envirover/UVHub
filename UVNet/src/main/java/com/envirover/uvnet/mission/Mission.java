package com.envirover.uvnet.mission;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_TYPE;

/**
 * Mission waypoints
 * 
 * @see GeoFence
 * @see RallyPoints
 * 
 * @author Pavel Bobov
 *
 */
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
