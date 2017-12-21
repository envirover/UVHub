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

import java.util.Map;

/**
 * 
 * @author Pavel
 *
 */
public class Feature {

    private final Geometry geometry;

    private final Map<String, Object> properties;

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

    public Geometry getGeometry() {
        return geometry;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

}
