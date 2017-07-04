import java.io.IOException;
import java.text.ParseException;
//import java.util.Date;

import org.junit.Test;

import com.envirover.spl.stream.DynamoDBInputStream;
import com.envirover.spl.stream.MAVLinkRecord;

public class DynamoDBInputStreamTest {

    @Test
    public void testPostMobileOriginatedMessage() throws ParseException, IOException {

        DynamoDBInputStream stream = new DynamoDBInputStream("300234064280890", null, null);

        MAVLinkRecord record = null;
        while ((record = stream.readPacket()) != null) {
            System.out.println(record.getDeviceId());
        }

    }

}
