/*
This file is part of SPLGroundControl application.

SPLGroundControl is a ground control proxy station for ArduPilot rovers with
RockBLOCK satellite communication.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLGroundControl is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Rock7MAVLink.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

public class Config {
    // Configuration properties
    private final String PROP_HTTP_PORT          = "http.port";
    private final String PROP_MAVLINK_PORT       = "mavlink.port";
    private final String PROP_ROCKBLOCK_IMEI     = "rockblock.imei";
    private final String PROP_ROCKBLOCK_USERNAME = "rockblock.username";
    private final String PROP_ROCKBLOCK_PASSWORD = "rockblock.password";

    // default property values
    private final String  DEFAULT_HTTP_CONTEXT = "/spl";
    private final Integer DEFAULT_HTTP_PORT = 8000;
    private final Integer DEFAULT_MAVLINK_PORT = 5760;

    private String  httpContext = DEFAULT_HTTP_CONTEXT;
    private Integer httpPort = DEFAULT_HTTP_PORT;
    private Integer mavlinkPort = DEFAULT_MAVLINK_PORT;
    private String  imei = null;
    private String  username = null;
    private String  password = null;

    public void init() throws IOException {
        init(null);
    }

    public void init(String[] args) throws IOException {
        Properties props = new Properties();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        InputStream in = null;
        try {
            in = loader.getResourceAsStream("app.properties");
            props.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        if (props.getProperty(PROP_HTTP_PORT) != null)
            httpPort = Integer.valueOf(props.getProperty(PROP_HTTP_PORT));

        if (props.getProperty(PROP_MAVLINK_PORT) != null)
            mavlinkPort = Integer.valueOf(props.getProperty(PROP_MAVLINK_PORT));

        imei = props.getProperty(PROP_ROCKBLOCK_IMEI);

        if (imei == null || imei.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format("Required configuration property ''{0}'' is not set.", PROP_ROCKBLOCK_IMEI));
        }

        username = props.getProperty(PROP_ROCKBLOCK_USERNAME);

        if (username == null || username.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format("Required configuration property ''{0}'' is not set.", PROP_ROCKBLOCK_USERNAME));
        }

        password = props.getProperty(PROP_ROCKBLOCK_PASSWORD);

        if (password == null || password.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format("Required configuration property ''{0}'' is not set.", PROP_ROCKBLOCK_PASSWORD));
        }
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public String getHttpContext() {
        return httpContext;
    }

    public Integer getMAVLinkPort() {
        return mavlinkPort;
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
}
