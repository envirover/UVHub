package com.envirover.uvnet.mission;

import java.util.ArrayList;
import java.util.List;

public class RallyPoints {
	
	private final int version = 1;
	private List<List<Double>> points = new ArrayList<List<Double>>();

	public int getVersion() {
		return version;
	}

	public List<List<Double>> getPoints() {
		return points;
	}

	public void setPoints(List<List<Double>> points) {
		this.points = points;
	}
}
