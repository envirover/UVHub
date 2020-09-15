/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.envirover.uvhub;

import com.envirover.mavlink.MAVLinkSocket;
import com.envirover.uvnet.shadow.UVLogbook;
import com.envirover.uvnet.shadow.UVShadow;

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
    public ShadowTcpServer(Integer port, UVShadow shadow, UVLogbook logbook) {
        super(port, null, shadow, logbook);
    }

    @Override
    protected ClientSession createClientSession(MAVLinkSocket clientSocket) {
        return new ShadowClientSession(clientSocket, shadow, logbook);
    }

}
