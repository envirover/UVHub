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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.envirover.geojson.Feature;
import com.envirover.geojson.FeatureCollection;
import com.envirover.geojson.LineString;
import com.envirover.geojson.Point;

public class GeoJSONTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testFeatureCollection() throws JsonGenerationException, JsonMappingException, IOException {
        FeatureCollection features = new FeatureCollection();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("prop1", "value");
        properties.put("prop2", 1234);

        Feature pointFeature = new Feature(new Point(12.34, 56.78, 90.00), properties);

        features.getFeatures().add(pointFeature);


        Collection<Collection<Double>>  coordinates = new ArrayList<Collection<Double>>();
        Collection<Double> point1 = new ArrayList<Double>();
        point1.add(1.0);
        point1.add(2.0);
        point1.add(3.0);
        coordinates.add(point1);
        Collection<Double> point2 = new ArrayList<Double>();
        point2.add(1.0);
        point2.add(2.0);
        point2.add(3.0);
        coordinates.add(point2);

        Feature lineFeature = new Feature(new LineString(coordinates), properties);
        
        features.getFeatures().add(lineFeature);

        String geojson = mapper.writeValueAsString(features);

        System.out.println(geojson);
    }

}
