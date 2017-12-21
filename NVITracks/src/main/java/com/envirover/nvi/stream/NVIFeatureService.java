/*
 * Envirover confidential
 * 
 *  [2017] Envirover
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

package com.envirover.nvi.stream;

import java.io.IOException;
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
import javax.ws.rs.core.Response;

import com.envirover.geojson.Feature;
import com.envirover.geojson.FeatureCollection;
import com.envirover.geojson.LineString;
import com.envirover.geojson.Point;
import com.sun.jersey.api.view.Viewable;

/**
 * Retrieves MAVLink messages from DynamoDB table in JSON format.
 * 
 */

@Path("/")
public class NVIFeatureService {

    private final int MAVLINK_MSG_ID_HIGH_LATENCY = 234;
    
    private final MAVLinkMessagesTable stream;

    // private static final Logger logger =
    // Logger.getLogger(SPLFeatureService.class.getName());

    public NVIFeatureService() throws IOException {
        stream = new MAVLinkMessagesTable();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        return Response.ok(new Viewable("/index.jsp")).build();
    }

    @GET
    @Path("/features")
    @Produces(MediaType.APPLICATION_JSON)
    public FeatureCollection getFeatureCollection(
            @DefaultValue("") @QueryParam("devices") String devices,
            @DefaultValue("-1") @QueryParam("startTime") long startTime,
            @DefaultValue("-1") @QueryParam("endTime") long endTime,
            @DefaultValue("point") @QueryParam("type") String type)
            throws IOException, IllegalArgumentException, IllegalAccessException {

        FeatureCollection features = new FeatureCollection();

        String[] deviceIds = devices.split(",");

        for (String deviceId : deviceIds) {
            if (!deviceId.isEmpty()) {
                Iterable<MAVLinkRecord> records = stream.query(
                        deviceId, 
                        startTime > 0 ? new Date(startTime) : null,
                        endTime > 0 ? new Date(endTime) : null,
                        MAVLINK_MSG_ID_HIGH_LATENCY);

                if (type.equalsIgnoreCase("point")) {
                    buildPointFeatures(features, records);
                } else if (type.equalsIgnoreCase("linestring")) {
                    buildLineFeatures(features, deviceId, records);
                }
            }
        }

        return features;
    }

    private void buildLineFeatures(FeatureCollection features, String deviceId, Iterable<MAVLinkRecord> records)
            throws IOException {

        LineString line = new LineString(); 
        long minTime = -1;
        long maxTime = -1;

        for (MAVLinkRecord record : records) {
            if (minTime < 0 || record.getTime().getTime() < minTime) {
                minTime = record.getTime().getTime();
            }

            if (maxTime < 0 || record.getTime().getTime() > maxTime) {
                maxTime = record.getTime().getTime();
            }

            if (record.getLongitude() != 0.0 || record.getLatitude() != 0.0) {
                List<Double> coordinates = new ArrayList<Double>();
                coordinates.add(record.getLongitude());
                coordinates.add(record.getLatitude());
                coordinates.add(record.getAltitude());
                line.getCoordinates().add(coordinates);
            }
        }

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("device_id", deviceId);
        properties.put("from_time", minTime);
        properties.put("to_time", maxTime);

        Feature lineFeature = new Feature(line, properties);

        features.getFeatures().add(lineFeature);
    }

    private void buildPointFeatures(FeatureCollection features, Iterable<MAVLinkRecord> records)
            throws IOException, IllegalAccessException {

        for (MAVLinkRecord record : records) {
            if (record.getLongitude() != 0.0 || record.getLatitude() != 0.0) { 
                Point point = new Point(record.getLongitude(), 
                                        record.getLatitude(), 
                                        record.getAltitude());

                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put("device_id", record.getDeviceId());
                properties.put("time", record.getTime().getTime());
                properties.putAll(record.getPacket());

                features.getFeatures().add(new Feature(point, properties));
            }
        }
    }

}
