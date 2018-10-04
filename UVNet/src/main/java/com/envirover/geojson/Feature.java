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

import java.util.Map;

/**
 * GeoJSON feature.
 * 
 * @author Pavel
 *
 */
public class Feature {
	
	private Geometry geometry;
    private Map<String, Object> properties;

    public Feature() {
        this.geometry = null;
        this.properties = null;
    }

    public Feature(Geometry geometry, Map<String, Object> properties) {
        this.geometry = geometry;
        this.properties = properties;
    }

    public String getType() {
        return "Feature";
    }

    public void setType(String type) {
    }
    
    public Geometry getGeometry() {
        return geometry;
    }
    
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
