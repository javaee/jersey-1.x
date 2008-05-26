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

package com.sun.jersey.api.json;

import com.sun.jersey.impl.json.JSONMarshaller;
import com.sun.jersey.impl.json.JSONUnmarshaller;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

/**
 * JSONJAXBContext is a configurable JAXBContext wrapper. It allows 
 * serialization and deserialization of JAXB beans to and from JSON format. 
 * Configuration is done by providing a set of properties to the JSONJAXBContext
 * constructor. The properties could be also set directly on Marshaller/Unmarshaller
 * created by the context.
 */
public final class JSONJAXBContext extends JAXBContext {
    
    /**
     * A namespace for JSONJAXBContext related properties names.
     */
    public static final String NAMESPACE = "com.sun.ws.rest.impl.json.";
    
    /**
     * Expects a String corresponding to desired JSON notation.
     * Currently supported notations are <code>"MAPPED"</code>, <code>"MAPPED_JETTISON"</code> and <code>"BADGERFISH"</code>
     */
    public static final String JSON_NOTATION = NAMESPACE + "notation";
    
    /**
     * If set to true, JSON will be serialized/deserialized instead of XML
     */
    public static final String JSON_ENABLED = NAMESPACE + "enabled";
    
    /**
     * If set to true, JSON code corresponding to the XML root element will be stripped out
     * for MAPPED (default) notation.
     */
    public static final String JSON_ROOT_UNWRAPPING = NAMESPACE + "root.unwrapping";
    
    /**
     * Expects a list of names in JSON format, which represent arrays, and should be
     * treated as arrays even if they contain just one single element.
     * I.e. <code>{ ..., "arr1":["single element"], ... }</code> would be 
     * serialized as <code>{ ..., "arr1":"single element", ... }</code>,
     * if <code>JSON_ARRAYS</code> was not set to <code>"[\"arr1\"]"</code>
     * Related to MAPPED notation only.
     */
    public static final String JSON_ARRAYS = NAMESPACE + "arrays";

    /**
     * Expects a list of names in JSON format, which represent non-string values
     * (such as numbers), and should be serialized out without surrounding double quotes
     * I.e. <code>{ ..., "anumber":12, ... }</code> would be 
     * serialized as <code>{ ..., "anumber":"12", ... }</code>,
     * if <code>JSON_NON_STRINGS</code> was not set to <code>"[\"anumber\"]"</code>
     * Related to MAPPED notation only.
     */
    public static final String JSON_NON_STRINGS = NAMESPACE + "non.strings";

    /**
     * Via this property you can configure namespaces mapping used by 
     * MAPPED_JETTISON notation.
     */
    public static final String JSON_XML2JSON_NS = NAMESPACE + "xml.to.json.ns";
    
    // TODO: if need to replace jettison due to legal reasons, still want the badgerfish supported?
    public enum JSONNotation { MAPPED, MAPPED_JETTISON, BADGERFISH };
    
    private static final Map<String, Object> defaultJsonProperties = new HashMap<String, Object>();
    
    static {
        defaultJsonProperties.put(JSON_NOTATION, JSONNotation.MAPPED.name());
        defaultJsonProperties.put(JSON_ROOT_UNWRAPPING, Boolean.TRUE);
    }
    
    private final Map<String, Object> jsonProperties = new HashMap<String, Object>();
        
    private final JAXBContext jaxbContext;
    
    /**
     * Constructs a new JSONJAXBContext with default properties.
     * You will need to set JSON_ENABLED property to true on appropriate 
     * Marshaller/Unmarshaller to actually switch JSON on.
     * 
     * @param classesToBeBound
     * @throws javax.xml.bind.JAXBException
     */
    public JSONJAXBContext(Class... classesToBeBound) throws JAXBException {
        this(classesToBeBound, Collections.unmodifiableMap(defaultJsonProperties));
    }

    /**
     * Constructs a new JSONJAXBContext with a custom set of properties.
     * 
     * @param classesToBeBound
     * @throws javax.xml.bind.JAXBException
     */
    public JSONJAXBContext(Class[] classesToBeBound, Map<String, Object> properties) throws JAXBException {
        Map<String, Object> workProperties = new HashMap<String, Object>();
        for (Entry<String, Object> entry : properties.entrySet()) {
            workProperties.put(entry.getKey(), entry.getValue());
        }
        processProperties(workProperties);
        jaxbContext = JAXBContext.newInstance(classesToBeBound, workProperties);
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
        return new JSONUnmarshaller(jaxbContext, jsonProperties);
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
        return new JSONMarshaller(jaxbContext, jsonProperties);
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
    
    private final void processProperties(Map<String, Object> properties) {
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            if (e.getKey().startsWith(NAMESPACE)) {
                getJsonProperties().put(e.getKey(), e.getValue());
            }
        }
        for (String k : getJsonProperties().keySet()) {
            properties.remove(k);
        }
    }
    
    private Map<String, Object> getJsonProperties() {
        return jsonProperties;
    }
}
