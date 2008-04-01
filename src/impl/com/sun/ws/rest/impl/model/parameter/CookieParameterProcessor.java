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
import javax.ws.rs.core.Cookie;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class CookieParameterProcessor implements ParameterProcessor {
    
    private static final class CookieParameterExtractor implements ParameterExtractor {
        private final MultivaluedParameterExtractor extractor;
        
        CookieParameterExtractor(MultivaluedParameterExtractor extractor) {
            this.extractor = extractor;
        }
        
        public Object extract(HttpContext context) {
            return extractor.extract(context.getRequest().getCookieNameValueMap());
        }
    }
    
    private static final class CookieTypeParameterExtractor implements ParameterExtractor {
        private final String name;
        
        CookieTypeParameterExtractor(String name) {
            this.name = name;
        }
        
        public Object extract(HttpContext context) {
            return context.getRequest().getCookies().get(name);
        }
    }
    
    public ParameterExtractor process(Parameter parameter) {
        String parameterName = parameter.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid cookie parameter name
            return null;
        }
        
        if (parameter.getParameterClass() == Cookie.class) {
            return new CookieTypeParameterExtractor(parameterName);
        } else {
            MultivaluedParameterExtractor e = MultivaluedDefaultListParameterProcessor.
                    process(parameter.getDefaultValue(), parameter.getParameterClass(), 
                    parameter.getParameterType(), parameterName);

            if (e == null)
                return null;
            return new CookieParameterExtractor(e);
        }
    }
}