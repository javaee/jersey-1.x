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
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.ThreadLocalInvoker;
import com.sun.ws.rest.spi.container.WebApplication;
import com.sun.ws.rest.spi.container.WebApplicationFactory;
import com.sun.ws.rest.spi.resource.Injectable;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
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
public class ServletAdaptor extends HttpServlet {
    private static final String WEB_RESOURCE_CLASS = "webresourceclass";
    
    @SuppressWarnings("unchecked")
    private static List<Class<?>> INJECTABLES = Arrays.asList(
            HttpServletRequest.class, HttpServletResponse.class,
            ServletConfig.class, EntityManagerFactory.class);
    
    private WebApplication application;
    
    private ServletContext context;
    
    private boolean isEE;
    
    private ThreadLocalInvoker<HttpServletRequest> requestInvoker =
            new ThreadLocalInvoker<HttpServletRequest>();
    
    private ThreadLocalInvoker<HttpServletResponse> responseInvoker =
            new ThreadLocalInvoker<HttpServletResponse>();
    
    private Map<String, String> persistenceUnits =
            new HashMap<String, String>();
    
    
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        
        // tomcat returns null, glassfish returns a string
        isEE = System.getProperty("product.name") == null ? false : true;
        
        context = servletConfig.getServletContext();
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        
        /* Persistence units that may be injected must be configured in web.xml
         * in the normal way plus an additional servlet parameter to enable the
         * Jersey servlet to locate them in JNDI. E.g. with the following
         * persistence unit configuration:
         *
         * <persistence-unit-ref>
         *     <persistence-unit-ref-name>persistence/widget</persistence-unit-ref-name>
         *     <persistence-unit-name>WidgetPU</persistence-unit-name>
         * </persistence-unit-ref>
         *
         * the Jersey servlet requires an additional servlet parameter as
         * follows:
         *
         * <init-param>
         *     <param-name>unit:WidgetPU</param-name>
         *     <param-value>persistence/widget</param-value>
         * </init-param>
         *
         * Given the above, Jersey will inject the EntityManagerFactory found
         * at java:comp/env/persistence/widget in JNDI when encountering a
         * field annotated with @PersistenceUnit(unitName="WidgetPU").
         */
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
            application.addInjectable(HttpServletRequest.class,
                    new Injectable<Resource, HttpServletRequest>() {
                public Class<Resource> getAnnotationClass() {
                    return Resource.class;
                }
                
                public HttpServletRequest getInjectableValue(Resource r) {
                    HttpServletRequest servletRequest = (HttpServletRequest)Proxy.newProxyInstance(
                            HttpServletRequest.class.getClassLoader(),
                            new Class[] { HttpServletRequest.class },
                            requestInvoker);
                    return servletRequest;
                }
            }
            );
            application.addInjectable(HttpServletResponse.class,
                    new Injectable<Resource, HttpServletResponse>() {
                public Class<Resource> getAnnotationClass() {
                    return Resource.class;
                }
                
                public HttpServletResponse getInjectableValue(Resource r) {
                    HttpServletResponse servletResponse = (HttpServletResponse)Proxy.newProxyInstance(
                            HttpServletResponse.class.getClassLoader(),
                            new Class[] { HttpServletResponse.class },
                            responseInvoker);
                    return servletResponse;
                }
            }
            );
            application.addInjectable(ServletConfig.class,
                    new Injectable<Resource, ServletConfig>() {
                public Class<Resource> getAnnotationClass() {
                    return Resource.class;
                }
                
                public ServletConfig getInjectableValue(Resource r) {
                    return getServletConfig();
                }
            }
            );
            application.addInjectable(EntityManagerFactory.class,
                    new Injectable<PersistenceUnit, EntityManagerFactory>() {
                public Class<PersistenceUnit> getAnnotationClass() {
                    return PersistenceUnit.class;
                }
                public EntityManagerFactory getInjectableValue(PersistenceUnit pu) {
                    // TODO localize error message
                    if (!persistenceUnits.containsKey(pu.unitName()))
                        throw new ContainerException("Persistence unit '"+
                                pu.unitName()+
                                "' is not configured as a servlet parameter in web.xml");
                    String jndiName = persistenceUnits.get(pu.unitName());
                    ThreadLocalNamedInvoker<EntityManagerFactory> emfHandler =
                            new ThreadLocalNamedInvoker<EntityManagerFactory>(jndiName);
                    EntityManagerFactory emf = (EntityManagerFactory) Proxy.newProxyInstance(
                            EntityManagerFactory.class.getClassLoader(),
                            new Class[] {EntityManagerFactory.class },
                            emfHandler);
                    return emf;
                }
            }
            );
            application.initiate(this, resourceConfig);
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
        HttpResponseAdaptor responseAdaptor = new HttpResponseAdaptor(context, 
                resp, req, requestAdaptor);
        
        try {
            // save thread locals for use in injection
            requestInvoker.set(req);
            responseInvoker.set(resp);
            application.handleRequest(requestAdaptor, responseAdaptor);
        } catch (ContainerException e) {
            throw new ServletException(e);
        } finally {
            requestInvoker.set(null);
        }
        
        // Let all other runtime exceptions be handled by the servlet container
        // Those exceptions will be bugs
        
        responseAdaptor.commitAll();
        
        if (responseAdaptor.getRequestDispatcher() != null) {
            // For some odd reason forward can only be called from the
            // same class as the servlet
            responseAdaptor.getRequestDispatcher().forward(req, resp);
        }
    }
    
}
