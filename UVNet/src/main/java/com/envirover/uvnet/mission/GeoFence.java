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

/**
 * JSON serialization class for mission geo fence.
 * 
 * @author Pavel Bobov
 *
 */
public class GeoFence {

    private List<List<Double>> polygon = new ArrayList<List<Double>>();
    private final int version = 1;

    public List<List<Double>> getPolygon() {
        return polygon;
    }

    public void setPolygon(List<List<Double>> polygon) {
        this.polygon = polygon;
    }

    public int getVersion() {
        return version;
    }

}
