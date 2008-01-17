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
import com.sun.ws.rest.impl.ThreadLocalInvoker;
import com.sun.ws.rest.api.core.ClasspathResourceConfig;
import com.sun.ws.rest.impl.container.servlet.HttpRequestAdaptor;
import com.sun.ws.rest.impl.container.servlet.HttpResponseAdaptor;
import com.sun.ws.rest.spi.container.WebApplication;
import com.sun.ws.rest.spi.container.WebApplicationFactory;
import com.sun.ws.rest.spi.resource.Injectable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * The web.xml MAY configure the servlet to have an initialization parameter 
 * "com.sun.ws.rest.config.property.resourceConfigClass" and whose value is a 
 * fully qualified name of a class that implements {@link ResourceConfig}. 
 * If the concrete class has a constructor that takes a single parameter of the 
 * type Map then the class is instantiated with that constructor and an instance 
 * of Map that contains all the initialization parameters is passed as the parameter.
 * Otherwise the default contructor is used to instantate the class.
 * <p>
 * If the initialization parameter 
 * "com.sun.ws.rest.config.property.resourceConfigClass" is not present a new 
 * instance of {@link ClasspathResourceConfig} is created. The initialization 
 * parameter "com.sun.ws.rest.config.property.classpath" MAY be set to provide 
 * one or more paths. Each path MUST be separated by ';'. Each path MUST
 * be a virtual path as specified by the {@link Servlet#getRealPath} method,
 * and each path is transformed by that method. The transformed paths are
 * added as a property value to a Map instance using the property name 
 * (@link ClasspathResourceConfig.PROPERTY_CLASSPATH}. Any additional 
 * initialization parameters are then added to the Map instance. Then that Map
 * instance is passe to the constructor of {@link ClasspathResourceConfig}.
 * If this parameter is not set then the 
 * default value is set to the following virtual paths: 
 * "/WEB-INF/lib;/WEB-INF/classes".
 * <p>
 * A new {@link WebApplication} instance will be created and configured such
 * that the following classes may be injected onto the field of a root 
 * resource class or a parameter of a method of root resource class that is 
 * annotated with {@link Resource}: @{link HttpServletRequest}, 
 * {@link HttpServletResponse} and {@link ServletConfig}.
 */
public class ServletContainer extends HttpServlet {
    private static final String RESOURCE_CONFIG_CLASS = 
            "com.sun.ws.rest.config.property.resourceConfigClass";
    
    private WebApplication application;
    
    private ServletContext context;
    
    private ThreadLocalInvoker<HttpServletRequest> requestInvoker =
            new ThreadLocalInvoker<HttpServletRequest>();
    
    private ThreadLocalInvoker<HttpServletResponse> responseInvoker =
            new ThreadLocalInvoker<HttpServletResponse>();
    
    
    @Override
    public final void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        
        this.context = servletConfig.getServletContext();
        
        ResourceConfig resourceConfig = createResourceConfig(servletConfig);
        initResourceConfigFeatures(servletConfig, resourceConfig);
        
        this.application = create();
        configure(servletConfig, resourceConfig, this.application);
        initiate(resourceConfig, this.application);
    }
    
    @Override
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
    
    
    private ResourceConfig createResourceConfig(ServletConfig servletConfig) 
            throws ServletException {        
        Map<String, Object> props = getInitParams(servletConfig);
        
        String resourceConfigClassName = servletConfig.getInitParameter(RESOURCE_CONFIG_CLASS);
        if (resourceConfigClassName == null) {
            String[] paths = getPaths(servletConfig.getInitParameter(
                    ClasspathResourceConfig.PROPERTY_CLASSPATH));
            props.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
            return new ClasspathResourceConfig(props);
        }

        try {
            Class resourceConfigClass = getClassLoader().
                    loadClass(resourceConfigClassName);
            
            if (resourceConfigClass == ClasspathResourceConfig.class) {
                String[] paths = getPaths(servletConfig.getInitParameter(
                        ClasspathResourceConfig.PROPERTY_CLASSPATH));
                props.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
                return new ClasspathResourceConfig(props);
            } else if (ResourceConfig.class.isAssignableFrom(resourceConfigClass)) {
                try {                    
                    Constructor constructor = resourceConfigClass.getConstructor(Map.class);
                    return (ResourceConfig)constructor.newInstance(props);
                } catch (NoSuchMethodException ex) {
                    // Pass through and try the default constructor
                } catch (Exception e) {
                    throw new ServletException(e);
                }
                
                try {
                    return (ResourceConfig)resourceConfigClass.newInstance();
                } catch(Exception e) {                    
                    throw new ServletException(e);
                }
            } else {
                String message = "Resource configuration class, " + resourceConfigClassName + 
                        ", is not a super class of " + ResourceConfig.class;
                throw new ServletException(message);
            }
        } catch (ClassNotFoundException e) {
            String message = "Resource configuration class, " + resourceConfigClassName + 
                    ", could not be loaded";
            throw new ServletException(message, e);
        }
    }

    private ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();        
        return (classLoader == null) ? getClass().getClassLoader() : classLoader;
    }
    
    private Map<String, Object> getInitParams(ServletConfig servletConfig) {
        Map<String, Object> props = new HashMap<String, Object>();
        Enumeration names = servletConfig.getInitParameterNames();
        while(names.hasMoreElements()) {
            String name = (String)names.nextElement();
            props.put(name, servletConfig.getInitParameter(name));
        }
        return props;
    }
    
    private String[] getPaths(String classpath) {
        if (classpath == null) {
            return new String[] {
                context.getRealPath("/WEB-INF/lib"), 
                context.getRealPath("/WEB-INF/classes")
            };
        } else {
            String[] virtualPaths = classpath.split(";");
            List<String> resourcePaths = new ArrayList<String>();
            for (String virtualPath : virtualPaths) {
                virtualPath = virtualPath.trim();
                if (virtualPath.length() == 0) continue;
                
                resourcePaths.add(context.getRealPath(virtualPath));
            }
            
            return resourcePaths.toArray(new String[resourcePaths.size()]);
        }        
    }
    
    private void initResourceConfigFeatures(ServletConfig servletConfig, ResourceConfig rc) {
        setResourceConfigFeature(servletConfig, rc, 
                ResourceConfig.FEATURE_CANONICALIZE_URI_PATH);
        setResourceConfigFeature(servletConfig, rc, 
                ResourceConfig.FEATURE_IGNORE_MATRIX_PARAMS);
        setResourceConfigFeature(servletConfig, rc, 
                ResourceConfig.FEATURE_NORMALIZE_URI);
        setResourceConfigFeature(servletConfig, rc, 
                ResourceConfig.FEATURE_REDIRECT);
    }
    
    private void setResourceConfigFeature(ServletConfig servletConfig, ResourceConfig rc, String feature) {
        String value = servletConfig.getInitParameter(feature);
        if (value != null)
            rc.getFeatures().put(feature, Boolean.valueOf(value));
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
     * features and properties of the {@link ResourceConfig}. For an inheriting
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
        wa.addInjectable(ServletContext.class,
                new Injectable<Resource, ServletContext>() {
            public Class<Resource> getAnnotationClass() {
                return Resource.class;
            }

            public ServletContext getInjectableValue(Resource r) {
                return (null != getServletConfig()) ? getServletConfig().getServletContext() : null;
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
