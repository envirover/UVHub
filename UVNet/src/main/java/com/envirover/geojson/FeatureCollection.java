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

package com.envirover.geojson;

import java.util.ArrayList;
import java.util.List;

/**
 * GeoJSON feature collection. 
 * 
 * @author Pavel Bobov
 *
 */
public class FeatureCollection {

    private List<Feature> features;

    public FeatureCollection() {
        this.features = new ArrayList<Feature>();
    }
    
    public FeatureCollection(List<Feature> features) {
        this.features = features;
    }

    public String getType() {
        return "FeatureCollection";
    }

    public void setType(String type) {
    }
    
    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

}
