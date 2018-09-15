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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_value;
import com.envirover.geojson.Feature;
import com.envirover.geojson.FeatureCollection;
import com.envirover.geojson.Geometry;
import com.envirover.geojson.Point;

/**
 * Serializes and deserializes to/from json objects used by 
 * {@link com.envirover.uvnet.shadow.PersistentUVShadow}. 
 * 
 * @author Pavel Bobov
 *
 */
class JsonSerializer  {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final Field[] mission_item_fields = msg_mission_item.class.getFields();
	
	/**
	 * Serializes msg_param_value object to JSON.
	 * 
	 * @param parameterValue msg_param_value object
	 * @return JSON string

	 */
	public static String toJSON(msg_param_value parameterValue) 
			throws JsonGenerationException, JsonMappingException, IOException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("sysid", parameterValue.sysid);
		jsonObject.put("compid", parameterValue.compid);
		jsonObject.put("msgid", parameterValue.msgid);
		jsonObject.put("param_count", parameterValue.param_count);
		jsonObject.put("param_index", parameterValue.param_index);
		jsonObject.put("param_id", parameterValue.getParam_Id());
		jsonObject.put("param_type", parameterValue.param_type);
		jsonObject.put("param_value", parameterValue.param_value);
		return jsonObject.toString();
	}
	
	/**
	 * Deserializes msg_param_value object from JSON.
	 * 
	 * @param json JSON string
	 * @return msg_param_value object
	 */
	public static msg_param_value paramValueFromJSON(String json) 
			throws JsonParseException, JsonMappingException, IOException {
		JSONObject jsonObject = new JSONObject(json);
		
		msg_param_value parameterValue = new msg_param_value();
		parameterValue.sysid = jsonObject.getInt("sysid");
		parameterValue.compid = jsonObject.getInt("compid");
		parameterValue.msgid = jsonObject.getInt("msgid");
		parameterValue.param_count = jsonObject.getInt("param_count");
		parameterValue.param_index = jsonObject.getInt("param_index");
		parameterValue.setParam_Id(jsonObject.getString("param_id"));
		parameterValue.param_type = (short)jsonObject.getInt("param_type");
		parameterValue.param_value = jsonObject.getFloat("param_value");
		
		return parameterValue;
	}
	
	public static Feature featureFromJSON(String json)
			throws JsonParseException, JsonMappingException, IOException {
		 return mapper.readValue(json, Feature.class);
	}
	
	/**
	 * Converts this list of mission items to JSON string.
	 * 
	 * @return JSON string
	 */
	public static String toJSON(List<msg_mission_item> missionItems) throws JsonGenerationException, JsonMappingException, IOException {
		FeatureCollection features = new FeatureCollection();
		
		for (msg_mission_item missionItem : missionItems) {
			Geometry geometry = new Point((double)missionItem.y,
					                      (double)missionItem.x, 
					                      (double)missionItem.z); 
			
			Map<String, Object> properties = new HashMap<String, Object>();
            
            for (Field f : mission_item_fields) {
                if (!Modifier.isFinal(f.getModifiers()))
					try {
						Object val =  f.get(missionItem);
						
						if (val != null && val instanceof Float && Float.isNaN((Float)val)) {
							val = null;
						}
						
						properties.put(f.getName(), val);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
            }
            
            features.getFeatures().add(new Feature(geometry, properties));
		}
		
		return mapper.writeValueAsString(features);
	}
	
	/**
	 * Constructs list of mission items from JSON string.
	 * 
	 * @param json GeoJSON feature collection  
	 * @return list of mission items
	 */
	public static List<msg_mission_item> missionsFromJSON(String json)
			throws JsonParseException, JsonMappingException, IOException {
		FeatureCollection features = mapper.readValue(new ByteArrayInputStream(json.getBytes("UTF-8")), 
				 FeatureCollection.class);
		
		List<msg_mission_item> missionItems = new ArrayList<msg_mission_item>(features.getFeatures().size());
		
		for (Feature feature : features.getFeatures()) {
			msg_mission_item missionItem = new msg_mission_item();
			
			for (Field f : mission_item_fields) {
                if (!Modifier.isFinal(f.getModifiers())) {
                	Object value = feature.getProperties().get(f.getName());
                	
                	if (value != null) {
	                	try {
	                		if (f.getType().equals(float.class)) {
	                			float param = ((Double)value).floatValue();
	                			f.set(missionItem, param);
	                		} else if (f.getType().equals(short.class)) {
	                			short param = ((Integer)value).shortValue();
	                			f.set(missionItem, param);
	                		} else {
	                			f.set(missionItem, value);
	                		}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
                	}
                }
            }
			
			missionItems.add(missionItem);
		}
	
		return missionItems;
	}
	
	/**
	 * Serializes MAVLink message of HIGH_LATENCY type to GeoJSON feature representation.
	 * 
	 * @param msg MAVLink message of HIGH_LATENCY type
	 * @return GeoJSON feature representation
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static String toJSON(MAVLinkMessage msg, long timestamp)
			throws JsonGenerationException, JsonMappingException, IOException, IllegalArgumentException, IllegalAccessException {
		Geometry geometry;

		if (msg.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
			msg_high_latency hl = (msg_high_latency)msg;
			geometry = new Point(hl.longitude / 1.0E7, hl.latitude / 1.0E7, (double) hl.altitude_amsl);
		} else {
			geometry = null;
		}

		Map<String, Object> properties = new HashMap<String, Object>();

		for (Field f : msg.getClass().getFields()) {
			 if (!Modifier.isFinal(f.getModifiers())) {
				 Object value = f.get(msg);
				
				 if (f.getType() == byte[].class) {
					 value = bytesToString((byte[])value);
				 }

				 properties.put(f.getName(), value);
			 }
		}

		Feature feature = new Feature(geometry, properties);
		
		feature.getProperties().put("time", Long.valueOf(timestamp));

		return mapper.writeValueAsString(feature);
	}
	
	public static MAVLinkMessage mavlinkMessageFromJSON(String json)
			throws JsonParseException, JsonMappingException, IOException {
		Feature feature = mapper.readValue(new ByteArrayInputStream(json.getBytes("UTF-8")), Feature.class);

		if ((int) feature.getProperties().get("msgid") == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
			msg_high_latency msg = new msg_high_latency();

			for (Field f : msg.getClass().getFields()) {
				if (!Modifier.isFinal(f.getModifiers())) {
					Object value = feature.getProperties().get(f.getName());
					
					try {
						if (f.getType().equals(float.class)) {
							float param = ((Double)value).floatValue();
							f.set(msg, param);
						} else if (f.getType().equals(short.class)) {
							short param = ((Integer)value).shortValue();
							f.set(msg, param);
						} else if (f.getType().equals(byte.class)) {
							byte param = ((Integer)value).byteValue();
							f.set(msg, param);
						} else {
							f.set(msg, value);
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			
			return msg;
		}

		return null;
	}
	
	private static String bytesToString(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0)
				break;
			
			result.append(bytes[i]);
		}
		
		return result.toString();
	}

}
