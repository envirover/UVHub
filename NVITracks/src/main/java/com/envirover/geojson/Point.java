/*
This file is part of SPLTracks application.

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLStrean is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SPLTracks.  If not, see <http://www.gnu.org/licenses/>.
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
