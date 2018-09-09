package com.envirover.uvnet.shadow;

import java.io.IOException;

import com.envirover.geojson.FeatureCollection;
import com.envirover.uvnet.mission.Plan;

/**
 * Retrieves reported states and missions of UV as JSON objects
 * 
 * @author Pavel Bobov
 *
 */
public interface UVShadowView {

	/**
	 * Retrieves messages of the specified type reported by the specified 
	 * system and returns them in GeoJSON representation.
	 * 
	 * @param sysId MAVLink system id
	 * @param msgId MAVlink message id 
	 * @param geometryType GeoJSON geometry type
	 * @param startTime minimum reported time. No minimum  limit if 'null'.
	 * @param endTime maximum reported time. No maximum time limit if 'null'.
	 * @param top maximum number of reported points returned
	 * @return GeoJSON FeaureCollection with the reported messages
	 * @throws IOException in case of I/O exception
	 */
	FeatureCollection queryMessages(int sysId, int msgId, Long startTime, Long endTime,
			int top) throws IOException;
	
	/**
	 * Retrieves mission of the specified system
	 * 
	 * @param sysid system Id
	 * @return GeoJSON FeaureCollection with the mission
	 * @throws IOException in case of I/O exception
	 */
	Plan queryMissions(int sysid) throws IOException;

}