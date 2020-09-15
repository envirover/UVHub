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

import com.MAVLink.common.msg_mission_item;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * 
 * The standard JSON file format for missions, as implemented in the
 * QGroundControl reference implementation.
 * 
 * @author Pavel Bobov
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
