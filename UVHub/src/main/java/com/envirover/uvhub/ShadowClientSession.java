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

package com.envirover.uvhub;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_high_latency;
import com.MAVLink.common.msg_log_entry;
import com.MAVLink.common.msg_log_erase;
import com.MAVLink.common.msg_log_request_list;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_mission_clear_all;
import com.MAVLink.common.msg_mission_count;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.common.msg_mission_request;
import com.MAVLink.common.msg_mission_request_list;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_MISSION_RESULT;
import com.MAVLink.enums.MAV_MODE;
import com.MAVLink.enums.MAV_STATE;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkLogger;
import com.envirover.uvnet.Config;
import com.envirover.uvnet.shadow.UVShadow;

/**
 * TCP and WebSocket MAVLink client sessions that handle communications with GCS clients
 * to update reported states of on-board parameters and missions in the shadow.
 * 
 * @author Pavel Bobov
 */
public class ShadowClientSession implements ClientSession {

    private final static Logger logger = LogManager.getLogger(ShadowClientSession.class);
    private static final Config config = Config.getInstance();

    private final ScheduledExecutorService heartbeatTimer;

    private final MAVLinkChannel src;
    private final UVShadow shadow;

    private boolean isOpen = false;
    //private int desiredMissionCount = 0;
    private List<msg_mission_item> desiredMission = new ArrayList<msg_mission_item>();
    private int desiredMissionCount = 0;
    private List<msg_mission_item> reportedMission = new ArrayList<msg_mission_item>();
    private int sysId = 1;  //TODO set system Id for the client session
    
    public ShadowClientSession(MAVLinkChannel src, UVShadow shadow) {
    	this.heartbeatTimer = Executors.newScheduledThreadPool(2);
        this.src = src;
        this.shadow = shadow;
    }

