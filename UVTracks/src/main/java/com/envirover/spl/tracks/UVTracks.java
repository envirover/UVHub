/*
 * Envirover confidential
 * 
 *  [2018] Envirover
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

package com.envirover.spl.tracks;

import java.io.IOException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MAVLink.common.msg_high_latency;
import com.envirover.geojson.FeatureCollection;
import com.envirover.geojson.GeometryType;
import com.sun.jersey.api.view.Viewable;

/**
 * A REST service that provides access to vehicle tracks and missions.
 * 
 */
@Path("/")
public class UVTracks {
	
	private final static String DEFAULT_SYSTEM_ID = "1";
	private final static String DEFAULT_TOP = "100";

	private final UVShadowView stream = new UVShadowView();

	public UVTracks() throws IOException {
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response index() {
		return Response.ok(new Viewable("/index.jsp")).build();
	}

	/**
	 * Returns track of the specified system in GeoJSON format as a feature collection of points or a line string. 
	 * 
	 * The query supports range the tracks by start and/or end times of the reports.
	 * 
	 * @param sysid system Id. Default value is 1.
	 * @param startTime (optional) track start time in UNIX epoch time.   
	 * @param endTime (optional) track end time in UNIX epoch time.
	 * @param type GeoJSON geometry type that can be either 'point' or 'linestring'. Default value is point.
	 * @param top maximum number of points returned
	 * @return GeoJSON FeatureCollection 
	 * @throws IOException on I/O error
	 */
	@GET
	@Path("/tracks")
	@Produces(MediaType.APPLICATION_JSON)
	public FeatureCollection getTracks(
			@DefaultValue(DEFAULT_SYSTEM_ID) @QueryParam("sysid") int sysid,
			@QueryParam("startTime") Long startTime,
			@QueryParam("endTime") Long endTime,
			@DefaultValue("Point") @QueryParam("type") String type, 
			@DefaultValue(DEFAULT_TOP) @QueryParam("top") int top) throws IOException
			{

		GeometryType geometryType = type.equalsIgnoreCase(GeometryType.LineString.toString()) ? 
				GeometryType.LineString : GeometryType.Point;
		
		return stream.queryMessages(sysid, msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY,
				                    geometryType, startTime, endTime,  top);
	}

}
