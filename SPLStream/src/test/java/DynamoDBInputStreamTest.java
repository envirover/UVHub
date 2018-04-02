import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_high_latency;
import com.envirover.spl.stream.DynamoDBOutputStream;

public class DynamoDBInputStreamTest {

//    @Test
//    public void testPostMobileOriginatedMessage() throws ParseException, IOException {
//        DynamoDBOutputStream stream = null;
//        try {
//            stream = new DynamoDBOutputStream();
//            stream.open();
//            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
//            Date date = new Date();
//            String transmitTime = dateFormat.format(date); //2016/11/16 12:08:43
//            stream.writePacket("012345678901234", "momsn", transmitTime, "-48.8", "48.8", "iridiumCep", getSamplePacket());
//        } finally {
//            stream.close();
//        }
//    }

    private MAVLinkPacket getSamplePacket() {
        msg_high_latency msg = new msg_high_latency();
        msg.latitude = 523868; //FIXME ES only accepts WGS 84. What coord ref MAVLink pushes latitude/longitude?
        msg.longitude = 2938;

//I cannot set this ones!
//        msg.sysid = 1;
//        msg.compid = 2;
//        msg.msgid = 5;
        
        msg.custom_mode = 5;
        msg.roll = 5;
        msg.pitch = 5;
        msg.heading = 5;
        msg.heading_sp = 5;
        msg.altitude_amsl = 5;
        msg.altitude_sp = 5;
        msg.wp_distance = 5;
        msg.base_mode = 5;
        msg.landed_state = 5;
        msg.throttle = 5;
        msg.airspeed = 5;
        msg.airspeed_sp = 5;
        msg.groundspeed = 5;
        msg.climb_rate = 5;
        msg.gps_nsat = 5;
        msg.gps_fix_type = 5;
        msg.battery_remaining = 5;
        msg.temperature = 5;
        msg.temperature_air = 5;
        msg.failsafe = 5;
        msg.wp_num = 5;
        
        
        return msg.pack();
    }

}
