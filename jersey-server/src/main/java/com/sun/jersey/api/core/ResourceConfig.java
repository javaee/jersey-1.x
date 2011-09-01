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

import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.core.header.LanguageTag;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * The resource configuration for configuring a web application.
 */
public abstract class ResourceConfig extends Application implements FeaturesAndProperties {
    private static final Logger LOGGER = 
            Logger.getLogger(ResourceConfig.class.getName());
    
    /**
     * If true and {@link #FEATURE_CANONICALIZE_URI_PATH} is true then the
     * request URI will be normalized as specified by
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
     * If true, and {@link #FEATURE_CANONICALIZE_URI_PATH} is true,
     * and the canonicalization/normalization operations on the
     * request URI result in a new URI that is not equal to the request URI,
     * then the client is (temporarily) redirected to the new URI.
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
     * If true then matrix parameters (if present) in the request URI path component
     * will not be ignored when matching the path to URI templates declared by
     * resource classes.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_MATCH_MATRIX_PARAMS 
            = "com.sun.jersey.config.feature.IgnoreMatrixParams";
    
    /**
     * If true then the matching algorithm will attempt to match and accept
     * any static content or templates associated with a resource that were
     * not explicitly declared by that resource.
     * <p>
     * If a template is matched then the model for the viewable will be the
     * resource instance associated with the template.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_IMPLICIT_VIEWABLES 
            = "com.sun.jersey.config.feature.ImplicitViewables";

    /**
     * If true then disable WADL generation.
     * <p>
     * By default WADL generation is automatically enabled, if JAXB is
     * present in the classpath.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_DISABLE_WADL
            = "com.sun.jersey.config.feature.DisableWADL";

    /**
     * If true then enable tracing.
     * <p>
     * Tracing provides useful information that describes how a request
     * is processed and dispatched to JAX-RS/Jersey components. This can aid
     * debugging when the application is not behaving as expected either
     * because of a bug in the application code or in the Jersey code.
     * <p>
     * Trace messages will be primarily output as response headers
     * with a header name of the form "X-Jersey-Trace-XXX", where XXX is a
     * decimal value corresponding to the trace message number, and a header
     * value that is the trace message.
     * <p>
     * In certain cases trace messages will be logged on the server-side if such
     * messages are not suitable as response headers, for example
     * if such messages are too verbose.
     * <p>
     * Trace messages will be output in the same order as traces occur.
     * <p>
     * To log response header trace messages on the server-side enable response
     * logging, see {@link LoggingFilter}.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_TRACE
            = "com.sun.jersey.config.feature.Trace";

    /**
     * If true then enable tracing on a per-request basis.
     * <p>
     * Tracing provides useful information that describes how a request
     * is processed and dispatched to JAX-RS/Jersey components. This can aid
     * debugging when the application is not behaving as expected either
     * because of a bug in the application code or in the Jersey code.
     * <p>
     * Trace messages will be output if a request header is present with a
     * header name of "X-Jersey-Trace-Accept" (the header value is ignored).
     * <p>
     * Trace messages will be primarily output as response headers
     * with a header name of the form "X-Jersey-Trace-XXX", where XXX is a
     * decimal value corresponding to the trace message number, and a header
     * value that is the trace message.
     * <p>
     * In certain cases trace messages will be logged on the server-side if such
     * messages are not suitable as response headers, for example
     * if such messages are too verbose.
     * <p>
     * Trace messages will be output in the same order as traces occur.
     * <p>
     * To log response header trace messages on the server-side enable response
     * logging, see {@link LoggingFilter}.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_TRACE_PER_REQUEST
            = "com.sun.jersey.config.feature.TracePerRequest";

    /**
     * If set the map of file extension to media type mappings that will be
     * added to the map that is returned from {@link #getMediaTypeMappings() }.
     * <p>
     * This type of this property must be a String or String[] that contains
     * one or more comma separated key value pairs of the form
     * "&lt;key&gt; : &lt;value&gt;, &lt;key&gt; : &lt;value&gt;" where the key
     * is the file extension and the value is the media type. For example,
     * the following declares two mappings for XML and JSON:
     * "xml : application/xml, json : application/json"
     * <p>
     * The value of this property will be validated, and keys/values added to
     * map returned from {@link #getMediaTypeMappings()}, when the
     * {@link #validate()} method is invoked.
     * <p>
     * Validation will verify that media types are valid.
     */
    public static final String PROPERTY_MEDIA_TYPE_MAPPINGS
            = "com.sun.jersey.config.property.MediaTypeMappings";

