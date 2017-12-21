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

package com.envirover.nvi.stream;

import java.io.IOException;

/**
 * Instantiates MAVLinkOutputStream.
 *
 */
public class MAVLinkOutputStreamFactory {

    private static MAVLinkOutputStream stream = null;

    public synchronized static MAVLinkOutputStream getMAVLinkOutputStream() throws IOException {
        if (stream == null) {
            stream = new DynamoDBOutputStream();
            stream.open();
        }

        return stream;
    }

}
