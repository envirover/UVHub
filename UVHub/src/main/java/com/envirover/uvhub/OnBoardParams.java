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

package com.envirover.uvhub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.common.msg_param_value;
import com.MAVLink.enums.MAV_PARAM_TYPE;
import com.envirover.uvhub.Config;

/**
 * Loads on-board parameters form file in QGroundControl parameters file format.
 * 
 * @author Pavel Bobov
 *
 */
public class OnBoardParams {

    private final static String DEFAULT_PARAMS_FILE = "default.params";
    private final static String PARAMS_FILE_FORMAT = "mavtype_%d_autopilot_%d.params";

    public final static String HL_REPORT_PERIOD_PARAM = "HL_REPORT_PERIOD";

    private final static Logger logger = LogManager.getLogger(UVHubDaemon.class);

    /**
     * Returns list of default on-board parameters for the specified MAV type and
     * autopilot class.
     * 
     * @param mavType MAV_TYPE
     * @param sysId system ID
     * @param autopilot MAV_AUTOPILOT
     * @return list of default on-buard parameters
     * @throws IOException on I/O error
     */
    public static List<msg_param_value> getDefaultParams(int mavType, int sysId, int autopilot) throws IOException {
        // Load on-board parameters from file.

        try (InputStream paramsStream = getParamStream(mavType, autopilot)) {
            if (paramsStream != null) {
                return loadParams(sysId, paramsStream);
            } else {
                logger.warn("File with default parameters values not found.");
            }
        }

        return null;
    }

    public static Set<String> getReadOnlyParamIds() {
        Set<String> paramIds = new HashSet<String>();
        paramIds.add("SERIAL0_PROTOCOL");
        return paramIds;
    }

    private static InputStream getParamStream(int mavType, int autopilot) {
        ClassLoader loader = UVHubDaemon.class.getClassLoader();

        String paramFile = String.format(PARAMS_FILE_FORMAT, mavType, autopilot);

        InputStream paramsStream = loader.getResourceAsStream(paramFile);

        if (paramsStream != null) {
            logger.info(String.format("Parameters file '%s' found.", paramFile));
            return paramsStream;
        }

        paramsStream = loader.getResourceAsStream(DEFAULT_PARAMS_FILE);

        if (paramsStream != null) {
            logger.info(String.format("Parameters file '%s' found.", DEFAULT_PARAMS_FILE));
            return paramsStream;
        }

        return null;
    }

    /**
     * Loads parameters from the specified input stream and adds UVHub-specific
     * HL_REPORT_PERIOD parameter to the list.
     * 
     * @param sysId system Id
     * @param stream input stream
     * @return list of on-board parameters
     * @throws IOException on I/O error
     */
    private static List<msg_param_value> loadParams(int sysId, InputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Invalid parameters stream.");
        }

        List<msg_param_value> params = new ArrayList<msg_param_value>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

        String str;
        int index = 0;
        boolean hlReportPeriodParamFound = false;

        while ((str = reader.readLine()) != null) {
            if (!str.isEmpty() && !str.startsWith("#")) {
                String[] tokens = str.split("\t");
                if (tokens.length >= 5) {
                    msg_param_value param = new msg_param_value();
                    param.sysid = sysId;
                    param.compid = Integer.parseInt(tokens[1]);
                    param.setParam_Id(tokens[2].trim());
                    param.param_index = index;
                    param.param_value = Float.parseFloat(tokens[3]);
                    param.param_type = Short.parseShort(tokens[4]);
                    params.add(index, param);
                    index++;
                    if (HL_REPORT_PERIOD_PARAM.equals(param.getParam_Id())) {
                        hlReportPeriodParamFound = true;
                    }
                }
            }
        }

        if (!hlReportPeriodParamFound) {
            // Add HL_REPORT_PERIOD parameter
            msg_param_value param = new msg_param_value();
            param.sysid = sysId;
            param.compid = 190;
            param.setParam_Id(HL_REPORT_PERIOD_PARAM);
            param.param_index = index;
            param.param_value = Config.getInstance().getDefaultHLReportPeriod();
            param.param_type = MAV_PARAM_TYPE.MAV_PARAM_TYPE_REAL32;
            params.add(index, param);
            index++;
        }

        // Set param_count for all the parameters.
        for (int i = 0; i < index; i++) {
            params.get(i).param_count = index;
        }

        return params;
    }

}
