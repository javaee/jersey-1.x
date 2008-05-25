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

package com.sun.jersey.api.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Abstraction for a method/constructor parameter
 */
public class Parameter {
    
    public enum Source {ENTITY, QUERY, MATRIX, PATH, COOKIE, HEADER, CONTEXT, UNKNOWN};
    
    private final Annotation annotation;
    private final Parameter.Source source;
    private final String sourceName;
    private final boolean encoded;
    private final String defaultValue;
    private final Type type;
    private final Class<?> clazz;
    
    public Parameter(Annotation a, Source source, String sourceName, Type type, Class<?> clazz) {
        this(a, source, sourceName, type, clazz, false, null);
    }

    public Parameter(Annotation a, Source source, String sourceName, Type type, Class<?> clazz, boolean encoded) {
        this(a, source, sourceName, type, clazz, encoded, null);
    }

    public Parameter(Annotation a, Source source, String sourceName, Type type, Class<?> clazz, String defaultValue) {
        this(a, source, sourceName, type, clazz, false, defaultValue);
    }
    
    public Parameter(Annotation a, Source source, String sourceName, Type type, Class<?> clazz, boolean encoded, String defaultValue) {
        this.annotation = a;
        this.source = source;
        this.sourceName = sourceName;
        this.type = type;
        this.clazz = clazz;
        this.encoded = encoded;
        this.defaultValue = defaultValue;
    }

    public Annotation getAnnotation() {
        return annotation;
    }
    
    public Parameter.Source getSource() {
        return source;
    }

    public String getSourceName() {
        return sourceName;
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