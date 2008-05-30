/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.spi.container.servlet;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ApplicationConfigAdapter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.ThreadLocalInvoker;
import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.impl.container.servlet.HttpRequestAdaptor;
import com.sun.jersey.impl.container.servlet.HttpResponseAdaptor;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.ApplicationConfig;
import javax.ws.rs.core.Context;


/**
 * A servlet container for deploying root resource classes.
 * <p>
 * The web.xml MAY configure the servlet to have an initialization parameter 
 * "com.sun.jersey.config.property.resourceConfigClass" and whose value is a 
 * fully qualified name of a class that implements {@link ResourceConfig}. 
 * If the concrete class has a constructor that takes a single parameter of the 
 * type Map then the class is instantiated with that constructor and an instance 
 * of Map that contains all the initialization parameters is passed as the parameter.
 * Otherwise the default contructor is used to instantate the class.
 * <p>
 * If the initialization parameter 
 * "com.sun.jersey.config.property.resourceConfigClass" is not present a new 
 * instance of {@link ClasspathResourceConfig} is created. The initialization 
 * parameter "com.sun.jersey.config.property.classpath" MAY be set to provide 
 * one or more paths. Each path MUST be separated by ';'. Each path MUST
 * be a virtual path as specified by the {@link ServletContext#getRealPath} method,
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
 * annotated with {@link javax.ws.rs.core.Context}: {@link HttpServletRequest}, 
 * {@link HttpServletResponse}, {@link ServletContext}, and {@link ServletConfig}.
 */
public class ServletContainer extends HttpServlet implements ContainerListener {
    public static final String APPLICATION_CONFIG_CLASS =
            "javax.ws.rs.ApplicationConfig";
    
    public static final String RESOURCE_CONFIG_CLASS = 
            "com.sun.jersey.config.property.resourceConfigClass";
    
    private final ThreadLocalInvoker<HttpServletRequest> requestInvoker =
            new ThreadLocalInvoker<HttpServletRequest>();
    
    private final ThreadLocalInvoker<HttpServletResponse> responseInvoker =
            new ThreadLocalInvoker<HttpServletResponse>();
    
    private ServletConfig config;
    
    private ServletContext context;

    private ResourceConfig resourceConfig;
    
    private WebApplication application;
    
    @Override
    public final void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    
        config = servletConfig;
        
        context = config.getServletContext();
        
        resourceConfig = createResourceConfig(config);
        initResourceConfigFeatures(servletConfig, resourceConfig);
        
        load();
        
