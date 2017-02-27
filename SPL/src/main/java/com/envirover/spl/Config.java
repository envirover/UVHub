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
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/*
 * Configuration properties
 */
public class Config {
    private final static String CONFIG_PROPERTIES_FILE  = "app.properties"; 
    
    // Configuration properties
    private final static String PROP_QUEUE_SIZE         = "queue.size";
    private final static String PROP_HTTP_PORT          = "http.port";
    private final static String PROP_MAVLINK_PORT       = "mavlink.port";
    private final static String PROP_ROCKBLOCK_URL      = "rockblock.url";
    private final static String PROP_ROCKBLOCK_IMEI     = "rockblock.imei";
    private final static String PROP_ROCKBLOCK_USERNAME = "rockblock.username";
    private final static String PROP_ROCKBLOCK_PASSWORD = "rockblock.password";

    //CLI options
    private final static String CLI_OPTION_HELP         = "h";
    private final static String CLI_OPTION_IMEI         = "i";
    private final static String CLI_OPTION_USERNAME     = "u";
    private final static String CLI_OPTION_PASSWORD     = "p";

    // default property values
    private final static String  DEFAULT_ROCKBLOCK_URL  = "https://core.rock7.com/rockblock/MT";
    private final static String  DEFAULT_HTTP_CONTEXT   = "/mo";
    private final static Integer DEFAULT_HTTP_PORT      = 8000;
    private final static Integer DEFAULT_MAVLINK_PORT   = 5760;
    private final static Integer DEFAULT_QUEUE_SIZE     = 10;

    private String  rockblockUrl = DEFAULT_ROCKBLOCK_URL;
    private String  httpContext  = DEFAULT_HTTP_CONTEXT;
    private Integer httpPort     = DEFAULT_HTTP_PORT;
    private Integer mavlinkPort  = DEFAULT_MAVLINK_PORT;
    private Integer queueSize    = DEFAULT_QUEUE_SIZE;
    private String  imei         = null;
    private String  username     = null;
    private String  password     = null;

    public void init() throws IOException, ParseException {
        init(null);
    }

    public boolean init(String[] args) throws IOException, ParseException {
        Options options = new Options();
        options.addOption(CLI_OPTION_HELP, false, "help");
        options.addOption(CLI_OPTION_IMEI, true, "IMEI of RockBLOCK");
        options.addOption(CLI_OPTION_USERNAME, true, "Rock 7 Core username");
        options.addOption(CLI_OPTION_PASSWORD, true, "Rock 7 Core password");
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse( options, args);

        if (cmd.hasOption(CLI_OPTION_HELP)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "spl", options );
            return false;
        }

        Properties props = new Properties();

        InputStream in = null;
        try {
            ClassLoader loader = Config.class.getClassLoader();
            in = loader.getResourceAsStream(CONFIG_PROPERTIES_FILE);
            if (in != null)
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

        if (props.getProperty(PROP_QUEUE_SIZE) != null)
            queueSize = Integer.valueOf(props.getProperty(PROP_QUEUE_SIZE));

        if (props.getProperty(PROP_ROCKBLOCK_URL) != null)
            rockblockUrl = props.getProperty(PROP_ROCKBLOCK_URL);

        imei = cmd.getOptionValue(CLI_OPTION_IMEI, props.getProperty(PROP_ROCKBLOCK_IMEI)); 

        if (imei == null || imei.isEmpty()) {
            System.out.println(MessageFormat.format("Required configuration property ''{0}'' is not set.", PROP_ROCKBLOCK_IMEI));
            return false;
        }

        username = cmd.getOptionValue(CLI_OPTION_USERNAME, props.getProperty(PROP_ROCKBLOCK_USERNAME));

        if (username == null || username.isEmpty()) {
            System.out.println(MessageFormat.format("Required configuration property ''{0}'' is not set.", PROP_ROCKBLOCK_USERNAME));
            return false;
        }

        password = cmd.getOptionValue(CLI_OPTION_PASSWORD, props.getProperty(PROP_ROCKBLOCK_PASSWORD));

        if (password == null || password.isEmpty()) {
            System.out.println(MessageFormat.format("Required configuration property ''{0}'' is not set.", PROP_ROCKBLOCK_PASSWORD));
            return false;
        }

        return true;
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

    public Integer getQueueSize() {
        return queueSize;
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

}
