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

package com.sun.ws.rest.impl.container.servlet;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.ThreadLocalInvoker;
import com.sun.ws.rest.spi.container.WebApplication;
import com.sun.ws.rest.spi.container.WebApplicationFactory;
import com.sun.ws.rest.spi.resolver.WebResourceResolver;
import com.sun.ws.rest.spi.resolver.WebResourceResolverFactory;
import com.sun.ws.rest.spi.resolver.WebResourceResolverListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Dispatch a servlet request to the appropriate Resource
 * 
 */
public class ServletAdaptor extends HttpServlet implements WebResourceResolverFactory {
    private static final String WEB_RESOURCE_CLASS = "webresourceclass";
    
    private WebApplication application;
    
    private ServletContext context;
    
    private boolean isEE;
    
    @SuppressWarnings("unchecked")
    private static List<Class<?>> injectables = Arrays.asList(HttpServletRequest.class, ServletConfig.class, EntityManagerFactory.class);
    
    private ThreadLocalInvoker<HttpServletRequest> requestInvoker = new ThreadLocalInvoker<HttpServletRequest>();
    
    private Map<String, String> persistenceUnits = new HashMap<String, String>();
    
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    
        // tomcat returns null, glassfish returns a string
        isEE = System.getProperty("product.name") == null ? false : true;
        
        context = servletConfig.getServletContext();
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        
        String resources = null;
        for (Enumeration e = servletConfig.getInitParameterNames() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            String value = servletConfig.getInitParameter(key);
            if (key.equals(WEB_RESOURCE_CLASS)) {
                resources = value;
            } else if (key.startsWith("unit:")) {
                persistenceUnits.put(key.substring(5),"java:comp/env/"+value);
            }
            
        }
        
        if (resources == null)
            throw new ServletException(ImplMessages.NO_WEBRESOURCECLASS_IN_WEBXML());
            
        try {
            Class resClassSetClass = classLoader.loadClass(resources);
            ResourceConfig resourceConfig = (ResourceConfig)resClassSetClass.newInstance();
        
            application = WebApplicationFactory.createWebApplication();
            application.initiate(this, resourceConfig, this);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new ServletException(
                    ImplMessages.FAILED_TO_CREATE_WEB_RESOURCE(servletConfig.getServletName()),
                    e);
        }
    }
    
    public void service(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        HttpRequestAdaptor requestAdaptor = new HttpRequestAdaptor(req);
        HttpResponseAdaptor responseAdaptor = new HttpResponseAdaptor(context, resp, req, requestAdaptor);

        try {
            requestInvoker.set(req); // save req as thread local
            application.handleRequest(requestAdaptor, responseAdaptor);
        } catch (ContainerException e) {
            throw new ServletException(e);
        } finally {
            requestInvoker.set(null);
        }
        // Let all other runtime exceptions be handled by the servlet container
        // Those exceptions will be bugs
        
        responseAdaptor.commit();                
        if (responseAdaptor.getRequestDispatcher() != null) {
            // For some odd reason forward can only be called from the
            // same class as the servlet
            responseAdaptor.getRequestDispatcher().forward(req, resp);
        }
    }

    public WebResourceResolver createWebResourceResolver(Class<?> resourceClass) {
        return this.new StatelessServletResolver(resourceClass);
    }
    
    /**
     * A servlet specific resolver
     */
    private class StatelessServletResolver implements WebResourceResolver {
        private Class resourceClass;
        private Object resource;

        public StatelessServletResolver(Class resourceClass) {
            this.resourceClass = resourceClass;
        }

        public Class<?> getWebResourceClass() {
            return resource.getClass();
        }

        public Object resolve(HttpRequestContext request, WebResourceResolverListener listener) {
            if (resource == null) {
                instantiate();
                // perform any requested injections
                Field[] fs = resourceClass.getDeclaredFields();
                for (final Field f : fs) {
                    Class c = f.getType();
                    if (!injectables.contains(c))
                        continue; // skip fields we don't know how to inject
                    if (c==HttpServletRequest.class) {
                        Resource ra = f.getAnnotation(Resource.class);
                        if (ra==null)
                            continue; // skip fields that aren't annotated
                        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                                HttpServletRequest.class.getClassLoader(),
                                new Class[] { HttpServletRequest.class },
                                requestInvoker);
                        setFieldValue(f, req);
                    } else if (c == ServletConfig.class) {
                        Resource ra = f.getAnnotation(Resource.class);
                        if (ra==null)
                            continue; // skip fields that aren't annotated
                        setFieldValue(f, getServletConfig());
                    } else if (c==EntityManagerFactory.class && isEE) {
                        PersistenceUnit pu = f.getAnnotation(PersistenceUnit.class);
                        if (pu==null)
                            continue;
                        if (!persistenceUnits.containsKey(pu.unitName()))
                            throw new ContainerException("Persistence unit '"+pu.unitName()+"' is not configured as a servlet parameter in web.xml");
                        String jndiName = persistenceUnits.get(pu.unitName());
                        ThreadLocalNamedInvoker<EntityManagerFactory> emfHandler = new ThreadLocalNamedInvoker<EntityManagerFactory>(jndiName);
                        EntityManagerFactory emf = (EntityManagerFactory) Proxy.newProxyInstance(
                                EntityManagerFactory.class.getClassLoader(),
                                new Class[] {EntityManagerFactory.class },
                                emfHandler);
                        setFieldValue(f, emf);
                    }
                }

                // call listeners to perform any additional injections
                listener.onInstantiation(resource);
            }
            return resource;
        }
        
        private void setFieldValue(final Field f, final Object value) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        f.set(resource, value);
                        return null;
                    } catch (IllegalAccessException e) {
                        throw new ContainerException(e);
                    }
                }
            });
        }

        private void instantiate() {
            try {
                resource = resourceClass.newInstance();
            } catch (IllegalAccessException e) {
                throw new ContainerException("Illegal access to Class " + resourceClass, e);
            } catch (InstantiationException e) {
                throw new ContainerException("Instantiation error for Class " + resourceClass, e);
            }
        }
    }
}
