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

package com.sun.jersey.impl.model.method.dispatch;

import javax.ws.rs.WebApplicationException;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.ResponseBuilderImpl;
import com.sun.jersey.impl.application.InjectableProviderContext;
import com.sun.jersey.impl.model.ReflectionHelper;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestInjectable;
import com.sun.jersey.spi.inject.SingletonInjectable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EntityParamDispatchProvider implements ResourceMethodDispatchProvider {
                
    public EntityParamDispatchProvider() {
        int i = 0;
    }
    
    static final class EntityInjectable implements PerRequestInjectable<Object> {
        final Class<?> c;
        final Type t;
        final Annotation[] as;
        
        EntityInjectable(Class c, Type t, Annotation[] as) {
            this.c = c;
            this.t = t;
            this.as = as;
        }

        public Object getValue(HttpContext context) {
            return context.getRequest().getEntity(c, t, as);
        }        
    }
        
    static abstract class EntityParamInInvoker extends ResourceJavaMethodDispatcher {        
        final private List<Injectable> is;
        
        EntityParamInInvoker(AbstractResourceMethod abstractResourceMethod, 
                List<Injectable> is) {
            super(abstractResourceMethod);
            this.is = is;
        }

        protected final Object[] getParams(HttpContext context) {
            final Object[] params = new Object[is.size()];
            try {
                int index = 0;
                for (Injectable i : is) {
                    params[index++] = i.getValue(context);                        
                }
                return params;
            } catch (WebApplicationException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ContainerException("Exception injecting parameters to Web resource method", e);
            }
        }        
    }
    
    static final class VoidOutInvoker extends EntityParamInInvoker {
        VoidOutInvoker(AbstractResourceMethod abstractResourceMethod, List<Injectable> is) {
            super(abstractResourceMethod, is);
        }

        @SuppressWarnings("unchecked")
        public void _dispatch(Object resource, HttpContext context) 
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);
            method.invoke(resource, params);
        }
    }
    
    static final class TypeOutInvoker extends EntityParamInInvoker {
        TypeOutInvoker(AbstractResourceMethod abstractResourceMethod, List<Injectable> is) {
            super(abstractResourceMethod, is);
        }

        @SuppressWarnings("unchecked")
        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);
            
            Object o = method.invoke(resource, params);
            MediaType mediaType = getAcceptableMediaType(context.getRequest());
            Response r = new ResponseBuilderImpl().status(200).entity(o).type(mediaType).build();
            context.getResponse().setResponse(r);
        }
    }
    
    static final class ResponseOutInvoker extends EntityParamInInvoker {
        ResponseOutInvoker(AbstractResourceMethod abstractResourceMethod, List<Injectable> is) {
            super(abstractResourceMethod, is);
        }

        @SuppressWarnings("unchecked")
        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);

            Response r = (Response)method.invoke(resource, params);
            MediaType mediaType = getAcceptableMediaType(context.getRequest());
            context.getResponse().setResponse(r, mediaType);
        }
    }
    
    static final class ObjectOutInvoker extends EntityParamInInvoker {
        ObjectOutInvoker(AbstractResourceMethod abstractResourceMethod, List<Injectable> is) {
            super(abstractResourceMethod, is);
        }

        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);
            
            Object o = method.invoke(resource, params);
            
            MediaType mediaType = getAcceptableMediaType(context.getRequest());
            if (o instanceof Response) {
                Response r = (Response)o;
                context.getResponse().setResponse(r, mediaType);
            } else {
                Response r = new ResponseBuilderImpl().status(200).entity(o).type(mediaType).build();
                context.getResponse().setResponse(r);
            }            
        }
    }

    @Context InjectableProviderContext ipc;
    
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        boolean requireReturnOfRepresentation = false;
        boolean requireNoEntityParameter = false;
        
        // TODO
        // Strictly speaking a GET request can contain an entity in the
        // message body, but this is likely to be not implemented by many
        // servers and clients, but should we support it?
        if ("GET".equals(abstractResourceMethod.getHttpMethod())) {
            requireReturnOfRepresentation = true;
            requireNoEntityParameter = true;
        }
        
        // Let through other methods
        
        List<Injectable> is = processParameters(abstractResourceMethod, 
                requireNoEntityParameter);
        if (is == null)
            return null;
        
        Class<?> returnType = abstractResourceMethod.getMethod().getReturnType();
        if (Response.class.isAssignableFrom(returnType)) {
            return new ResponseOutInvoker(abstractResourceMethod, is);                
        } else if (returnType != void.class) {
            if (returnType == Object.class) {
                return new ObjectOutInvoker(abstractResourceMethod, is);
            } else {
                return new TypeOutInvoker(abstractResourceMethod, is);
            }
        } else if (requireReturnOfRepresentation) {
            return null;
        } else {
            return new VoidOutInvoker(abstractResourceMethod, is);
        }
    }
    
    private List<Injectable> processParameters(AbstractResourceMethod method,
            boolean requireNoEntityParameter) {
        
        if ((null == method.getParameters()) || (0 == method.getParameters().size())) {
            return Collections.emptyList();
        }
        
        List<Injectable> is = new ArrayList<Injectable>(method.getParameters().size());
        for (int i = 0; i < method.getParameters().size(); i++) {
            Injectable injectable = processParameter(
                    method,
                    method.getParameters().get(i),
                    method.getMethod().getParameterAnnotations()[i],
                    requireNoEntityParameter);
            
            if (injectable == null)
                return null;
            
            is.add(injectable);
        }
        
        return is;
    }

    private Injectable processParameter(
            AbstractResourceMethod method,
            Parameter parameter, 
            Annotation[] annotations,
            boolean requireNoEntityParameter) {

        if (Parameter.Source.ENTITY == parameter.getSource()) {
            if (requireNoEntityParameter) {
                // Entity as a method parameterClass is not required
                return null;
            }
            
            if (parameter.getParameterType() instanceof TypeVariable) {
                ReflectionHelper.ClassTypePair ct = ReflectionHelper.resolveTypeVariable(
                        method.getDeclaringResource().getResourceClass(), 
                        method.getMethod().getDeclaringClass(),
                        (TypeVariable)parameter.getParameterType());
                
                return (ct != null) ? new EntityInjectable(ct.c, ct.t, annotations) : null;
            } else {
                return new EntityInjectable(parameter.getParameterClass(), 
                        parameter.getParameterType(), annotations);
            }
        }

        Injectable i = ipc.getInjectable(parameter);
        return i;
    }        
}