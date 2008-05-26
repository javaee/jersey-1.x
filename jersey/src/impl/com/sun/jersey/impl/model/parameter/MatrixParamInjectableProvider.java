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
import java.util.List;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.PathSegment;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MatrixParamInjectableProvider implements 
        InjectableProvider<MatrixParam, Parameter, PerRequestInjectable> {

    private static final class MatrixParamInjectable implements PerRequestInjectable<Object> {
        private final MultivaluedParameterExtractor extractor;
        private final boolean decode;
        
        MatrixParamInjectable(MultivaluedParameterExtractor extractor, boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }
        
        public Object getValue(HttpContext context) {
            List<PathSegment> l = context.getUriInfo().getPathSegments(decode);
            PathSegment p = l.get(l.size() - 1);
            return extractor.extract(p.getMatrixParameters());
        }
    }

    public PerRequestInjectable getInjectable(InjectableContext ic, MatrixParam a, Parameter c) {
        String parameterName = c.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid header parameter name
            return null;
        }
        
        MultivaluedParameterExtractor e =  MultivaluedParameterProcessor.
                process(c.getDefaultValue(), c.getParameterClass(), 
                c.getParameterType(), parameterName);
        
        if (e == null)
            return null;
        
        return new MatrixParamInjectable(e, !c.isEncoded());
    }
}
