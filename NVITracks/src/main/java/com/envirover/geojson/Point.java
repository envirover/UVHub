/*
 * Envirover confidential
 * 
 *  [2017] Envirover
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
import java.util.Collection;

public class Point implements Geometry {

    private Collection<Double> coordinates = new ArrayList<Double>();

    public Point() {
        coordinates.add(0.0);
        coordinates.add(0.0);
    }

    public Point(Double x, Double y) {
        coordinates.add(x);
        coordinates.add(y);
    }

    public Point(Double x, Double y, Double z) {
        coordinates.add(x);
        coordinates.add(y);
        coordinates.add(z);
    }

    @Override
    public String getType() {
        return "Point";
    }

    public Collection<Double> getCoordinates() {
        return coordinates;
    }

}
