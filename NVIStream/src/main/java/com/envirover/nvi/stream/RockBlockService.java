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

package com.envirover.nvi.stream;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;

/**
 * Saves MAVLink messages received from Rock7Core web services to a DynamoDB table.
 * 
 */

@Path("/rockblock")
public class RockBlockService {
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

   // private final MAVLinkOutputStream stream;

    private static final Logger logger = Logger.getLogger(RockBlockService.class.getName());

    public RockBlockService() throws IOException {
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String info() {
        return "POST mobile-originated MAVLink messages to this URL.";
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public String postMobileOriginatedMessage(@FormParam(PARAM_IMEI) String imei, @FormParam(PARAM_MOMSN) String momsn,
            @FormParam(PARAM_TRANSMIT_TIME) String transmitTime,
            @FormParam(PARAM_IRIDIUM_LATITUDE) String iridiumLatitude,
            @FormParam(PARAM_IRIDIUM_LONGITUDE) String iridiumLongitude,
            @FormParam(PARAM_IRIDIUM_CEP) String iridiumCep, @FormParam(PARAM_DATA) String data) {

        Date time = new Date();

        try {
            //Time stamp like '17-04-03 02:11:35'
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            time = sdf.parse(transmitTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            MAVLinkPacket packet = getPacket(data);

            if (packet != null) {
                MAVLinkOutputStream stream = MAVLinkOutputStreamFactory.getMAVLinkOutputStream();

                stream.writePacket(imei, time, packet);
            } else {
                logger.warning("Invalid MAVLink message received: " + data);
            }
        } catch (DecoderException e) {
            logger.severe(e.toString());
        } catch (IOException e) {
            logger.severe(e.toString());
        }

        return "";
    }

    private MAVLinkPacket getPacket(String data) throws DecoderException {
        if (data == null || data.isEmpty()) {
            return null;
        }

        Parser parser = new Parser();

        MAVLinkPacket packet = null;

        for (byte b : Hex.decodeHex(data.toCharArray())) {
            packet = parser.mavlink_parse_char(b & 0xFF);
        }

        return packet;
    }

}
