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

import javax.ws.rs.MatrixParam;
import com.sun.ws.rest.api.core.HttpRequestContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.core.PathSegment;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MatrixParameterProcessor extends AbstractParameterProcessor<MatrixParam> {

    private static final class MatrixParameterExtractor implements ParameterExtractor {
        private final MultivaluedParameterExtractor extractor;
        private final boolean decode;
        
        MatrixParameterExtractor(MultivaluedParameterExtractor extractor) {
            this(extractor, true);
        }
        
        MatrixParameterExtractor(MultivaluedParameterExtractor extractor, boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }
        
        public Object extract(HttpRequestContext request) {
            List<PathSegment> l = request.getPathSegments(decode);
            PathSegment p = l.get(l.size() - 1);
            return extractor.extract(p.getMatrixParameters());
        }
    }
    
    public ParameterExtractor process(boolean decode,
            MatrixParam parameterAnnotation,
            Class<?> parameter, 
            Type parameterType, 
            Annotation[] parameterAnnotations) {
        String parameterName = parameterAnnotation.value();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid header parameter name
            return null;
        }
        
        MultivaluedParameterExtractor e = 
                MultivaluedDefaultListParameterProcessor.process(parameterAnnotations, parameter, 
                parameterType, parameterName);
        if (e == null)
            return null;
        
        return new MatrixParameterExtractor(e, decode);
    }
}
