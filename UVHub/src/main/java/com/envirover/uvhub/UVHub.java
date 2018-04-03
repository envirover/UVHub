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

import java.util.Scanner;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;

/**
 * The main application class. 
 */
public class UVHub {

    public static void main(String[] args) {
        try {
            UVHubDaemon daemon = new UVHubDaemon();

            daemon.init(new UVHubDaemonContext(args));

            daemon.start();

            System.out.println("Enter 'stop' to exit the program.");

            Scanner scanner = new Scanner(System.in);

            String str;
            while (!(str = scanner.next()).equalsIgnoreCase("stop")) {
                //Just echo the user input for now.
                System.out.println(str);
            }

            System.out.println("Exiting...");

            scanner.close();

            daemon.stop();
            daemon.destroy();

            System.out.println("Done.");
            
            System.exit(0);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } 
    }

    public static class UVHubDaemonContext implements DaemonContext {

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
