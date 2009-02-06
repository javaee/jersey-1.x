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

package com.sun.jersey.api.json;

import com.sun.jersey.json.impl.JSONMarshaller;
import com.sun.jersey.json.impl.JSONUnmarshaller;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

/**
 * An adaption of {@link JAXBContext} that supports marshalling
 * and unmarshalling of JAXB beans using the JSON format.
 * <p>
 * The JSON format may be configured by using a {@link JSONConfiguration} object 
 * as a constructor parameter of this class.
 */
public final class JSONJAXBContext extends JAXBContext implements JSONConfigurated {
    
    /**
     * A namespace for JSONJAXBContext related properties names.
     */
    @Deprecated
    public static final String NAMESPACE = "com.sun.jersey.impl.json.";

    /**
     * Enumeration of supported JSON notations.
     */
     @Deprecated
    public enum JSONNotation {
        /**
         * The mapped (default) JSON notation.
         */
        @Deprecated
        MAPPED,
        /**
         * The mapped Jettison JSON notation.
         */
        @Deprecated
        MAPPED_JETTISON,
        /**
         * The mapped Badgerfish JSON notation.
         */
        @Deprecated
        BADGERFISH,
        /**
         * The natural JSON notation, leveraging tight JAXB RI integration.
         */
        @Deprecated
        NATURAL
    };

    /**
     * JSON notation property is now deprecated. See {@link JSONConfiguration}.
     * <p>
     * The type of this property is enum type {@link JSONNotation}.
     * <p>
     * The value may be one of the following that are the currently supported JSON
     * notations: <code>JSONNotation.MAPPED</code>,
     * <code>JSONNotation.MAPPED_JETTISON</code> and <code>JSONNotation.BADGERFISH</code>
     * <p>
     * The default value is <code>JSONNotation.MAPPED</code>.
     */
    @Deprecated
    public static final String JSON_NOTATION = NAMESPACE + "notation";
    
    /**
     * JSON enabled property is now deprecated. See {@link JSONConfiguration}.
     * <p>
     * The type of this property is {@link Boolean}
     * <p>
     * If set to true, JSON will be serialized/deserialized instead of XML
     * <p>
     * The default value is false.
     */
    @Deprecated
    public static final String JSON_ENABLED = NAMESPACE + "enabled";
    
    /**
     * XML root element unwrapping property is now deprecated. See {@link JSONConfiguration}.
     * <p>
     * The type of this property is {@link Boolean}
     * <p>
     * If set to true, JSON code corresponding to the XML root element will be stripped out
     * for <code>JSONNotation.MAPPED</code> (default) notation.
     * <p>
     * The default value is false.
     */
    @Deprecated
    public static final String JSON_ROOT_UNWRAPPING = NAMESPACE + "root.unwrapping";
    
    /**
     * JSON arrays property is now deprecated. See {@link JSONConfiguration}.
     * This property is valid for the <code>JSONNotation.MAPPED</code> notation only.
     * <p>
     * The type of this property is <code>java.util.Collection&lt;String&gt;</code>.
     * <p>
     * The value is a collection of string values that are
     * object names.
     * The value of an object name in the JSON document that exists in the collection
     * of object names will be declared as an array, even if only one
     * element is present.
     * <p>
     * For example, consider that the the property value is not set and the
     * JSON document is <code>{ ..., "arr1":"single element", ... }</code>.
     * If the property value is set to contain <code>"arr1"</code> then
     * the JSON document would be <code>{ ..., "arr1":["single element"], ... }</code>.
     * <p>
     * The default value is an empty collection.
     */
    @Deprecated
    public static final String JSON_ARRAYS = NAMESPACE + "arrays";

    /**
     * JSON non-string values property is now deprecated. See {@link JSONConfiguration}.
     * This property is valid for the <code>JSONNotation.MAPPED</code> notation only.
     * <p>
     * The type of this property is <code>Collection&lt;String&gt;</code>.
     * <p>
     * The value is collection of string values that are
     * object names.
     * The value of an object name in the JSON document that exists in the collection
     * of object names will be declared as non-string value, which is not surrounded 
     * by double quotes.
     * <p>
     * For example, consider that the the property value is not set and the
     * JSON document is <code>{ ..., "anumber":"12", ... }</code>.
     * If the property value is set to contain <code>"anumber"</code>
     * then the JSON document would be <code>{ ..., "anumber":12, ... }</code>.
     * <p>
     * The default value is an empty collection.
     */
    @Deprecated
    public static final String JSON_NON_STRINGS = NAMESPACE + "non.strings";

