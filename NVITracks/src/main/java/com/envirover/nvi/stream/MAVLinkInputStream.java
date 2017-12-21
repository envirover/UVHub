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
import java.util.Date;

/**
 * Input stream of MAVLink records.
 *
 */
public interface MAVLinkInputStream {

    /**
     * Opens the stream.
     * 
     * @throws IOException
     */
    void open() throws IOException;

    /**
     * Closes the stream.
     * 
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * 
     * @return MAVLinkRecord or null if the end of the stream is reached.
     * @throws IOException
     */
    Iterable<MAVLinkRecord> query(String deviceId, Date startTime, Date endTime, Integer msgId) throws IOException;

}