    /* (non-Javadoc)
     * @see com.envirover.nvi.ClientSession#onOpen()
     */
    @Override
    public void onOpen() throws IOException {
        Runnable heartbeatTask = new Runnable() {
            @Override
            public void run() {
                try {
                    reportState();
                } catch (IOException | InterruptedException ex) {
                	if (ex.getMessage().equals("Software caused connection abort: socket write error")) {
	                	try {
							onClose();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
                	}
                }
            }
        };

        heartbeatTimer.scheduleAtFixedRate(heartbeatTask, 0, config.getHeartbeatInterval(), TimeUnit.MILLISECONDS);
        
        isOpen = true;
        
        logger.info("Shadow client session opened.");
    }

    /* (non-Javadoc)
     * @see com.envirover.nvi.ClientSession#onClose()
     */
    @Override
    public void onClose() throws IOException {
    	if (isOpen) {
	    	isOpen = false;
	    	
	        heartbeatTimer.shutdownNow();
	
	        if (src != null) {
	            src.close();
	        }
	        
	        logger.info("Shadow client session closed.");
    	}
    }

    /* (non-Javadoc)
     * @see com.envirover.nvi.ClientSession#onMessage(com.MAVLink.MAVLinkPacket)
     */
    @Override
    public void onMessage(MAVLinkPacket packet) throws IOException {
        handleParams(packet);
        handleMissions(packet);
        handleLogs(packet);
    }

	@Override
	public boolean isOpen() {
		return isOpen;
	}
	
    private void handleParams(MAVLinkPacket packet) throws IOException {
        if (packet == null) {
            return;
        }

        switch (packet.msgid) {
            case msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                msg_param_request_list msg = (msg_param_request_list)packet.unpack();
                List<msg_param_value> params = shadow.getParams(msg.target_system);
                
                for (msg_param_value param : params) {
                    sendToSource(param);
                    try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
                }

                logger.info(MessageFormat.format("{0} on-board parameters sent to the MAVLink client.", params.size()));
                break;
            }
            case msg_param_request_read.MAVLINK_MSG_ID_PARAM_REQUEST_READ: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);

                msg_param_request_read request = (msg_param_request_read)packet.unpack();
                //logger.info(MessageFormat.format("Sending value of parameter ''{0}'' to MAVLink client.", request.getParam_Id()));
                sendToSource(shadow.getParamValue(request.target_system, request.getParam_Id(), request.param_index));
                break;
            }
            case msg_param_set.MAVLINK_MSG_ID_PARAM_SET: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);

                msg_param_set paramSet = (msg_param_set)packet.unpack();
                shadow.setParam(paramSet.target_system, paramSet);
                sendToSource(shadow.getParamValue(paramSet.target_system, paramSet.getParam_Id(), (short)-1));
                break;
            }
        }
    }

    private void handleMissions(MAVLinkPacket packet) throws IOException {
        if (packet == null) {
            return;
        }

        switch (packet.msgid) {
	        case msg_mission_request_list.MAVLINK_MSG_ID_MISSION_REQUEST_LIST: {
	            MAVLinkLogger.log(Level.INFO, "<<", packet);
	            msg_mission_request_list msg = (msg_mission_request_list)packet.unpack();
	            reportedMission = shadow.getMission(msg.target_system);            
	            msg_mission_count count = new msg_mission_count();
	            count.count = reportedMission != null ? reportedMission.size() : 0;
	            count.sysid = msg.target_system;
	            count.compid = msg.target_component;
	            count.target_system = (short) packet.sysid;
	            count.target_component = (short) packet.compid;
	            sendToSource(count);
	            break;
	        }
	        case msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST: {
	            MAVLinkLogger.log(Level.INFO, "<<", packet);
	            msg_mission_request msg = (msg_mission_request)packet.unpack();
	            if (reportedMission != null && msg.seq < reportedMission.size()) {
	                msg_mission_item mission = reportedMission.get(msg.seq);
	                mission.sysid = msg.target_system;
	                mission.compid = msg.target_component;
	                sendToSource(mission);
	            }
	            break;
	        }
            case msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                desiredMission.clear();
                break;
            }
            case msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                msg_mission_count msg = (msg_mission_count)packet.unpack();
                desiredMission = new ArrayList<msg_mission_item>(msg.count);
                desiredMissionCount = msg.count;
                msg_mission_request request = new msg_mission_request();
                request.seq = 0;
                request.sysid = msg.target_system;
                request.compid = msg.target_component;
                request.target_system = (short) packet.sysid;
                request.target_component = (short) packet.compid;
                sendToSource(request);
                break;
            }
            case msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                break;
            }
            case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM: {
                MAVLinkLogger.log(Level.INFO, "<<", packet);
                msg_mission_item msg = (msg_mission_item)packet.unpack();
                try {
                	//desiredMission.set(msg.seq, msg);
                	desiredMission.add(msg);
                } catch(Exception e) {
                	e.printStackTrace();
                }
                if (msg.seq + 1 != desiredMissionCount) {
                    msg_mission_request mission_request = new msg_mission_request();
                    mission_request.seq = msg.seq + 1;
                    mission_request.sysid = msg.target_system;
                    mission_request.compid = msg.target_component;
                    mission_request.target_system = (short) packet.sysid;
                    mission_request.target_component = (short) packet.compid;
                    sendToSource(mission_request);
                } else {
                    msg_mission_ack mission_ack = new msg_mission_ack();
                    mission_ack.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
                    mission_ack.sysid = msg.target_system;
                    mission_ack.compid = msg.target_component;
                    mission_ack.target_system = (short) packet.sysid;
                    mission_ack.target_component = (short) packet.compid;
                    sendToSource(mission_ack);
                    shadow.setMission(msg.target_system, desiredMission);
                }
                break;
            }
        }
    }

    private void handleLogs(MAVLinkPacket packet) throws IOException {
        if (packet == null) {
            return;
        }

        switch (packet.msgid) {
	        case msg_log_request_list.MAVLINK_MSG_ID_LOG_REQUEST_LIST:
	        	MAVLinkLogger.log(Level.INFO, "<<", packet);
	        	
	        	msg_log_request_list log_request_list = (msg_log_request_list)packet.unpack();
	        	
	        	List<msg_log_entry> logs = shadow.getLogs(log_request_list.target_system);
	        	
	        	if (logs != null) {
		        	for (msg_log_entry log_entry : logs) {
		        		sendToSource(log_entry);
		        	}
	        	}
	        	
	        	break;
	        case msg_log_erase.MAVLINK_MSG_ID_LOG_ERASE:
	        	MAVLinkLogger.log(Level.INFO, "<<", packet);
	        	
	        	msg_log_erase log_erase = (msg_log_erase)packet.unpack();
	        	
	        	shadow.eraseLogs(log_erase.target_system);
	        	
	        	logger.info("Messages log erased.");
	        	
	        	break;
        }
    }
    
    private void sendToSource(MAVLinkMessage msg) throws IOException {
        if (msg == null) {
            return;
        }

        try {
            MAVLinkPacket packet = msg.pack();
            packet.sysid = msg.sysid;
            packet.compid = 1;
            src.sendMessage(packet);
            MAVLinkLogger.log(Level.DEBUG, ">>", packet);
        } catch (IOException ex) {
            ex.printStackTrace();
            if (ex.getMessage().equals("Software caused connection abort: socket write error")) {
            	onClose();
            }
            throw ex;
        }
    }

    /**
     * Sends heartbeat and other status messages derived 
     * from HIGH_LATENCY message to the specified client channel.
     *
     * @param dst destination channel
     * @throws IOException if a message sending failed
     * @throws InterruptedException 
     */
    private void reportState() throws IOException, InterruptedException {
        msg_high_latency msgHighLatency = (msg_high_latency)shadow.getLastMessage(
        		sysId, msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY);

        sendToSource(getHeartbeatMsg(msgHighLatency));
        sendToSource(getSysStatusMsg(msgHighLatency));
        sendToSource(getGpsRawIntMsg(msgHighLatency));
        sendToSource(getAttitudeMsg(msgHighLatency));
        sendToSource(getGlobalPositionIntMsg(msgHighLatency));
        sendToSource(getMissionCurrentMsg(msgHighLatency));
        sendToSource(getNavControllerOutputMsg(msgHighLatency));
        sendToSource(getVfrHudMsg(msgHighLatency));
    }

    private MAVLinkMessage getHeartbeatMsg(msg_high_latency msgHighLatency) {
        msg_heartbeat msg = new msg_heartbeat();
        
    	if (msgHighLatency != null) {
	        msg.sysid = msgHighLatency.sysid;
	        msg.compid = msgHighLatency.compid;
	        msg.base_mode = msgHighLatency.base_mode;
	        msg.custom_mode = msgHighLatency.custom_mode;
    	} else {
    		msg.sysid = sysId;
    		msg.compid = 0;
    		msg.base_mode = MAV_MODE.MAV_MODE_PREFLIGHT;
    		msg.custom_mode = 0;
    	}
    	
        msg.system_status = MAV_STATE.MAV_STATE_ACTIVE;
        msg.autopilot = config.getAutopilot();
        msg.type = config.getMavType();
      
        return msg;
    }

    private MAVLinkMessage getSysStatusMsg(msg_high_latency msgHighLatency) {
    	if (msgHighLatency == null) {
    		return null;
    	}
    	
        msg_sys_status msg = new msg_sys_status();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.battery_remaining = (byte)msgHighLatency.battery_remaining;
        msg.voltage_battery = msgHighLatency.temperature * 1000;
        msg.current_battery = msgHighLatency.temperature_air < 0 ? 
                -1 : (short)(msgHighLatency.temperature_air * 100);
        
        return msg;
    }

    private MAVLinkMessage getGpsRawIntMsg(msg_high_latency msgHighLatency) {
    	if (msgHighLatency == null) {
    		return null;
    	}
    	
        msg_gps_raw_int msg = new msg_gps_raw_int();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.fix_type = msgHighLatency.gps_fix_type;
        msg.satellites_visible = msgHighLatency.gps_nsat;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.alt = msgHighLatency.altitude_amsl;
        
        return msg;
    }

    private MAVLinkMessage getAttitudeMsg(msg_high_latency msgHighLatency) {
    	if (msgHighLatency == null) {
    		return null;
    	}
    	
        msg_attitude msg = new msg_attitude();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.yaw = (float)Math.toRadians(msgHighLatency.heading / 100.0);
        msg.pitch = (float)Math.toRadians(msgHighLatency.pitch / 100.0);
        msg.roll = (float)Math.toRadians(msgHighLatency.roll / 100.0);
        
        return msg;
    }

    private MAVLinkMessage getGlobalPositionIntMsg(msg_high_latency msgHighLatency) {
    	if (msgHighLatency == null) {
    		return null;
    	}
    	
        msg_global_position_int msg = new msg_global_position_int();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.alt = msgHighLatency.altitude_amsl;
        msg.lat = msgHighLatency.latitude;
        msg.lon = msgHighLatency.longitude;
        msg.hdg = msgHighLatency.heading;
        msg.relative_alt = msgHighLatency.altitude_sp;
        
        return msg;
    }

    private MAVLinkMessage getMissionCurrentMsg(msg_high_latency msgHighLatency) {
    	if (msgHighLatency == null) {
    		return null;
    	}
    	
        msg_mission_current msg = new msg_mission_current();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.seq = msgHighLatency.wp_num;
        
        return msg;
    }

    private MAVLinkMessage getNavControllerOutputMsg(msg_high_latency msgHighLatency) {
    	if (msgHighLatency == null) {
    		return null;
    	}
    	
        msg_nav_controller_output msg = new msg_nav_controller_output();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.nav_bearing = (short)(msgHighLatency.heading_sp / 100);
        
        return msg;
    }
 
    private MAVLinkMessage getVfrHudMsg(msg_high_latency msgHighLatency) {
    	if (msgHighLatency == null) {
    		return null;
    	}
    	
        msg_vfr_hud msg = new msg_vfr_hud();
        msg.sysid = msgHighLatency.sysid;
        msg.compid = msgHighLatency.compid;
        msg.airspeed = msgHighLatency.airspeed;
        msg.alt = msgHighLatency.altitude_amsl;
        msg.climb = msgHighLatency.climb_rate;
        msg.groundspeed = msgHighLatency.groundspeed;
        msg.heading = (short)(msgHighLatency.heading / 100);
        msg.throttle = msgHighLatency.throttle;
        
        return msg;
    }

}
