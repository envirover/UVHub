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

package com.envirover.geojson;

import java.util.ArrayList;
import java.util.Collection;

/**
 * GeoJSON feature collection. 
 * 
 * @author Pavel
 *
 */
public class FeatureCollection {

    private final Collection<Feature> features;

    public FeatureCollection() {
        this.features = new ArrayList<Feature>();
    }
    
    public FeatureCollection(Collection<Feature> features) {
        this.features = features;
    }

    public String getType() {
        return "FeatureCollection";
    }

    public Collection<Feature> getFeatures() {
        return features;
    }

}
