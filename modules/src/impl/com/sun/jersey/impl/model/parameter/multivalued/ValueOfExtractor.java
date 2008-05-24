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

package com.sun.jersey.impl.model.parameter.multivalued;

import javax.ws.rs.WebApplicationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class ValueOfExtractor 
        extends com.sun.jersey.impl.model.parameter.multivalued.BaseValueOfExtractor 
        implements MultivaluedParameterExtractor {
    final String parameter;
    final Object defaultValue;

    public ValueOfExtractor(Method valueOf, String parameter) {
        super(valueOf);
        this.parameter = parameter;
        this.defaultValue = null;
    }
    
    public ValueOfExtractor(Method valueOf, String parameter, String defaultValueString) 
    throws IllegalAccessException, InvocationTargetException {
        super(valueOf);
        this.parameter = parameter;
        this.defaultValue = (defaultValueString != null) ? 
            getValue(defaultValueString) : null;
    }

    public Object extract(MultivaluedMap<String, String> parameters) {
        String v = parameters.getFirst(parameter);
        if (v != null) {
            try {
                return getValue(v);
            } catch (Exception e) {
                throw new WebApplicationException(e, 400);
            }
        } else if (defaultValue != null) {
            // TODO do we need to clone the default value
            return defaultValue;
        }

        return null;
    }
}
