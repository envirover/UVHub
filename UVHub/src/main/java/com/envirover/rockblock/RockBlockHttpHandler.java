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

package com.envirover.rockblock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkLogger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handles HTTP requests with messages sent by RockBLOCK.
 *
 * @author PAvel Bobov
 */
@SuppressWarnings("restriction")
public class RockBlockHttpHandler implements HttpHandler {

    private final static Logger logger = Logger.getLogger(RockBlockHttpHandler.class);

    private final MAVLinkChannel dst;
    private final String imei;

    /**
     * Constructs instance of RockBlockHttpHandler.
     * 
     * @param dst MAVLink message handler
     * @param imei RockBLOCK imei
     */
    public RockBlockHttpHandler(MAVLinkChannel dst, String imei) {
        this.dst = dst;
        this.imei = imei;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            InputStream is = t.getRequestBody();
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, "UTF-8");
            String theString = writer.toString();

            List<NameValuePair> params = URLEncodedUtils.parse(theString, Charset.forName("UTF-8"));

            IridiumMessage message = new IridiumMessage(params);

            logger.debug(message);

            if (message.data == null || message.data.isEmpty()) {
                logger.info(MessageFormat.format("Empty MO message received ''{0}''.", message.toString()));
            } else if (message.imei.equalsIgnoreCase(imei)) {
                MAVLinkPacket packet = message.getPacket();
    
                if (packet != null) {
                    dst.sendMessage(packet);
                    
                    MAVLinkLogger.log(Level.INFO, "MO", packet);
                } else {
                    logger.warn(MessageFormat.format("Invalid MAVLink message ''{0}''.", message.toString()));
                }
            } else {
                logger.warn(MessageFormat.format("Invalid IMEI ''{0}''.", message.imei));
            }

            //Send response
            String response = "";
            t.sendResponseHeaders(200, response.length());
            
            try (OutputStream os = t.getResponseBody()) {
            	os.write(response.getBytes("UTF-8"));
            }
        } catch (DecoderException e) {
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    static class IridiumMessage {
        // HTTP POST request parameters

        // IMEI of the RockBLOCK
        private final static String PARAM_IMEI = "imei"; 
        // Message Sequence Number
        private final static String PARAM_MOMSN = "momsn"; 
        // UTC date & time
        private final static String PARAM_TRANSMIT_TIME = "transmit_time"; 
        // Approximate latitude of the RockBLOCK
        private final static String PARAM_IRIDIUM_LATITUDE = "iridium_latitude"; 
        // Approximate longitude of the RockBLOCK
        private final static String PARAM_IRIDIUM_LONGITUDE = "iridium_longitude";
        // Estimate of the accuracy (in km) of the position
        private final static String PARAM_IRIDIUM_CEP = "iridium_cep"; 
        // Hex-encoded message.
        private final static String PARAM_DATA = "data"; 

        private String imei = null;
        private String momsn = null;
        private String transmitTime = null;
        private String iridiumLatitude = null;
        private String iridiumLongitude = null;
        private String iridiumCep = null;
        private String data = null;

        public IridiumMessage(List<NameValuePair> params) {
            for (NameValuePair param : params) {
                switch (param.getName()) {
                    case PARAM_IMEI:
                        imei = param.getValue();
                        break;
                    case PARAM_MOMSN:
                        momsn = param.getValue();
                        break;
                    case PARAM_TRANSMIT_TIME:
                        transmitTime = param.getValue();
                        break;
                    case PARAM_IRIDIUM_LATITUDE:
                        iridiumLatitude = param.getValue();
                        break;
                    case PARAM_IRIDIUM_LONGITUDE:
                        iridiumLongitude = param.getValue();
                        break;
                    case PARAM_IRIDIUM_CEP:
                        iridiumCep = param.getValue();
                        break;
                    case PARAM_DATA:
                        data = param.getValue();
                        break;
                }
            }
        }

        public MAVLinkPacket getPacket() throws DecoderException {
            Parser parser = new Parser();
            MAVLinkPacket packet = null;

            for (byte b : Hex.decodeHex(data.toCharArray())) {
                packet = parser.mavlink_parse_char(b & 0xFF);
            }

            return packet;
        }

        @Override
        public String toString() {
            return String.format("%s %s %s %s %s %s %s",
                                 imei, momsn, transmitTime, iridiumLatitude,
                                 iridiumLongitude, iridiumCep, data);
        }
    }

}
