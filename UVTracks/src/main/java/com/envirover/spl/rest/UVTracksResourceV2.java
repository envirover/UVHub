/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.envirover.spl.rest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.envirover.geojson.Feature;
import com.envirover.geojson.FeatureCollection;
import com.envirover.geojson.Geometry;
import com.envirover.geojson.LineString;
import com.envirover.geojson.Point;
import com.envirover.spl.uvtracks.Config;
import com.envirover.uvnet.shadow.StateReport;
import com.envirover.uvnet.shadow.UVLogbook;
import com.envirover.uvnet.shadow.UVShadow;
import com.envirover.uvnet.shadow.impl.PersistentUVShadow;

import net.sf.geographiclib.Geodesic;

/**
 * A REST resource that provides access to vehicle tracks, missions, and
 * on-board parameters.
 * 
 * v2 version supports geometryType=line query string parameter on /tracks and
 * /missions resources and returns GeoJSON instead of Plan object for /missions
 * resource.
 */

@Path("/v2")
public class UVTracksResourceV2 {

    private final static String DEFAULT_TOP = "100";
    private final static String DEFAULT_GEOMETRY_TYPE = "Point";

    private final UVShadow shadow;
    private final UVLogbook logbook;

    /**
     * Constructs the resource.
     */
    public UVTracksResourceV2() {
        Config config = Config.getInstance();

        try {
            config.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PersistentUVShadow shadow = new PersistentUVShadow(config.getShadowConnectionString());

        this.shadow = shadow;
        this.logbook = shadow;
    }

    /**
     * Returns track of the specified system as GeoJSON feature collection of points
     * or single line.
     * 
     * The query supports range the tracks by start and/or end times of the reports.
     * 
     * @param sysid        system Id. Default value is 1.
     * @param startTime    (optional) track start time in UNIX epoch time.
     * @param endTime      (optional) track end time in UNIX epoch time.
     * @param top          maximum number of points returned
     * @param geometryType GeoJSON features geometry type <point|line>
     * @return GeoJSON feature collection
     * @throws IOException on I/O error
     * @throws IllegalAccessException if UV Logs is not accessible
     * @throws IllegalArgumentException if a parameter value is invalid
     */
    @GET
    @Path("/tracks")
    @Produces(MediaType.APPLICATION_JSON)
    public FeatureCollection getTracks(@QueryParam("sysid") Integer sysid, @QueryParam("startTime") Long startTime,
            @QueryParam("endTime") Long endTime, @DefaultValue(DEFAULT_TOP) @QueryParam("top") int top,
            @DefaultValue(DEFAULT_GEOMETRY_TYPE) @QueryParam("geometryType") String geometryType)
            throws IOException, IllegalArgumentException, IllegalAccessException {
        if (sysid == null) {
            sysid = Config.getInstance().getMavSystemId();
        }

        Date start = startTime != null ? new Date(startTime) : null;
        Date end = endTime != null ? new Date(endTime) : null;
        List<StateReport> reportedStates = logbook.getReportedStates(sysid, start, end, top);

        if (geometryType.equalsIgnoreCase("Line")) {
            return reportsToLineFeature(sysid, reportedStates);
        }

        return reportsToPointFeatures(reportedStates);
    }

    /**
     * Returns mission of the specified system in Point or LineString GeoJSON
     * objects.
     * 
     * @param sysid        sysid system Id. Default value is 1.
     * @param geometryType GeoJSON features geometry type <point|line>
     * @return GeoJSON feature collection
     * @throws IOException in case of I/O error
     * @throws IllegalAccessException if UV Shadow is not accessible
     * @throws IllegalArgumentException if a parameter value is invalid
     */
    @GET
    @Path("/missions")
    @Produces(MediaType.APPLICATION_JSON)
    public FeatureCollection getMissions(@QueryParam("sysid") Integer sysid,
            @DefaultValue(DEFAULT_GEOMETRY_TYPE) @QueryParam("geometryType") String geometryType)
            throws IOException, IllegalArgumentException, IllegalAccessException {
        if (sysid == null) {
            sysid = Config.getInstance().getMavSystemId();
        }

        List<msg_mission_item> missions = shadow.getMission(sysid);

        if (geometryType.equalsIgnoreCase("Line")) {
            return missionsToLineFeature(sysid, missions);
        }

        return missionsToPointFeatures(missions);
    }

    /**
     * Returns on-board parameters values in JSON of the specified vehicle.
     * 
     * @param sysid system Id. Default value is 1.
     * @return on-board parameters
     * @throws IOException in case of I/O error
     */
    @GET
    @Path("/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Double> getParameters(@QueryParam("sysid") Integer sysid) throws IOException {
        if (sysid == null) {
            sysid = Config.getInstance().getMavSystemId();
        }

        List<msg_param_value> params = shadow.getParams(sysid);

        Map<String, Double> parameters = new HashMap<>();

        for (msg_param_value param : params) {
            parameters.put(param.getParam_Id(), (double) param.param_value);
        }

        return parameters;
    }

    /**
     * Returns the last reported state of the vehicle as GeoJSON point feature collection.
     * 
     * If the state report is found, the returned feature collection contains single
     * point feature, otherwise the feature collection is empty.
     * 
     * @param sysid system Id. Default value is 1.
     * @return last reported state of the vehicle
     * @throws IOException in case of I/O error
     * @throws IllegalArgumentException if a parameter value is invalid
     * @throws IllegalAccessException if UV Shadow is not accessible
     */
    @GET
    @Path("/state")
    @Produces(MediaType.APPLICATION_JSON)
    public FeatureCollection getState(@QueryParam("sysid") Integer sysid)
            throws IOException, IllegalArgumentException, IllegalAccessException {
        if (sysid == null) {
            sysid = Config.getInstance().getMavSystemId();
        }

        FeatureCollection features = new FeatureCollection();

        StateReport stateReport = shadow.getLastReportedState(sysid);

        if (stateReport != null) {
            features.getFeatures().add(reportToPointFeature(stateReport));
        }

        return features;
    }

    private static Point getMissionCoordinates(List<msg_mission_item> missions, int idx) {
        if (idx >= missions.size()) {
            throw new IllegalArgumentException("Mission index out of range.");
        }

        double home_lat = missions.get(0).x;
        double home_lon = missions.get(0).y;
        double home_alt = missions.get(0).z;

        msg_mission_item mission = missions.get(idx);

        double lat = mission.x;
        double lon = mission.y;
        double alt = mission.z;

        if (mission.frame != MAV_FRAME.MAV_FRAME_GLOBAL) {
            alt += home_alt;
        }

        if (idx > 0) {
            switch (mission.command) {
            case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
                lat = missions.get(idx - 1).x;
                lon = missions.get(idx - 1).y;
                break;
            case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
                lat = home_lat;
                lon = home_lon;
                alt = missions.get(idx - 1).z;
                if (missions.get(idx - 1).frame != MAV_FRAME.MAV_FRAME_GLOBAL) {
                    alt += home_alt;
                }
                break;
            }
        }

        return new Point(lon, lat, alt);
    }

    // Converts MISSION_ITEM to GeoJSON Point feature
    private static FeatureCollection missionsToPointFeatures(List<msg_mission_item> missions)
            throws IllegalArgumentException, IllegalAccessException {
        FeatureCollection features = new FeatureCollection();

        for (int i = 0; i < missions.size(); i++) {
            msg_mission_item mission = missions.get(i);

            Geometry geometry = getMissionCoordinates(missions, i);

            Map<String, Object> properties = new HashMap<>();

            for (Field f : mission.getClass().getFields()) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    Object value = f.get(mission);

                    if (f.getType() == byte[].class) {
                        value = bytesToString((byte[]) value);
                    }

                    properties.put(f.getName(), value);
                }
            }

            features.getFeatures().add(new Feature(geometry, properties));
        }

