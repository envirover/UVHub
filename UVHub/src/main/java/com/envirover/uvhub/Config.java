/*
 * Envirover confidential
 * 
 *  [2019] Envirover
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
package com.envirover.uvhub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_TYPE;

/**
 * Provides access to UV Hub configuration properties specified by environment 
 * variables or in app.properties resource file.
 * 
 * @author Pavel Bobov
 */
public class Config {
    private final static String CONFIG_PROPERTIES_FILE = "app.properties";

    // Configuration properties
    private final static String PROP_RADIOROOM_PORT = "radioroom.port";
    private final static String PROP_QUEUE_SIZE = "queue.size";
    private final static String PROP_ROCKBLOCK_PORT = "rockblock.port";
    private final static String PROP_MAVLINK_PORT = "mavlink.port";
    private final static String PROP_SHADOW_PORT = "shadow.port";
    private final static String PROP_ROCKBLOCK_URL = "rockblock.url";
    private final static String PROP_ROCKBLOCK_IMEI = "rockblock.imei";
    private final static String PROP_ROCKBLOCK_USERNAME = "rockblock.username";
    private final static String PROP_ROCKBLOCK_PASSWORD = "rockblock.password";
    private final static String PROP_HEARTBEAT_INTERVAL = "heartbeat.interval";
    private final static String PROP_MAV_AUTOPILOT = "mav.autopilot";
    private final static String PROP_MAV_TYPE = "mav.type";
    private final static String PROP_ES_ENDPOINT = "elasticsearch.endpoint";
    private final static String PROP_ES_PORT = "elasticsearch.port";
    private final static String PROP_ES_PROTOCOL = "elasticsearch.protocol";

    // default property values
    private final static String DEFAULT_ROCKBLOCK_URL = "https://core.rock7.com/rockblock/MT";
    private final static String DEFAULT_HTTP_CONTEXT = "/mo";
    private final static Integer DEFAULT_ROCKBLOCK_PORT = 5080;
    private final static Integer DEFAULT_RADIOROOM_PORT = 5060;
    private final static Integer DEFAULT_MAVLINK_PORT = 5760;
    private final static Integer DEFAULT_SHADOW_PORT = 5757;
    private final static Integer DEFAULT_QUEUE_SIZE = 1000;
    private final static Integer DEFAULT_HEARTBEAT_INT = 1000;
    private final static Short DEFAULT_AUTOPILOT = MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA;
    private final static Short DEFAULT_MAV_TYPE = MAV_TYPE.MAV_TYPE_GROUND_ROVER;
    private final static String DEFAULT_ES_ENDPOINT = "localhost";
    private final static Integer DEFAULT_ES_PORT = 9200;
    private final static String DEFAULT_ES_PROTOCOL = "http";
    private final static Float DEFAULT_HL_REPORT_PERIOD = 60.0F; // 1 minute

    private String rockblockUrl = DEFAULT_ROCKBLOCK_URL;
    private String httpContext = DEFAULT_HTTP_CONTEXT;
    private Integer rockblockPort = DEFAULT_ROCKBLOCK_PORT;
    private Integer radioroomPort = DEFAULT_RADIOROOM_PORT;
    private Integer mavlinkPort = DEFAULT_MAVLINK_PORT;
    private Integer shadowPort = DEFAULT_SHADOW_PORT;
    private Integer queueSize = DEFAULT_QUEUE_SIZE;
    private Integer heartbeatInterval = DEFAULT_HEARTBEAT_INT;
    private String imei = null;
    private String username = null;
    private String password = null;
    private Short autopilot = DEFAULT_AUTOPILOT;
    private Short mavType = DEFAULT_MAV_TYPE;
    private String esEndpoint = DEFAULT_ES_ENDPOINT;
    private Integer esPort = DEFAULT_ES_PORT;
    private String esProtocol = DEFAULT_ES_PROTOCOL;
    private Properties props = new Properties();

    private static final Config config = new Config();
   
    private Config() {
    }

    public static Config getInstance() {
        return config;
    }

