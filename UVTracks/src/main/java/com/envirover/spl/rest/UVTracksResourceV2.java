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

package com.envirover.spl.rest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.envirover.uvnet.shadow.PersistentUVShadow;
import com.envirover.uvnet.shadow.UVLogbook;
import com.envirover.uvnet.shadow.UVShadow;

import net.sf.geographiclib.Geodesic;

/**
 * A REST resource that provides access to vehicle tracks and missions.
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

    public UVTracksResourceV2() {
        Config config = Config.getInstance();

        try {
            config.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PersistentUVShadow shadow = new PersistentUVShadow(config.getElasticsearchEndpoint(),
                config.getElasticsearchPort(), config.getElasticsearchProtocol());

        this.shadow = shadow;
        this.logbook = shadow;
    }

    /**
     * Returns track of the specified system as GeoJSON feature collection of points
     * or a line string.
     * 
     * The query supports range the tracks by start and/or end times of the reports.
     * 
     * @param sysid     system Id. Default value is 1.
     * @param startTime (optional) track start time in UNIX epoch time.
     * @param endTime   (optional) track end time in UNIX epoch time.
     * @param top       maximum number of points returned
     * @return GeoJSON feature collection
     * @throws IOException              on I/O error
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
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

        List<Entry<Long, msg_high_latency>> reportedStates = logbook.getReportedStates(sysid, startTime, endTime, top);

        if (geometryType.equalsIgnoreCase("Line")) {
            return reportsToLineFeature(reportedStates);
        }

        return reportsToPointFeatures(reportedStates);
    }

    /**
     * Returns mission items of the specified system.
     * 
     * @param sysid sysid system Id. Default value is 1.
     * @return mission plan
     * @throws IOException              in case of I/O error
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
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
            return missionsToLineFeature(missions);
        }

        return missionsToPointFeatures(missions);
    }

    @GET
    @Path("/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Double> getParameters(@QueryParam("sysid") Integer sysid) throws IOException {
        if (sysid == null) {
            sysid = Config.getInstance().getMavSystemId();
        }
        
        List<msg_param_value> params = shadow.getParams(sysid);

        Map<String, Double> parameters = new HashMap<String, Double>();
       
        for (msg_param_value param : params) {
            parameters.put(param.getParam_Id(), Double.valueOf(param.param_value));
        }

        return parameters;
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

            Map<String, Object> properties = new HashMap<String, Object>();

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
    private static FeatureCollection missionsToLineFeature(List<msg_mission_item> missions) {
        List<List<Double>> coordinates = new ArrayList<List<Double>>();

        for (int i = 0; i < missions.size(); i++) {
            coordinates.add(getMissionCoordinates(missions, i).getCoordinates());
        }

        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put("length", getGeodesicLength(coordinates));
        properties.put("cruise_speed", 0.0);
        properties.put("hover_speed", 0.0);

        FeatureCollection features = new FeatureCollection();
        features.getFeatures().add(new Feature(new LineString(coordinates), properties));

        return features;
    }

    // Converts HIGH_LATENCY message to Point GeoJSON feature.
    private static FeatureCollection reportsToPointFeatures(List<Entry<Long, msg_high_latency>> reportedStates)
            throws IllegalArgumentException, IllegalAccessException {
        FeatureCollection features = new FeatureCollection();

        for (Entry<Long, msg_high_latency> entry : reportedStates) {
            Geometry geometry;

            msg_high_latency msg = entry.getValue();

            msg_high_latency hl = (msg_high_latency) msg;
            geometry = new Point(hl.longitude / 1.0E7, hl.latitude / 1.0E7, (double) hl.altitude_amsl);

            Map<String, Object> properties = new HashMap<String, Object>();

            for (Field f : msg.getClass().getFields()) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    Object value = f.get(msg);

                    if (f.getType() == byte[].class) {
                        value = bytesToString((byte[]) value);
                    }

                    properties.put(f.getName(), value);
                }
            }

            properties.put("time", Long.valueOf(entry.getKey()));

            features.getFeatures().add(new Feature(geometry, properties));
        }

        return features;
    }

    // Converts list of HIGH_LATENCY message to LineString GeoJSON feature.
    private static FeatureCollection reportsToLineFeature(List<Entry<Long, msg_high_latency>> reportedStates) {

        List<List<Double>> coordinates = new ArrayList<List<Double>>();

        for (Entry<Long, msg_high_latency> entry : reportedStates) {
            msg_high_latency hl = entry.getValue();
            if (hl.longitude != 0 || hl.latitude != 0) {
                List<Double> point = new ArrayList<Double>();
                point.add(hl.longitude / 1.0E7);
                point.add(hl.latitude / 1.0E7);
                point.add((double) hl.altitude_amsl);
                coordinates.add(point);
            }
        }

        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put("length", getGeodesicLength(coordinates));

        if (reportedStates.size() > 0) {
            properties.put("from_time", reportedStates.get(0).getKey());
            properties.put("to_time", reportedStates.get(reportedStates.size() - 1).getKey());
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

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0)
                break;

            result.append(bytes[i]);
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
        Double length = 0.0;

        for (int i = 0; i < coordinates.size() - 1; i++) {
            length += getGeodesicLength(coordinates.get(i), coordinates.get(i + 1));
        }

        return length;
    }

}
