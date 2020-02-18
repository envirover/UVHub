/*
 * Envirover confidential
 * 
 *  [2020] Envirover
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
