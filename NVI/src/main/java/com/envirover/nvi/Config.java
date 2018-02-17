/*
 * Envirover confidential
 * 
 *  [2017] Envirover
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

package com.envirover.nvi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_TYPE;

/**
 * Provides access to configuration properties specified in app.properties file
 * in the classpath or in command line parameters. 
 */
public class Config {
    private final static String CONFIG_PROPERTIES_FILE  = "app.properties"; 
    
    // Configuration properties
    private final static String PROP_RADIOROOM_PORT     = "radioroom.port";
    private final static String PROP_QUEUE_SIZE         = "queue.size";
    private final static String PROP_MAVLINK_PORT       = "mavlink.port";
    private final static String PROP_WS_PORT            = "ws.port";
    private final static String PROP_HEARTBEAT_INTERVAL = "heartbeat.interval";
    private final static String PROP_MAV_AUTOPILOT      = "mav.autopilot";
    private final static String PROP_MAV_TYPE           = "mav.type";

    //CLI options
    private final static String CLI_OPTION_HELP         = "h";
    private final static String CLI_OPTION_AUTOPILOT    = "a";
    private final static String CLI_OPTION_MAV_TYPE     = "t";

    // default property values
    private final static Integer DEFAULT_RADIOROOM_PORT = 5060;
    private final static Integer DEFAULT_MAVLINK_PORT   = 5760;
    private final static Integer DEFAULT_WS_PORT        = 8000;
    private final static Integer DEFAULT_QUEUE_SIZE     = 1000;
    private final static Integer DEFAULT_HEARTBEAT_INT  = 1000;
    private final static Short   DEFAULT_AUTOPILOT      = MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA;
    private final static Short   DEFAULT_MAV_TYPE       = MAV_TYPE.MAV_TYPE_GROUND_ROVER;

    private Integer radioroomPort     = DEFAULT_RADIOROOM_PORT;
    private Integer mavlinkPort       = DEFAULT_MAVLINK_PORT;
    private Integer wsPort            = DEFAULT_WS_PORT;
    private Integer queueSize         = DEFAULT_QUEUE_SIZE;
    private Integer heartbeatInterval = DEFAULT_HEARTBEAT_INT;
    private Short   autopilot         = DEFAULT_AUTOPILOT;
    private Short   mavType           = DEFAULT_MAV_TYPE;

    static Config config = new Config();

    private Config() {
    }

    public static Config getInstance() {
        return config;
    }

    public void init() throws IOException, ParseException {
        init(null);
    }

    public boolean init(String[] args) throws IOException, ParseException {
        Options options = new Options();
        options.addOption(CLI_OPTION_HELP, false, "help");
        options.addOption(CLI_OPTION_AUTOPILOT, true, "Autopilot code");
        options.addOption(CLI_OPTION_MAV_TYPE, true, "MAV type");
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse( options, args);

        if (cmd.hasOption(CLI_OPTION_HELP)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("spl", options);
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

        if (props.getProperty(PROP_RADIOROOM_PORT) != null)
            radioroomPort = Integer.valueOf(props.getProperty(PROP_RADIOROOM_PORT));

        if (props.getProperty(PROP_MAVLINK_PORT) != null)
            mavlinkPort = Integer.valueOf(props.getProperty(PROP_MAVLINK_PORT));

        if (props.getProperty(PROP_WS_PORT) != null)
            wsPort = Integer.valueOf(props.getProperty(PROP_WS_PORT));

        if (props.getProperty(PROP_QUEUE_SIZE) != null)
            queueSize = Integer.valueOf(props.getProperty(PROP_QUEUE_SIZE));

        if (props.getProperty(PROP_HEARTBEAT_INTERVAL) != null)
            heartbeatInterval = Integer.valueOf(props.getProperty(PROP_HEARTBEAT_INTERVAL));

        autopilot = Short.valueOf(cmd.getOptionValue(CLI_OPTION_AUTOPILOT, props.getProperty(PROP_MAV_AUTOPILOT, DEFAULT_AUTOPILOT.toString())));

        mavType = Short.valueOf(cmd.getOptionValue(CLI_OPTION_MAV_TYPE, props.getProperty(PROP_MAV_TYPE, DEFAULT_MAV_TYPE.toString())));

        return true;
    }

    public Integer getMAVLinkPort() {
        return mavlinkPort;
    }

    public Integer getWSPort() {
        return wsPort;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
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
}
