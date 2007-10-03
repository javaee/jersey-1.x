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

import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.ws.rs.core.HttpContext;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractParameterProcessor<T> implements ParameterProcessor<T> {
    public final static Map<Class, ParameterProcessor<?>> PARAM_PROCESSOR_MAP = createParamProcessorMap();
    
    private static Map<Class, ParameterProcessor<?>> createParamProcessorMap() {
        Map<Class, ParameterProcessor<?>> m = 
                new WeakHashMap<Class, ParameterProcessor<?>>();
        m.put(MatrixParam.class, new MatrixParameterProcessor());
        m.put(HeaderParam.class, new HeaderParameterProcessor());
        m.put(QueryParam.class, new QueryParameterProcessor());
        m.put(UriParam.class, new UriParameterProcessor());
        m.put(HttpContext.class, new HttpContextParameterProcessor());
        return Collections.unmodifiableMap(m);
    }

    public static List<Annotation> getAnnotationList(Annotation[] parameterAnnotations) {
        List<Annotation> l = new ArrayList<Annotation>();
        for (Class c : PARAM_PROCESSOR_MAP.keySet()) {
            Annotation a = null;
            for (Annotation _a : parameterAnnotations)
                if (_a.annotationType() == c) {
                    a = _a;
                    break;
                }
            if (a != null)
                l.add(a);
        }
        
        return l;
    } 
}