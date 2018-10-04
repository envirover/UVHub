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

public class RallyPoints {

    private final int version = 1;
    private List<List<Double>> points = new ArrayList<List<Double>>();

    public int getVersion() {
        return version;
    }

    public List<List<Double>> getPoints() {
        return points;
    }

    public void setPoints(List<List<Double>> points) {
        this.points = points;
    }
}
