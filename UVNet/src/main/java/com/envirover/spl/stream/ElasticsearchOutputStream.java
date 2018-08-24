package com.envirover.spl.stream;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONArray;
import org.json.JSONObject;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_high_latency;

/**
 * Writes MAVLink message to Elasticsearch index 'mavlinkmessages'.
 * 
 */
class ElasticsearchOutputStream implements MAVLinkOutputStream {

    // Connection properties
    public static final String ELASTICSEARCH_ENDPOINT = "envirover.elasticsearch.endpoint";
    public static final String ELASTICSEARCH_PORT     = "envirover.elasticsearch.port";
    public static final String ELASTICSEARCH_PROTOCOL = "envirover.elasticsearch.protocol";
    
    private static final String DEFAULT_ELASTICSEARCH_ENDPOINT = "localhost";
    private static final String DEFAULT_ELASTICSEARCH_PORT     = "9200";
    private static final String DEFAULT_ELASTICSEARCH_PROTOCOL = "http";
    
    private static final Logger logger = Logger.getLogger(ElasticsearchOutputStream.class.getName());
    
    private static ResourceBundle definitions = ResourceBundle.getBundle("com.envirover.spl.stream.definitions");
    private static final String SPL_ELASTICSEARCH_TABLE = "SPL_ELASTICSEARCH_TABLE";
    
    private RestHighLevelClient client = null;
    private String tableName = "mavlinkmessages"; //must be lowercase for ES 
    
    private final String elasticsearchEndpoint;
    private final int    elasticsearchPort;
    private final String elasticsearchPotocol;
    
    public ElasticsearchOutputStream() throws IOException {
        if (System.getenv(SPL_ELASTICSEARCH_TABLE) != null) {
        	this.tableName = System.getenv(SPL_ELASTICSEARCH_TABLE);
        }
        
        this.elasticsearchEndpoint = System.getProperty(ELASTICSEARCH_ENDPOINT, DEFAULT_ELASTICSEARCH_ENDPOINT);
        this.elasticsearchPort = Integer.parseInt(System.getProperty(ELASTICSEARCH_PORT, DEFAULT_ELASTICSEARCH_PORT));
        this.elasticsearchPotocol = System.getProperty(ELASTICSEARCH_PROTOCOL, DEFAULT_ELASTICSEARCH_PROTOCOL);
    }
    
    public ElasticsearchOutputStream(String elasticsearchEndpoint, int elasticsearchPort, String elasticsearchPotocol) throws IOException {
        if (System.getenv(SPL_ELASTICSEARCH_TABLE) != null) {
        	this.tableName = System.getenv(SPL_ELASTICSEARCH_TABLE);
        }
        
        this.elasticsearchEndpoint = elasticsearchEndpoint;
        this.elasticsearchPort = elasticsearchPort;
        this.elasticsearchPotocol = elasticsearchPotocol;
    }
    
    @Override
    public void open() throws IOException {
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(elasticsearchEndpoint, elasticsearchPort, elasticsearchPotocol)));

        if (System.getenv(SPL_ELASTICSEARCH_TABLE) != null) {
            tableName = System.getenv(SPL_ELASTICSEARCH_TABLE);
        }
        
        // Create mavlinkmessages index in ES
        try {
            HttpEntity entity = new NStringEntity(definitions.getString("MAVLinkMessagesSchema"), ContentType.APPLICATION_JSON);
            
            Response response = client.getLowLevelClient().performRequest("PUT", tableName, new HashMap<String, String>(), entity); 
            
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.info(MessageFormat.format("Elasticsearch table ''{0}'' created.", tableName));
            }
        } catch(IOException ex) {
            logger.info(ex.getMessage());
        }
    }
    
    @Override
    public void close() throws IOException {
        client.close();
    }
    
    @Override
    public void writePacket(MAVLinkPacket packet, Map<String, String> metadata) throws IOException {
        if (packet == null) {
            return;
        }
        
        IndexRequest indexRequest = org.elasticsearch.client.Requests.indexRequest(tableName);
        indexRequest.type("_doc");
        
        JSONObject record = new JSONObject();
        
        Date time = new Date();
   
        if (metadata != null && metadata.get("transmit_time") != null) {
	        try {
	            //Time stamp like '17-04-03 02:11:35'
	            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
	           	time = sdf.parse(metadata.get("transmit_time"));
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }
        }
        
        JSONObject message = toJSON(packet);
        
        if (message != null) {
	        record.put("time", time.getTime());
	        record.put("sysid", message.getInt("sysid"));
	        record.put("msgid", message.getInt("msgid"));
	        record.put("compid", message.getInt("compid"));
	        record.put("message", message);
	        
	        JSONObject messageMetadata = new JSONObject();
	        
	        if (metadata != null) {
	        	for (Map.Entry<String, String> entry : metadata.entrySet()) {
	        		messageMetadata.put(entry.getKey(), entry.getValue());
	        	}
	        }
	        
	        record.put("message_metadata", messageMetadata);
	        
	        JSONArray point = new JSONArray();
	        point.put(message.getInt("longitude") / 1E7 ).put(message.getInt("latitude") / 1E7);
	        record.put("location", point); // TODO should the field be called location or position instead of geometry?
	
	        indexRequest.source(record.toString(), XContentType.JSON);
	        IndexResponse response = client.index(indexRequest);
	        System.out.println(response.status());
        }
    }
    
    public static JSONObject toJSON(MAVLinkMessage message) throws IOException {
        try { 
            JSONObject result = new JSONObject();
            Field[] fields = message.getClass().getFields();
            for(Field f : fields) {
                if (!Modifier.isFinal(f.getModifiers()))
                    result.put(f.getName(), f.get(message));
            }
            return result;
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    private JSONObject toJSON(MAVLinkPacket packet) throws IOException {
        if (packet != null && packet.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
            msg_high_latency msg = (msg_high_latency) packet.unpack();
            return toJSON(msg);
        } else
            return null;
    }
    
}
