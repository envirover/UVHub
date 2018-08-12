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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.http.HttpHost;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;

/**
 * Stores the vehicle shadow in Elasticsearch.
 * 
 * @author Pavel Bobov
 * 
 */
public class PersistentUVShadow implements UVShadow {
	
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
    private static final String PARAMETERS_INDEX_NAME = "reported_params";
    
    private static final String DOCUMENT_TYPE      = "_doc";
    
    //private static final Logger logger = Logger.getLogger(PersistentUVShadow.class.getName());
    
    private static ResourceBundle mappings = ResourceBundle.getBundle("com.envirover.uvnet.shadow.mappings");
    
    private final String elasticsearchEndpoint;
    private final int    elasticsearchPort;
    private final String elasticsearchPotocol;
    
    private RestHighLevelClient client = null;
    private List<msg_mission_item> desiredMission = new ArrayList<msg_mission_item>();
    
    public PersistentUVShadow() throws IOException {
        this.elasticsearchEndpoint = System.getProperty(ELASTICSEARCH_ENDPOINT, DEFAULT_ELASTICSEARCH_ENDPOINT);
        this.elasticsearchPort = Integer.valueOf(System.getProperty(ELASTICSEARCH_PORT, DEFAULT_ELASTICSEARCH_PORT));
        this.elasticsearchPotocol = System.getProperty(ELASTICSEARCH_PROTOCOL, DEFAULT_ELASTICSEARCH_PROTOCOL);
    }
    
    public PersistentUVShadow(String elasticsearchEndpoint, int elasticsearchPort, String elasticsearchPotocol) throws IOException {
        this.elasticsearchEndpoint = elasticsearchEndpoint;
        this.elasticsearchPort = elasticsearchPort;
        this.elasticsearchPotocol = elasticsearchPotocol;
    }
	
