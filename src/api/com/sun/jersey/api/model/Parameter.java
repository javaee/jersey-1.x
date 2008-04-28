/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * Parameter.java
 *
 * Created on October 5, 2007, 11:49 AM
 *
 */

package com.sun.jersey.api.model;

import java.lang.reflect.Type;

/**
 * Abstraction for a method/constructor parameter
 */
public class Parameter {
    
    public enum Source {ENTITY, QUERY, MATRIX, URI, COOKIE, HEADER, CONTEXT};
    
    private Parameter.Source source;
    private String sourceName;
    private boolean encoded;
    private String defaultValue;
    private Type type;
    private Class<?> clazz;
    
    public Parameter(Source source, String sourceName, Type type, Class<?> clazz) {
        this(source, sourceName, type, clazz, false, null);
    }

    public Parameter(Source source, String sourceName, Type type, Class<?> clazz, boolean encoded) {
        this(source, sourceName, type, clazz, encoded, null);
    }

    public Parameter(Source source, String sourceName, Type type, Class<?> clazz, String defaultValue) {
        this(source, sourceName, type, clazz, false, defaultValue);
    }
    
    public Parameter(Source source, String sourceName, Type type, Class<?> clazz, boolean encoded, String defaultValue) {
        // TODO: Check consistency, e.g. for query, matrix, uri and header params
        // sourceName must not be null
        this.source = source;
        this.sourceName = sourceName;
        this.type = type;
        this.clazz = clazz;
        this.encoded = encoded;
        this.defaultValue = defaultValue;
    }

    public String getSourceName() {
        return sourceName;
    }

    public Parameter.Source getSource() {
        return source;
    }

    public boolean isEncoded() {
        return encoded;
    }
    
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Class<?> getParameterClass() {
        return clazz;
    }

    public Type getParameterType() {
        return type;
    }
    
}
