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

package com.envirover.spl.tracks;

import java.io.IOException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.envirover.geojson.FeatureCollection;
import com.envirover.geojson.GeometryType;
import com.envirover.geojson.Point;
import com.sun.jersey.api.view.Viewable;

/**
 * Retrieves MAVLink messages from Elasticsearch table in JSON format.
 * 
 */
@Path("/")
public class SPLFeatureService {

    private final int MAVLINK_MSG_ID_HIGH_LATENCY = 234;
    
    private final UVShadowView stream;
    
    public SPLFeatureService() throws IOException {
        stream = new UVShadowView();
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
            @DefaultValue("point") @QueryParam("type") String type,
            @DefaultValue("100") @QueryParam("top") int top 
            )
            throws Exception {

        String[] deviceIds = devices.split(","); //TODO why is it devices and not just device?
        
        GeometryType geometryType = GeometryType.Point;
        
        if (type.equalsIgnoreCase(GeometryType.LineString.toString())) {
        	geometryType = GeometryType.LineString;
        }
        
        FeatureCollection records = null; 
        
       for (String deviceId : deviceIds) {
            records = stream.queryMessages(
                    Integer.parseInt(deviceId), 
                    startTime > 0 ? startTime : null,
                    endTime > 0 ? endTime : null,
                    MAVLINK_MSG_ID_HIGH_LATENCY,
                    geometryType, 
                    top);
        }
        
        return records;
    }
   
}
