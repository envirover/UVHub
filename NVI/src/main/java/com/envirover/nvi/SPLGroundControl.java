/*
This file is part of SPLGroundControl application.

SPLGroundControl is a ground control proxy station for ArduPilot rovers with
RockBLOCK satellite communication.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLGroundControl is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SPLGroundControl.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.nvi;

import java.util.Scanner;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;

/**
 * The main application class. 
 */
public class SPLGroundControl {

    public static void main(String[] args) {
        try {
            SPLDaemon daemon = new SPLDaemon();

            daemon.init(new SPLDaemonContext(args));

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

    public static class SPLDaemonContext implements DaemonContext {

        private final String[] args;

        public SPLDaemonContext(String[] args) {
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
