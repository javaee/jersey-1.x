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

package com.sun.ws.rest.impl.model.node;

import javax.ws.rs.UnmatchedPath;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.spi.dispatch.Dispatcher;
import com.sun.ws.rest.impl.model.parameter.AbstractParameterProcessor;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractor;
import com.sun.ws.rest.impl.model.parameter.ParameterProcessor;
import com.sun.ws.rest.spi.dispatch.URITemplateType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class NodeDispatcherFactory {
        
    private NodeDispatcherFactory() {
    }

    public static Dispatcher create(final URITemplateType t, final Method m) {
        ParameterExtractor[] extractors = processParameters(m);
        if (extractors == null) {
            String msg = ImplMessages.NOT_VALID_DYNAMICRESOLVINGMETHOD(m, 
                                                                t.toString(), 
                                                                m.getDeclaringClass());
            throw new ContainerException(msg);
        }
        
        return new NodeDispatcher(t, m, extractors);
    }
    
    private static ParameterExtractor[] processParameters(Method method) {
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        
        ParameterExtractor[] extractors = new ParameterExtractor[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            extractors[i] = processParameter(
                    parameterTypes[i], 
                    genericParameterTypes[i], 
                    parameterAnnotations[i]);
            
            if (extractors[i] == null)
                return null;
        }
        
        return extractors;
    }
    
    @SuppressWarnings("unchecked")
    private static ParameterExtractor processParameter(
            Class<?> parameterClass, 
            Type parameterType,  
            Annotation[] parameterAnnotations) {

        List<Annotation> l = AbstractParameterProcessor.getAnnotationList(parameterAnnotations);
        
        if (l.size() == 0) {
            if (AbstractParameterProcessor.hasAnnotation(UnmatchedPath.class, parameterAnnotations)) {
                if (parameterAnnotations.length > 1) {
                    // Only one param annotation must be present
                    return null;
                }
                return new UnmatchedPathExtractor();
            } else {
                // A param annotation must be present.
                return null;
            }
        } else if (l.size() > 1) {
            // Only one param annotation must be present
            return null;
        }

        Annotation annotation = l.get(0);
        ParameterProcessor p = AbstractParameterProcessor.PARAM_PROCESSOR_MAP.get(annotation.annotationType());
        return p.process(annotation, parameterClass, parameterType, parameterAnnotations);
    }
}