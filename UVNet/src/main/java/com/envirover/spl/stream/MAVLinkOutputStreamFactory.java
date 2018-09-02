package com.envirover.spl.stream;

import java.io.IOException;

/**
 * Instantiates MAVLinkOutputStream.
 *
 */
class MAVLinkOutputStreamFactory {

    private static MAVLinkOutputStream stream = null;

    public synchronized static MAVLinkOutputStream getMAVLinkOutputStream() throws IOException {
        if (stream == null) {
            stream = new ElasticsearchOutputStream();
            stream.open();
        }

        return stream;
    }

}
