/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.jersey.api.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;

/**
 * A mutable implementation of {@link ResourceConfig} that declares
 * default values for features.
 * <p>
 * The set of features and properties may be modified by modifying the instances 
 * returned from the methods {@link ResourceConfig#getFeatures} and 
 * {@link ResourceConfig#getProperties} respectively.
 */
public class DefaultResourceConfig extends ResourceConfig {
    
    private final Set<Class<?>> resources = new HashSet<Class<?>>();
    
    private final Set<Class<?>> providers = new HashSet<Class<?>>();
    
    private final Map<String, MediaType> mediaExtentions = new HashMap<String, MediaType>();
    
    private final Map<String, String> languageExtentions = new HashMap<String, String>();
    
    private final Map<String, Boolean> features = new HashMap<String, Boolean>();
    
    private final Map<String, Object> properties = new HashMap<String, Object>();
    
    /**
     */
    public DefaultResourceConfig() {
        this((Set<Class<?>>)null);
    }
    
    /**
     * @param resources the initial set of root resource classes
     */
    public DefaultResourceConfig(Class<?>... resources) {
        this(new HashSet<Class<?>>(Arrays.asList(resources)));
    }
    
    /**
     * @param resources the initial set of root resource classes
     */
    public DefaultResourceConfig(Set<Class<?>> resources) {
        this.features.put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, false);
        this.features.put(ResourceConfig.FEATURE_MATCH_MATRIX_PARAMS, false);
        this.features.put(ResourceConfig.FEATURE_NORMALIZE_URI, false);
        this.features.put(ResourceConfig.FEATURE_REDIRECT, false);
        this.features.put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, false);
        if (null != resources) {
            this.resources.addAll(resources);
        }
    }

    public Set<Class<?>> getResourceClasses() {
        return resources;
    }

    @Override
    public Set<Class<?>> getProviderClasses() {
        return providers;
    }
    
    @Override
    public Map<String, MediaType> getMediaTypeMappings() {
        return mediaExtentions;
    }

    @Override
    public Map<String, String> getLanguageMappings() {
        return languageExtentions;
    }
    
    public Map<String, Boolean> getFeatures() {
        return features;
    }
    
    public boolean getFeature(String featureName) {
        final Boolean v = features.get(featureName);
        return (v != null) ? v : false;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }

    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }
}