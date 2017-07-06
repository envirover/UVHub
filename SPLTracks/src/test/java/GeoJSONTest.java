import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.emvirover.geojson.Feature;
import com.emvirover.geojson.FeatureCollection;
import com.emvirover.geojson.Point;

public class GeoJSONTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testFeatureCollection() throws JsonGenerationException, JsonMappingException, IOException {
        FeatureCollection features = new FeatureCollection();

        Feature feature = new Feature();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("prop1", "value");
        properties.put("prop2", 1234);
        feature.setProperties(properties);

        List<Double> coordinates = new ArrayList<Double>();
        coordinates.add(12.34);
        coordinates.add(56.78);
        feature.setGeometry(new Point(coordinates));

        features.getFeatures().add(feature);

        String geojson = mapper.writeValueAsString(features);

        System.out.println(geojson);
    }

}
