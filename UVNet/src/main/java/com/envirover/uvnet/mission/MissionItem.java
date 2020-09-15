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

@JsonIgnoreProperties(ignoreUnknown = true)
public class MissionItem {

    private final String type = "SimpleItem";
    private boolean autoContinue = true;
    private int command;
    // private List<Double> coordinate = new ArrayList<Double>();
    private int doJumpId;
    private int frame;
    private List<Double> params = new ArrayList<Double>();

    public MissionItem() {

    }

    public MissionItem(msg_mission_item missionItem) {
        autoContinue = missionItem.autocontinue != 0 ? true : false;
        command = missionItem.command;
        doJumpId = missionItem.seq + 1;
        frame = missionItem.frame;
        params.add((double) missionItem.param1);
        params.add((double) missionItem.param2);
        params.add((double) missionItem.param3);
        params.add((double) missionItem.param4);
        params.add((double) missionItem.x);
        params.add((double) missionItem.y);
        params.add((double) missionItem.z);
    }

    public String getType() {
        return type;
    }

    public boolean isAutoContinue() {
        return autoContinue;
    }

    public void setAutoContinue(boolean autoContinue) {
        this.autoContinue = autoContinue;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    // public List<Double> getCoordinate() {
    // return coordinate;
    // }
    //
    // public void setCoordinate(List<Double> coordinate) {
    // this.coordinate = coordinate;
    // }

    public int getDoJumpId() {
        return doJumpId;
    }

    public void setDoJumpId(int doJumpId) {
        this.doJumpId = doJumpId;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public List<Double> getParams() {
        return params;
    }

    public void setParams(List<Double> params) {
        this.params = params;
    }

}