    /**
     * If set the map of file extension to langauge mappings that will be
     * added to the map that is returned from {@link #getLanguageMappings() }.
     * <p>
     * This type of this property must be a String or String[] that contains
     * one or more comma separated key value pairs of the form
     * "&lt;key&gt; : &lt;value&gt;, &lt;key&gt; : &lt;value&gt;" where the key
     * is the file extension and the value is the language tag. For example,
     * the following declares two mappings for the languages "en" and "en-US:
     * "english : en, american : en-US".
     * <p>
     * The value of this property will be validated, and keys/values added to
     * map returned from {@link #getMediaTypeMappings()}, when the
     * {@link #validate()} method is invoked.
     * <p>
     * Validation will verify that language tags are valid according to HTTP/1.1
     * and are of the form:
     * <pre>
     *   language-tag  = primary-tag *( "-" subtag )
     *   primary-tag   = 1*8ALPHA
     *   subtag        = 1*8ALPHA
     * </pre>
     */
    public static final String PROPERTY_LANGUAGE_MAPPINGS
            = "com.sun.jersey.config.property.LanguageMappings";

    /**
     * If set the default resource component provider factory for the
     * life-cycle of resource classes.
     * <p>
     * The type of this property must be a Class or a String that is a Class name,
     * and the Class must a sub-class of
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
     * to filter the request. When applying the list of request filters to
     * a request each request filter is applied, in order, from the first to
     * the last entry in the list.
     * <p>
     * The instance may be a String[] or String that contains one or more fully 
     * qualified class name of a request filter class separated by ';', ','
     * or ' ' (space).
     * Otherwise the instance may be List containing instances of String,
     * String[], Class&lt;? extends ContainerRequestFilter;&gt; or instances
     * of ContainerRequestFilter.
     * <p>
     * If a String[] or String of fully qualified class names or a Class then
     * each class is instantiated as a singleton. Thus, if there is more than one
     * class registered for this property or the same class is also registered for
     * the {@link #PROPERTY_CONTAINER_RESPONSE_FILTERS} property then only
     * one instance will be instantiated.
     * 
     * @see com.sun.jersey.api.container.filter
     */
    public static final String PROPERTY_CONTAINER_REQUEST_FILTERS = 
            "com.sun.jersey.spi.container.ContainerRequestFilters";
    
    /**
     * If set the list of {@link ContainerResponseFilter} that are applied
     * to filter the response. When applying the list of response filters to
     * a response each response filter is applied, in order, from the first to
     * the last entry in the list.
     * <p>
     * The instance may be a String[] or String that contains one or more fully
     * qualified class name of a request filter class separated by ';', ','
     * or ' ' (space).
     * Otherwise the instance may be List containing instances of String,
     * String[], Class&lt;? extends ContainerResponseFilter;&gt; or instances
     * of ContainerResponseFilter.
     * <p>
     * If a String[] or String of fully qualified class names or a Class then
     * each class is instantiated as a singleton. Thus, if there is more than one
     * class registered for this property or the same class is also registered for
     * the {@link #PROPERTY_CONTAINER_REQUEST_FILTERS} property then only
     * one instance will be instantiated.
     *
     * @see com.sun.jersey.api.container.filter
     */
    public static final String PROPERTY_CONTAINER_RESPONSE_FILTERS = 
            "com.sun.jersey.spi.container.ContainerResponseFilters";

