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
import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.server.impl.container.servlet.JSPTemplateProcessor;
import com.sun.jersey.server.impl.container.servlet.ServletContainerRequest;
import com.sun.jersey.server.impl.container.servlet.ThreadLocalInvoker;
import com.sun.jersey.server.impl.model.method.dispatch.FormDispatchProvider;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;


/**
 * A servlet container for deploying root resource classes.
 * <p>
 * The web.xml MAY configure the servlet to have an initialization parameter 
 * "com.sun.jersey.config.property.resourceConfigClass" or
 * "javax.ws.rs.Application" and whose value is a
 * fully qualified name of a class that implements {@link ResourceConfig} or
 * {@link Application}.
 * If the concrete class has a constructor that takes a single parameter of the 
 * type Map then the class is instantiated with that constructor and an instance 
 * of Map that contains all the initialization parameters is passed as the 
 * parameter. Otherwise the default contructor is used to instantate the class.
 * 
 * <p>
 * If the initialization parameter 
 * "com.sun.jersey.config.property.resourceConfigClass" or
 * "javax.ws.rs.Application" is not present and a
 * initialization parameter "com.sun.jersey.config.property.packages" is present 
 * (see {@link PackagesResourceConfig#PROPERTY_PACKAGES}) a new instance of
 * {@link PackagesResourceConfig} is created. The initialization parameter 
 * "com.sun.jersey.config.property.packages" MUST be set to provide one or
 * more package names. Each package name MUST be separated by ';'.
 * 
 * The package names are added as a property value to a Map instance using 
 * the property name (@link PackagesResourceConfig#PROPERTY_PACKAGES}. Any
 * additional initialization parameters are then added to the Map instance. 
 * Then that Map instance is passed to the constructor of 
 * {@link PackagesResourceConfig}.
 * 
 * <p>
 * If none of the above initialization parameters are present a new 
 * instance of {@link ClasspathResourceConfig} is created. The initialization 
 * parameter "com.sun.jersey.config.property.classpath" MAY be set to provide 
 * one or more paths. Each path MUST be separated by ';'. Each path MUST
 * be a virtual path as specified by the {@link ServletContext#getRealPath} 
 * method, and each path is transformed by that method. 
 * 
 * The transformed paths are added as a property value to a Map instance using 
 * the property name (@link ClasspathResourceConfig.PROPERTY_CLASSPATH}. Any 
 * additional initialization parameters are then added to the Map instance. 
 * Then that Map instance is passed to the constructor of 
 * {@link ClasspathResourceConfig}.
 * 
 * If this parameter is not set then the default value is set to the following 
 * virtual paths: 
 * "/WEB-INF/lib;/WEB-INF/classes".
 * <p>
 * All servlet initialization parameters are added as properties of the created
 * {@link ResourceConfig}.
 *
 * <p>
 * A new {@link WebApplication} instance will be created and configured such
 * that the following classes may be injected onto the field of a root 
 * resource class or a parameter of a method of root resource class that is 
 * annotated with {@link javax.ws.rs.core.Context}: {@link HttpServletRequest}, 
 * {@link HttpServletResponse}, {@link ServletContext}, and {@link ServletConfig}.
 * 
 * <p>
 * A {@link IoCComponentProviderFactory} instance may be registered by extending this class
 * and overriding the method {@link #initiate(ResourceConfig, WebApplication)}
 * to initiate the {@link WebApplication} with the {@link IoCComponentProviderFactory}
 * instance.
 * 
 */
public class ServletContainer extends HttpServlet implements ContainerListener {
    /**
     * The servlet initializaton property whose value is a fully qualified
     * class name of a class that implements {@link ResourceConfig} or
     * {@link Application}.
     */
    public static final String APPLICATION_CONFIG_CLASS =
            "javax.ws.rs.Application";

    /**
     * The servlet initializaton property whose value is a fully qualified
     * class name of a class that implements {@link ResourceConfig} or
     * {@link Application}.
     */
    public static final String RESOURCE_CONFIG_CLASS = 
            "com.sun.jersey.config.property.resourceConfigClass";
    
    /**
     * The base path in the Web Pages where JSP templates, associated with
     * viewables of resource classes, are located.
     * <p>
     * If this property is not set then the base path will be the root path
     * of the Web Pages.
     */
    public static final String JSP_TEMPLATES_BASE_PATH =
            "com.sun.jersey.config.property.JSPTemplatesBasePath";

    private static final Logger LOGGER = 
            Logger.getLogger(ServletContainer.class.getName());
    
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
    public void destroy() {
        application.destroy();
    }

    private final static class Writer extends OutputStream implements ContainerResponseWriter {
        final HttpServletResponse response;
        
