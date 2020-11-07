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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;

/**
 * The main application class.
 * 
 * @author Pavel Bobov
 */
public class UVHub {
    
    private final static Logger logger = LogManager.getLogger(UVHub.class);

    public static void main(String[] args) {
        UVHubDaemon daemon = new UVHubDaemon();
        try {
            daemon.init(new UVHubDaemonContext(args));
            daemon.start();
        } catch (Exception ex) {
            daemon.destroy();
            logger.error("UV Hub init failed: " + ex.getMessage());
            System.exit(1);
        } 

        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            logger.info(ex.getMessage());
        } 

        try {
            daemon.stop();
            daemon.destroy();
        } catch (Exception e) {
            logger.error("UV Hub stop failed: " + e.getMessage());
        }

        System.exit(0);
    }

    static class UVHubDaemonContext implements DaemonContext {

        private final String[] args;

        public UVHubDaemonContext(String[] args) {
            this.args = args;
        }

        @Override
        public DaemonController getController() {
            return null;
        }

        @Override
        public String[] getArguments() {
            return args;
        }
        
    }
}
