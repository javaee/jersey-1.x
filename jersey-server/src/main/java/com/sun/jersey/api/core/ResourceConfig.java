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

import com.sun.jersey.impl.wadl.WadlGenerator;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;


/**
 * The resource configuration for configuring a web application.
 */
public abstract class ResourceConfig extends Application {
    private static final Logger LOGGER = 
            Logger.getLogger(ResourceConfig.class.getName());
    
    /**
     * If true the request URI will be normalized as specified by 
     * {@link java.net.URI#normalize}. If not true the request URI is not
     * modified.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_NORMALIZE_URI 
            = "com.sun.jersey.config.feature.NormalizeURI";
    
    /**
     * If true the request URI path component will be canonicalized by removing 
     * contiguous slashes (i.e. all /+ will be replaced by /). If not true the
     * request URI path component is mot modified.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_CANONICALIZE_URI_PATH 
            = "com.sun.jersey.config.feature.CanonicalizeURIPath";
    
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
            = "com.sun.jersey.config.feature.Redirect";
    
    /**
     * If true matrix parameters (if present) in the request URI path component
     * will be ignored when matching the path to URI templates declared by
     * resource classes.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_MATCH_MATRIX_PARAMS 
            = "com.sun.jersey.config.feature.IgnoreMatrixParams";
    
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
            = "com.sun.jersey.config.feature.ImplicitViewables";
    
    /**
     * If set the default resource component provider factory for the
     * life-cycle of resource classes.
     * <p>
     * The type of this property must be a Java class that implementations
     * {@link com.sun.jersey.server.spi.component.ResourceComponentProviderFactory}.
     * <p>
     * If not set the default resource component provider factory will be the 
     * per-request resource component provider factory.
     */
    public static final String PROPERTY_DEFAULT_RESOURCE_COMPONENT_PROVIDER_FACTORY_CLASS
            = "com.sun.jersey.config.property.DefaultResourceComponentProviderFactoryClass";
    
    /**
     * If set the instance of {@link ContainerNotifier} to register
     * {@link ContainerListener} instances.
     * <p>
     * If the instance does not implement the {@link ContainerNotifier}
     * then the property is ignored.
     */
    public static final String PROPERTY_CONTAINER_NOTIFIER = 
            "com.sun.jersey.spi.container.ContainerNotifier";
    
    /**
     * If set the list of {@link ContainerRequestFilter} that are applied
     * to filter the request. The filters are applied in order form 
     * first to last as declared.
     * <p>
     * The instance may be a String[] or String that contains one or more fully 
     * qualified class name of a request filter class separeted by ';'.
     * Otherwise the instance may be List&lt;ContainerRequestFilter&gt;.
     */
    public static final String PROPERTY_CONTAINER_REQUEST_FILTERS = 
            "com.sun.jersey.spi.container.ContainerRequestFilters";
    
    /**
     * If set the list of {@link ContainerResponseFilter} that are applied
     * to filter the response. The filters are applied in order form 
     * first to last as declared.
     * <p>
     * The instance may be a String[] or String that contains one or more fully 
     * qualified class name of a response filter class separeted by ';'.
     * Otherwise the instance may be List&lt;ContainerResponseFilter&gt;.
     */
    public static final String PROPERTY_CONTAINER_RESPONSE_FILTERS = 
            "com.sun.jersey.spi.container.ContainerResponseFilters";

    /**
     * If set the wadl generator configuration that provides that {@link WadlGenerator}
     * to be used by the {@link com.sun.jersey.impl.wadl.WadlFactory}.
     * <p>
     * The type of this property must be a subclass or an instance of a subclass of
     * {@link com.sun.jersey.api.wadl.config.WadlGeneratorConfig}.
     * </p>
     * <p>
     * If this property is not set the default wadl generator will be used for generating wadl.
     * </p>
     */
    public static final String PROPERTY_WADL_GENERATOR_CONFIG = 
            "com.sun.jersey.config.property.WadlGeneratorConfig";
    
    /**
     * Get the map of features associated with the Web application.
     *
     * @return the features.
     *         The returned value shall never be null.
     */
    public abstract Map<String, Boolean> getFeatures();
    
    /**
     * Get the value of a feature.
     *
     * @param featureName the feature name.
     * @return true if the feature is present and set to true, otherwise false
     *         if the feature is present and set to false or the feature is not 
     *         present.
     */
    public abstract boolean getFeature(String featureName);
    
    /**
     * Get the map of properties associated with the Web application.
     *
     * @return the properties.
     *         The returned value shall never be null.
     */
    public abstract Map<String, Object> getProperties();

    /**
     * Get the value of a property.
     *
     * @param propertyName the property name.
     * @return the property, or null if there is no property present for the
     *         given property name.
     */
    public abstract Object getProperty(String propertyName);
    
    /**
     * Get a map of file extension to media type. This is used to drive 
     * URI-based content negotiation such that, e.g.:
     * <pre>GET /resource.atom</pre>
     * <p>is equivalent to:</p>
     * <pre>GET /resource
     *Accept: application/atom+xml</pre>
     * <p>The default implementation returns an empty map.</p>
     * @return a map of file extension to media type
     */
    public Map<String, MediaType> getMediaTypeMappings() {
        return Collections.emptyMap();
    }

