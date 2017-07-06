/*
This file is part of SPLStream application.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

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
along with SPLStream.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl.stream;

import java.io.IOException;
import java.lang.reflect.Field;
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

import org.codehaus.jackson.map.ObjectMapper;

import com.MAVLink.common.msg_high_latency;
import com.emvirover.geojson.Feature;
import com.emvirover.geojson.FeatureCollection;
import com.emvirover.geojson.LineString;
import com.emvirover.geojson.Point;
import com.sun.jersey.api.view.Viewable;

/**
 * Retrieves MAVLink messages from DynamoDB table in JSON format.
 * 
 */

@Path("/")
public class SPLFeatureService {

    private static final ObjectMapper mapper = new ObjectMapper();

    // private static final Logger logger =
    // Logger.getLogger(SPLFeatureService.class.getName());

    public SPLFeatureService() throws IOException {
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        return Response.ok(new Viewable("/index.jsp")).build();
    }

    @GET
    @Path("/features")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFeatureCollection(@QueryParam("devices") String devices,
            @DefaultValue("-1") @QueryParam("startTime") long startTime,
            @DefaultValue("-1") @QueryParam("endTime") long endTime,
            @DefaultValue("point") @QueryParam("type") String type)
            throws IOException, IllegalArgumentException, IllegalAccessException {

        FeatureCollection features = new FeatureCollection();

        String[] deviceIds = devices.split(",");

        for (String deviceId : deviceIds) {
            DynamoDBInputStream stream = new DynamoDBInputStream(
                    deviceId, 
                    startTime > 0 ? new Date(startTime) : null,
                    endTime > 0 ? new Date(endTime) : null);

            if (type.equalsIgnoreCase("point")) {
                buildPointFeatures(features, stream);
            } else if (type.equalsIgnoreCase("linestring")) {
                buildLineFeatures(features, deviceId, stream);
            }
        }

        return mapper.writeValueAsString(features);
    }

    private void buildLineFeatures(FeatureCollection features, String deviceId, DynamoDBInputStream stream)
            throws IOException {
        Feature lineFeature = new Feature();
        MAVLinkRecord record = null;
        LineString line = new LineString(); 
        long minTime = -1;
        long maxTime = -1;
        while ((record = stream.readPacket()) != null) {
            if (record.getPacket().msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
                msg_high_latency msg = (msg_high_latency)record.getPacket().unpack();
      
                if (minTime < 0 || record.getTime().getTime() < minTime) {
                    minTime = record.getTime().getTime();
                }

                if (maxTime < 0 || record.getTime().getTime() > maxTime) {
                    maxTime = record.getTime().getTime();
                }

                List<Double> coordinates = new ArrayList<Double>();
                coordinates.add(msg.longitude / 10000000.0);
                coordinates.add(msg.latitude / 10000000.0);
                coordinates.add((double) (msg.altitude_amsl / 1.0));
                line.getCoordinates().add(coordinates);
            }
        }

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("device_id", deviceId);
        properties.put("from_time", minTime);
        properties.put("to_time", maxTime);

        lineFeature.setProperties(properties);

        lineFeature.setGeometry(line);

        features.getFeatures().add(lineFeature);
    }

    private void buildPointFeatures(FeatureCollection features, DynamoDBInputStream stream)
            throws IOException, IllegalAccessException {
        MAVLinkRecord record = null;
        while ((record = stream.readPacket()) != null) {
            if (record.getPacket().msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
                msg_high_latency msg = (msg_high_latency) record.getPacket().unpack();
                Feature pointFeature = new Feature();
   
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put("device_id", record.getDeviceId());
                properties.put("time", record.getTime().getTime());
   
                for (Field field : msg.getClass().getFields()) {
                    properties.put(field.getName(), field.get(msg));
                }
   
                pointFeature.setProperties(properties);
   
                List<Double> coordinates = new ArrayList<Double>();
                coordinates.add(msg.longitude / 10000000.0);
                coordinates.add(msg.latitude / 10000000.0);
                coordinates.add((double) (msg.altitude_amsl / 1.0));
                pointFeature.setGeometry(new Point(coordinates));
   
                features.getFeatures().add(pointFeature);
            }
        }
    }

}
