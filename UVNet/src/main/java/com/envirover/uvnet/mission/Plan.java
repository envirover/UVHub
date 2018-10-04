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

import com.MAVLink.common.msg_mission_item;

/**
 * 
 * The standard JSON file format for missions, as implemented in the
 * QGroundControl reference implementation.
 * 
 * @author Pavel Bobov
 *
 */
public class Plan {

    private final String fileType = "Plan";
    private final String groundStation = "QGroundControl";
    private final int version = 1;
    private GeoFence geoFence = new GeoFence();
    private Mission mission = new Mission();
    private RallyPoints rallyPoints = new RallyPoints();

    public Plan() {
    }

    public Plan(List<msg_mission_item> missionItems) {
        setMissionItems(missionItems);
    }

    public String getFileType() {
        return fileType;
    }

    public String getGroundStation() {
        return groundStation;
    }

    public int getVersion() {
        return version;
    }

    public GeoFence getGeoFence() {
        return geoFence;
    }

    public void setGeoFence(GeoFence geoFence) {
        this.geoFence = geoFence;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public RallyPoints getRallyPoints() {
        return rallyPoints;
    }

    public void setRallyPoints(RallyPoints rallyPoints) {
        this.rallyPoints = rallyPoints;
    }

    public void setMissionItems(List<msg_mission_item> missionItems) {
        mission.getItems().clear();

        if (missionItems.size() == 0) {
            return;
        }

        msg_mission_item home = missionItems.get(0);
        List<Double> plannedHomePosition = new ArrayList<Double>();
        plannedHomePosition.add((double) home.x);
        plannedHomePosition.add((double) home.y);
        plannedHomePosition.add((double) home.z);
        mission.setPlannedHomePosition(plannedHomePosition);

        if (missionItems.size() == 1) {
            return;
        }

        // msg_mission_item takeoff = missionItems.get(1);
        // mission.setCruiseSpeed((int)takeoff.param2);

        for (int i = 1; i < missionItems.size(); i++) {
            mission.getItems().add(new MissionItem(missionItems.get(i)));
        }
    }

}