    /**
     * Builds Elasticsearch client and creates mavlinkmissions and 
     * mavlinkmessages indices if they do not exist.
     * 
     * @throws IOException I/O exception
     */
    public void open() throws IOException  {
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(elasticsearchEndpoint, elasticsearchPort, elasticsearchPotocol)));
       
        try {
        	CreateIndexRequest createIndexRequest = new CreateIndexRequest(MISSIONS_INDEX_NAME);
        	client.indices().create(createIndexRequest);			
		} catch (ElasticsearchStatusException e) {
			if (!e.getDetailedMessage().contains("resource_already_exists_exception")) {
				e.printStackTrace();
			}
		}
        
        try {
        	CreateIndexRequest createIndexRequest = new CreateIndexRequest(PARAMETERS_INDEX_NAME);
        	client.indices().create(createIndexRequest);
		} catch (ElasticsearchStatusException e) {
			if (!e.getDetailedMessage().contains("resource_already_exists_exception")) {
				e.printStackTrace();
			}
		} 
        
        try {
        	CreateIndexRequest createIndexRequest = new CreateIndexRequest(MESSAGES_INDEX_NAME);
           	String mapping = mappings.getString("ReportedMessagesMapping");
        	createIndexRequest.mapping(DOCUMENT_TYPE, mapping, XContentType.JSON);
        	client.indices().create(createIndexRequest);
		} catch (ElasticsearchStatusException e) {
			if (!e.getDetailedMessage().contains("resource_already_exists_exception")) {
				e.printStackTrace();
			}
		} 
    }
    
    public void close() throws IOException {
    	if (client != null) {
    		client.close();
    		client = null;
    	}
    }
    
	@Override
	public List<msg_param_value> getParams(int sysId) throws IOException {
     	// Get all parameters from mavlinkparameters index 
    	SearchRequest searchRequest = new SearchRequest(PARAMETERS_INDEX_NAME); 
    	SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
    	searchSourceBuilder.query(QueryBuilders.termQuery("sysid", sysId)); 
    	searchSourceBuilder.size(10000);
    	searchRequest.source(searchSourceBuilder); 

    	SearchResponse searchResponse = client.search(searchRequest);
    	
    	// Build a map of existing parameters.
    	List<msg_param_value> params = new ArrayList<msg_param_value>();
    	
    	for (SearchHit hit : searchResponse.getHits()) {
     		params.add(JsonSerializer.paramValueFromJSON(hit.getSourceAsString()));
    	}
    	
		return params;
	}
	
    @Override
    public void setParams(int sysId, List<msg_param_value> params) throws IOException {
    	// Build a map of existing parameters.
    	Map<String, msg_param_value> existingParamsMap = new HashMap<String, msg_param_value>();
    	
    	for (msg_param_value paramValue : getParams(sysId)) {
    		existingParamsMap.put(paramValue.getParam_Id(), paramValue);
    	}
    	
    	Set<String> newParamsSet = new HashSet<String>();
    	
    	// Index new parameters and update param_count and param_index attributes of
    	// existing parameters, if they do not match param_count and param_index
    	// of the new parameters.
    	for (msg_param_value paramValue : params) {
     		msg_param_value existingParam = existingParamsMap.get(paramValue.getParam_Id());
    		
    		if (existingParam == null) { 
    			// Index the new parameter.
        		indexParamValue(sysId, paramValue);
    		} else {
     			if (existingParam.param_count != paramValue.param_count ||
     				existingParam.param_index != paramValue.param_index) {
     	   			// Update param_count for existing parameters,
        	    	// if it does not match the new number of parameters.
     				existingParam.param_count = paramValue.param_count;
     				existingParam.param_index = paramValue.param_index;
     				indexParamValue(sysId, existingParam);
    			}
    		}
    		
       		newParamsSet.add(paramValue.getParam_Id());
    	}
    	
    	// Delete existing parameters, if they are not in the new list of parameters
    	for (String paramId : existingParamsMap.keySet()) {
    		if (!newParamsSet.contains(paramId)) {
    			client.delete(new DeleteRequest(PARAMETERS_INDEX_NAME, DOCUMENT_TYPE, paramId));
    		}
    	}
    }

    @Override
 	public msg_param_value getParamValue(int sysId, String paramId, short paramIndex)  throws IOException {
    	if (paramIndex < 0) {
    		String id = String.format("%d_%s", sysId, paramId);
    		GetRequest getRequest = new GetRequest(PARAMETERS_INDEX_NAME, DOCUMENT_TYPE, id);
    		
    		GetResponse getResponse = client.get(getRequest);
    		
    		if (getResponse.isExists()) {
    			return JsonSerializer.paramValueFromJSON(getResponse.getSourceAsString());
     		} else {
     			throw new IOException(String.format("Parameter '%s' not found.", paramId));
      		}
    	} else {
    		// Get all parameters from mavlinkparameters index 
        	SearchRequest searchRequest = new SearchRequest(PARAMETERS_INDEX_NAME); 
        	
        	SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        	searchSourceBuilder.query(QueryBuilders.termQuery("param_index", paramIndex));
        	searchRequest.source(searchSourceBuilder);
        	
        	SearchResponse searchResponse = client.search(searchRequest);
        	
        	if (searchResponse.getHits().totalHits == 0) {
        		throw new IOException(String.format("Parameter with index %d not found.", (int)paramIndex));
        	}
        	
        	if (searchResponse.getHits().totalHits > 1) {
        		throw new IOException(String.format("More than one parameter with index %d found.", (int)paramIndex));
        	}
        	
        	String source = searchResponse.getHits().getAt(0).getSourceAsString();
        	
        	return JsonSerializer.paramValueFromJSON(source);
    	}
    }
    
    @Override
	public void setParam(int sysId, msg_param_set parameter) throws IOException {
    	msg_param_value paramValue = getParamValue(sysId, parameter.getParam_Id(), (short)-1);
    	
    	if (paramValue != null) { 
    		// Modify existing parameter value
    		paramValue.param_type = parameter.param_type;
    		paramValue.param_value = parameter.param_value;
	    	
    		indexParamValue(sysId, paramValue);
    	} else {
    		throw new IOException(String.format("Parameter '%s' not found.", parameter.getParam_Id()));
    	}
    }
    
    @Override
    public List<msg_mission_item> getDesiredMission() throws IOException {
    	return desiredMission;
    }
    
	@Override
	public List<msg_mission_item> getMission(int sysId) throws IOException {
		String id = Integer.toString(sysId);
		
		GetRequest getRequest = new GetRequest(MISSIONS_INDEX_NAME, DOCUMENT_TYPE, id);
		
		GetResponse getResponse = client.get(getRequest);
		
		if (getResponse.isExists()) {
			String source = getResponse.getSourceAsString();
	        
			return JsonSerializer.missionsFromJSON(source);
		} else {
			return new ArrayList<msg_mission_item>();
		}
	}

	@Override
	public void setMission(int sysId, List<msg_mission_item> mission) throws IOException {
		if (mission.size() == 0) {
			System.out.println("Skip saving empty mission.");
			return;
		}
			
        IndexRequest indexRequest = org.elasticsearch.client.Requests.indexRequest(MISSIONS_INDEX_NAME);
        indexRequest.type(DOCUMENT_TYPE);
        
        // Use system id of the first mission item as the document id 
        // for the reported mission. 
        String id = Integer.toString(sysId);
        indexRequest.id(id);
        
        String source = JsonSerializer.toJSON(mission);

        indexRequest.source(source, XContentType.JSON);
        
        IndexResponse response = client.index(indexRequest);
        
        System.out.println(response.status().toString());
	}

	@Override
	public void updateReportedState(MAVLinkMessage msg, long timestamp) throws IOException {
		// Save the message to the persistent store
        IndexRequest indexRequest = org.elasticsearch.client.Requests.indexRequest(MESSAGES_INDEX_NAME);
        indexRequest.type(DOCUMENT_TYPE);
		indexRequest.id(Long.toString(timestamp));
		
		try {
			String source = JsonSerializer.toJSON(msg, timestamp);
			indexRequest.source(source, XContentType.JSON);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IOException(e);
		}
		
		client.index(indexRequest);
	}

	@Override
	public MAVLinkMessage getLastMessage(int sysId, int msgId) throws IOException {
    	SearchRequest searchRequest = new SearchRequest(MESSAGES_INDEX_NAME); 
    	
    	SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
    	searchSourceBuilder.query(QueryBuilders.boolQuery()
    			.must(QueryBuilders.termQuery("properties.sysid", sysId))
    			.must(QueryBuilders.termQuery("properties.msgid", msgId))); 
    	searchSourceBuilder.sort("properties.time", SortOrder.DESC);
    	searchSourceBuilder.size(1);
    	searchRequest.source(searchSourceBuilder);
    	
    	SearchResponse searchResponse = client.search(searchRequest);
    	
    	if (searchResponse.getHits().getTotalHits() == 0) {
    		return null;
    	}
    	
    	String source = searchResponse.getHits().getAt(0).getSourceAsString();
    	return JsonSerializer.mavlinkMessageFromJSON(source);
	}
   
	private void indexParamValue(int sysId, msg_param_value paramValue)
			throws JsonGenerationException, JsonMappingException, IOException {
		String id = String.format("%d_%s", sysId, paramValue.getParam_Id());
		String source = JsonSerializer.toJSON(paramValue);

		IndexRequest indexRequest = org.elasticsearch.client.Requests.indexRequest(PARAMETERS_INDEX_NAME)
				.type(DOCUMENT_TYPE).id(id).source(source, XContentType.JSON);
 	       
		client.index(indexRequest);
	}
    
}
