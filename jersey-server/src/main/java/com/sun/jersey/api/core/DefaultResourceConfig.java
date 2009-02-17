/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
    
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    
    private final Set<Object> singletons = new HashSet<Object>();
    
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
     * @param classes the initial set of root resource classes 
     *        and provider classes
     */
    public DefaultResourceConfig(Class<?>... classes) {
        this(new HashSet<Class<?>>(Arrays.asList(classes)));
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
        
    /**
     * Get a cannonical array of String elements from a String array
     * where each entry may contain zero or more elements separated by ';'.
     * 
     * @param elements an array where each String entry may contain zero or more
     *        ';' separated elements.
     * @return the array of elements, each element is trimmed, the array will
     *         not contain any empty or null entries.
     */
    public static String[] getElements(String[] elements) {
        List<String> es = new LinkedList<String>();
        for (String element : elements) {
            if (element == null) continue;
            element = element.trim();
            if (element.length() == 0) continue;
            for (String subElement : getElements(element)) {
                if (subElement == null || subElement.length() == 0) continue;
                es.add(subElement);
            }
        }
        return es.toArray(new String[es.size()]);
    }
    
    /**
     * Get a cannonical array of String elements from a String
     * that may contain zero or more elements separated by ';'.
     * 
     * @param elements a String that may contain zero or more
     *        ';' separated elements.
     * @return the array of elements, each element is trimmed.
     */
    private static String[] getElements(String elements) {
        String[] es = elements.split(";");
        for (int i = 0; i < es.length; i++) {
            es[i] = es[i].trim();
        }
        return es;
    }     
}