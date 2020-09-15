/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.envirover.uvnet.shadow.impl;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.MAVLink.common.msg_log_entry;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.envirover.uvnet.shadow.StateReport;
import com.envirover.uvnet.shadow.UVLogbook;
import com.envirover.uvnet.shadow.UVShadow;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;

/**
 * Implementation of UVShadow and UVLogbook that uses MongoDB NonSQL database.
 */
public class PersistentUVShadow implements UVShadow, UVLogbook {

    // MongoDB databases
    private static final String SHADOW_DB = "shadow";
    private static final String LOGBOOK_DB = "logbook";

    // UVShadow database collections
    private static final String STATES_COLLECTION = "states";
    private static final String MISSIONS_COLLECTION = "missions";
    private static final String PARAMS_COLLECTION = "params";

    // UVLogbook database collections
    private static final String TRACKS_COLLECTION = "tracks";

    private static final String STATE_REPORTS_SYSID = "state.sysid";
    private static final String STATE_REPORTS_TIME = "time";

    private static final String MISSIONS_TARGET_SYSTEM = "target_system";
    private static final String MISSIONS_SEQ = "seq";

    private static final String PARAMS_SYSID = "sysid";
    private static final String PARAMS_PARAM_ID = "param_Id";
    private static final String PARAMS_PARAM_INDEX = "param_index";
    private static final String PARAMS_PARAM_VALUE = "param_value";

    private final String mongoClientURI;

    private MongoClient mongoClient;
    private MongoCollection<StateReport> tracksCollection;
    private MongoCollection<StateReport> statesCollection;
    private MongoCollection<msg_mission_item> missionsCollection;
    private MongoCollection<msg_param_value> paramsCollection;

    private List<msg_mission_item> desiredMission = new ArrayList<msg_mission_item>();

    public PersistentUVShadow() {
        this("mongodb://localhost:27017");
    }

    public PersistentUVShadow(String mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
    }

