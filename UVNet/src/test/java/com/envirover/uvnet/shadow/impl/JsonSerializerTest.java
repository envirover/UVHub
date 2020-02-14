package com.envirover.uvnet.shadow.impl;

import java.io.IOException;
import java.util.Date;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.enums.MAV_COMPONENT;
import com.MAVLink.enums.MAV_PARAM_TYPE;
import com.envirover.uvnet.shadow.StateReport;

public class JsonSerializerTest {

	@Test
	public void testToJSON() throws JsonGenerationException, JsonMappingException, IllegalArgumentException, IllegalAccessException, IOException {
		msg_param_value paramValue = new msg_param_value();
		paramValue.setParam_Id("param1");
		paramValue.compid = MAV_COMPONENT.MAV_COMP_ID_ALL;
		paramValue.sysid = 1;
		paramValue.param_count = 2;
		paramValue.param_index = 1;
		paramValue.param_type = MAV_PARAM_TYPE.MAV_PARAM_TYPE_REAL32;
		paramValue.param_value = (float)123.456;

		String json = JsonSerializer.toJSON(paramValue);
		
		System.out.println(json);
		
		json = JsonSerializer.toJSON(new StateReport(new Date().getTime(), 
		(msg_high_latency)getSamplePacket().unpack()));
		
		System.out.println(json);
		
		StateReport stateReport = JsonSerializer.stateReportFromJSON(json);
		System.out.print(stateReport.getTime());
		System.out.print(" : ");
		System.out.println(stateReport.getState());
	}

	private MAVLinkPacket getSamplePacket() {
		msg_high_latency msg = new msg_high_latency();
		msg.latitude = 523867;
		msg.longitude = 2938;
		msg.altitude_amsl = 400;
		msg.sysid = 1;
		msg.compid = 0;
		return msg.pack();
	}
}
