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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.envirover.geojson.LineString;
import com.envirover.geojson.Point;

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
    
    //private static int MAX_HITS = 10000;
    private static final int SEARCH_TIMEOUT = 60; //seconds

    // Properties
    private static final String ATTR_TIME = "properties.time";
    private static final String ATTR_SYS_ID = "properties.sysid";
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
     * @throws IOException
     */
     public FeatureCollection queryMessages(int sysId, int msgId, GeometryType geometryType, Long startTime, Long endTime, int top) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        qb.must(QueryBuilders.termQuery(ATTR_SYS_ID, sysId));
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
        sourceBuilder.size(top);
        sourceBuilder.timeout(new TimeValue(SEARCH_TIMEOUT, TimeUnit.SECONDS));
        sourceBuilder.sort(ATTR_TIME, SortOrder.DESC);
        SearchRequest searchRequest = new SearchRequest(MESSAGES_INDEX_NAME);
        searchRequest.types(DOCUMENT_TYPE);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        
        switch (geometryType) {
        case Point:
        	return buildPointFeatures(sysId, hits);
	    case LineString:
        	return buildLineFeatures(sysId, hits);
        default:
        	throw new IllegalArgumentException("Unsupported GeoJSON geometry type.");
        }
    }
     
	private FeatureCollection buildPointFeatures(int sysid, SearchHits hits) throws IOException {
		FeatureCollection result = new FeatureCollection();
		
		for (Iterator<SearchHit> iter = hits.iterator(); iter.hasNext();) {
			SearchHit hit = iter.next();
			Feature feature = mapper.readValue(hit.getSourceAsString(), Feature.class);
			result.getFeatures().add(feature);
		}

		return result;
	}
     
	private FeatureCollection buildLineFeatures(int sysid, SearchHits hits) throws IOException {
		long minTime = -1;
		long maxTime = -1;
		List<List<Double>> coordinates = new ArrayList<List<Double>>();

		for (Iterator<SearchHit> iter = hits.iterator(); iter.hasNext();) {
			SearchHit hit = iter.next();
			
			Feature feature = mapper.readValue(hit.getSourceAsString(), Feature.class);

			long recordTime = (long) feature.getProperties().get("time");
			
			if (minTime < 0 || recordTime < minTime) {
				minTime = recordTime;
			}

			if (maxTime < 0 || recordTime > maxTime) {
				maxTime = recordTime;
			}

			if (feature.getGeometry().getType() == GeometryType.Point) {
				coordinates.add(((Point)feature.getGeometry()).getCoordinates());
			}
		}

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("sysid", sysid);
		properties.put("from_time", minTime);
		properties.put("to_time", maxTime);

		Feature lineFeature = new Feature(new LineString(coordinates), properties);

		FeatureCollection result = new FeatureCollection();
		result.getFeatures().add(lineFeature);

		return result;
	}

}
