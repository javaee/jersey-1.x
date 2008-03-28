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

import com.sun.ws.rest.api.core.HttpContext;
import com.sun.ws.rest.api.model.Parameter;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpContextParameterProcessor implements ParameterProcessor {
        
    private static final class HttpRequestContextExtractor implements ParameterExtractor {
        public Object extract(HttpContext context) {
            return context.getRequest();
        }
    }
    
    private static final class UriInfoExtractor implements ParameterExtractor {
        public Object extract(HttpContext context) {
            return context.getUriInfo();
        }
    }
    
    private final Map<Class<?>, ParameterExtractor> extractors;
    
    public HttpContextParameterProcessor() {        
        extractors = new HashMap<Class<?>, ParameterExtractor>();
        
        HttpRequestContextExtractor re = new HttpRequestContextExtractor();
        extractors.put(HttpHeaders.class, re);
        extractors.put(Request.class, re);
        extractors.put(SecurityContext.class, re);
        
        UriInfoExtractor ue = new UriInfoExtractor();
        extractors.put(UriInfo.class, ue);
    }
    
    public ParameterExtractor process(Parameter parameter) {
        
        return extractors.get(parameter.getParameterClass());
    }
}
