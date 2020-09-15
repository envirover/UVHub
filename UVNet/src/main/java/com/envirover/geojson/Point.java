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