    /**
     * Get a map of file extension to language. This is used to drive 
     * URI-based content negotiation such that, e.g.:
     * <pre>GET /resource.english</pre>
     * <p>is equivalent to:</p>
     * <pre>GET /resource
     *Accept-Language: en</pre>
     * <p>The default implementation returns an empty map.</p>
     * @return a map of file extension to language
     */
    public Map<String, String> getLanguageMappings() {
        return Collections.emptyMap();
    }
    
    /**
     * Validate the set of classes and singletons.
     * <p>
     * A registered class is removed from the set of registered classes
     * if an instance of that class is a member of the set of registered
     * singletons.
     * <p>A registered class that is an interface of an abstract class
     * is removed from the registered classes.
     * 
     * @throws IllegalArgumentException if the set of registered singletons 
     *         contains more than one instance of the same root resource class.
     */
    public void validate() {
        // Remove any registered classes if instances exist in registered 
        // singletons
        Iterator<Class<?>> i = getClasses().iterator();
        while (i.hasNext()) {
            Class<?> c = i.next();
            for (Object o : getSingletons()) {
                if (c.isInstance(o)) {
                    i.remove();
                    LOGGER.log(Level.WARNING, 
                            "Class " + c.getName() + 
                            " is ignored as an instance is registered in the set of singletons");                    
                }
            }            
        }


        // Remove any classes that cannot be instantiated
        i = getClasses().iterator();
        while (i.hasNext()) {
            Class<?> c = i.next();
            int modifiers = c.getModifiers();
            if (Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers)) {
                LOGGER.warning("The abstract " + c + ", registered as a root resource class " +
                        "of the ResourceConfig cannot be instantiated" +
                        ". This class will be ignored");
                i.remove();
            } else if (Modifier.isInterface(modifiers)) {
                LOGGER.warning("The " + c + ", registered as a root resource class " +
                        "of the ResourceConfig cannot be instantiated" +
                        ". This interface will be ignored");
                i.remove();
            }
        }
        
        // Find conflicts
        Set<Class<?>> objectClassSet = new HashSet<Class<?>>();
        Set<Class<?>> conflictSet = new HashSet<Class<?>>();
        for (Object o : getSingletons()) {
            if (o.getClass().isAnnotationPresent(Path.class)) {
                if (objectClassSet.contains(o.getClass())) {
                    conflictSet.add(o.getClass());
                } else {
                    objectClassSet.add(o.getClass());
                }
            }
        }
        
        if (!conflictSet.isEmpty()) {
            for (Class<?> c : conflictSet) {
                LOGGER.log(Level.SEVERE, 
                        "Root resource class " + c.getName() + 
                        " is instantated more than once in the set of registered singletons");                                    
            }
            throw new IllegalArgumentException(
                    "The set of registered singletons contains " +
                    "more than one instance of the same root resource class");
        }
    }
    
    /**
     * Get the set of root resource classes.
     * <p>
     * A root resource class is a registered class that is annotated with
     * Path.
     * 
     * @return the unmodifiable set of root resource classes.
     */
    public Set<Class<?>> getRootResourceClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        
        for (Class<?> c : getClasses()) {
            if (isRootResourceClass(c))
                s.add(c);
        }
        
        return Collections.unmodifiableSet(s);
    }

    /**
     * Get the set of provider classes.
     * <p>
     * A provider class is a registered class that is not annotated with
     * Path.
     * 
     * @return the unmodifiable set of provider classes.
     */
    public Set<Class<?>> getProviderClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        
        for (Class<?> c : getClasses()) {
            if (!c.isAnnotationPresent(Path.class))
                s.add(c);
        }
        
        return Collections.unmodifiableSet(s);
    }

    /**
     * Get the set of root resource singleton instances.
     * <p>
     * A root resource singleton instance is a registered instance whose class
     * is annotated with Path.
     * 
     * @return the unmodifiable set of root resource singleton instances.
     */
    public Set<Object> getRootResourceSingletons() {
        Set<Object> s = new HashSet<Object>();
        
        for (Object o : getSingletons()) {
            if (isRootResourceClass(o.getClass()))
                s.add(o);
        }
        
        return Collections.unmodifiableSet(s);
    }
    
    /**
     * Get the set of provider singleton instances.
     * <p>
     * A provider singleton instances is a registered instance whose class
     * is not annotated with Path.
     * 
     * @return the unmodifiable set of provider singleton instances.
     */
    public Set<Object> getProviderSingletons() {
        Set<Object> s = new HashSet<Object>();
        
        for (Object o : getSingletons()) {
            if (!o.getClass().isAnnotationPresent(Path.class))
                s.add(o);
        }
        
        return Collections.unmodifiableSet(s);
    }

    /**
     * Determine if a class is a root resource class.
     *
     * @param c the class.
     * @return true if the class is a root resource class, otherwise false.
     */
    public static boolean isRootResourceClass(Class<?> c) {
        if (c.isAnnotationPresent(Path.class)) return true;

        for (Class i : c.getInterfaces())
            if (i.isAnnotationPresent(Path.class)) return true;

        return false;
    }

    /**
     * Determine if a class is a provider class.
     *
     * @param c the class.
     * @return true if the class is a provider class, otherwise false.
     */
    public static boolean isProviderClass(Class<?> c) {
        return c.isAnnotationPresent(Provider.class);
    }
}