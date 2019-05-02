/*
 * Envirover confidential
 * 
 *  [2019] Envirover
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

package com.envirover.uvtracks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.envirover.geojson.FeatureCollection;
import com.envirover.uvnet.mission.Plan;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Client class for UV Tracks web service.
 */
class UVTracksClient {
    // Environment variables
    private static final String UVTRACKS_PROTOCOL = "UVTRACKS_PROTOCOL";
    private static final String UVTRACKS_HOSTNAME = "UVTRACKS_HOSTNAME";
    private static final String UVTRACKS_PORT = "UVTRACKS_PORT";

    private String protocol = "http";
    private String hostname = "localhost";
    private int port = 8080;

    private static final ObjectMapper mapper = new ObjectMapper();

    private HttpClient httpclient = HttpClients.createDefault();

    /**
     * Constructs UVTracksClient.
     */
    public UVTracksClient() {
        if (System.getenv(UVTRACKS_PROTOCOL) != null) {
            this.protocol = System.getenv(UVTRACKS_PROTOCOL);
        }

        if (System.getenv(UVTRACKS_HOSTNAME) != null) {
            this.hostname = System.getenv(UVTRACKS_HOSTNAME);
        }

        if (System.getenv(UVTRACKS_PORT) != null) {
            this.port = Integer.parseInt(System.getenv(UVTRACKS_PORT));
        }
    }

    /**
     * Constructs UVTracksClient with the specified URI attributes.
     * 
     * @param protocol UV Tracks service protocol (http|https)
     * @param hostname UVTracks service hostname
     * @param port UVTracks service port
     */
    public UVTracksClient(String protocol, String hostname, int port) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.port = port;
    }
    
    /**
     * Returns reported positions and state information of the vehicle in GeoJSON format.
     * 
     * @param sysId System ID. The default value is 1.
     * @param startTime Start of time range query as UNIX epoch time.
     * @param endTime End of time range query as UNIX epoch time.
     * @param top Maximum number of returned entries. The default value is 100.
     * @return reported positions and state information of the vehicle in GeoJSON format.
     * @throws URISyntaxException invalid URI syntax
     * @throws ClientProtocolException in case of a problem or the connection was aborted
     * @throws IOException in case of an http protocol error
     */
    public FeatureCollection getTracks(Integer sysId, Long startTime, Long endTime, Integer top)
            throws URISyntaxException, ClientProtocolException, IOException {

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();

        if (sysId != null) {
            parameters.add(new BasicNameValuePair("sysid", Integer.toString(sysId)));
        }

        if (startTime != null) {
            parameters.add(new BasicNameValuePair("startTime", Long.toString(startTime)));
        }

        if (endTime != null) {
            parameters.add(new BasicNameValuePair("endTime", Long.toString(endTime)));
        }

        if (top != null) {
            parameters.add(new BasicNameValuePair("top", Integer.toString(top)));
        }

        URI uri = new URIBuilder().setScheme(this.protocol).setHost(this.hostname).setPort(this.port)
                .setPath("/uvtracks/api/v1/tracks").setParameters(parameters).build();

        HttpGet request = new HttpGet(uri);

        HttpResponse response = httpclient.execute(request);

        String json = "";

        try (InputStream content = response.getEntity().getContent()) {
            json = IOUtils.toString(content, "UTF-8");
        }

        return mapper.readValue(json, FeatureCollection.class);
    }

    /**
     * Returns mission plan of the vehicle.
     * 
     * @param sysId System ID. The default value is 1.
     * @return mission plan of the vehicle.
     * @throws URISyntaxException invalid URI syntax
     * @throws ClientProtocolException in case of a problem or the connection was aborted
     * @throws IOException in case of an http protocol error
     */
    public Plan getMissions(Integer sysId) throws URISyntaxException, ClientProtocolException, IOException {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();

        if (sysId != null) {
            parameters.add(new BasicNameValuePair("sysid", Integer.toString(sysId)));
        }

        URI uri = new URIBuilder().setScheme(this.protocol).setHost(this.hostname).setPort(this.port)
                .setPath("/uvtracks/api/v1/missions").setParameters(parameters).build();

        HttpGet request = new HttpGet(uri);

        HttpResponse response = httpclient.execute(request);

        String json = "";

        try (InputStream content = response.getEntity().getContent()) {
            json = IOUtils.toString(content, "UTF-8");
        }

        return mapper.readValue(json, Plan.class);
    }

}
