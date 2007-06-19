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

import com.sun.ws.rest.api.core.HttpRequestContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.PreconditionEvaluator;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpContextParameterProcessor implements ParameterProcessor<HttpContext> {
        
    private static final class HttpRequestContextExtractor implements ParameterExtractor {
        public Object extract(HttpRequestContext request) {
            return request;
        }
    }
    
    private final Map<Class<?>, ParameterExtractor> extractors;
    
    private final HttpRequestContextExtractor extractor;
    
    public HttpContextParameterProcessor() {
        extractor = new HttpRequestContextExtractor();
        
        extractors = new HashMap<Class<?>, ParameterExtractor>();
        
        extractors.put(HttpHeaders.class, extractor);
        extractors.put(UriInfo.class, extractor);
        extractors.put(PreconditionEvaluator.class, extractor);
    }
    
    public ParameterExtractor process(HttpContext parameterAnnotation, 
            Class<?> parameter, 
            Type parameterType, 
            Annotation[] parameterAnnotations) {
        
        return extractors.get(parameter);
    }
}
