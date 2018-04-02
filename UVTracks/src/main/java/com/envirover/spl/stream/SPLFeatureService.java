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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.view.Viewable;

/**
 * Retrieves MAVLink messages from Elasticsearch table in JSON format.
 * 
 */

@Path("/")
public class SPLFeatureService {

    private final int MAVLINK_MSG_ID_HIGH_LATENCY = 234;
    
    private final MAVLinkInputStream stream;
    
    public SPLFeatureService() throws IOException {
        stream = new MAVLinkMessagesElasticsearchTable();
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        return Response.ok(new Viewable("/index.jsp")).build();
    }
    
    
    @GET
    @Path("/features")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFeatureCollection(
            @DefaultValue("") @QueryParam("devices") String devices,
            @DefaultValue("-1") @QueryParam("startTime") long startTime,
            @DefaultValue("-1") @QueryParam("endTime") long endTime,
            @DefaultValue("point") @QueryParam("type") String type //TODO do I need type = "linestring"
            )
            throws Exception {

        String[] deviceIds = devices.split(","); //TODO why is it devices and not just device?
        String deviceId = deviceIds[0];
        
        JSONArray records = null; 
        if (!deviceId.isEmpty()) {
            records = stream.query(
                    deviceId, 
                    startTime > 0 ? startTime : null,
                    endTime > 0 ? endTime : null,
                    MAVLINK_MSG_ID_HIGH_LATENCY);
            if (type.equalsIgnoreCase("point")) {
                return records.toString();
            } else if (type.equalsIgnoreCase("linestring")) {
                return buildLineFeatures(deviceId, records).toString();//TODO Do we need this? 
            }
        }
        return records == null ? null : records.toString();
    }
    
    private JSONArray buildLineFeatures(String deviceId, JSONArray records) throws Exception {
        JSONArray line = new JSONArray(); 
        long minTime = -1;
        long maxTime = -1;
        
        for (int i = 0; i < records.length(); i++) {
            JSONObject record = records.getJSONObject(i); //"Mon Jul 10 19:46:51 PDT 2017"
            long recordTime = record.getLong("time");
            if (minTime < 0 || recordTime < minTime) {
                minTime = recordTime;
            }
            
            if (maxTime < 0 || recordTime > maxTime) {
                maxTime = recordTime;
            }
            
            double longitude = record.getDouble("longitude");
            double latitude = record.getDouble("latitude");
            double altitude = record.optDouble("altitude", 0);
            if (longitude != 0.0 || latitude != 0.0) {
                JSONArray point = new JSONArray();
                point.put(longitude);
                point.put(latitude);
                point.put(altitude);
                line.put(point);
            }
        }
        
        JSONObject properties = new JSONObject();
        properties.put("device_id", deviceId);
        properties.put("from_time", minTime);
        properties.put("to_time", maxTime);
        
        JSONObject lineFeature = new JSONObject().put("geometry", line).put("properties", properties); //TODO check GeoJSON and output format!
        return new JSONArray().put(lineFeature);
    }
}
