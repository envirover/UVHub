/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    private final static String PROP_SHADOW_CONNECTIONSTRING = "shadow.connectionstring";
    private final static String PROP_ROCKBLOCK_URL = "rockblock.url";
    private final static String PROP_ROCKBLOCK_IMEI = "rockblock.imei";
    private final static String PROP_ROCKBLOCK_USERNAME = "rockblock.username";
    private final static String PROP_ROCKBLOCK_PASSWORD = "rockblock.password";
    private final static String PROP_HEARTBEAT_INTERVAL = "heartbeat.interval";
    private final static String PROP_MAV_AUTOPILOT = "mav.autopilot";
    private final static String PROP_MAV_TYPE = "mav.type";
    private final static String PROP_MAV_SYSID = "mav.sysid";

    // default property values
    private final static String DEFAULT_ROCKBLOCK_URL = "https://core.rock7.com/rockblock/MT";
    private final static String DEFAULT_HTTP_CONTEXT = "/mo";
    private final static Integer DEFAULT_ROCKBLOCK_PORT = 5080;
    private final static Integer DEFAULT_RADIOROOM_PORT = 5060;
    private final static Integer DEFAULT_MAVLINK_PORT = 5760;
    private final static Integer DEFAULT_SHADOW_PORT = 5757;
    private final static Integer DEFAULT_QUEUE_SIZE = 1000;
    private final static Integer DEFAULT_HEARTBEAT_INT = 500; //ms
    private final static Short DEFAULT_AUTOPILOT = MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA;
    private final static Short DEFAULT_MAV_TYPE = MAV_TYPE.MAV_TYPE_GROUND_ROVER;
    private final static Integer DEFAULT_MAV_SYSID = 1;
    private final static String DEFAULT_SHADOW_CONNECTIONSTRING = "mongodb://localhost:27017";
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
    private Integer mavSysId = DEFAULT_MAV_SYSID;
    private String shadowConnectionString = DEFAULT_SHADOW_CONNECTIONSTRING;
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

        if (getProperty(PROP_SHADOW_CONNECTIONSTRING) != null)
            shadowConnectionString = getProperty(PROP_SHADOW_CONNECTIONSTRING);

        if (getProperty(PROP_QUEUE_SIZE) != null)
            queueSize = Integer.valueOf(getProperty(PROP_QUEUE_SIZE));

        if (getProperty(PROP_HEARTBEAT_INTERVAL) != null)
            heartbeatInterval = Integer.valueOf(getProperty(PROP_HEARTBEAT_INTERVAL));

        if (getProperty(PROP_ROCKBLOCK_URL) != null)
            rockblockUrl = getProperty(PROP_ROCKBLOCK_URL);

        if (getProperty(PROP_MAV_AUTOPILOT) != null)
            autopilot = Short.valueOf(getProperty(PROP_MAV_AUTOPILOT));
        
        if (getProperty(PROP_MAV_TYPE) != null)
            mavType = Short.valueOf(getProperty(PROP_MAV_TYPE));   
        
        if (getProperty(PROP_MAV_SYSID) != null)
            mavSysId = Integer.valueOf(getProperty(PROP_MAV_SYSID)); 

        imei = getProperty(PROP_ROCKBLOCK_IMEI);
        username = getProperty(PROP_ROCKBLOCK_USERNAME);
        password = getProperty(PROP_ROCKBLOCK_PASSWORD);
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

    public Integer getMavSystemId() {
        return mavSysId;
    }

    public Integer getRadioRoomPort() {
        return radioroomPort;
    }

    public String getShadowConnectionString() {
        return shadowConnectionString;
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
