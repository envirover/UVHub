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
