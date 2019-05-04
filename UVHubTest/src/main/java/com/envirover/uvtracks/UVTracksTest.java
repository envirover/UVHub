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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import com.envirover.geojson.FeatureCollection;
import com.envirover.uvnet.mission.Plan;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

// UV Tracks web service integration test
public class UVTracksTest {

    UVTracksClient uvTracks = new UVTracksClient();

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetTracks() throws ClientProtocolException, URISyntaxException, IOException {
        System.out.println("UVTracksTest: GET /uvtracks/api/v1/tracks");
    
        FeatureCollection tracks = this.uvTracks.getTracks(null, null, null, null);

        assertEquals("FeatureCollection", tracks.getType());
    }

    @Test
    public void testGetMissions() throws ClientProtocolException, URISyntaxException, IOException {
        System.out.println("UVTracksTest: GET /uvtracks/api/v1/missions");
    
        Plan missions = this.uvTracks.getMissions(null);

        assertEquals("Plan", missions.getFileType());
    }
}