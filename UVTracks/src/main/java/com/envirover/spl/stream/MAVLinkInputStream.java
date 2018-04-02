/*
This file is part of SPLStream application.

See http://www.rock7mobile.com/downloads/RockBLOCK-Web-Services-User-Guide.pdf

Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLStrean is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SPLStream.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.envirover.spl.stream;

import java.io.IOException;
import java.util.Date;

import org.json.JSONArray;

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
    JSONArray query(String deviceId, Long startTime, Long endTime, Integer msgId) throws IOException;
}