    /**
     * If set the list of {@link ResourceFilterFactory} that are applied
     * to resources. When applying the list of resource filters factories to a
     * request each resource filter factory is applied, in order, from the first
     * to last entry in the list.
     * <p>
     * The instance may be a String[] or String that contains one or more fully
     * qualified class name of a response filter class separated by ';', ','
     * or ' ' (space).
     * Otherwise the instance may be List containing instances of String,
     * String[], Class&lt;? extends ResourceFilterFactory;&gt; or instances
     * of ResourceFilterFactory.
     * <p>
     * If a String[] or String of fully qualified class names or a Class then
     * each class is instantiated as a singleton. Thus, if there is more than one
     * class registered for this property one instance will be instantiated.
     * 
     * @see com.sun.jersey.api.container.filter
     */
    public static final String PROPERTY_RESOURCE_FILTER_FACTORIES =
            "com.sun.jersey.spi.container.ResourceFilters";

    /**
     * If set the wadl generator configuration that provides a {@link WadlGenerator}.
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
     * Common delimiters used by various properties.
     */
    public static final String COMMON_DELIMITERS = " ,;";
    
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
     * <p>
     * The default implementation returns an empty map.
     *
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
     * <p>
     * The default implementation returns an empty map.
     * 
     * @return a map of file extension to language
     */
    public Map<String, String> getLanguageMappings() {
        return Collections.emptyMap();
    }

    /**
     * Get a map of explicit root resource classes and root resource singleton
     * instances. The default lifecycle for root resource class instances is
     * per-request.
     * <p>
     * The root resource path template is declared using the key in the map. This
     * is a substitute for the declaration of a {@link Path} annotation on a root
     * resource class or singleton instance. The key has the same semantics as the
     * {@link Path#value() }. If such a {@link Path} annotation is present
     * it will be ignored.
     * <p>
     * For example, the following will register two root resources, first
     * a root resource class at the path "class" and a root resource singleton
     * at the path "singleton":
     * <blockquote><pre>
     *     getExplicitRootResources().put("class", RootResourceClass.class);
     *     getExplicitRootResources().put("singleton", new RootResourceSingleton());
     * </pre></blockquote>
     *
     * @return a map of explicit root resource classes and root resource 
     *         singleton instances.
     */
    public Map<String, Object> getExplicitRootResources() {
        return Collections.emptyMap();
    }

