package com.emvirover.geojson;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Pavel
 *
 */
public class Geometry {

    private String type = "Point";

    private List<Double> coordinates = new ArrayList<Double>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

}
