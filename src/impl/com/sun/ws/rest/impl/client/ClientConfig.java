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

package com.sun.ws.rest.impl.client;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface ClientConfig {
    /**
     * Get the set of provider classes to be instantiated in the scope
     * of the Client
     * <p>
     * A provider class is a Java class with a {@link javax.ws.rs.ext.Provider} 
     * annotation declared on the class that implements a specific service 
     * interface.
     * 
     * @return the set of provider classes. 
     *         The returned value shall never be null.
     */
    Set<Class> getProviderClasses();
    
    /**
     * Get the map of features associated with the Web application.
     *
     * @return the features.
     *         The returned value shall never be null.
     */
    Map<String, Boolean> getFeatures();
    
    /**
     * Get the value of a feature.
     *
     * @param featureName the feature name.
     * @return true if the feature is present and set to true, otherwise false
     *         if the feature is present and set to false or the feature is not 
     *         present.
     */
    boolean getFeature(String featureName);
    
    /**
     * Get the map of properties associated with the Web application.
     *
     * @return the properties.
     *         The returned value shall never be null.
     */
    Map<String, Object> getProperties();

    /**
     * Get the value of a property.
     *
     * @param propertyName the property name.
     * @return the property, or null if there is no property present for the
     *         given property name.
     */
    Object getProperty(String propertyName);
}