    /**
     * Validate the set of classes and singletons.
     * <p>
     * A registered class is removed from the set of registered classes
     * if an instance of that class is a member of the set of registered
     * singletons.
     * <p>
     * A registered class that is an interface or an abstract class
     * is removed from the registered classes.
     * <p>
     * File extension to media type and language mappings in the properties
     * {@link #PROPERTY_MEDIA_TYPE_MAPPINGS} and {@link #PROPERTY_LANGUAGE_MAPPINGS},
     * respectively, are processed and key/values pairs added to the maps
     * returned from {@link #getMediaTypeMappings() } and 
     * {@link #getLanguageMappings() }, respectively. The characters of file
     * extension values will be contextually encoded according to the set of 
     * valid characters defined for a path segment.
     * 
     * @throws IllegalArgumentException if the set of registered singletons 
     *         contains more than one instance of the same root resource class,
     *         or validation of media type and language mappings failed.
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
                        " is instantiated more than once in the set of registered singletons");
            }
            throw new IllegalArgumentException(
                    "The set of registered singletons contains " +
                    "more than one instance of the same root resource class");
        }

        // parse and validate mediaTypeMappings set thru PROPERTY_MEDIA_TYPE_MAPPINGS property
        parseAndValidateMappings(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS,
                getMediaTypeMappings(), new TypeParser<MediaType>() {
            public MediaType valueOf(String value) {
                return MediaType.valueOf(value);
            }
        });

        // parse and validate language mappings set thru PROPERTY_LANGUAGE_MAPPINGS property
        parseAndValidateMappings(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS,
                getLanguageMappings(), new TypeParser<String>() {
            public String valueOf(String value) {
                return LanguageTag.valueOf(value).toString();
            }
        });

        // encode key values of mediaTypeMappings and languageMappings maps
        encodeKeys(getMediaTypeMappings());
        encodeKeys(getLanguageMappings());
    }

    private interface TypeParser<T> {
        public T valueOf(String s);
    }
    
    private <T> void parseAndValidateMappings(String property,
            Map<String, T> mappingsMap, TypeParser<T> parser) {
        Object mappings = getProperty(property);
        if (mappings == null)
            return;

        if (mappings instanceof String) {
            parseMappings(property, (String) mappings, mappingsMap, parser);
        } else if (mappings instanceof String[]) {
            final String[] mappingsArray = (String[])mappings;
            for (int i = 0; i < mappingsArray.length; i++)
                parseMappings(property, mappingsArray[i], mappingsMap, parser);
        } else {
            throw new IllegalArgumentException("Provided " + property +
                    " mappings is invalid. Acceptable types are String" +
                    " and String[].");
        }
    }

    private <T> void parseMappings(String property, String mappings,
            Map<String, T> mappingsMap, TypeParser<T> parser) {
        if (mappings == null)
            return;
        
        String[] records = mappings.split(",");

        for(int i = 0; i < records.length; i++) {
            String[] record = records[i].split(":");
            if (record.length != 2)
                throw new IllegalArgumentException("Provided " + property +
                        " mapping \"" + mappings + "\" is invalid. It " +
                        "should contain two parts, key and value, separated by ':'.");

            String trimmedSegment = record[0].trim();
            String trimmedValue = record[1].trim();

            if (trimmedSegment.length() == 0)
                throw new IllegalArgumentException("The key in " + property +
                        " mappings record \"" + records[i] + "\" is empty.");
            if (trimmedValue.length() == 0)
                throw new IllegalArgumentException("The value in " + property +
                        " mappings record \"" + records[i] + "\" is empty.");

            mappingsMap.put(trimmedSegment, parser.valueOf(trimmedValue));
        }
    }

    private <T> void encodeKeys(Map<String, T> map) {
        Map<String, T> tempMap = new HashMap<String, T>();
        for(Map.Entry<String, T> entry : map.entrySet())
            tempMap.put(UriComponent.contextualEncode(entry.getKey(), UriComponent.Type.PATH_SEGMENT), entry.getValue());
        map.clear();
        map.putAll(tempMap);
    }

    /**
     * Get the set of root resource classes.
     * <p>
     * A root resource class is a registered class that is annotated with
     * Path.
     * 
     * @return the set of root resource classes.
     */
    public Set<Class<?>> getRootResourceClasses() {
        Set<Class<?>> s = new LinkedHashSet<Class<?>>();
        
        for (Class<?> c : getClasses()) {
            if (isRootResourceClass(c))
                s.add(c);
        }
        
        return s;
    }

    /**
     * Get the set of provider classes.
     * <p>
     * A provider class is a registered class that is not annotated with
     * Path.
     * 
     * @return the set of provider classes.
     */
    public Set<Class<?>> getProviderClasses() {
        Set<Class<?>> s = new LinkedHashSet<Class<?>>();
        
        for (Class<?> c : getClasses()) {
            if (!isRootResourceClass(c))
                s.add(c);
        }
        
        return s;
    }

    /**
     * Get the set of root resource singleton instances.
     * <p>
     * A root resource singleton instance is a registered instance whose class
     * is annotated with Path.
     * 
     * @return the set of root resource singleton instances.
     */
    public Set<Object> getRootResourceSingletons() {
        Set<Object> s = new LinkedHashSet<Object>();
        
        for (Object o : getSingletons()) {
            if (isRootResourceClass(o.getClass()))
                s.add(o);
        }
        
        return s;
    }
    