    public void open() throws UnknownHostException {
        if (mongoClient == null) {
            // Create a CodecRegistry containing the PojoCodecProvider instance.
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
                    fromProviders(pojoCodecProvider));
            mongoClient = MongoClients.create(mongoClientURI);

            // shadow db
            MongoDatabase shadowDb = mongoClient.getDatabase(SHADOW_DB).withCodecRegistry(pojoCodecRegistry);
            statesCollection = shadowDb.getCollection(STATES_COLLECTION, StateReport.class);
            statesCollection.createIndex(Indexes.descending(STATE_REPORTS_SYSID), new IndexOptions().unique(true));

            missionsCollection = shadowDb.getCollection(MISSIONS_COLLECTION, msg_mission_item.class);
            missionsCollection.createIndex(Indexes.ascending(MISSIONS_TARGET_SYSTEM, MISSIONS_SEQ),
                    new IndexOptions().unique(true));

            paramsCollection = shadowDb.getCollection(PARAMS_COLLECTION, msg_param_value.class);
            paramsCollection.createIndex(Indexes.ascending(PARAMS_SYSID, PARAMS_PARAM_ID),
                    new IndexOptions().unique(true));
            paramsCollection.createIndex(Indexes.ascending(PARAMS_SYSID, PARAMS_PARAM_INDEX),
                    new IndexOptions().unique(true));

            // logbook db
            MongoDatabase logbookDb = mongoClient.getDatabase(LOGBOOK_DB).withCodecRegistry(pojoCodecRegistry);
            tracksCollection = logbookDb.getCollection(TRACKS_COLLECTION, StateReport.class);
            tracksCollection.createIndex(Indexes.descending(STATE_REPORTS_SYSID, STATE_REPORTS_TIME));
        }
    }

    public synchronized void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }

    @Override
    public void addReportedState(StateReport state) throws IOException {
        if (state == null) {
            throw new IllegalArgumentException("Parameter 'state' must not be null.");
        }

        open();

        tracksCollection.insertOne(state);
    }

    @Override
    public List<StateReport> getReportedStates(int sysId, Date startTime, Date endTime, int top) throws IOException {
        open();

        Bson filter = eq(STATE_REPORTS_SYSID, sysId);

        if (endTime != null) {
            filter = and(filter, lte(STATE_REPORTS_TIME, endTime));
        }

        if (startTime != null) {
            filter = and(filter, gte(STATE_REPORTS_TIME, startTime));
        }

        return tracksCollection.find(filter).sort(descending(STATE_REPORTS_TIME)).limit(top)
                .into(new ArrayList<StateReport>());
    }

    @Override
    public List<msg_log_entry> getLogs(int sysId) throws IOException {
        open();

        Bson filter = eq(STATE_REPORTS_SYSID, sysId);
        FindIterable<StateReport> reports = tracksCollection.find(filter).limit(1);

        List<msg_log_entry> logs = new ArrayList<msg_log_entry>();

        for (StateReport report : reports) {
            msg_log_entry log_entry = new msg_log_entry();
            log_entry.sysid = sysId;
            log_entry.compid = 0;
            log_entry.id = 1;
            log_entry.last_log_num = 1;
            log_entry.num_logs = 1;
            log_entry.time_utc = report.getTime().getTime() / 1000;
            log_entry.size = tracksCollection.countDocuments(filter);

            logs.add(log_entry);
        }

        return logs;
    }

    @Override
    public void eraseLogs(int sysId) throws IOException {
        open();

        Bson filter = eq(STATE_REPORTS_SYSID, sysId);
        tracksCollection.deleteMany(filter);
    }

    @Override
    public void setParams(int sysId, List<msg_param_value> params) throws IOException {
        if (params == null) {
            throw new IllegalArgumentException("Parameter 'params' must not be null.");
        }

        open();

        paramsCollection.deleteMany(eq(PARAMS_SYSID, sysId));

        paramsCollection.insertMany(params);
    }

    @Override
    public msg_param_value getParamValue(int sysId, String paramId, short paramIndex) throws IOException {
        open();

        if (paramIndex < 0) {
            return paramsCollection.find(and(eq(PARAMS_SYSID, sysId), eq(PARAMS_PARAM_ID, paramId))).first();
        }

        return paramsCollection.find(and(eq(PARAMS_SYSID, sysId), eq(PARAMS_PARAM_INDEX, paramIndex))).first();
    }

    @Override
    public void setParam(int sysId, msg_param_set parameter) throws IOException {
        open();

        paramsCollection.updateOne(and(eq(PARAMS_SYSID, sysId), eq(PARAMS_PARAM_ID, parameter.getParam_Id())),
                set(PARAMS_PARAM_VALUE, parameter.param_value));
    }

    @Override
    public List<msg_param_value> getParams(int sysId) throws IOException {
        open();

        return paramsCollection.find(eq(PARAMS_SYSID, sysId)).sort(ascending(PARAMS_PARAM_INDEX))
                .into(new ArrayList<msg_param_value>());
    }

    @Override
    public List<msg_mission_item> getDesiredMission() throws IOException {
        return desiredMission;
    }

    @Override
    public void setMission(int sysId, List<msg_mission_item> mission) throws IOException {
        open();

        missionsCollection.deleteMany(eq(MISSIONS_TARGET_SYSTEM, sysId));

        missionsCollection.insertMany(mission);
    }

    @Override
    public List<msg_mission_item> getMission(int sysId) throws IOException {
        open();

        return missionsCollection.find(eq(MISSIONS_TARGET_SYSTEM, sysId)).sort(ascending(MISSIONS_SEQ))
                .into(new ArrayList<msg_mission_item>());
    }

    @Override
    public void updateReportedState(StateReport state) throws IOException {
        if (state == null) {
            throw new IllegalArgumentException("Parameter 'state' must not be null.");
        }

        open();

        statesCollection.replaceOne(eq(STATE_REPORTS_SYSID, state.getState().sysid), state,
                new ReplaceOptions().upsert(true));
    }

    @Override
    public StateReport getLastReportedState(int sysId) throws IOException {
        open();

        return statesCollection.find(eq(STATE_REPORTS_SYSID, sysId)).first();
    }

}