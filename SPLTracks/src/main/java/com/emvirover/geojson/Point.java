package com.emvirover.geojson;

import java.util.ArrayList;
import java.util.List;

public class Point extends Geometry {

    private List<Double> coordinates;

    public Point() {
        setType("Point");
        setCoordinates(new ArrayList<Double>());
    }

    public Point(List<Double> coordinates) {
        setType("Point");
        setCoordinates(coordinates);
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

}
