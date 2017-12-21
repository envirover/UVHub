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

public class LineString implements Geometry {

    private final Collection<Collection<Double>> coordinates;

    public LineString() {
        this.coordinates = new ArrayList<Collection<Double>>();
    }

    public LineString(Collection<Collection<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String getType() {
        return "LineString";
    }

    public Collection<Collection<Double>> getCoordinates() {
        return coordinates;
    }

}
