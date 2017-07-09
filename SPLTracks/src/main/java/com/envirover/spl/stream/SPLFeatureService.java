/*
This file is part of SPLTracks application.

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLStrean is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SPLTracks.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl.stream;

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
public class SPLFeatureService {

    private final int MAVLINK_MSG_ID_HIGH_LATENCY = 234;
    
    private final DynamoDBInputStream stream;

    // private static final Logger logger =
    // Logger.getLogger(SPLFeatureService.class.getName());

    public SPLFeatureService() throws IOException {
        stream = new DynamoDBInputStream();
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
                        endTime > 0 ? new Date(endTime) : null);

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
            if (record.getMsgId() == MAVLINK_MSG_ID_HIGH_LATENCY) {
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
            if (record.getMsgId() == MAVLINK_MSG_ID_HIGH_LATENCY) {
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

}
