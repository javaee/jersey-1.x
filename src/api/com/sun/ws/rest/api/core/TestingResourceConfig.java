/*
 * TestingResourceConfig.java
 *
 * Created on August 14, 2007, 5:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.ws.rest.impl.test.util;

import com.sun.ws.rest.api.core.ResourceConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author japod
 */
public class TestingResourceConfig implements ResourceConfig {
    
    private final Map<String, Boolean> features = new HashMap<String, Boolean>();
    
    private Set<Class> resources = new HashSet<Class>();
    
    public TestingResourceConfig() {
        this(null);
    }
    
    public TestingResourceConfig(Set<Class> resources) {
        this.features.put(ResourceConfig.CANONICALIZE_URI_PATH, true);
        this.features.put(ResourceConfig.IGNORE_MATRIX_PARAMS, true);
        this.features.put(ResourceConfig.NORMALIZE_URI, true);
        this.features.put(ResourceConfig.REDIRECT, true);
        if (null != resources) {
            this.resources.addAll(resources);
        }
    }

    public Set<Class> getResourceClasses() {
        return resources;
    }

    public Map<String, Boolean> getFeatures() {
        return features;
    }
    
}