        return features;
    }

    // Converts list of MISSION_ITEMs to GeoJSON LineString feature
    private static FeatureCollection missionsToLineFeature(Integer sysid, List<msg_mission_item> missions) {
        List<List<Double>> coordinates = new ArrayList<>();

        for (int i = 0; i < missions.size(); i++) {
            coordinates.add(getMissionCoordinates(missions, i).getCoordinates());
        }

        Map<String, Object> properties = new HashMap<>();

        properties.put("length", getGeodesicLength(coordinates));
        properties.put("target_system", sysid);

        FeatureCollection features = new FeatureCollection();
        features.getFeatures().add(new Feature(new LineString(coordinates), properties));

        return features;
    }

    private static Feature reportToPointFeature(StateReport reportedState)
            throws IllegalArgumentException, IllegalAccessException {
        Geometry geometry;

        msg_high_latency msg = reportedState.getState();

        geometry = new Point(msg.longitude / 1.0E7, msg.latitude / 1.0E7, (double) msg.altitude_amsl);

        Map<String, Object> properties = new HashMap<>();

        for (Field f : msg.getClass().getFields()) {
            if (!Modifier.isFinal(f.getModifiers())) {
                Object value = f.get(msg);

                if (f.getType() == byte[].class) {
                    value = bytesToString((byte[]) value);
                }

                properties.put(f.getName(), value);
            }
        }

        properties.put("time", reportedState.getTime());

        return new Feature(geometry, properties);
    }

    // Converts HIGH_LATENCY message to Point GeoJSON feature.
    private static FeatureCollection reportsToPointFeatures(List<StateReport> reportedStates)
            throws IllegalArgumentException, IllegalAccessException {
        FeatureCollection features = new FeatureCollection();

        for (StateReport stateReport : reportedStates) {
            features.getFeatures().add(reportToPointFeature(stateReport));
        }

        return features;
    }

    // Converts list of HIGH_LATENCY message to LineString GeoJSON feature.
    private static FeatureCollection reportsToLineFeature(Integer sysid, List<StateReport> reportedStates) {

        List<List<Double>> coordinates = new ArrayList<>();

        for (StateReport entry : reportedStates) {
            msg_high_latency hl = entry.getState();
            if (hl.longitude != 0 || hl.latitude != 0) {
                List<Double> point = new ArrayList<>();
                point.add(hl.longitude / 1.0E7);
                point.add(hl.latitude / 1.0E7);
                point.add((double) hl.altitude_amsl);
                coordinates.add(point);
            }
        }

        Map<String, Object> properties = new HashMap<>();

        properties.put("sysid", sysid);
        properties.put("length", getGeodesicLength(coordinates));

        if (reportedStates.size() > 0) {
            properties.put("from_time", reportedStates.get(reportedStates.size() - 1).getTime());
            properties.put("to_time", reportedStates.get(0).getTime());
        } else {
            properties.put("from_time", 0);
            properties.put("to_time", 0);
        }

        FeatureCollection features = new FeatureCollection();
        features.getFeatures().add(new Feature(new LineString(coordinates), properties));

        return features;
    }

    private static String bytesToString(byte[] bytes) {
        StringBuilder result = new StringBuilder();

        for (byte b : bytes) {
            if (b == 0)
                break;

            result.append(b);
        }

        return result.toString();
    }

    private static double getGeodesicLength(List<Double> p1, List<Double> p2) {
        if (p1.size() < 2 || p2.size() < 2) {
            return 0.0;
        }

        double s12 = Geodesic.WGS84.Inverse(p1.get(1), p1.get(0), p2.get(1), p2.get(0)).s12;

        // Add distance change due to the elevation change.
        double elev = 0;
        if (p1.size() >= 3 && p2.size() >= 3) {
            elev = p2.get(2) - p1.get(2);
        }

        return Math.sqrt(s12 * s12 + elev * elev);
    }

    private static Double getGeodesicLength(List<List<Double>> coordinates) {
        double length = 0.0;

        for (int i = 0; i < coordinates.size() - 1; i++) {
            length += getGeodesicLength(coordinates.get(i), coordinates.get(i + 1));
        }

        return length;
    }

}