        Writer(HttpServletResponse response) {
            this.response = response;
        }

        public OutputStream writeStatusAndHeaders(long contentLength, 
                ContainerResponse cResponse) throws IOException {
            response.setStatus(cResponse.getStatus());
            if (contentLength != -1 && contentLength < Integer.MAX_VALUE) 
                response.setContentLength((int)contentLength);
        
            MultivaluedMap<String, Object> headers = cResponse.getHttpHeaders();
            for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
                for (Object v : e.getValue()) {
                    response.addHeader(e.getKey(), ContainerResponse.getHeaderValue(v));
                }
            }
            
            return this;
        }
        
        public void finish() throws IOException {            
        }

        OutputStream out;
        
        public void write(int b) throws IOException {
            initiate();
            out.write(b);
        }

        @Override
        public void write(byte b[]) throws IOException {
            initiate();
            out.write(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            initiate();
            out.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            if (out != null)
                out.flush();
        }

        @Override
        public void close() throws IOException {
            initiate();
            out.close();
        }
        
        void initiate() throws IOException {
            if (out == null)
                out = response.getOutputStream();
        }        
    }
    
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        /**
         * There is an annoying edge case where the service method is
         * invoked for the case when the URI is equal to the deployment URL
         * minus the '/', for example http://locahost:8080/HelloWorldWebApp
         */
        if (request.getPathInfo() != null && 
                request.getPathInfo().equals("/") && !request.getRequestURI().endsWith("/")) {
            response.setStatus(404);
            return;            
        }
        
        // Copy the application field to local instance to ensure that the 
        // currently loaded web application is used to process
        // request
        final WebApplication _application = application;
        
        
        /**
         * The HttpServletRequest.getRequestURL() contains the complete URI
         * minus the query and fragment components.
         */
        UriBuilder absoluteUriBuilder = UriBuilder.fromUri(
                request.getRequestURL().toString());
        
        /**
         * The HttpServletRequest.getPathInfo() and 
         * HttpServletRequest.getServletPath() are in decoded form.
         *
         * On some servlet implementations the getPathInfo() removed
         * contiguous '/' characters. This is problematic if URIs
         * are embedded, for example as the last path segment.
         * We need to work around this and not use getPathInfo
         * for the decodedPath.
         */
        final String decodedBasePath = (request.getPathInfo() != null)
            ? request.getContextPath() + request.getServletPath() + "/"
            : request.getContextPath() + "/";
        
        final String encodedBasePath = UriComponent.encode(decodedBasePath, 
                UriComponent.Type.PATH);
        
        if (!decodedBasePath.equals(encodedBasePath)) {
            throw new ContainerException("The servlet context path and/or the " +
                    "servlet path contain characters that are percent enocded");
        }
        
        final URI baseUri = absoluteUriBuilder.
                replacePath(encodedBasePath).
                build();
        
        String queryParameters = request.getQueryString();
        if (queryParameters == null) queryParameters = "";
        
        final URI requestUri = absoluteUriBuilder.
                replacePath(request.getRequestURI()).
                replaceQuery(queryParameters).
                build();
        
        final ContainerRequest cRequest = new ServletContainerRequest(
                request,
                _application,
                request.getMethod(),
                baseUri,
                requestUri,
                getHeaders(request),
                request.getInputStream());

        // Check if any servlet filters have consumed a request entity
        // of the media type application/x-www-form-urlencoded
        // This can happen if a filter calls request.getParameter(...)
        filterFormParameters(request, cRequest);

