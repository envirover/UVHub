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
import java.util.Properties;

public class Config {
    private Properties props = new Properties();

    // Configuration properties
    private final String PROP_HTTP_PORT = "http.port";
    private final String PROP_MAVLINK_PORT = "mavlink.port";

    // default property values
    private final Integer DEFAULT_HTTP_PORT = 8000;
    private final Integer DEFAULT_MAVLINK_PORT = 5760;

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
    }

    public Integer getHttpPort() {
        String str = props.getProperty(PROP_HTTP_PORT);

        if (str == null)
            return DEFAULT_HTTP_PORT;

        return Integer.valueOf(str);
    }
    
    public String getHtppContext() {
        return "/test";
    }

    public Integer getMAVLinkPort() {
        String str = props.getProperty(PROP_MAVLINK_PORT);

        if (str == null)
            return DEFAULT_MAVLINK_PORT;

        return Integer.valueOf(str);
    }

}
