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

package com.envirover.spl.uvtracks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides access to configuration properties specified in environment variables, 
 * in app.properties file in the classpath, or in command line parameters.
 * 
 * @author Pavel Bobov
 */
public class Config {
    private final static String CONFIG_PROPERTIES_FILE = "app.properties";

    // Configuration properties
    private final static String PROP_SHADOW_CONNECTIONSTRING = "shadow.connectionstring";
    private final static String PROP_MAV_SYSID = "mav.sysid";

    // default property values
    private final static String DEFAULT_SHADOW_CONNECTIONSTRING = "mongodb://localhost:27017";
    private final static Integer DEFAULT_MAV_SYSID = 1;

    private String shadowConnectionString = DEFAULT_SHADOW_CONNECTIONSTRING;
    private Integer mavSysId = DEFAULT_MAV_SYSID;

    private Properties props = new Properties();

    private static final Config config = new Config();
   
    private Config() {
    }

    public static Config getInstance() {
        return config;
    }

    /**
     * Loads UV Trucks configuration properties from app.properties file and 
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
    
        if (getProperty(PROP_SHADOW_CONNECTIONSTRING) != null)
            shadowConnectionString = getProperty(PROP_SHADOW_CONNECTIONSTRING);

        if (getProperty(PROP_MAV_SYSID) != null)
            mavSysId = Integer.valueOf(getProperty(PROP_MAV_SYSID));    
    }

    public String getShadowConnectionString() {
        return shadowConnectionString;
    }

    public Integer getMavSystemId() {
        return mavSysId;
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