        try {
            requestInvoker.set(request);
            responseInvoker.set(response);
            
            _application.handleRequest(cRequest, new Writer(response));
        } catch (ContainerException e) {
            throw new ServletException(e);
        } finally {
            requestInvoker.set(null);
            responseInvoker.set(null);
        }        
    }
    
    @SuppressWarnings("unchecked")
    private InBoundHeaders getHeaders(HttpServletRequest request) {
        InBoundHeaders rh = new InBoundHeaders();   
        
        for (Enumeration<String> names = request.getHeaderNames() ; names.hasMoreElements() ;) {
            String name = names.nextElement();
            List<String> valueList = new LinkedList<String>();
            for (Enumeration<String> values = request.getHeaders(name); values.hasMoreElements() ;) {
                valueList.add(values.nextElement());
            }
            rh.put(name, valueList);
        }
        
        return rh;
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
        
        // If no resource config class property is present 
        if (resourceConfigClassName == null) {
            // If the packages property is present then
            // use the packages resource config
            String packages = servletConfig.getInitParameter(
                    PackagesResourceConfig.PROPERTY_PACKAGES);
            if (packages != null) {
                props.put(PackagesResourceConfig.PROPERTY_PACKAGES, packages);
                return new PackagesResourceConfig(props);                    
            }

            return getDefaultResourceConfig(props, servletConfig);
        }

        try {
            Class resourceConfigClass = ReflectionHelper.
                    classForNameWithException(resourceConfigClassName);
            
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
            } else if (Application.class.isAssignableFrom(resourceConfigClass)) {
                try {
                    ResourceConfig rc = new ApplicationAdapter(
                            (Application)resourceConfigClass.newInstance());
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

    private Map<String, Object> getInitParams(ServletConfig servletConfig) {
        Map<String, Object> props = new HashMap<String, Object>();
        Enumeration names = servletConfig.getInitParameterNames();
        while(names.hasMoreElements()) {
            String name = (String)names.nextElement();
            props.put(name, servletConfig.getInitParameter(name));
        }
        return props;
    }

    private String[] getPaths(String classpath) throws ServletException {
        if (classpath == null) {
            String[] paths =  {
                context.getRealPath("/WEB-INF/lib"), 
                context.getRealPath("/WEB-INF/classes")
            };
            if (paths[0] == null && paths[1] == null) {
                String message = "The default deployment configuration that scans for " +
                        "classes in /WEB-INF/lib and /WEB-INF/classes is not supported " +
                        "for the application server." +
                        "Try using the package scanning configuration, see the JavaDoc for " +
                        PackagesResourceConfig.class.getName() + " and the property " +
                        PackagesResourceConfig.PROPERTY_PACKAGES + ".";
                throw new ServletException(message);                        
            }
            return paths;
        } else {
            String[] virtualPaths = classpath.split(";");
            List<String> resourcePaths = new ArrayList<String>();
            for (String virtualPath : virtualPaths) {
                virtualPath = virtualPath.trim();
                if (virtualPath.length() == 0) continue;
                String path = context.getRealPath(virtualPath);
                if (path != null) resourcePaths.add(path);
            }
            if (resourcePaths.isEmpty()) {
                String message = "None of the declared classpath locations, " +
                        classpath +
                        ", could be resolved. " +
                        "This could be because the default deployment configuration that scans for " +
                        "classes in classpath locations is not supported. " +
                        "Try using the package scanning configuration, see the JavaDoc for " +
                        PackagesResourceConfig.class.getName() + " and the property " +
                        PackagesResourceConfig.PROPERTY_PACKAGES + ".";
                throw new ServletException(message);                                        
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

    private void filterFormParameters(HttpServletRequest hsr, ContainerRequest cr) throws IOException {
        if (cr.getMethod().equals("POST")
                && MediaTypes.typeEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, cr.getMediaType())
                && !isEntityPresent(cr)) {
            Form f = new Form();

            Enumeration e = hsr.getParameterNames();
            while (e.hasMoreElements()) {
                String name = (String)e.nextElement();
                String[] values = hsr.getParameterValues(name);

                f.put(name, Arrays.asList(values));
            }

            if (!f.isEmpty()) {
                cr.getProperties().put(FormDispatchProvider.FORM_PROPERTY, f);
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING,
                     "A servlet POST request, to the URI " + cr.getRequestUri() + ", " +
                     "contains form parameters in " +
                     "the request body but the request body has been consumed " +
                     "by the servlet or a servlet filter accessing the request " +
                     "parameters. Only resource methods using @FormParam " +
                     "will work as expected. Resource methods consuming the " +
                     "request body by other means will not work as expected.");
                }
            }
        }
    }

    private boolean isEntityPresent(ContainerRequest cr) throws IOException {
        InputStream in = cr.getEntityInputStream();
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
            cr.setEntityInputStream(in);
        }

        in.mark(1);
        if (in.read() == -1)
            return false;
        else {
            in.reset();
            return true;
        }
    }

    /**
     * Load the Web application. This will create, configure and initiate
     * the web application.
     */
    public final void load() {
        WebApplication _application = create();        
        configure(config, resourceConfig, _application);        
        initiate(resourceConfig, _application);
        application = _application;
    }
    
    /**
     * Reload the Web application. This will create and initiate the web
     * application using the same {@link ResourceConfig} implementation
     * that was used to load the Web application.
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
    public final void reload() {
        WebApplication oldApplication = application;
        WebApplication newApplication = create();
        initiate(resourceConfig, newApplication);

        application = newApplication;
        oldApplication.destroy();
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
     * Configure the {@link ResourceConfig}.
     * <p>
     * The {@link ResourceConfig} is configured such that the following classes
     * may be injected onto the field of a root resource class or a parameter 
     * of a method of root resource class that is annotated with 
     * {@link javax.ws.rs.core.Context}: {@link HttpServletRequest}, {@link HttpServletResponse}
     * , {@link ServletContext}, and {@link ServletConfig}.
     * <p>
     * An inheriting class may override this method to configure the 
     * {@link ResourceConfig} to provide alternative or additional instances
     * that are resource or provider classes or instances, and may modify the
     * features and properties of the {@link ResourceConfig}. For an inheriting
     * class to extend configuration behaviour the overriding method MUST call
     * super.configure(servletConfig, rc, wa) as the first statement of that 
     * method.
     * <p>
     * This method will be called only once at servlet initiation. Subsequent
     * reloads of the Web application will not result in subsequence calls to
     * this method.
     *
     * @param sc the Servlet configuration
     * @param rc the Resource configuration
     * @param wa the Web application
     */
    protected void configure(final ServletConfig sc, ResourceConfig rc, WebApplication wa) {
        rc.getSingletons().add(new ContextInjectableProvider<HttpServletRequest>(
                HttpServletRequest.class,
                (HttpServletRequest)Proxy.newProxyInstance(
                        HttpServletRequest.class.getClassLoader(),
                        new Class[] { HttpServletRequest.class },
                        requestInvoker)));
        
        rc.getSingletons().add(new ContextInjectableProvider<HttpServletResponse>(
                HttpServletResponse.class,
                (HttpServletResponse)Proxy.newProxyInstance(
                        HttpServletResponse.class.getClassLoader(),
                        new Class[] { HttpServletResponse.class },
                        responseInvoker)));
        
        rc.getSingletons().add(new ContextInjectableProvider<ServletConfig>(
                ServletConfig.class, sc));
        
        rc.getSingletons().add(new ContextInjectableProvider<ServletContext>(
                ServletContext.class, 
                sc.getServletContext()));
        
        rc.getSingletons().add(new JSPTemplateProcessor(
                resourceConfig,
                requestInvoker.getThreadLocal(), 
                responseInvoker.getThreadLocal()));
    }
    
    /**
     * Initiate the {@link WebApplication}.
     * <p>
     * Any root resource class in registered in the resource configuration
     * that is an interface is processed as follows.
     * If the class is an interface and there exists a JNDI named object
     * with the fully qualified class name as the JNDI name then that named
     * object is added as a singleton root resource and the class is removed
     * from the set of root resource classes.
     * <p>
     * This method will be called once at servlet initiation and for
     * each reload of the Web application.
     * 
     * @param rc the Resource configuration
     * @param wa the Web application
     */
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        validate(rc);
        wa.initiate(rc);
    }

    /**
     * Get the default resource configuration if one is not declared in the
     * web.xml.
     * <p>
     * This implementaton returns an instance of {@link ClasspathResourceConfig}
     * that scans in files and directories as declared by the
     * {@link ClasspathResourceConfig.PROPERTY_CLASSPATH} if present, otherwise
     * in the "WEB-INF/lib" and "WEB-INF/classes" directories.
     * <p>
     * An inheriting class may override this method to supply a different
     * default resource configuraton implementaton.
     * 
     * @param props the properties to pass to the resource configuraton.
     * @param servletConfig the servlet configuration.
     * @return the default resource configuraton.
     * 
     * @throws javax.servlet.ServletException
     */
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
            ServletConfig servletConfig) throws ServletException  {
        // Default to using class path resource config
        String[] paths = getPaths(servletConfig.getInitParameter(
                ClasspathResourceConfig.PROPERTY_CLASSPATH));
        props.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        return new ClasspathResourceConfig(props);
    }

    private void validate(ResourceConfig rc) {
        // Obtain any instances that are registered in JNDI
        // Assumes such instances are singletons
        // Registered classes have to be interfaces
        javax.naming.Context x = getContext();
        if (context != null) {
            Iterator<Class<?>> i = rc.getClasses().iterator();
            while (i.hasNext()) {
                Class<?> c = i.next();
                if (!c.isInterface()) continue;
                
                try {
                    Object o = x.lookup(c.getName());
                    if (o != null) {
                        i.remove();
                        rc.getSingletons().add(o);
                        LOGGER.log(Level.CONFIG,
                                "An instance of the class " + c.getName() +
                                " is found by JNDI look up using the class name as the JNDI name. " +
                                "The instance will be registered as a singleton.");
                    }
                } catch (NamingException ex) {
                }
            }
        }
    }
    
    private javax.naming.Context getContext() {
        try {
            return new InitialContext();
        } catch (NamingException ex) {
            return null;
        }
    }
    
    // ContainerListener
    
    public void onReload() {
        reload();
    }
}