    /**
     * Loads UV Hub configuration properties from app.properties file and 
     * overrides the properties set by the environment variables.
     * 
     * The environmental variables names are constructed from property names
     * by converting them to upper case and replacing dots by underscore characters.
     * 
     * @throws IOException if loading properties failed 
     */
    public void init() throws IOException {
        ClassLoader loader = Config.class.getClassLoader();

        try (InputStream in = loader.getResourceAsStream(CONFIG_PROPERTIES_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } 
    
        if (getProperty(PROP_ROCKBLOCK_PORT) != null)
            rockblockPort = Integer.valueOf(getProperty(PROP_ROCKBLOCK_PORT));

        if (getProperty(PROP_RADIOROOM_PORT) != null)
            radioroomPort = Integer.valueOf(getProperty(PROP_RADIOROOM_PORT));

        if (getProperty(PROP_MAVLINK_PORT) != null)
            mavlinkPort = Integer.valueOf(getProperty(PROP_MAVLINK_PORT));

        if (getProperty(PROP_SHADOW_PORT) != null)
            shadowPort = Integer.valueOf(getProperty(PROP_SHADOW_PORT));

        if (getProperty(PROP_QUEUE_SIZE) != null)
            queueSize = Integer.valueOf(getProperty(PROP_QUEUE_SIZE));

        if (getProperty(PROP_HEARTBEAT_INTERVAL) != null)
            heartbeatInterval = Integer.valueOf(getProperty(PROP_HEARTBEAT_INTERVAL));

        if (getProperty(PROP_ROCKBLOCK_URL) != null)
            rockblockUrl = getProperty(PROP_ROCKBLOCK_URL);

        if (getProperty(PROP_ES_ENDPOINT) != null)
            esEndpoint = getProperty(PROP_ES_ENDPOINT);

        if (getProperty(PROP_ES_PORT) != null)
            esPort = Integer.valueOf(getProperty(PROP_ES_PORT));

        if (getProperty(PROP_ES_PROTOCOL) != null)
            esProtocol = getProperty(PROP_ES_PROTOCOL);

        if (getProperty(PROP_MAV_AUTOPILOT) != null)
            autopilot = Short.valueOf(getProperty(PROP_MAV_AUTOPILOT));
        
        if (getProperty(PROP_MAV_TYPE) != null)
            mavType = Short.valueOf(getProperty(PROP_MAV_TYPE));   

        imei = getProperty(PROP_ROCKBLOCK_IMEI);
        username = getProperty(PROP_ROCKBLOCK_USERNAME);
        password = getProperty(PROP_ROCKBLOCK_PASSWORD);
    }

    public Integer getSystemId() {
        return 1;
    }

    public Integer getRockblockPort() {
        return rockblockPort;
    }

    public String getHttpContext() {
        return httpContext;
    }

    public Integer getMAVLinkPort() {
        return mavlinkPort;
    }

    public Integer getShadowPort() {
        return shadowPort;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public String getRockBlockIMEI() {
        return imei;
    }

    public String getRockBlockUsername() {
        return username;
    }

    public String getRockBlockPassword() {
        return password;
    }

    public String getRockBlockURL() {
        return rockblockUrl;
    }

    public short getAutopilot() {
        return autopilot;
    }

    public short getMavType() {
        return mavType;
    }

    public Integer getRadioRoomPort() {
        return radioroomPort;
    }

    public String getElasticsearchEndpoint() {
        return esEndpoint;
    }

    public Integer getElasticsearchPort() {
        return esPort;
    }

    public String getElasticsearchProtocol() {
        return esProtocol;
    }

    public Float getDefaultHLReportPeriod() {
        return DEFAULT_HL_REPORT_PERIOD;
    }

    private String getProperty(String name) {
        return getProperty(name, null);
    }

    private String getProperty(String name, String defaultValue) {
        String envName = name.replace('.', '_').toUpperCase();
        String value = System.getenv(envName);
        return value != null ? value : props.getProperty(name, defaultValue);
    }

}
