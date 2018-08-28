
import java.io.IOException;
import java.text.ParseException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.envirover.geojson.Feature;
import com.envirover.geojson.FeatureCollection;
import com.envirover.geojson.GeometryType;
import com.envirover.spl.tracks.UVShadowView;



public class MAVLinkMessagesTableTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final int MAVLINK_MSG_ID_HIGH_LATENCY = 234;
    
    @Test
    public void testQueryMAVLinkRecords() throws ParseException, IOException {
        UVShadowView stream = new UVShadowView();
  
        FeatureCollection records = stream.queryMessages(1, MAVLINK_MSG_ID_HIGH_LATENCY, GeometryType.Point, null, null, 100);
       
        for (Feature f : records.getFeatures()) {
        	System.out.println(mapper.writeValueAsString(f));
        }
    }
    
}
