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

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;

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

    public RockBlockClient(String imei, String username, String password) {
       this(imei, username, password, "https://core.rock7.com/rockblock/MT");
    }

    public RockBlockClient(String imei, String username, String password, String serviceURL) {
        this.imei = imei;
        this.username = username;
        this.password = password;
        this.serviceURL = serviceURL;
    }

    public void sendMessage(MAVLinkPacket packet) throws ClientProtocolException, IOException {
        HttpPost httppost = new HttpPost(serviceURL);

        String data = Hex.encodeHexString(packet.encodePacket());
        System.out.printf("Rock7 service message: %s", data);
        System.out.println();

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
                System.out.printf("Rock7 service response: %d (%s)", response.getStatusLine().getStatusCode(), responseString);
                System.out.println();
            } finally {
                responseStream.close();
            }
        }

        if (responseString == null || responseString.startsWith("FAILED")) {
            throw new IOException(String.format("Failed to post message to RockBLOCK API. %s", responseString));
        }
    }

    @Override
    public MAVLinkPacket receiveMessage() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }
}
