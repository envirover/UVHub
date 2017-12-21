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
