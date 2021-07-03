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

package com.envirover.rockblock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;

import com.envirover.uvhub.MOMessageHandler;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
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

    public final static String CHANNEL_NAME = "isbd";

    private final static Logger logger = LogManager.getLogger(RockBlockHttpHandler.class);

    private final MOMessageHandler handler;
    private final String imei;

    /**
     * Constructs instance of RockBlockHttpHandler.
     * 
     * @param handler Mobile-originated message handler
     * @param imei RockBLOCK imei
     */
    public RockBlockHttpHandler(MOMessageHandler handler, String imei) {
        this.handler = handler;
        this.imei = imei;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            InputStream is = t.getRequestBody();
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, "UTF-8");
            String theString = writer.toString();

            List<NameValuePair> params = URLEncodedUtils.parse(theString, StandardCharsets.UTF_8);

            IridiumMessage message = new IridiumMessage(params);

            logger.debug(message);

            if (message.data == null || message.data.isEmpty()) {
                logger.info(MessageFormat.format("Empty MO message received ''{0}''.", message.toString()));
            } else if (message.imei != null && message.imei.equalsIgnoreCase(imei)) {
                MAVLinkPacket packet = message.getPacket();

                if (packet != null) {
                    handler.handleMessage(packet, CHANNEL_NAME);

                    MAVLinkLogger.log(Level.INFO, "MO", packet);
                } else {
                    logger.warn(MessageFormat.format("Invalid MAVLink message ''{0}''.", message.toString()));
                }
            } else {
                logger.warn(MessageFormat.format("Invalid IMEI ''{0}''.", message.imei));
            }

            // Send response
            String response = "OK";
            t.sendResponseHeaders(200, response.length());

            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        private String imei = "";
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
            return CustomEncoder.decodePacket(Hex.decodeHex(data.toCharArray()));
        }

        @Override
        public String toString() {
            return String.format("%s %s %s %s %s %s %s", imei, momsn, transmitTime, iridiumLatitude, iridiumLongitude,
                    iridiumCep, data);
        }
    }

}
