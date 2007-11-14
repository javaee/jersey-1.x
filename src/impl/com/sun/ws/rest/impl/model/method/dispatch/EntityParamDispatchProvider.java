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

package com.sun.ws.rest.impl.model.method.dispatch;

import javax.ws.rs.WebApplicationException;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.model.ReflectionHelper;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractor;
import com.sun.ws.rest.impl.model.parameter.ParameterProcessor;
import com.sun.ws.rest.impl.model.method.ResourceMethodData;
import com.sun.ws.rest.impl.model.parameter.AbstractParameterProcessor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.Encoded;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EntityParamDispatchProvider implements ResourceMethodDispatchProvider {
                
    static final class EntityExtractor implements ParameterExtractor {
        final Class parameterEntityType;
        
        EntityExtractor(Class parameterEntityType) {
            this.parameterEntityType = parameterEntityType;
        }
        
        @SuppressWarnings("unchecked")
        public Object extract(HttpRequestContext request) {
            return request.getEntity(parameterEntityType);
        }
    }
    
    static abstract class EntityParamInInvoker extends ResourceJavaMethodDispatcher {        
        final private ParameterExtractor[] injectors;
        
        EntityParamInInvoker(AbstractResourceMethod abstractResourceMethod, ParameterExtractor[] injectors) {
            super(abstractResourceMethod);
            this.injectors = injectors;
        }

        protected final Object[] getParams(HttpRequestContext request) {
            final Object[] params = new Object[injectors.length];
            try {
                for (int i = 0; i < injectors.length; i++)
                    params[i] = injectors[i].extract(request);
                
                return params;
            } catch (WebApplicationException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ContainerException("Exception injecting parameters to Web resource method", e);
            }
        }        
    }
    
    static final class VoidOutInvoker extends EntityParamInInvoker {
        VoidOutInvoker(AbstractResourceMethod abstractResourceMethod, ParameterExtractor[] injectors) {
            super(abstractResourceMethod, injectors);
        }

        @SuppressWarnings("unchecked")
        public void _dispatch(Object resource, HttpRequestContext requestContext, HttpResponseContext responseContext) 
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(requestContext);
            method.invoke(resource, params);
        }
    }
    
    static final class TypeOutInvoker extends EntityParamInInvoker {
        TypeOutInvoker(AbstractResourceMethod abstractResourceMethod, ParameterExtractor[] injectors) {
            super(abstractResourceMethod, injectors);
        }

        @SuppressWarnings("unchecked")
        public void _dispatch(Object resource, HttpRequestContext requestContext, HttpResponseContext responseContext)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(requestContext);
            
            Object o = method.invoke(resource, params);
            MediaType mediaType = getAcceptableMediaType(requestContext);
            Response r = new ResponseBuilderImpl().status(200).entity(o).type(mediaType).build();
            responseContext.setResponse(r);
        }
    }
    
    static final class ResponseOutInvoker extends EntityParamInInvoker {
        ResponseOutInvoker(AbstractResourceMethod abstractResourceMethod, ParameterExtractor[] injectors) {
            super(abstractResourceMethod, injectors);
        }

        @SuppressWarnings("unchecked")
        public void _dispatch(Object resource, HttpRequestContext requestContext, HttpResponseContext responseContext)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(requestContext);

            Response r = (Response)method.invoke(resource, params);
            MediaType mediaType = getAcceptableMediaType(requestContext);
            responseContext.setResponse(r, mediaType);
        }
    }
    
    static final class ObjectOutInvoker extends EntityParamInInvoker {
        ObjectOutInvoker(AbstractResourceMethod abstractResourceMethod, ParameterExtractor[] injectors) {
            super(abstractResourceMethod, injectors);
        }

        @SuppressWarnings("unchecked")
        public void _dispatch(Object resource, HttpRequestContext requestContext, HttpResponseContext responseContext)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(requestContext);
            
            Object o = method.invoke(resource, params);
            
            MediaType mediaType = getAcceptableMediaType(requestContext);
            if (o instanceof Response) {
                Response r = (Response)o;
                responseContext.setResponse(r, mediaType);
            } else {
                Response r = new ResponseBuilderImpl().status(200).entity(o).type(mediaType).build();
                responseContext.setResponse(r);
            }            
        }
    }
        
    
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        boolean requireReturnOfRepresentation = false;
        boolean requireNoEntityParameter = false;
        
        if ("GET".equals(abstractResourceMethod.getHttpMethod())) {
            requireReturnOfRepresentation = true;
            requireNoEntityParameter = true;
        } else if ("POST".equals(abstractResourceMethod.getHttpMethod())) {
        } else if ("PUT".equals(abstractResourceMethod.getHttpMethod())) {
        } else if ("DELETE".equals(abstractResourceMethod.getHttpMethod())) {
            requireNoEntityParameter = true;
        }
        // Let through other methods
        
        ParameterExtractor[] injectors = processParameters(abstractResourceMethod.getMethod(), requireNoEntityParameter);
        if (injectors == null)
            return null;
        
        Class<?> returnType = abstractResourceMethod.getMethod().getReturnType();
        if (Response.class.isAssignableFrom(returnType)) {
            return new ResponseOutInvoker(abstractResourceMethod, injectors);                
        } else if (returnType != void.class) {
            if (returnType == Object.class) {
                return new ObjectOutInvoker(abstractResourceMethod, injectors);
            } else {
                return new TypeOutInvoker(abstractResourceMethod, injectors);
            }
        } else if (requireReturnOfRepresentation) {
            return null;
        } else {
            return new VoidOutInvoker(abstractResourceMethod, injectors);
        }
    }
    
    private ParameterExtractor[] processParameters(Method method,
            boolean requireNoEntityParameter) {
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        
        ParameterExtractor[] injectors = new ParameterExtractor[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            injectors[i] = processParameter(
                    method,
                    parameterTypes[i], 
                    genericParameterTypes[i], 
                    parameterAnnotations[i],
                    requireNoEntityParameter);
            
            if (injectors[i] == null)
                return null;
        }
        
        return injectors;
    }

    @SuppressWarnings("unchecked")
    private ParameterExtractor processParameter(
            Method method,
            Class<?> parameterClass, 
            Type parameterType,  
            Annotation[] parameterAnnotations, 
            boolean requireNoEntityParameter) {

        List<Annotation> l = AbstractParameterProcessor.getAnnotationList(parameterAnnotations);

        if (l.isEmpty()) {
            if (requireNoEntityParameter) {
                // Entity as a method parameterClass is not required
                return null;
            }
            
            return new EntityExtractor(parameterClass);
        }
                
        if (l.size() == 0) {
            // A param annotation must be present.
            return null;            
        } else if (l.size() > 1) {
            // Only one param annotation must be present
            return null;
        }

        Annotation annotation = l.get(0);
        ParameterProcessor p = AbstractParameterProcessor.PARAM_PROCESSOR_MAP.get(annotation.annotationType());
        if (p == null)
            return null;
        boolean decode = !ReflectionHelper.hasAnnotation(
                Encoded.class, parameterAnnotations,
                method.getDeclaringClass(), method);
        return p.process(decode, annotation, 
                parameterClass, parameterType, parameterAnnotations);
    }        
}