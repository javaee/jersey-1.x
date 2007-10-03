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

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.dispatch.UriTemplateDispatcher;
import com.sun.ws.rest.impl.model.ReflectionHelper;
import com.sun.ws.rest.impl.model.parameter.AbstractParameterProcessor;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractor;
import com.sun.ws.rest.impl.model.parameter.ParameterProcessor;
import com.sun.ws.rest.spi.dispatch.UriTemplateType;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.Encoded;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class NodeDispatcherFactory {
        
    private NodeDispatcherFactory() {
    }

    public static UriTemplateDispatcher create(final UriTemplateType t, final Method m) {
        ParameterExtractor[] extractors = processParameters(m);
        for (ParameterExtractor extractor: extractors) {
            if (extractor == null) {
                String msg = ImplMessages.NOT_VALID_DYNAMICRESOLVINGMETHOD(m, 
                                                                    t.toString(), 
                                                                    m.getDeclaringClass());
                throw new ContainerException(msg);
            }
        }
        
        return new NodeDispatcher(t, m, extractors);
    }
    
    public static ParameterExtractor[] processParameters(Constructor ctor) {
        Class[] parameterTypes = ctor.getParameterTypes();
        Type[] genericParameterTypes = ctor.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = ctor.getParameterAnnotations();
        
        return processParameters(ctor.getDeclaringClass(),
                ctor,
                parameterTypes, genericParameterTypes, parameterAnnotations);
    }
    
    private static ParameterExtractor[] processParameters(Method method) {
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        
        return processParameters(method.getDeclaringClass(),
                method,
                parameterTypes, genericParameterTypes, parameterAnnotations);
    }
        
    private static ParameterExtractor[] processParameters(
            Class<?> declaringClass,
            AccessibleObject accessible,
            Class[] parameterTypes,
            Type[] genericParameterTypes, Annotation[][] parameterAnnotations) {

        ParameterExtractor[] extractors = new ParameterExtractor[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            extractors[i] = processParameter(
                    declaringClass,
                    accessible,
                    parameterTypes[i], 
                    genericParameterTypes[i], 
                    parameterAnnotations[i]);
        }
        
        return extractors;
    }
    
    @SuppressWarnings("unchecked")
    private static ParameterExtractor processParameter(
            Class<?> declaringClass,
            AccessibleObject accessible,
            Class<?> parameterClass, 
            Type parameterType,  
            Annotation[] parameterAnnotations) {

        List<Annotation> l = AbstractParameterProcessor.getAnnotationList(parameterAnnotations);
        
        if (l.size() == 0) {
            // A param annotation must be present.
            return null;
        } else if (l.size() > 1) {
            // Only one param annotation must be present
            return null;
        }

        Annotation annotation = l.get(0);
        ParameterProcessor p = AbstractParameterProcessor.PARAM_PROCESSOR_MAP.
                get(annotation.annotationType());
        boolean decode = !ReflectionHelper.hasAnnotation(
                Encoded.class, parameterAnnotations,
                declaringClass, accessible);
        return p.process(decode, annotation, 
                parameterClass, parameterType, parameterAnnotations);
    }
}