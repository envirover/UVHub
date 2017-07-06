package com.emvirover.geojson;

import java.util.ArrayList;
import java.util.List;

public class LineString extends Geometry {

    public LineString() {
        setType("LineString");
        setCoordinates(new ArrayList<List<Double>>());
    }

    public LineString(List<List<Double>> coordinates) {
        setType("LineString");
        setCoordinates(coordinates);
    }

    private List<List<Double>> coordinates;

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

}
