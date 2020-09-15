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

package com.envirover.uvtracks;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import com.envirover.geojson.FeatureCollection;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

// UV Tracks v2 web service integration test
public class UVTracksV2Test {

    private final static Logger logger = LogManager.getLogger(UVTracksV2Test.class);

    UVTracksV2Client uvTracks = new UVTracksV2Client();

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
