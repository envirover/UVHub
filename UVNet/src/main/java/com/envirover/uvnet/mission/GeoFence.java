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

package com.envirover.uvnet.mission;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * JSON serialization class for mission geo fence.
 * 
 * @author Pavel Bobov
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoFence {

    private List<List<Double>> polygon = new ArrayList<List<Double>>();
    private final int version = 1;

    public List<List<Double>> getPolygon() {
        return polygon;
    }

    public void setPolygon(List<List<Double>> polygon) {
        this.polygon = polygon;
    }

    public int getVersion() {
        return version;
    }

}
