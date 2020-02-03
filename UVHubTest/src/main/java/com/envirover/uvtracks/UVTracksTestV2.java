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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import com.envirover.geojson.FeatureCollection;
import com.envirover.uvnet.mission.Plan;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

// UV Tracks v2 web service integration test
public class UVTracksTestV2 {

    private final static Logger logger = LogManager.getLogger(UVTracksTestV2.class);

    UVTracksClientV2 uvTracks = new UVTracksClientV2();

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetTracks() throws ClientProtocolException, URISyntaxException, IOException {
        logger.info("UVTracks TEST: GET /uvtracks/api/v2/tracks");
    
        FeatureCollection tracks = this.uvTracks.getTracks(null, null, null, null, null);

        assertEquals("FeatureCollection", tracks.getType());
    }

    @Test
    public void testGetState() throws ClientProtocolException, URISyntaxException, IOException {
        logger.info("UVTracks TEST: GET /uvtracks/api/v2/state");
    
        FeatureCollection tracks = this.uvTracks.getState(null);

        assertEquals("FeatureCollection", tracks.getType());
    }

    @Test
    public void testGetMissions() throws ClientProtocolException, URISyntaxException, IOException {
        logger.info("UVTracks TEST: GET /uvtracks/api/v2/missions");
    
        FeatureCollection missions = this.uvTracks.getMissions(null, null);

        assertEquals("FeatureCollection", missions.getType());
    }

    @Test
    public void testGetParameters() throws ClientProtocolException, URISyntaxException, IOException {
        logger.info("UVTracks TEST: GET /uvtracks/api/v2/parameters");
    
        Map<String, Double> parameters = this.uvTracks.getParameters(null);

        assert(parameters.size() > 0);
    }

}