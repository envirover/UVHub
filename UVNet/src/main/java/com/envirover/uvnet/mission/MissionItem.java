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
