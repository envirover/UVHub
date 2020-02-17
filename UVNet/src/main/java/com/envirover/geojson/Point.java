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

/**
 * GeoJSON Point.
 * 
 * @author Pavel Bobov
 *
 */
public class Point implements Geometry {

    private List<Double> coordinates = new ArrayList<Double>();

    public Point() {
        coordinates.add(0.0);
        coordinates.add(0.0);
    }

    /**
     * Constructs 2D point.
     * 
     * @param x longitude
     * @param y latitude
     */
    public Point(Double x, Double y) {
        coordinates.add(x);
        coordinates.add(y);
    }

    /**
     * Constructs 3D point.
     * 
     * @param x longitude
     * @param y latitude
     * @param z altitude
     */
    public Point(Double x, Double y, Double z) {
        coordinates.add(x);
        coordinates.add(y);
        coordinates.add(z);
    }

    @Override
    public GeometryType getType() {
        return GeometryType.Point;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

}
