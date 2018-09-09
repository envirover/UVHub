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


package com.envirover.uvnet.shadow;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
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
import com.envirover.uvnet.mission.Plan;

/**
 * Implementation of UVShadowView interface that uses Elasticsearch.
 * 
 * @author Pavel Bobov
 */
public class PersistentUVShadowView implements UVShadowView {
	
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
    private static final String MISSIONS_INDEX_NAME   = "reported_missions";
    
    private static final String DOCUMENT_TYPE      = "_doc";
    
    //private static int MAX_HITS = 10000;
    private static final int SEARCH_TIMEOUT = 60; //seconds

    // Properties
    private static final String ATTR_TIME = "properties.time";
    private static final String ATTR_SYS_ID = "properties.sysid";
    private static final String ATTR_MSG_ID = "properties.msgid";

    private static RestHighLevelClient client = null; 

    public PersistentUVShadowView() {
        client = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost(System.getProperty(ELASTICSEARCH_ENDPOINT, DEFAULT_ELASTICSEARCH_ENDPOINT), 
                Integer.valueOf(System.getProperty(ELASTICSEARCH_PORT, DEFAULT_ELASTICSEARCH_PORT)), 
                System.getProperty(DEFAULT_ELASTICSEARCH_PROTOCOL, DEFAULT_ELASTICSEARCH_PROTOCOL))));
    }

    @Override
	public FeatureCollection queryMessages(int sysId, int msgId, Long startTime, Long endTime, int top) throws IOException {
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
        
        FeatureCollection result = new FeatureCollection();
		
		for (Iterator<SearchHit> iter = hits.iterator(); iter.hasNext();) {
			SearchHit hit = iter.next();
			Feature feature = mapper.readValue(hit.getSourceAsString(), Feature.class);
			result.getFeatures().add(feature);
		}

		return result;
    }
     
    @Override
	public Plan queryMissions(int sysid) throws IOException {
		String id = Integer.toString(sysid);
		
		GetRequest getRequest = new GetRequest(MISSIONS_INDEX_NAME, DOCUMENT_TYPE, id);
		
		GetResponse getResponse = client.get(getRequest);
		
		if (getResponse.isExists()) {
			String source = getResponse.getSourceAsString();
			return new Plan(JsonSerializer.missionsFromJSON(source));
		} else {
			return new Plan();
		}
    }

}
