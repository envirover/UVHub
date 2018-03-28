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

package com.envirover.spl.stream;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
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
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Reads MAVLink messages from Elasticsearch table.
 * 
 * 
 */
public class MAVLinkMessagesElasticsearchTable implements MAVLinkInputStream {
    private static final Logger logger = Logger.getLogger(MAVLinkMessagesElasticsearchTable.class.getName());
    private static final String SPL_ELASTICSEARCH_TABLE = "SPL_ELASTICSEARCH_TABLE";

    private static final String ATTR_TIME = "time";
    private static final String ATTR_DEVICE_ID = "sysid";
    private static final String ATTR_MSG_ID = "msgid";
    private static final String ATTR_MESSAGE = "message";

    private static RestHighLevelClient client = null; 
    private static String tableName = "mavlinkmessages";


    public MAVLinkMessagesElasticsearchTable() {
        client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200, "http"))); 
        
        
        if (System.getenv(SPL_ELASTICSEARCH_TABLE) != null) {
            tableName = System.getenv(SPL_ELASTICSEARCH_TABLE);
        }
    }

    @Override
    public void open() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public JSONArray query(String deviceId, Long startTime, Long endTime, Integer msgId) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        qb.must(QueryBuilders.termQuery(ATTR_DEVICE_ID, deviceId));
        
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
        
        QueryBuilder filter = QueryBuilders.termQuery(ATTR_MSG_ID, msgId); //msgId is a message type -  MAVLINK_MSG_ID_HIGH_LATENCY = 234;
        qb.must(filter);
        
        sourceBuilder.query(qb);
        sourceBuilder.from(0);
        sourceBuilder.size(10000);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        SearchRequest searchRequest = new SearchRequest(tableName);
        searchRequest.types("spl_track");
        searchRequest.source(sourceBuilder);
        logger.info(sourceBuilder.toString());
        SearchResponse searchResponse = client.search(searchRequest);
        //TODO do I need to check searchResponse here?
        SearchHits hits = searchResponse.getHits();
        JSONArray result = new JSONArray();
        for(Iterator<SearchHit> iter = hits.iterator(); iter.hasNext(); ) {
            SearchHit hit = iter.next();
            Map<String, Object> source = hit.getSourceAsMap();
            JSONObject mavLinkRecord = new JSONObject();
            mavLinkRecord.put("deviceId", (int) source.get(ATTR_DEVICE_ID)); //"sysid"
            mavLinkRecord.put("time", source.get(ATTR_TIME)); //"time" - I suggest that we return time in milliseconds
            mavLinkRecord.put("msgId", (int) source.get(ATTR_MSG_ID)); //msgid
            Map<String, Object> message = (Map<String, Object>) source.get(ATTR_MESSAGE); //"message"
            mavLinkRecord.put("latitude", ((Integer) message.get("latitude")) / 10000000.0);
            mavLinkRecord.put("longitude", ((Integer) message.get("longitude")) / 10000000.0);
            mavLinkRecord.put("messsage", message);
            result.put(mavLinkRecord);
        }
        return result;
    }
}
