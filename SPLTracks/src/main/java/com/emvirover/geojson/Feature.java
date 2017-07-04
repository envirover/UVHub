package com.emvirover.geojson;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Pavel
 *
 */
public class Feature {

    private String type = "Feature";

    private Geometry geometry = new Geometry();

    private Map<String, Object> properties = new HashMap<String, Object>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
