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

import com.sun.ws.rest.api.Entity;
import javax.ws.rs.WebApplicationException;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.model.method.HttpRequestDispatcher;
import com.sun.ws.rest.impl.model.ReflectionHelper;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractor;
import com.sun.ws.rest.impl.model.parameter.ParameterProcessor;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import com.sun.ws.rest.impl.model.parameter.AbstractParameterProcessor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
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
    
    static final class TypeFromEntityExtractor implements ParameterExtractor {
        final Class parameterEntityType;
        
        TypeFromEntityExtractor(Class parameterEntityType) {
            this.parameterEntityType = parameterEntityType;
        }
        
        @SuppressWarnings("unchecked")
        public Object extract(HttpRequestContext request) {
            return request.getEntity(parameterEntityType).getContent();
        }
    }
    
    static abstract class EntityParamInInvoker extends AbstractResourceMethodDispatcher {        
        final private ParameterExtractor[] injectors;
        
        final protected MediaType mediaType;

        EntityParamInInvoker(ResourceMethod method, ParameterExtractor[] injectors) {
            super(method);
            this.injectors = injectors;

            if (method.produceMime.size() == 1) {
                MediaType c = method.produceMime.get(0);
                if (c.getType().equals("*") || c.getSubtype().equals("*")) 
                    mediaType = null;
                else
                    mediaType = method.produceMime.get(0);
            } else {
                mediaType = null;
            }
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
        VoidOutInvoker(ResourceMethod method, ParameterExtractor[] injectors) {
            super(method, injectors);
        }

        @SuppressWarnings("unchecked")
        public void dispatch(Object resource, HttpRequestContext request, HttpResponseContext response) 
        throws WebApplicationException, IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(request);
            method.invoke(resource, params);
        }
    }
    
    static final class TypeOutInvoker extends EntityParamInInvoker {
        TypeOutInvoker(ResourceMethod method, ParameterExtractor[] injectors) {
            super(method, injectors);
        }

        @SuppressWarnings("unchecked")
        public void dispatch(Object resource, HttpRequestContext request, HttpResponseContext response)
        throws WebApplicationException, IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(request);
            
            Object o = (Object)method.invoke(resource, params);
            Response r = ResponseBuilderImpl.representation(o, mediaType).build();
            response.setResponse(r);
        }
    }
    
    static final class ResponseOutInvoker extends EntityParamInInvoker {
        ResponseOutInvoker(ResourceMethod method, ParameterExtractor[] injectors) {
            super(method, injectors);
        }

        @SuppressWarnings("unchecked")
        public void dispatch(Object resource, HttpRequestContext requestContext, HttpResponseContext responseContext)
        throws WebApplicationException, IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(requestContext);

            Response r = (Response)method.invoke(resource, params);
            responseContext.setResponse(r, mediaType);
        }
    }
    
    static final class ObjectOutInvoker extends EntityParamInInvoker {
        ObjectOutInvoker(ResourceMethod method, ParameterExtractor[] injectors) {
            super(method, injectors);
        }

        @SuppressWarnings("unchecked")
        public void dispatch(Object resource, HttpRequestContext requestContext, HttpResponseContext responseContext)
        throws WebApplicationException, IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(requestContext);
            
            Object o = method.invoke(resource, params);
            
            if (o instanceof Response) {
                Response r = (Response)o;
                responseContext.setResponse(r, mediaType);
            } else {
                Response r = ResponseBuilderImpl.representation(o, mediaType).build();
                responseContext.setResponse(r);
            }            
        }
    }
        
    
    public HttpRequestDispatcher create(ResourceMethod method) {
        boolean requireReturnOfRepresentation = false;
        boolean requireNoEntityParameter = false;
        
        if (method.httpMethod.equals("GET")) {
            requireReturnOfRepresentation = true;
            requireNoEntityParameter = true;
        } else if (method.httpMethod.equals("POST")) {
        } else if (method.httpMethod.equals("PUT")) {
        } else if (method.httpMethod.equals("DELETE")) {
            requireNoEntityParameter = true;
        }
        // Let through other methods
        
        ParameterExtractor[] injectors = processParameters(method.method, requireNoEntityParameter);
        if (injectors == null)
            return null;
        
        Class<?> returnType = method.method.getReturnType();
        if (Response.class.isAssignableFrom(returnType)) {
            return new ResponseOutInvoker(method, injectors);                
        } else if (returnType != void.class) {
            if (returnType == Object.class) {
                return new ObjectOutInvoker(method, injectors);
            } else {
                return new TypeOutInvoker(method, injectors);
            }
        } else if (requireReturnOfRepresentation) {
            return null;
        } else {
            return new VoidOutInvoker(method, injectors);
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
            
            // Process annotated parameterClass
            if (parameterClass == Entity.class) {
                // Get the generic type used for the Entity
                Class genericClass = ReflectionHelper.getGenericClass(parameterType);
                if (genericClass == null) {
                    // There is no generic type
                    return null;
                }

                return new EntityExtractor(genericClass);
            } else {
                return new TypeFromEntityExtractor(parameterClass);
            }
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
        return p.process(annotation, parameterClass, parameterType, parameterAnnotations);        
    }        
}