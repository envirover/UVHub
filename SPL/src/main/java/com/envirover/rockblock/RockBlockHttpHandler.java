
/*
This file is part of Rock7MAVLink.

Rock7MAVLink is MAVLink Proxy for RockBLOCK Web Services.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

Rock7MAVLink is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Rock7MAVLink is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Rock7MAVLink.  If not, see <http://www.gnu.org/licenses/>.
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
import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.envirover.mavlink.MAVLinkChannel;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public class RockBlockHttpHandler implements HttpHandler {

    private final static Logger logger = Logger.getLogger(RockBlockHttpHandler.class);

    private final MAVLinkChannel queue;
    private final String imei;

    /**
     * Constructor
     * 
     * @param queue MAVLink message queue
     */
    public RockBlockHttpHandler(MAVLinkChannel queue, String imei) {
        this.queue = queue;
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

            MAVLinkPacket packet = message.getPacket();

            if (packet != null) {
                logger.info(MessageFormat.format("MO message received (msgid={0})", packet.msgid));

                if (message.imei.equalsIgnoreCase(imei))
                    queue.sendMessage(packet);
                else
                    logger.warn(MessageFormat.format("Invalid IMEI ''{0}''.", message.imei));
            }

            //Send response
            String response = "";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (DecoderException e) {
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    class IridiumMessage {
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
