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

package com.sun.ws.rest.impl.model.parameter;

import javax.ws.rs.WebApplicationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MultivaluedDefaultValueOfListExtractor 
        extends ValueOfExtractor implements MultivaluedParameterExtractor {
    final String parameter;
    final Object defaultValue;

    public MultivaluedDefaultValueOfListExtractor(Method valueOf, String parameter, String defaultValueString) 
    throws IllegalAccessException, InvocationTargetException {
        super(valueOf);
        this.parameter = parameter;
        this.defaultValue = (defaultValueString != null) ? 
            getValue(defaultValueString) : null;
    }

    @SuppressWarnings("unchecked")
    public Object extract(MultivaluedMap<String, String> parameters) {
        List<String> stringList = parameters.get(parameter);
        if (stringList != null) {            
            List valueList = new ArrayList();
            for (String v : stringList) {
                try {
                    valueList.add(getValue(v));
                } catch (Exception e) {
                    throw new WebApplicationException(e, 400);
                }
            }

            return valueList;
        } else if (defaultValue != null) {
            List valueList = new ArrayList();
            // TODO do we need to clone the default value
            valueList.add(defaultValue);
            return valueList;
        }

        return null;
    }
}
