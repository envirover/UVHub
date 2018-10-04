package com.envirover.uvnet.mission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

public class TestPlan {
	
	private static final int TEST_SYSTEM_ID = 2;
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testPlanJSONSerialization() throws JsonGenerationException, JsonMappingException, IOException {
		Plan plan = new Plan();
		
		plan.setMissionItems(getSampleMission());
		
		System.out.println(mapper.writeValueAsString(plan));
	}

    private List<msg_mission_item> getSampleMission() {
    	List<msg_mission_item> mission = new ArrayList<msg_mission_item>();
    	
    	msg_mission_item missionItem1 = new msg_mission_item();
    	missionItem1.sysid = TEST_SYSTEM_ID;
    	missionItem1.compid = 0;
    	missionItem1.seq = 0;
    	missionItem1.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
    	missionItem1.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
    	missionItem1.x = (float)34.0;
    	missionItem1.y = (float)-117.0;
    	missionItem1.z = (float)100.0;
    	missionItem1.param1 = 1;
    	missionItem1.param2 = 2;
    	missionItem1.param3 = 3;
    	missionItem1.param4 = 4;
    	mission.add(missionItem1);
    	
    	msg_mission_item missionItem2 = new msg_mission_item();
    	missionItem2.sysid = TEST_SYSTEM_ID;
    	missionItem2.compid = 0;
    	missionItem2.seq = 1;
    	missionItem2.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
    	missionItem2.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
    	missionItem2.x = (float)34.5;
    	missionItem2.y = (float)-117.5;
    	missionItem2.z = (float)200.0;
    	missionItem2.param1 = (float)1.1;
    	missionItem2.param2 = (float)2.1;
    	missionItem2.param3 = (float)3.1;
    	missionItem2.param4 = (float)4.1;
    	mission.add(missionItem2);
    	
    	msg_mission_item missionItem3 = new msg_mission_item();
    	missionItem3.sysid = TEST_SYSTEM_ID;
    	missionItem3.compid = 0;
    	missionItem3.seq = 1;
    	missionItem3.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
    	missionItem3.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
    	missionItem3.x = (float)34.8;
    	missionItem3.y = (float)-117.8;
    	missionItem3.z = (float)300.0;
    	missionItem3.param1 = (float)1.2;
    	missionItem3.param2 = (float)2.2;
    	missionItem3.param3 = (float)3.2;
    	missionItem3.param4 = (float)4.2;   	
    	mission.add(missionItem3);
    	
    	return mission;
    }
}
