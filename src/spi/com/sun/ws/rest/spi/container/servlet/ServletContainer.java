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

package com.sun.ws.rest.spi.container.servlet;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.ThreadLocalInvoker;
import com.sun.ws.rest.impl.container.servlet.HttpRequestAdaptor;
import com.sun.ws.rest.impl.container.servlet.HttpResponseAdaptor;
import com.sun.ws.rest.spi.container.WebApplication;
import com.sun.ws.rest.spi.container.WebApplicationFactory;
import com.sun.ws.rest.spi.resource.Injectable;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Enumeration;
import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A servlet container for deploying root resource classes.
 * <p>
 * The web.xml that uses this class MUST configure the servlet to have an 
 * initialization parameter whose param name is "webresourceclass" and whose 
 * value is a fully qualified name of a class that implements 
 * {@link ResourceConfig}.
 * <p>
 * A new {@link WebApplication} instance will be created and configured such
 * that the following classes may be injected onto the field of a root 
 * resource class or a parameter of a method of root resource class that is 
 * annotated with {@link Resource}: @{link HttpServletRequest}, 
 * @{link HttpServletResponse} and {@link ServletConfig}.
 */
public class ServletContainer extends HttpServlet {
    private static final String WEB_RESOURCE_CLASS = "webresourceclass";
    
    private WebApplication application;
    
    private ServletContext context;
    
    private ThreadLocalInvoker<HttpServletRequest> requestInvoker =
            new ThreadLocalInvoker<HttpServletRequest>();
    
    private ThreadLocalInvoker<HttpServletResponse> responseInvoker =
            new ThreadLocalInvoker<HttpServletResponse>();
    
    
    public final void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        
        this.context = servletConfig.getServletContext();
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        
        String resources = null;
        for (Enumeration e = servletConfig.getInitParameterNames() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            if (key.equals(WEB_RESOURCE_CLASS)) {
                resources = servletConfig.getInitParameter(key);
                break;
            }
        }
        
        if (resources == null)
            throw new ServletException(ImplMessages.NO_WEBRESOURCECLASS_IN_WEBXML());
        
        try {
            ResourceConfig resourceConfig = (ResourceConfig)classLoader.
                    loadClass(resources).newInstance();
            
            this.application = create();
            configure(servletConfig, resourceConfig, this.application);
            initiate(resourceConfig, this.application);            
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new ServletException(
                    ImplMessages.FAILED_TO_CREATE_WEB_RESOURCE(servletConfig.getServletName()),
                    e);
        }
    }
    
    public final void service(HttpServletRequest req, HttpServletResponse resp)
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
            responseInvoker.set(null);
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
    
    /**
     * Create a new instance of a {@link WebApplication}.
     */
    protected WebApplication create() {
        return WebApplicationFactory.createWebApplication();
    }
    
    /**
     * Configure the {@link WebApplication}.
     * <p>
     * The {@link WebApplication} is configured such that the following classes 
     * may be injected onto the field of a root resource class or a parameter 
     * of a method of root resource class that is annotated with 
     * {@link Resource}: @{link HttpServletRequest}, @{link HttpServletResponse}
     * and {@link ServletConfig}.
     * <p>
     * An inheriting class may override this method to configure the 
     * {@link WebApplication} to provide alternative or additional instance
     * that may be injected into a root resource class, and may modify the
     * features and properties of the {@ResourceConfig}. For an inheriting
     * class to extend configuration behaviour the overriding method MUST call
     * super.configure(servletConfig, rc, wa) as the first statement of that 
     * method.
     * 
     * @param sc the Servlet configuration
     * @param rc the Resource configuration
     * @param wa the Web application
     */
    protected void configure(ServletConfig sc, ResourceConfig rc, WebApplication wa) {
        wa.addInjectable(HttpServletRequest.class,
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
        wa.addInjectable(HttpServletResponse.class,
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
        wa.addInjectable(ServletConfig.class,
                new Injectable<Resource, ServletConfig>() {
            public Class<Resource> getAnnotationClass() {
                return Resource.class;
            }

            public ServletConfig getInjectableValue(Resource r) {
                return getServletConfig();
            }
        }
        );
    }
    
    /**
     * Initiate the {@link WebApplication}.
     *
     * @param rc the Resource configuration
     * @param wa the Web application
     */
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        wa.initiate(this, rc);
    }    
}
