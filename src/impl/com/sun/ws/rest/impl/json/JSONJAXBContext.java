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

package com.sun.ws.rest.impl.json;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author japod
 */
public class JSONJAXBContext extends JAXBContext {
    
    public static final String NAMESPACE = "com.sun.ws.rest.impl.json.";
    
    public static final String JSON_NOTATION = NAMESPACE + "notation";
    public static final String JSON_ENABLED = NAMESPACE + "enabled";
    public static final String JSON_ROOT_UNWRAPPING = NAMESPACE + "root.unwrapping";
    public static final String JSON_ARRAYS = NAMESPACE + "arrays";
    public static final String JSON_NON_STRINGS = NAMESPACE + "non.strings";
    public static final String JSON_XML2JSON_NS = NAMESPACE + "xml.to.json.ns";
    
    // TODO: if need to replace jettison due to legal reasons, still want the badgerfish supported?
    public enum JSONNotation { MAPPED, MAPPED_JETTISON, BADGERFISH };
    
    private Map<String, Object> jsonProperties;
    
    private static final Map<String, Object> defaultJsonProperties = new HashMap<String, Object>();
    
    static {
        defaultJsonProperties.put(JSON_NOTATION, JSONNotation.MAPPED.name());
        defaultJsonProperties.put(JSON_ROOT_UNWRAPPING, Boolean.TRUE);
    }
    
    JAXBContext jaxbContext;
    
    public JSONJAXBContext(Class... classesToBeBound) throws JAXBException {
        this(classesToBeBound, Collections.unmodifiableMap(defaultJsonProperties));
    }

    public JSONJAXBContext(Class[] classesToBeBound, Map<String, Object> properties) throws JAXBException {
        Map<String, Object> workProperties = new HashMap<String, Object>();
        for (Entry<String, Object> entry : properties.entrySet()) {
            workProperties.put(entry.getKey(), entry.getValue());
        }
        processProperties(workProperties);
        jaxbContext = JAXBContext.newInstance(classesToBeBound, workProperties);
    }
    
    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        return new JSONUnmarshaller(jaxbContext, jsonProperties);
    }

    @Override
    public Marshaller createMarshaller() throws JAXBException {
        return new JSONMarshaller(jaxbContext, jsonProperties);
    }

    @Override
    public Validator createValidator() throws JAXBException {
        return jaxbContext.createValidator();
    }
    
    private final void processProperties(Map<String, Object> properties) {
        for (String k : properties.keySet()) {
            if (k.startsWith(NAMESPACE)) {
                getJsonProperties().put(k, properties.get(k));
            }
        }
        for (String k : getJsonProperties().keySet()) {
            properties.remove(k);
        }
    }
    
    private Map<String, Object> getJsonProperties() {
        if (null == this.jsonProperties) {
            this.jsonProperties = new HashMap<String, Object>();
        }
        return this.jsonProperties;
    }
    
    @SuppressWarnings("unchecked")
    static <T> Map<String, T> asMap(String jsonObjectVal) throws JSONException {
        if (null == jsonObjectVal) {
            return null;
        }
        Map<String, T> result = new HashMap<String, T>();

        JSONObject sourceMap = new JSONObject(jsonObjectVal);
        Iterator<String> keyIterator = sourceMap.keys();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            result.put(key, (T)sourceMap.get(key));
        }
        return result;
    }
    
    
    @SuppressWarnings("unchecked")
    static <T> Collection<T> asCollection(String jsonArrayVal) throws JSONException {
        if (null == jsonArrayVal) {
            return null;
        }
        Collection<T> result = new LinkedList<T>();

        JSONArray arrayVal = new JSONArray(jsonArrayVal);
        for (int i = 0; i < arrayVal.length(); i++) {
            result.add((T)arrayVal.get(i));
        }
        return result;
    }
    
    static String asJsonArray(Collection<? extends Object> collection) {
        return (null == collection) ? "[]" : (new JSONArray(collection)).toString();
    }
    
    static String asJsonObject(Map map) {
        return (null == map) ? "{}" : (new JSONObject(map)).toString();
    }
}
