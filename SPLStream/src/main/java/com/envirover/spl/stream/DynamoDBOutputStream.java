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
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_high_latency;
import com.amazonaws.protocol.json.SdkJsonGenerator.JsonGenerationException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.dynamodbv2.util.TableUtils.TableNeverTransitionedToStateException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Writes MAVLink message to DynamoDB table.
 * 
 * 
 */
public class DynamoDBOutputStream implements MAVLinkOutputStream {

    private static final String SPL_DYNAMODB_TABLE = "SPL_DYNAMODB_TABLE"; 

    private static final String ATTR_DEVICE_ID = "DeviceId";
    private static final String ATTR_TIME      = "Time";
    private static final String ATTR_MSG_ID    = "MsgId";
    private static final String ATTR_MESSAGE   = "Message";

    private static final Long READ_CAPACITY    = 5L;
    private static final Long WRITE_CAPACITY   = 5L;

    private static final ObjectMapper mapper   = new ObjectMapper();

    private static final Logger logger = Logger.getLogger(DynamoDBOutputStream.class.getName());
    
    private static String tableName            = "MAVLinkMessages";
    
    private DynamoDB dynamoDB;

    public DynamoDBOutputStream() {
        if (System.getenv(SPL_DYNAMODB_TABLE) != null) {
            tableName = System.getenv(SPL_DYNAMODB_TABLE);
        }
    }

    @Override
    public void open() throws IOException {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();

        if (TableUtils.createTableIfNotExists(dynamoDBClient,
                new CreateTableRequest()
                   .withTableName(tableName)
                   .withKeySchema(
                        new KeySchemaElement(ATTR_DEVICE_ID, KeyType.HASH),
                        new KeySchemaElement(ATTR_TIME, KeyType.RANGE))
                   .withAttributeDefinitions(
                        new AttributeDefinition(ATTR_DEVICE_ID, ScalarAttributeType.S),
                        new AttributeDefinition(ATTR_TIME, ScalarAttributeType.N))
                   .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(READ_CAPACITY)
                        .withWriteCapacityUnits(WRITE_CAPACITY)))) {

            try {
                TableUtils.waitUntilActive(dynamoDBClient, tableName);
            } catch (TableNeverTransitionedToStateException e) {
                throw new IOException(e);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            logger.info(MessageFormat.format("DynamoDB table ''{0}'' created.", tableName));
        }

        dynamoDB = new DynamoDB(dynamoDBClient);
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void writePacket(String deviceId, Date time, MAVLinkPacket packet) throws IOException {
        if (deviceId == null || deviceId.isEmpty() || time == null || packet == null) {
            return;
        }

        Table table = dynamoDB.getTable(tableName);

        table.putItem(new Item().withPrimaryKey(ATTR_DEVICE_ID, deviceId, ATTR_TIME, time.getTime())
                .withNumber(ATTR_MSG_ID, packet.msgid)
                .withJSON(ATTR_MESSAGE, toJSON(packet)));
    }

    private String toJSON(MAVLinkPacket packet) throws JsonGenerationException, JsonMappingException, IOException {
        if (packet == null) {
            return "{}";
        }

        if (packet.msgid == msg_high_latency.MAVLINK_MSG_ID_HIGH_LATENCY) {
            msg_high_latency msg = (msg_high_latency) packet.unpack();

            return mapper.writeValueAsString(msg);
        }

        return "{}";
    }

}