        Object o = resourceConfig.getProperties().get(
                ResourceConfig.PROPERTY_CONTAINER_NOTIFIER);
        if (o instanceof ContainerNotifier) {
            ContainerNotifier crf = (ContainerNotifier)o;
            crf.addListener(this);
        }        
    }
    
    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        // Copy the application field to local instance to ensure that the 
        // currently loaded web application is used to process
        // request
        final WebApplication _application = application;
        
        HttpRequestAdaptor requestAdaptor = new HttpRequestAdaptor(
                _application.getMessageBodyContext(), 
                req);
        HttpResponseAdaptor responseAdaptor = new HttpResponseAdaptor(
                context, 
                resp, 
                req, 
                _application.getMessageBodyContext(), 
                requestAdaptor);
        
        try {
            // save thread locals for use in injection
            requestInvoker.set(req);
            responseInvoker.set(resp);
            _application.handleRequest(requestAdaptor, responseAdaptor);
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
        
        // Check if the resource config class property is present
        String resourceConfigClassName = servletConfig.getInitParameter(RESOURCE_CONFIG_CLASS);
        // Otherwise check if the JAX-RS applicaion config class property is
        // present
        if (resourceConfigClassName == null)
            resourceConfigClassName = servletConfig.getInitParameter(APPLICATION_CONFIG_CLASS);
        
        // If no property is present use the default class scanning property
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
                    if (ClasspathResourceConfig.class.isAssignableFrom(resourceConfigClass)) {
                        String[] paths = getPaths(servletConfig.getInitParameter(
                                ClasspathResourceConfig.PROPERTY_CLASSPATH));
                        props.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);                        
                    }
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
            } else if (ApplicationConfig.class.isAssignableFrom(resourceConfigClass)) {
                try {
                    ResourceConfig rc = new ApplicationConfigAdapter(
                            (ApplicationConfig)resourceConfigClass.newInstance());
                    rc.getProperties().putAll(props);
                    return rc;
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
                ResourceConfig.FEATURE_MATCH_MATRIX_PARAMS);
        setResourceConfigFeature(servletConfig, rc, 
                ResourceConfig.FEATURE_NORMALIZE_URI);
        setResourceConfigFeature(servletConfig, rc, 
                ResourceConfig.FEATURE_REDIRECT);
        setResourceConfigFeature(servletConfig, rc, 
                ResourceConfig.FEATURE_IMPLICIT_VIEWABLES);
    }
    
    private void setResourceConfigFeature(ServletConfig servletConfig, ResourceConfig rc, String feature) {
        String value = servletConfig.getInitParameter(feature);
        if (value != null)
            rc.getFeatures().put(feature, Boolean.valueOf(value));
    }
    
    /**
     * Load the Web application. This will create, configure and initiate
     * the web application.
     * <p>
     * This method may be called at runtime, more than once, to reload the 
     * Web application. For example, if a {@link ResourceConfig} implementation 
     * is capable of detecting changes to resource classes (addition or removal)
     * or providers then this method may be invoked to reload the web 
     * application for such changes to take effect.
     * <p>
     * If this method is called when there are pending requests then such
     * requests will be processed using the previously loaded web application.
     */
    public final void load() {
        WebApplication _application = create();        
        configure(config, resourceConfig, _application);        
        initiate(resourceConfig, _application);
        application = _application;
    }
    
    /**
     * Create a new instance of a {@link WebApplication}.
     * 
     * @return the {@link WebApplication} instance.
     */
    protected WebApplication create() {
        return WebApplicationFactory.createWebApplication();
    }
    
    private static class ContextInjectableProvider<T> extends
            SingletonTypeInjectableProvider<Context, T> {

        ContextInjectableProvider(Type type, T instance) {
            super(type, instance);
        }
    }
    
    /**
     * Configure the {@link WebApplication}.
     * <p>
     * The {@link WebApplication} is configured such that the following classes 
     * may be injected onto the field of a root resource class or a parameter 
     * of a method of root resource class that is annotated with 
     * {@link javax.ws.rs.core.Context}: {@link HttpServletRequest}, {@link HttpServletResponse}
     * , {@link ServletContext}, and {@link ServletConfig}.
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
    protected void configure(final ServletConfig sc, ResourceConfig rc, WebApplication wa) {
        wa.addInjectable(new ContextInjectableProvider<HttpServletRequest>(
                HttpServletRequest.class,
                (HttpServletRequest)Proxy.newProxyInstance(
                        HttpServletRequest.class.getClassLoader(),
                        new Class[] { HttpServletRequest.class },
                        requestInvoker)));
        
        wa.addInjectable(new ContextInjectableProvider<HttpServletResponse>(
                HttpServletResponse.class,
                (HttpServletResponse)Proxy.newProxyInstance(
                        HttpServletResponse.class.getClassLoader(),
                        new Class[] { HttpServletResponse.class },
                        responseInvoker)));
        
        wa.addInjectable(new ContextInjectableProvider<ServletConfig>(
                ServletConfig.class, sc));
        
        wa.addInjectable(new ContextInjectableProvider<ServletContext>(
                ServletContext.class, 
                sc.getServletContext()));
    }
    
    /**
     * Initiate the {@link WebApplication}.
     *
     * @param rc the Resource configuration
     * @param wa the Web application
     */
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        wa.initiate(rc);        
    }

    // ContainerListener
    
    public void onReload() {
        load();
    }
}