    /**
     * JSON attributes as elements property is now deprecated. See {@link JSONConfiguration}.
     * This property is valid for the <code>JSONNotation.MAPPED</code> notation only.
     * <p>
     * The type of this property is <code>Collection&lt;String&gt;</code>.
     * <p>
     * The value is a collection of string values that are
     * object names that correspond to XML attribute information items.
     * The value of an object name in the JSON document that exists in the collection
     * of object names will be declared as an element as not as an attribute if
     * the object corresponds to an XML attribute information item.
     * <p>
     * For example, consider that the the property value is not set and the
     * JSON document is <code>{ ..., "@number":"12", ... }</code>.
     * If the property value is set contain <code>"number"</code>
     * then the JSON document would be <code>{ ..., "number":"12", ... }</code>.
     * <p>
     * The default value is an empty collection.
     */
    @Deprecated
    public static final String JSON_ATTRS_AS_ELEMS = NAMESPACE + "attrs.as.elems";

    /**
     * XML to JSON namespace mapping property is now deprecated. See {@link JSONConfiguration}.
     * This property is valid for the MAPPED_JETTISON notation only.
     * <p>
     * <p>
     * The type of this property is <code>Map&lt;String,String&gt;</code>.
     * <p>
     * The value is a map with zero or more
     * key/value pairs, where the key is an XML namespace and the value
     * is the prefix to use as the replacement for the XML namespace.
     * <p>
     * The default value is a map with zero key/value pairs.
     */
    @Deprecated
    public static final String JSON_XML2JSON_NS = NAMESPACE + "xml.to.json.ns";
        
    private static final Map<String, Object> defaultJsonProperties = new HashMap<String, Object>();
    
    static {
        defaultJsonProperties.put(JSON_NOTATION, JSONNotation.MAPPED);
        defaultJsonProperties.put(JSON_ROOT_UNWRAPPING, Boolean.TRUE);
    }
    
    private  JSONConfiguration jsonConfiguration;
    private final JAXBContext jaxbContext;
    
