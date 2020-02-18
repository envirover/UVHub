/*
 * Envirover confidential
 * 
 *  [2020] Envirover
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
package com.envirover.geojson;

import java.util.ArrayList;
import java.util.List;

public class LineString implements Geometry {

    private List<List<Double>> coordinates;

    public LineString() {
        this.coordinates = new ArrayList<List<Double>>();
    }

    public LineString(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public GeometryType getType() {
        return GeometryType.LineString;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void getCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }
    
}
