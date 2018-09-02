package com.envirover.uvnet.mission;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON serialization class for mission geo fence.
 * 
 * @author Pavel Bobov
 *
 */
public class GeoFence {
	
	private List<List<Double>> polygon = new ArrayList<List<Double>>();
	private final int version = 1;
	
	public List<List<Double>> getPolygon() {
		return polygon;
	}
	
	public void setPolygon(List<List<Double>> polygon) {
		this.polygon = polygon;
	}

	public int getVersion() {
		return version;
	}
	
}
