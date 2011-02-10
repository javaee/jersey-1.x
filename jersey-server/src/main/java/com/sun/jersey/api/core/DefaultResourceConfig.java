/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.api.core;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A mutable implementation of {@link ResourceConfig} that declares
 * default values for features.
 * <p>
 * The set of features and properties may be modified by modifying the instances 
 * returned from the methods {@link ResourceConfig#getFeatures} and 
 * {@link ResourceConfig#getProperties} respectively.
 */
public class DefaultResourceConfig extends ResourceConfig {
    
    private final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    
    private final Set<Object> singletons = new LinkedHashSet<Object>(1);
    
    private final Map<String, MediaType> mediaExtentions = new HashMap<String, MediaType>(1);
    
    private final Map<String, String> languageExtentions = new HashMap<String, String>(1);

    private final Map<String, Object> explicitRootResources = new HashMap<String, Object>(1);

    private final Map<String, Boolean> features = new HashMap<String, Boolean>();
    
    private final Map<String, Object> properties = new HashMap<String, Object>();
    
    /**
     */
    public DefaultResourceConfig() {
        this((Set<Class<?>>)null);
    }
    
    /**
     * @param classes the initial set of root resource classes 
     *        and provider classes
     */
    public DefaultResourceConfig(Class<?>... classes) {
        this(new LinkedHashSet<Class<?>>(Arrays.asList(classes)));
    }
    
    /**
     * @param classes the initial set of root resource classes 
     *        and provider classes
     */
    public DefaultResourceConfig(Set<Class<?>> classes) {
        if (null != classes) {
            this.classes.addAll(classes);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
    
    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
    
    @Override
    public Map<String, MediaType> getMediaTypeMappings() {
        return mediaExtentions;
    }

    @Override
    public Map<String, String> getLanguageMappings() {
        return languageExtentions;
    }
    
    @Override
    public Map<String, Object> getExplicitRootResources() {
        return explicitRootResources;
    }

    @Override
    public Map<String, Boolean> getFeatures() {
        return features;
    }
    
    @Override
    public boolean getFeature(String featureName) {
        final Boolean v = features.get(featureName);
        return (v != null) ? v : false;
    }
    
    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }
}