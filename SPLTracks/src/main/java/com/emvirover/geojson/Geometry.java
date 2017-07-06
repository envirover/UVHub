package com.emvirover.geojson;

/**
 * Base class for geometric primitices.
 *  
 * @author Pavel
 *
 */
abstract public class Geometry {

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