    /**
     * Get the set of provider singleton instances.
     * <p>
     * A provider singleton instances is a registered instance whose class
     * is not annotated with Path.
     * 
     * @return the set of provider singleton instances.
     */
    public Set<Object> getProviderSingletons() {
        Set<Object> s = new LinkedHashSet<Object>();
        
        for (Object o : getSingletons()) {
            if (!isRootResourceClass(o.getClass()))
                s.add(o);
        }
        
        return s;
    }

    /**
     * Determine if a class is a root resource class.
     *
     * @param c the class.
     * @return true if the class is a root resource class, otherwise false
     *         (including if the class is null).
     */
    public static boolean isRootResourceClass(Class<?> c) {
        if (c == null)
            return false;
        
        if (c.isAnnotationPresent(Path.class)) return true;

        for (Class i : c.getInterfaces())
            if (i.isAnnotationPresent(Path.class)) return true;

        return false;
    }

    /**
     * Determine if a class is a provider class.
     *
     * @param c the class.
     * @return true if the class is a provider class, otherwise false
     *         (including if the class is null)
     */
    public static boolean isProviderClass(Class<?> c) {
        return c != null && c.isAnnotationPresent(Provider.class);
    }

    /**
     * Get the list of container request filters.
     * <p>
     * This list may be modified to add or remove filter elements.
     * See {@link #PROPERTY_CONTAINER_REQUEST_FILTERS} for the valid elements
     * of the list.
     *
     * @return the list of container request filters.
     *         An empty list will be returned if no filters are present.
     */
    public List getContainerRequestFilters() {
        return getFilterList(PROPERTY_CONTAINER_REQUEST_FILTERS);
    }

    /**
     * Get the list of container response filters.
     * <p>
     * This list may be modified to add or remove filter elements.
     * See {@link #PROPERTY_CONTAINER_RESPONSE_FILTERS} for the valid elements
     * of the list.
     *
     * @return the list of container response filters.
     *         An empty list will be returned if no filters are present.
     */
    public List getContainerResponseFilters() {
        return getFilterList(PROPERTY_CONTAINER_RESPONSE_FILTERS);
    }

    /**
     * Get the list of resource filter factories.
     * <p>
     * This list may be modified to add or remove filter elements.
     * See {@link #PROPERTY_RESOURCE_FILTER_FACTORIES} for the valid elements
     * of the list.
     *
     * @return the list of resource filter factories.
     *         An empty list will be returned if no filters are present.
     */
    public List getResourceFilterFactories() {
        return getFilterList(PROPERTY_RESOURCE_FILTER_FACTORIES);
    }

    private List getFilterList(String propertyName) {
        final Object o = getProperty(propertyName);
        if (o == null) {
            final List l = new ArrayList();
            getProperties().put(propertyName, l);
            return l;
        } else if (o instanceof List) {
            return (List)o;
        } else {
            final List l = new ArrayList();
            l.add(o);
            getProperties().put(propertyName, l);
            return l;
        }
    }

    /**
     * Set the properties and features given a map of entries.
     *
     * @param entries the map of entries. All entries are added as properties.
     * Properties are only added if an existing property does not currently exist.
     *
     * Any entry with a value that is an instance of Boolean is added as a
     * feature with the feature name set to the entry name and the feature value
     * set to the entry value. Any entry with a value that is an instance String
     * and is equal (ignoring case and white space) to "true" or "false" is added
     * as a feature with the feature name set to the entry name and the feature
     * value set to the Boolean value of the entry value. Features are only added
     * if an existing feature does not currently exist.
     */
    public void setPropertiesAndFeatures(Map<String, Object> entries) {
        for (Map.Entry<String, Object> e : entries.entrySet()) {
            if (!getProperties().containsKey(e.getKey())) {
                getProperties().put(e.getKey(), e.getValue());
            }

            if (!getFeatures().containsKey(e.getKey())) {
                Object v = e.getValue();
                if (v instanceof String) {
                    String sv = ((String)v).trim();
                    if (sv.equalsIgnoreCase("true")) {
                        getFeatures().put(e.getKey(), true);
                    } else if (sv.equalsIgnoreCase("false")) {
                        getFeatures().put(e.getKey(), false);
                    }
                } else if (v instanceof Boolean) {
                    getFeatures().put(e.getKey(), (Boolean)v);
                }
            }
        }
    }

