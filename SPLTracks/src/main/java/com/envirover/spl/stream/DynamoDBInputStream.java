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
import java.util.Iterator;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.MAVLink.common.msg_high_latency;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;

/**
 * Reads MAVLink messages from DynamoDB table.
 * 
 * 
 */
public class DynamoDBInputStream implements MAVLinkInputStream {

    private static final String SPL_DYNAMODB_TABLE = "SPL_DYNAMODB_TABLE";

    private static final String ATTR_DEVICE_ID = "DeviceId";
    private static final String ATTR_TIME = "Time";
    private static final String ATTR_MSG_ID = "MsgId";
    private static final String ATTR_MESSAGE = "Message";

    //private static final Logger logger = Logger.getLogger(DynamoDBInputStream.class.getName());

    private static String tableName = "MAVLinkMessages";

    private final Table table ;

    public DynamoDBInputStream() {
        if (System.getenv(SPL_DYNAMODB_TABLE) != null) {
            tableName = System.getenv(SPL_DYNAMODB_TABLE);
        }

        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();

        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);

        table = dynamoDB.getTable(tableName);
    }

    @Override
    public void open() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public Iterable<MAVLinkRecord> query(String deviceId, Date startTime, Date endTime) throws IOException {
        RangeKeyCondition timeInterval;

        if (startTime == null && endTime == null) {
            timeInterval = null;
        } else if (startTime == null && endTime != null) {
            timeInterval = new RangeKeyCondition(ATTR_TIME).le(endTime.getTime());
        } else if (startTime != null && endTime == null) {
            timeInterval = new RangeKeyCondition(ATTR_TIME).ge(startTime.getTime());
        } else {
            timeInterval = new RangeKeyCondition(ATTR_TIME).between(startTime.getTime(), endTime.getTime());
        }

        return new MAVLinkRecordIterable(table.query(ATTR_DEVICE_ID, deviceId, timeInterval).iterator());
    }

    static class MAVLinkRecordIterable implements Iterable<MAVLinkRecord> {

        private static final ObjectMapper mapper = new ObjectMapper();

        private final IteratorSupport<Item, QueryOutcome> itemIterator;

        public MAVLinkRecordIterable(IteratorSupport<Item, QueryOutcome> it) {
            this.itemIterator = it;
        }

        @Override
        public Iterator<MAVLinkRecord> iterator() {
            return new MAVLinkRecordIterator();
        }

        class MAVLinkRecordIterator implements Iterator<MAVLinkRecord> {

            @Override
            public boolean hasNext() {
                return itemIterator.hasNext();
            }

            @Override
            public MAVLinkRecord next() {
                Item item = itemIterator.next();

                MAVLinkRecord record = new MAVLinkRecord();

                record.setDeviceId(item.getString(ATTR_DEVICE_ID));
                record.setTime(new Date(item.getLong(ATTR_TIME)));
                record.setMsgId(item.getInt(ATTR_MSG_ID));

                if (item.getInt(ATTR_MSG_ID) == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
                    String json = item.getJSON(ATTR_MESSAGE);
                    try {
                        msg_high_latency msg;
                        msg = mapper.readValue(json, msg_high_latency.class);
                        record.setPacket(msg.pack());
                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return record;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
