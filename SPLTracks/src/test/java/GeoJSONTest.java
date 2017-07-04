import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.emvirover.geojson.Feature;
import com.emvirover.geojson.FeatureCollection;

public class GeoJSONTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testFeatureCollection() throws JsonGenerationException, JsonMappingException, IOException {
        FeatureCollection features = new FeatureCollection();

        Feature feature = new Feature();
        feature.getProperties().put("prop1", "value");
        feature.getProperties().put("prop2", 1234);
        List<Double> coordinates = new ArrayList<Double>();
        coordinates.add(12.34);
        coordinates.add(56.78);
        feature.getGeometry().setCoordinates(coordinates);
        features.getFeatures().add(feature);

        String geojson = mapper.writeValueAsString(features);

        System.out.println(geojson);
    }

}
