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

package com.sun.ws.rest.api.core;

import java.util.Map;
import java.util.Set;

/**
 * The resource configuration interface for configuring a web application.
 */
public interface ResourceConfig {
    /**
     * If true the request URI will be normalized as specified by 
     * {@link java.net.URI#normalize}. If not true the request URI is not
     * modified.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_NORMALIZE_URI 
            = "com.sun.ws.rest.config.feature.NormalizeURI";
    
    /**
     * If true the request URI path component will be canonicalized by removing 
     * contiguous slashes (i.e. all /+ will be replaced by /). If not true the
     * request URI path component is mot modified.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_CANONICALIZE_URI_PATH 
            = "com.sun.ws.rest.config.feature.CanonicalizeURIPath";
    
    /**
     * If true, and either NORMALIZE_URI or CANONICALIZE_URI_PATH is true, 
     * and the normalization and/or path canonicalization operations on the
     * request URI result in a new URI that is not equal to the request URI,
     * then the client is (temporarily) redirected to the new URI. Otherwise
     * the request URI is set to be the new URI.
     * <p>
     * If true, and the path value of a {@link javax.ws.rs.Path} annotation ends 
     * in a slash, the request URI path does not end in a '/' and would otherwise
     * match the path value if it did, then the client is (temporarily) 
     * redirected to a new URI that is the request URI with a '/' appended to the
     * the end of the path.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_REDIRECT 
            = "com.sun.ws.rest.config.feature.Redirect";
    
    /**
     * If true matrix parameters (if present) in the request URI path component
     * will be ignored when matching the path to URI templates declared by
     * resource classes.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_MATCH_MATRIX_PARAMS 
            = "com.sun.ws.rest.config.feature.IgnoreMatrixParams";
    
    /**
     * If true then the matching algorithm will attempt to match and accept
     * any static content or templates associated with a resource that were
     * not explicitly decared by that resource.
     * <p>
     * If a template is matched then the model for the viewable will be the
     * resource instance associated with the template.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_IMPLICIT_VIEWABLES 
            = "com.sun.ws.rest.config.feature.ImplicitViewables";
    
    /**
     * If set the default resource provider to be used by the 
     * {@link com.sun.ws.rest.spi.resource.ResourceProviderFactory}.
     * <p>
     * The type of this property must be a Java class that implementations
     * {@link com.sun.ws.rest.spi.resource.ResourceProvider}.
     * <p>
     * If not set the default resource provider will be the per-request 
     * resource provider
     */
    public static final String PROPERTY_DEFAULT_RESOURCE_PROVIDER_CLASS
            = "com.sun.ws.rest.config.property.DefaultResourceProviderClass";

    /**
     * Get the set of root resource classes to be deployed by the Web
     * application.
     * <p>
     * A root resource class is a Java class with a {@link javax.ws.rs.Path} 
     * annotation declared on the class.
     * 
     * @return the set of root resource classes. 
     *         The returned value shall never be null.
     */
    Set<Class<?>> getResourceClasses();
    
    /**
     * Get the set of provider classes to be instantiated in the scope
     * of the Web application.
     * <p>
     * A provider class is a Java class with a {@link javax.ws.rs.ext.Provider} 
     * annotation declared on the class that implements a specific service 
     * interface.
     * 
     * @return the set of provider classes. 
     *         The returned value shall never be null.
     */
    Set<Class<?>> getProviderClasses();
    
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
