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

package com.sun.jersey.impl.model.parameter;

import com.sun.jersey.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.impl.model.parameter.multivalued.MultivaluedParameterProcessor;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.spi.inject.InjectableContext;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.PerRequestInjectable;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.PathSegment;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class PathParamInjectableProvider implements 
        InjectableProvider<PathParam, Parameter, PerRequestInjectable> {
    
    private static final class PathParamInjectable implements 
            PerRequestInjectable<Object> {
        private final MultivaluedParameterExtractor extractor;
        private final boolean decode;
        
        PathParamInjectable(MultivaluedParameterExtractor extractor, boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }
        
        public Object getValue(HttpContext context) {
            return extractor.extract(context.getUriInfo().getPathParameters(decode));
        }
    }
    
    private static final class PathParamSegmentInjectable implements 
            PerRequestInjectable<PathSegment> {
        private final String name;
        private final boolean decode;
        
        PathParamSegmentInjectable(String name, boolean decode) {
            this.name = name;
            this.decode = decode;
        }
        
        public PathSegment getValue(HttpContext context) {
            return context.getUriInfo().getPathSegment(name, decode);
        }
    }
    
    public PerRequestInjectable<?> getInjectable(InjectableContext ic, PathParam a, Parameter c) {
        String parameterName = c.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid URI parameter name
            return null;
        }
        
        if (c.getParameterClass() == PathSegment.class) {
            return new PathParamSegmentInjectable(parameterName, 
                    !c.isEncoded());
        } else {
            MultivaluedParameterExtractor e =  MultivaluedParameterProcessor.
                    process(c.getParameterClass(), 
                    c.getParameterType(), 
                    parameterName);        
            if (e == null)
                return null;

            return new PathParamInjectable(e, !c.isEncoded());
        }        
    }
}
