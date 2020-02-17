/*
 * Envirover confidential
 * 
 *  [2018] Envirover
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

package com.envirover.uvnet.shadow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import org.junit.Test;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_log_entry;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.MAVLink.enums.MAV_PARAM_TYPE;
import com.envirover.uvnet.shadow.impl.PersistentUVShadow;

public class PersistentUVShadowTest {

	private static int TEST_SYSTEM_ID = 2;

	private PersistentUVShadow shadow = null;

	@Before
	public void setUp() throws Exception {
		shadow = new PersistentUVShadow();
		shadow.open();
	}

	@After
	public void tearDown() throws Exception {
		shadow.close();
	}

	@Test
	public void testParameters() throws IOException, InterruptedException {
		List<msg_param_value> params = new ArrayList<msg_param_value>();

		msg_param_value param0 = new msg_param_value();
		param0.setParam_Id("param0");
		param0.sysid = TEST_SYSTEM_ID;
		param0.param_index = 0;
		param0.param_count = 2;
		param0.param_type = MAV_PARAM_TYPE.MAV_PARAM_TYPE_REAL32;
		param0.param_value = (float) 123.456;
		params.add(param0);

		msg_param_value param1 = new msg_param_value();
		param1.setParam_Id("param1");
		param1.sysid = TEST_SYSTEM_ID;
		param1.param_index = 1;
		param1.param_count = 2;
		param1.param_type = MAV_PARAM_TYPE.MAV_PARAM_TYPE_REAL32;
		param1.param_value = (float) 456.789;
		params.add(param1);

		shadow.setParams(TEST_SYSTEM_ID, params);

		msg_param_set updatedParam1 = new msg_param_set();
		updatedParam1.setParam_Id("param1");
		updatedParam1.sysid = TEST_SYSTEM_ID;
		updatedParam1.param_type = MAV_PARAM_TYPE.MAV_PARAM_TYPE_REAL32;
		updatedParam1.param_value = (float) 987.654;

		shadow.setParam(TEST_SYSTEM_ID, updatedParam1);

		//Thread.sleep(5000);

		assert (shadow.getParamValue(TEST_SYSTEM_ID, "param0", (short) -1).param_value == (float) 123.456);
		assert (shadow.getParamValue(TEST_SYSTEM_ID, "param1", (short) -1).param_value == (float) 987.654);
		assert (shadow.getParamValue(TEST_SYSTEM_ID, "", (short) 0).param_value == (float) 123.456);
		assert (shadow.getParamValue(TEST_SYSTEM_ID, "", (short) 1).param_value == (float) 987.654);

		params = shadow.getParams(TEST_SYSTEM_ID);
		assert(params.get(0).param_index == 0);
		assert(params.get(1).param_index == 1);
	}

	@Test
	public void testMissionAccepted() throws IOException {
		List<msg_mission_item> mission = getSampleMission();

		shadow.setMission(TEST_SYSTEM_ID, mission);

		List<msg_mission_item> reportedMission = shadow.getMission(TEST_SYSTEM_ID);

		assert (mission.size() == reportedMission.size());

		for (msg_mission_item missionItem : mission) {
			msg_mission_item reportedMissionItem = reportedMission.get(missionItem.seq);
			assert (missionItem.command == reportedMissionItem.command);
		}
	}

	@Test
	public void testUpdateReportedState() throws IOException, InterruptedException {
		MAVLinkPacket packet = getSamplePacket();

		StateReport stateReport = new StateReport(new Date(), (msg_high_latency) packet.unpack());
		shadow.updateReportedState(stateReport);

		//Thread.sleep(1000);

		msg_high_latency originalMsg = (msg_high_latency) packet.unpack();

		stateReport = shadow.getLastReportedState(TEST_SYSTEM_ID);

		assert (stateReport != null);

		msg_high_latency msg = stateReport.getState();

		assert (originalMsg.latitude == msg.latitude);
		assert (originalMsg.longitude == msg.longitude);
	}

	@Test
    public void testUVLogbook() throws IOException {
        PersistentUVShadow shadow = new PersistentUVShadow();
        shadow.open();
        
        StateReport report = new StateReport();
        report.setTime(new Date());
        report.getState().sysid = 1;
        shadow.addReportedState(report);
        
        List<StateReport> reports = shadow.getReportedStates(1, null, null, 1);
        for (StateReport r : reports) {
            System.out.println(String.format("%d/%d", r.getState().sysid, r.getTime()));
        }

        List<msg_log_entry> logs = shadow.getLogs(1);
        for (msg_log_entry log : logs) {
            System.out.println(String.format("Count: %d, Time: %d", log.size, log.time_utc));
        }

        shadow.eraseLogs(1);
        shadow.close();
	}
	
	private MAVLinkPacket getSamplePacket() {
		msg_high_latency msg = new msg_high_latency();
		msg.latitude = 523867;
		msg.longitude = 29380;
		// msg.sysid = TEST_SYSTEM_ID;
		// msg.compid = 0;

		MAVLinkPacket packet = msg.pack();
		packet.sysid = TEST_SYSTEM_ID;
		packet.compid = 0;
		return packet;
	}

	private List<msg_mission_item> getSampleMission() {
		List<msg_mission_item> mission = new ArrayList<msg_mission_item>();

		msg_mission_item missionItem1 = new msg_mission_item();
		missionItem1.target_system = (short)TEST_SYSTEM_ID;
		missionItem1.compid = 0;
		missionItem1.seq = 0;
		missionItem1.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
		missionItem1.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		missionItem1.x = (float) 34.0;
		missionItem1.y = (float) -117.0;
		missionItem1.z = (float) 100.0;
		mission.add(missionItem1);

		msg_mission_item missionItem2 = new msg_mission_item();
		missionItem2.target_system = (short)TEST_SYSTEM_ID;
		missionItem2.compid = 0;
		missionItem2.seq = 1;
		missionItem2.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
		missionItem2.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		missionItem2.x = (float) 34.5;
		missionItem2.y = (float) -117.5;
		missionItem2.z = (float) 200.0;
		mission.add(missionItem2);

		return mission;
	}

}
