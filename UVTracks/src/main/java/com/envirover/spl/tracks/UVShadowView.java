/*
This file is part of SPLTracks application.

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

package com.envirover.spl.tracks;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.envirover.geojson.Feature;
import com.envirover.geojson.FeatureCollection;
import com.envirover.geojson.GeometryType;

/**
 * Reads MAVLink messages from Elasticsearch indexes.
 * 
 * 
 */
public class UVShadowView {
	
	private static final ObjectMapper mapper = new ObjectMapper();
    //private static final Logger logger = Logger.getLogger(UVShadowView.class.getName());

    // Elasticsearch connection properties
    public static String ELASTICSEARCH_ENDPOINT = "envirover.elasticsearch.endpoint";
    public static String ELASTICSEARCH_PORT     = "envirover.elasticsearch.port";
    public static String ELASTICSEARCH_PROTOCOL = "envirover.elasticsearch.protocol";
    
    // Default values of Elasticsearch connection properties
    private static final String DEFAULT_ELASTICSEARCH_ENDPOINT = "localhost";
    private static final String DEFAULT_ELASTICSEARCH_PORT     = "9200";
    private static final String DEFAULT_ELASTICSEARCH_PROTOCOL = "http";
    
    // Elasticsearch indices
    private static final String MESSAGES_INDEX_NAME   = "reported_messages";
    //private static final String MISSIONS_INDEX_NAME   = "reported_missions";
    
    private static final String DOCUMENT_TYPE      = "_doc";
    
    private static int MAX_HITS = 10000;

    // Properties
    private static final String ATTR_TIME = "properties.time";
    private static final String ATTR_DEVICE_ID = "properties.sysid";
    private static final String ATTR_MSG_ID = "properties.msgid";

    private static RestHighLevelClient client = null; 

    public UVShadowView() {
        client = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost(System.getProperty(ELASTICSEARCH_ENDPOINT, DEFAULT_ELASTICSEARCH_ENDPOINT), 
                Integer.valueOf(System.getProperty(ELASTICSEARCH_PORT, DEFAULT_ELASTICSEARCH_PORT)), 
                System.getProperty(DEFAULT_ELASTICSEARCH_PROTOCOL, DEFAULT_ELASTICSEARCH_PROTOCOL))));
    }

    public void open() throws IOException {
    }

    public void close() throws IOException {
    }
    
     public FeatureCollection queryMessages(String deviceId, Long startTime, Long endTime, Integer msgId, GeometryType geometryType) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        qb.must(QueryBuilders.termQuery(ATTR_DEVICE_ID, deviceId));
        qb.must(QueryBuilders.termQuery(ATTR_MSG_ID, msgId));
           
        QueryBuilder timeIntervalQueryBuilder = null;
        if (startTime == null && endTime == null)
            timeIntervalQueryBuilder = null;
        else if (startTime == null && endTime != null)
            timeIntervalQueryBuilder = QueryBuilders.rangeQuery(ATTR_TIME).to(endTime, true).includeUpper(true);
        else if (startTime != null && endTime == null)
            timeIntervalQueryBuilder = QueryBuilders.rangeQuery(ATTR_TIME).from(startTime, true).includeLower(true);
        else
            timeIntervalQueryBuilder = QueryBuilders.rangeQuery(ATTR_TIME).from(startTime, true).to(endTime, true).includeLower(true).includeUpper(true);
        
        if (timeIntervalQueryBuilder != null)
            qb.must(timeIntervalQueryBuilder);
        

        sourceBuilder.query(qb);
        sourceBuilder.from(0);
        sourceBuilder.size(MAX_HITS);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.sort(ATTR_TIME, SortOrder.DESC);
        SearchRequest searchRequest = new SearchRequest(MESSAGES_INDEX_NAME);
        searchRequest.types(DOCUMENT_TYPE);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();

        FeatureCollection result = new FeatureCollection();
        
        if (geometryType == GeometryType.Point) {
	        for(Iterator<SearchHit> iter = hits.iterator(); iter.hasNext(); ) {
	            SearchHit hit = iter.next();
	            Feature feature = mapper.readValue(hit.getSourceAsString(), Feature.class);
	            result.getFeatures().add(feature);
	        }
        } else if (geometryType == GeometryType.LineString) {
        	//TODO: Build line string 
        }
        
        return result;
    }
     
//     JSONArray buildLineFeatures(String deviceId, JSONArray records) throws Exception {
//         JSONArray line = new JSONArray(); 
//         long minTime = -1;
//         long maxTime = -1;
//         
//         for (int i = 0; i < records.length(); i++) {
//             JSONObject record = records.getJSONObject(i); //"Mon Jul 10 19:46:51 PDT 2017"
//             long recordTime = record.getLong("time");
//             if (minTime < 0 || recordTime < minTime) {
//                 minTime = recordTime;
//             }
//             
//             if (maxTime < 0 || recordTime > maxTime) {
//                 maxTime = recordTime;
//             }
//             
//             double longitude = record.getDouble("longitude");
//             double latitude = record.getDouble("latitude");
//             double altitude = record.optDouble("altitude", 0);
//             if (longitude != 0.0 || latitude != 0.0) {
//                 JSONArray point = new JSONArray();
//                 point.put(longitude);
//                 point.put(latitude);
//                 point.put(altitude);
//                 line.put(point);
//             }
//         }
//         
//         JSONObject properties = new JSONObject();
//         properties.put("device_id", deviceId);
//         properties.put("from_time", minTime);
//         properties.put("to_time", maxTime);
//         
//         JSONObject lineFeature = new JSONObject().put("geometry", line).put("properties", properties); //TODO check GeoJSON and output format!
//         return new JSONArray().put(lineFeature);
//     }
     
}
