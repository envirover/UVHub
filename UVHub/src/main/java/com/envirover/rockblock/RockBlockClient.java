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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Level;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkLogger;

/**
 * Client class used to send mobile-terminated MAVLink messages to RockBLOCK 
 * Web Services API.
 * 
 * @author Pavel Bobov
 *
 */
public class RockBlockClient implements MAVLinkChannel {
    // HTTP POST request parameters

    // IMEI of the RockBLOCK
    private final static String PARAM_IMEI = "imei"; 
    // Rock 7 Core username
    private final static String PARAM_USERNAME = "username";
    // Rock 7 Core password
    private final static String PARAM_PASSWORD = "password";
    // Hex-encoded message.
    private final static String PARAM_DATA = "data"; 

    private final HttpClient httpclient = HttpClients.createDefault();

    private final String imei;
    private final String username;
    private final String password; 
    private final String serviceURL;

    /**
     * Constructs instance of RockBlockClient
     * 
     * @param imei The unique IMEI of RockBLOCK
     * @param username Rock 7 Core username
     * @param password Rock 7 Core password
     * @param serviceURL RockBLOCK Web Services URL
     */
    public RockBlockClient(String imei, String username, String password, String serviceURL) {
        this.imei = imei;
        this.username = username;
        this.password = password;
        this.serviceURL = serviceURL;
    }

    /**
     * Sends MAVLink packet to RockBLOCK.
     * 
     * @param packet MAVLink packet to send.
     */
    public void sendMessage(MAVLinkPacket packet) throws ClientProtocolException, IOException {
        if (packet == null)
            return;

        HttpPost httppost = new HttpPost(serviceURL);

        String data = Hex.encodeHexString(packet.encodePacket());

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair(PARAM_IMEI, imei));
        params.add(new BasicNameValuePair(PARAM_USERNAME, username));
        params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
        params.add(new BasicNameValuePair(PARAM_DATA, data));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpclient.execute(httppost);

        HttpEntity entity = response.getEntity();

        String responseString = null;

        if (entity != null) {
            InputStream responseStream = entity.getContent();
            try {
                responseString = IOUtils.toString(responseStream);
            } finally {
                responseStream.close();
            }
        }

        if (responseString == null || responseString.startsWith("FAILED")) {
            throw new IOException(String.format("Failed to post message to RockBLOCK API. %s", responseString));
        }

        MAVLinkLogger.log(Level.INFO, "MT", packet);
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

}