    /**
     * Constructs a new instance with default {@link JSONConfiguration}.
     * 
     * @param classesToBeBound list of java classes to be recognized by the 
     *        new JSONJAXBContext. Can be empty, in which case a JSONJAXBContext
     *        that only knows about spec-defined classes will be returned. 
     * @throws JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JSONJAXBContext(Class... classesToBeBound) throws JAXBException {
        this(JSONConfiguration.DEFAULT, classesToBeBound);
    }

    /**
     * Constructs a new instance with given {@link JSONConfiguration}.
     *
     * @param config {@link JSONConfiguration}, can not be null
     * @param classesToBeBound list of java classes to be recognized by the
     *        new JSONJAXBContext. Can be empty, in which case a JSONJAXBContext
     *        that only knows about spec-defined classes will be returned.
     * @throws JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JSONJAXBContext(final JSONConfiguration config, final Class... classesToBeBound) throws JAXBException {
        if (config == null)
            throw new IllegalArgumentException("JSONConfiguration MUST not be null");
        
        jsonConfiguration = config;
        if (config.getNotation() == JSONConfiguration.Notation.NATURAL) {
            jaxbContext = JAXBContext.newInstance(classesToBeBound, new HashMap<String,Object>(1){{put(JAXBContextImpl.RETAIN_REFERENCE_TO_INFO, Boolean.TRUE);}});
        } else {
            jaxbContext = JAXBContext.newInstance(classesToBeBound);
        }
    }

    /**
     * Constructs a new instance with a custom set of properties. 
     * The default {@link JSONConfiguration} is used if no (now deprecated)
     * JSON related properties are specified
     * 
     * @param classesToBeBound list of java classes to be recognized by the 
     *        new JSONJAXBContext. Can be empty, in which case a JSONJAXBContext
     *        that only knows about spec-defined classes will be returned. 
     * @param properties the custom set of properties. If it contains(now deprecated) JSON related properties,
     *                  then a non-default {@link JSONConfiguration} is used reflecting the JSON properties
     * @throws JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JSONJAXBContext(Class[] classesToBeBound, Map<String, Object> properties) 
            throws JAXBException {
        jaxbContext = JAXBContext.newInstance(classesToBeBound, 
                createProperties(properties));
        if(jsonConfiguration == null) {
            jsonConfiguration = JSONConfiguration.DEFAULT;
        }
    }

    /**
     * Constructs a new instance with a custom set of properties.
     * If no (now deprecated) JSON related properties are specified,
     * the {@link JSONConfiguration.DEFAULT} is used as {@link JSONConfiguration}
     *
     * @param config {@link JSONConfiguration}, can not be null
     * @param classesToBeBound list of java classes to be recognized by the
     *        new JSONJAXBContext. Can be empty, in which case a JSONJAXBContext
     *        that only knows about spec-defined classes will be returned.
     * @param properties the custom set of properties.
     * @throws JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JSONJAXBContext(final JSONConfiguration config, final Class[] classesToBeBound, final Map<String, Object> properties)
            throws JAXBException {
        if (config == null)
            throw new IllegalArgumentException("JSONConfiguration MUST not be null");
        
        jsonConfiguration = config;
        if (config.getNotation() == JSONConfiguration.Notation.NATURAL) {
            Map<String,Object> myProps = new HashMap<String, Object>(properties.size() + 1);
            myProps.putAll(properties);
            myProps.put(JAXBContextImpl.RETAIN_REFERENCE_TO_INFO, Boolean.TRUE);
            jaxbContext = JAXBContext.newInstance(classesToBeBound, myProps);
        } else {
            jaxbContext = JAXBContext.newInstance(classesToBeBound, properties);
        }
    }

    /**
     * Construct a new instance of using context class loader of the thread
     * with default {@link JSONConfiguration}.
     * 
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @throws JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JSONJAXBContext(String contextPath)
            throws JAXBException {
        this(JSONConfiguration.DEFAULT, contextPath);
    }

    /**
     * Construct a new instance of using context class loader of the thread
     * with given {@link JSONConfiguration}.
     *
     * @param config {@link JSONConfiguration}, can not be null
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @throws JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JSONJAXBContext(JSONConfiguration config, String contextPath)
            throws JAXBException {
        if (config == null)
            throw new IllegalArgumentException("JSONConfiguration MUST not be null");
        
        if (config.getNotation() == JSONConfiguration.Notation.NATURAL) {
            jaxbContext = JAXBContext.newInstance(contextPath, Thread.currentThread().getContextClassLoader(), new HashMap<String,Object>(1){{put(JAXBContextImpl.RETAIN_REFERENCE_TO_INFO, Boolean.TRUE);}});
        } else {
            jaxbContext = JAXBContext.newInstance(contextPath, Thread.currentThread().getContextClassLoader());
        }
        jsonConfiguration = config;
    }
    
    /**
     * Construct a new instance using a specified class loader with
     * default  {@link JSONConfiguration}.
     * 
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @param classLoader 
     * @throws JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JSONJAXBContext(String contextPath, ClassLoader classLoader)
            throws JAXBException {
        jaxbContext = JAXBContext.newInstance(contextPath, classLoader);
        jsonConfiguration = JSONConfiguration.DEFAULT;
    }
    
    /**
     * Construct a new instance using a specified class loader and 
     * a custom set of properties. {@link JSONConfiguration} is set to default,
     * if user does not specify any (now deprecated) JSON related properties
     * 
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @param classLoader 
     * @param properties the custom set of properties.
     * @throws JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JSONJAXBContext(String contextPath, ClassLoader classLoader,  Map<String, Object> properties)
            throws JAXBException {
        jaxbContext = JAXBContext.newInstance(contextPath, classLoader, createProperties(properties));
        if(jsonConfiguration == null) {
            jsonConfiguration = JSONConfiguration.DEFAULT;
        }
    }

    /**
     * Construct a new instance using a specified class loader,
     * set of properties and {@link JSONConfiguration} .
     *
     * @param config {@link JSONConfiguration}, can not be null
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @param classLoader
     * @param properties the custom set of properties.
     * @throws JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JSONJAXBContext(JSONConfiguration config, String contextPath, ClassLoader classLoader,  Map<String, Object> properties)
            throws JAXBException {
        if (config == null)
            throw new IllegalArgumentException("JSONConfiguration MUST not be null");
        
        if (config.getNotation() == JSONConfiguration.Notation.NATURAL) {
            Map<String,Object> myProps = new HashMap<String, Object>(properties.size() + 1);
            myProps.putAll(properties);
            myProps.put(JAXBContextImpl.RETAIN_REFERENCE_TO_INFO, Boolean.TRUE);
            jaxbContext = JAXBContext.newInstance(contextPath,  classLoader,  myProps);
        } else {
            jaxbContext = JAXBContext.newInstance(contextPath,  classLoader,  properties);
        }
        jsonConfiguration = config;
    }

    public JSONConfiguration getJSONConfiguration() {
        return jsonConfiguration;
    }
    
    private Map<String, Object> createProperties(Map<String, Object> properties) {
        Map<String, Object> workProperties = new HashMap<String, Object>();
        workProperties.putAll(defaultJsonProperties);
        workProperties.putAll(properties);
        if (JSONJAXBContext.JSONNotation.NATURAL == workProperties.get(JSONJAXBContext.JSON_NOTATION)) {
            workProperties.put(JAXBContextImpl.RETAIN_REFERENCE_TO_INFO, Boolean.TRUE);
        }
        processProperties(workProperties);
        return workProperties;
    }
   
    /**
     * Overrides underlaying createUnmarshaller method and returns
     * an unmarshaller which is capable of JSON deserialization.
     * 
     * @return unmarshaller instance with JSON capabilities
     * @throws javax.xml.bind.JAXBException
     */
    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        return new JSONUnmarshaller(jaxbContext, getJSONConfiguration());
    }

    /**
     * Overrides underlaying createMarshaller method and returns
     * a marshaller which is capable of JSON serialization.
     * 
     * @return marshaller instance with JSON capabilities
     * @throws javax.xml.bind.JAXBException
     */
    @Override
    public Marshaller createMarshaller() throws JAXBException {
        return new JSONMarshaller(jaxbContext, getJSONConfiguration());
    }

    /**
     * Simply delegates to underlying JAXBContext implementation.
     * 
     * @return what underlying JAXBContext returns
     * @throws javax.xml.bind.JAXBException
     */
    @Override
    public Validator createValidator() throws JAXBException {
        return jaxbContext.createValidator();
    }

    static final Map<String, JSONConfiguration.Notation> _notationMap = new HashMap<String, JSONConfiguration.Notation>() {
        {
            put(JSONJAXBContext.JSONNotation.BADGERFISH.toString(), JSONConfiguration.Notation.BADGERFISH);
            put(JSONJAXBContext.JSONNotation.MAPPED.toString(), JSONConfiguration.Notation.MAPPED);
            put(JSONJAXBContext.JSONNotation.MAPPED_JETTISON.toString(), JSONConfiguration.Notation.MAPPED_JETTISON);
            put(JSONJAXBContext.JSONNotation.NATURAL.toString(), JSONConfiguration.Notation.NATURAL);
        }
    };
    
    private final void processProperties(Map<String, Object> properties) {
        final Collection<String> jsonKeys = new HashSet<String>();
        for (String k : Collections.unmodifiableSet(properties.keySet())) {
            if (k.startsWith(NAMESPACE)) {
                jsonKeys.add(k);
            }
        }
        if (!jsonKeys.isEmpty()) {
            if (jsonConfiguration == null) {
                JSONConfiguration.Notation pNotation = JSONConfiguration.Notation.MAPPED;
                if (properties.containsKey(JSONJAXBContext.JSON_NOTATION)) {
                    Object nO = properties.get(JSONJAXBContext.JSON_NOTATION);
                    if ((nO instanceof JSONJAXBContext.JSONNotation) || (nO instanceof String)) {
                        pNotation = _notationMap.get(nO.toString());
                    }
                }
                jsonConfiguration = getConfiguration(pNotation, properties);
            }
        }
        for (String k : jsonKeys) {
            properties.remove(k);
        }
    }

    private JSONConfiguration getConfiguration(JSONConfiguration.Notation pNotation,
            Map<String, Object> properties) {

        String [] a = new String[0];

        switch (pNotation) {
            case BADGERFISH :
                return JSONConfiguration.badgerFish().build();
            case MAPPED_JETTISON : 
                JSONConfiguration.MappedJettisonBuilder mappedJettisonBuilder = JSONConfiguration.mappedJettison();
                if (properties.containsKey(JSONJAXBContext.JSON_XML2JSON_NS)) {
                    mappedJettisonBuilder.xml2JsonNs((Map<String, String>) properties.get(JSONJAXBContext.JSON_XML2JSON_NS));
                }
                return mappedJettisonBuilder.build();
            case NATURAL : 
                return JSONConfiguration.natural().build();
            case MAPPED :
            default: {
                JSONConfiguration.MappedBuilder mappedBuilder = JSONConfiguration.mapped();
                if (properties.containsKey(JSONJAXBContext.JSON_ARRAYS)) {
                    mappedBuilder.arrays(((Collection<String>) properties.get(JSONJAXBContext.JSON_ARRAYS)).toArray(a));
                }
                if (properties.containsKey(JSONJAXBContext.JSON_ATTRS_AS_ELEMS)) {
                    mappedBuilder.attributeAsElement(((Collection<String>) properties.get(JSONJAXBContext.JSON_ATTRS_AS_ELEMS)).toArray(a));
                }
                if (properties.containsKey(JSONJAXBContext.JSON_NON_STRINGS)) {
                    mappedBuilder.nonStrings(((Collection<String>) properties.get(JSONJAXBContext.JSON_NON_STRINGS)).toArray(a));
                }
                if (properties.containsKey(JSONJAXBContext.JSON_ROOT_UNWRAPPING)) {
                    mappedBuilder.rootUnwrapping((Boolean) properties.get(JSONJAXBContext.JSON_ROOT_UNWRAPPING));
                }
                return mappedBuilder.build();
            }
        }
    }
}