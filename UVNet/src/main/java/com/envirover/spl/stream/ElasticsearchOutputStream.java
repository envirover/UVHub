package com.envirover.spl.stream;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
import com.amazonaws.protocol.json.SdkJsonGenerator.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ElasticsearchOutputStream implements MAVLinkOutputStream {

    private static final Logger logger = Logger.getLogger(ElasticsearchOutputStream.class.getName());
    
    private static ResourceBundle definitions = ResourceBundle.getBundle("com.envirover.spl.stream.definitions");
    private static final String SPL_ELASTICSEARCH_TABLE = "SPL_ELASTICSEARCH_TABLE";
    
    private static RestHighLevelClient client = null;
    private static String tableName = "mavlinkmessages"; //must be lowercase for ES (was "MAVLinkMessages")
    
    public ElasticsearchOutputStream() throws IOException {
        if (System.getenv(SPL_ELASTICSEARCH_TABLE) != null) {
            tableName = System.getenv(SPL_ELASTICSEARCH_TABLE);
        }
    }
    
    @Override
    public void open() throws IOException {
        client = new RestHighLevelClient(
                RestClient.builder(
                    new HttpHost(System.getProperty("envirover.elasticsearch.endpoint"), 
                    Integer.valueOf(System.getProperty("envirover.elasticsearch.port")), 
                    "https")));
//TODO remove if unused!
//                RestClient.builder(new HttpHost("localhost", 9200, "http"))); 
        if (System.getenv(SPL_ELASTICSEARCH_TABLE) != null) {
            tableName = System.getenv(SPL_ELASTICSEARCH_TABLE);
        }
        
        //create mavlinkmessages index in ES
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
    public void writePacket(
           String imei, String momsn, String transmitTime, 
           String iridiumLatitude, String iridiumLongitude, 
           String iridiumCep, MAVLinkPacket packet) throws IOException {
        if (imei == null || imei.isEmpty() || transmitTime == null || packet == null) {
            return;
        }
        IndexRequest indexRequest = org.elasticsearch.client.Requests.indexRequest(tableName);
        indexRequest.type("spl_track");
        JSONObject record = new JSONObject();
        
        Date time = new Date();
        try {
            //Time stamp like '17-04-03 02:11:35'
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            time = sdf.parse(transmitTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        
        JSONObject message = toJSON(packet);
        record.put("time", time.getTime());
        record.put("sysid", message.getInt("sysid"));
        record.put("msgid", message.getInt("msgid"));
        record.put("compid", message.getInt("compid"));
        record.put("message", message);
        JSONObject messageMetadata = new JSONObject();
        messageMetadata.put("imei", imei);
        	messageMetadata.put("momsn", momsn);
        	messageMetadata.put("transmit_time", transmitTime); //TODO can we store transmitTime as millis?
        	messageMetadata.put("iridium_latitude", iridiumLatitude);
        	messageMetadata.put("iridium_longitude", iridiumLongitude);
        	messageMetadata.put("iridium_cep",iridiumCep);
        record.put("message_metadata", messageMetadata);
        
        JSONArray point = new JSONArray();
        point.put(message.getInt("longitude") / 1E7 ).put(message.getInt("latitude") / 1E7);
        record.put("location", point); // TODO should the field be called location or position instead of geometry?

        indexRequest.source(record.toString(), XContentType.JSON);
        IndexResponse response = client.index(indexRequest);
        System.out.println(response.status());
        
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
    
    private JSONObject toJSON(MAVLinkPacket packet) throws JsonGenerationException, JsonMappingException, IOException {
        if (packet != null && packet.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
            msg_high_latency msg = (msg_high_latency) packet.unpack();
            return toJSON(msg);
        } else
            return null;
    }
}
