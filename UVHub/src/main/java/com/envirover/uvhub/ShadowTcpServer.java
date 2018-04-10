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

import com.envirover.mavlink.MAVLinkSocket;

/**
 * TCP server that accepts connections from GCS clients to update on-board parameters
 * and missions in the reported state of the shadow.
 * 
 * {@link com.envirover.uvhub.ShadowClientSession} is created for each client connection. 
 *  
 * @author Pavel Bobov
 *
 */
public class ShadowTcpServer extends GCSTcpServer {
    
    /**
     * Creates an instance of ShadowTcpServer 
     * 
     * @param port TCP port used for MAVLink ground control stations connections 
     */
    public ShadowTcpServer(Integer port) {
        super(port, null);
    }

    @Override
    protected ClientSession createClientSession(MAVLinkSocket clientSocket) {
        return new ShadowClientSession(clientSocket);
    }

}
