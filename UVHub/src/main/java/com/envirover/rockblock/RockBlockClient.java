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
import org.apache.logging.log4j.Level;

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

        String data = Hex.encodeHexString(CustomEncoder.encodePacket(packet));

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair(PARAM_IMEI, imei));
        params.add(new BasicNameValuePair(PARAM_USERNAME, username));
        params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
        params.add(new BasicNameValuePair(PARAM_DATA, data));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpclient.execute(httppost);

        HttpEntity entity = response.getEntity();

        String responseString = null;

        if (entity != null) {
            try (InputStream responseStream = entity.getContent()) {
            	responseString = IOUtils.toString(responseStream);
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