    /**
     * Add the state of an {@link Application} to this instance.
     *
     * @param app the application.
     */
    public void add(Application app) {
        if (app.getClasses() != null)
            addAllFirst(getClasses(), app.getClasses());
        if (app.getSingletons() != null)
            addAllFirst(getSingletons(), app.getSingletons());
        
        if (app instanceof ResourceConfig) {
            ResourceConfig rc = (ResourceConfig)app;

            getExplicitRootResources().putAll(rc.getExplicitRootResources());
            
            getLanguageMappings().putAll(rc.getLanguageMappings());
            getMediaTypeMappings().putAll(rc.getMediaTypeMappings());

            getFeatures().putAll(rc.getFeatures());
            getProperties().putAll(rc.getProperties());
        }
    }

    private <T> void addAllFirst(Set<T> a, Set<T> b) {
        Set<T> x = new LinkedHashSet<T>();
        x.addAll(b);
        x.addAll(a);
 
        a.clear();
        a.addAll(x);
    }

    /**
     * Clone this resource configuration.
     * <p>
     * The set of classes, set of singletons, map of explicit root resources,
     * map of language mappings, map of media type mappings, map of features and
     * map of properties will be cloned.
     *
     * @return a cloned instance of this resource configuration.
     */
    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public ResourceConfig clone() {
        ResourceConfig that = new DefaultResourceConfig();

        that.getClasses().addAll(this.getClasses());
        that.getSingletons().addAll(this.getSingletons());

        that.getExplicitRootResources().putAll(this.getExplicitRootResources());

        that.getLanguageMappings().putAll(this.getLanguageMappings());
        that.getMediaTypeMappings().putAll(this.getMediaTypeMappings());

        that.getFeatures().putAll(this.getFeatures());
        that.getProperties().putAll(this.getProperties());
        
        return that;
    }

    /**
     * Get a canonical array of String elements from a String array
     * where each entry may contain zero or more elements separated by ';'.
     *
     * @param elements an array where each String entry may contain zero or more
     *        ';' separated elements.
     * @return the array of elements, each element is trimmed, the array will
     *         not contain any empty or null entries.
     */
    public static String[] getElements(String[] elements) {
        // keeping backwards compatibility
        return getElements(elements, ";");
    }

    /**
     * Get a canonical array of String elements from a String array
     * where each entry may contain zero or more elements separated by characters
     * in delimiters string.
     *
     * @param elements an array where each String entry may contain zero or more
     *        delimiters separated elements.
     * @param delimiters string with delimiters, every character represents one
     *        delimiter.
     * @return the array of elements, each element is trimmed, the array will
     *         not contain any empty or null entries.
     */
    public static String[] getElements(String[] elements, String delimiters) {
        List<String> es = new LinkedList<String>();
        for (String element : elements) {
            if (element == null) continue;
            element = element.trim();
            if (element.length() == 0) continue;
            for (String subElement : getElements(element, delimiters)) {
                if (subElement == null || subElement.length() == 0) continue;
                es.add(subElement);
            }
        }
        return es.toArray(new String[es.size()]);
    }

    /**
     * Get a canonical array of String elements from a String
     * that may contain zero or more elements separated by characters in
     * delimiters string.
     *
     * @param elements a String that may contain zero or more
     *        delimiters separated elements.
     * @param delimiters string with delimiters, every character represents one
     *        delimiter.
     * @return the array of elements, each element is trimmed.
     */
    private static String[] getElements(String elements, String delimiters) {
        String regex = "[";
        for(char c : delimiters.toCharArray())
            regex += Pattern.quote(String.valueOf(c));
        regex += "]";

        String[] es = elements.split(regex);
        for (int i = 0; i < es.length; i++) {
            es[i] = es[i].trim();
        }
        return es;
    }
}
