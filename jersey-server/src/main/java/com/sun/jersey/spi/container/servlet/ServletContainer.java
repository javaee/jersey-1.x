/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.WebAppResourceConfig;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.server.impl.application.DeferredResourceConfig;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.container.ReloadListener;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import com.sun.jersey.spi.service.ServiceFinder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * A {@link Servlet} or {@link Filter} for deploying root resource classes.
 * <p>
 * If this class is declared as a filter and the initialization parameter
 * {@link #PROPERTY_WEB_PAGE_CONTENT_REGEX} is not set
 * or {@link #FEATURE_FILTER_FORWARD_ON_404} is not set to true then the filter
 * must be declared at the last position in the filter chain as the filter will
 * not forward any request to a next filter (if any) in the chain.
 * <p>
 * The following sections make reference to initialization parameters. Unless
 * otherwise specified the initialization parameters apply to both server
 * and filter initialization parameters.
 * <p>
 * The servlet or filter may be configured to have an initialization
 * parameter "com.sun.jersey.config.property.resourceConfigClass" or
 * "javax.ws.rs.Application" and whose value is a
 * fully qualified name of a class that implements {@link ResourceConfig} or
 * {@link Application}.
 * If the concrete class has a constructor that takes a single parameter of the 
 * type Map then the class is instantiated with that constructor and an instance 
 * of Map that contains all the initialization parameters is passed as the 
 * parameter. Otherwise, the class is instantiated as a singleton component
 * managed by the runtime, and injection may be performed (the artifacts that
 * may be injected are limited to injectable providers registered when
 * the servlet or filter is configured).
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
 * If none of the above resource configuration related initialization parameters
 * are present a new instance of {@link WebAppResourceConfig} is created. The
 * initialization parameter "com.sun.jersey.config.property.classpath" MAY be
 * set to provide one or more resource paths. Each path MUST be separated by ';'.
 * 
 * The resource paths are added as a property value to a Map instance using
 * the property name (@link ClasspathResourceConfig.PROPERTY_CLASSPATH}. Any 
 * additional initialization parameters are then added to the Map instance. 
 * Then that Map instance is passed to the constructor of 
 * {@link WebAppResourceConfig}.
 * 
 * If the initialization parameter is not present then the following resource
 * paths are utilized:
 * "/WEB-INF/lib" and "/WEB-INF/classes".
 * <p>
 * All initialization parameters are added as properties of the created
 * {@link ResourceConfig}.
 * <p>
 * A new {@link WebApplication} instance will be created and configured such
 * that the following classes may be injected onto a root resource, provider
 * and {@link Application} classes using {@link javax.ws.rs.core.Context}:
 * {@link HttpServletRequest}, {@link HttpServletResponse},
 * {@link ServletContext}, {@link ServletConfig} and {@link WebConfig}.
 * If this class is used as a Servlet then the {@link ServletConfig} class may 
 * be injected. If this class is used as a Filter then the {@link FilterConfig}
 * class may be injected. {@link WebConfig} may be injected to abstract
 * servlet or filter deployment.
 * <p>
 * A {@link IoCComponentProviderFactory} instance may be registered by extending this class
 * and overriding the method {@link #initiate(ResourceConfig, WebApplication)}
 * to initiate the {@link WebApplication} with the {@link IoCComponentProviderFactory}
 * instance.
 * 
 */
public class ServletContainer extends HttpServlet implements Filter {
    /**
     * The servlet initialization property whose boolean value determines
     * if GlassFish default error pages will be returned or not.
     * <p>
     * The default value is true.
     * <p>
     * If false then GlassFish will not return default error pages.
     * <p>
     * This property is supported on GlassFish version 3.1 or greater.
     * @since 1.5
     */
    public static final String GLASSFISH_DEFAULT_ERROR_PAGE_RESPONSE = "org.glassfish.web.isDefaultErrorPageEnabled";
    
    /**
     * The servlet initialization property whose value is a fully qualified
     * class name of a class that implements {@link ResourceConfig} or
     * {@link Application}.
     */
    public static final String APPLICATION_CONFIG_CLASS =
            "javax.ws.rs.Application";

    /**
     * The servlet initialization property whose value is a fully qualified
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

    /**
     * If set the regular expression used to match an incoming servlet path URI
     * to some web page content such as static resources or JSPs to be handled
     * by the underlying servlet engine.
     * <p>
     * The type of this property must be a String and the value must be a valid
     * regular expression.
     * <p>
     * This property is only applicable when this class is used as a
     * {@link Filter}, otherwise this property will be ignored and not
     * processed.
     * <p>
     * If a servlet path matches this regular expression then the filter
     * forwards the request to the next filter in the filter chain so that the
     * underlying servlet engine can process the request otherwise Jersey
     * will process the request.
     * <p>
     * For example if you set the value to
     * <code>/(image|css)/.*</code>
     * then you can serve up images and CSS files for your Implicit or Explicit 
     * Views while still processing your JAX-RS resources.
     */
    public static final String PROPERTY_WEB_PAGE_CONTENT_REGEX
            = "com.sun.jersey.config.property.WebPageContentRegex";

    /**
     * If true and a 404 response with no entity body is returned from either
     * the runtime or the application then the runtime forwards the request to
     * the next filter in the filter chain. This enables another filter or
     * the underlying servlet engine to process the request.
     * Before the request is forwarded the response status is set to 200.
     * <p>
     * This property is only applicable when this class is used as a
     * {@link Filter}, otherwise this property will be ignored and not
     * processed.
     * <p>
     * Application code, such as methods corresponding to sub-resource locators
     * may be invoked when this feature is enabled.
     * <p>
     * This feature is an alternative to setting 
     * {@link #PROPERTY_WEB_PAGE_CONTENT_REGEX} and requires less configuration.
     * However, application code, such as methods corresponding to sub-resource
     * locators, may be invoked when this feature is enabled.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_FILTER_FORWARD_ON_404
            = "com.sun.jersey.config.feature.FilterForwardOn404";

    /**
     * The filter context path.
     * <p>
     * If the URL pattern of a filter is set to a base path and a wildcard,
     * such as "/base/*", then this property can be used to declare a filter
     * context path that behaves in the same manner as the Servlet context
     * path for determining the base URI of the application. (Note that with
     * the Servlet 2.x API it is not possible to determine the URL pattern
     * without parsing the web.xml, hence why this property is necessary.)
     * <p>
     * This property is only applicable when this class is used as a
     * {@link Filter}, otherwise this property will be ignored and not
     * processed.
     * <p>
     * This property may consist of one or more path segments separate by '/'.
     */
    public static final String PROPERTY_FILTER_CONTEXT_PATH
            = "com.sun.jersey.config.feature.FilterContextPath";

    /**
     * A helper class for creating an injectable provider that supports
     * {@link Context} with a type and constant value.
     *
     * @param <T> the type of the constant value.
     */
    protected static class ContextInjectableProvider<T> extends
            SingletonTypeInjectableProvider<Context, T> {

        /**
         * Create a new instance.
         *
         * @param type the type of the constant value.
         * @param instance the constant value.
         */
        protected ContextInjectableProvider(Type type, T instance) {
            super(type, instance);
        }
    }

    private transient WebComponent webComponent;

    private transient FilterConfig filterConfig;

    private transient Pattern staticContentPattern;

    private transient boolean forwardOn404;

    private class InternalWebComponent extends WebComponent {

        InternalWebComponent() {
        }

        InternalWebComponent(Application app) {
            super(app);
        }
        
        @Override
        protected WebApplication create() {
            return ServletContainer.this.create();
        }

        @Override
        protected void configure(WebConfig wc, ResourceConfig rc, WebApplication wa) {
            super.configure(wc, rc, wa);

            ServletContainer.this.configure(wc, rc, wa);
        }

        @Override
        protected void initiate(ResourceConfig rc, WebApplication wa) {
            ServletContainer.this.initiate(rc, wa);
        }

        @Override
        protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
                WebConfig wc) throws ServletException  {
            return ServletContainer.this.getDefaultResourceConfig(props, wc);
        }
    }

    private transient final Application app;

    public ServletContainer() {
        this.app = null;
    }

    public ServletContainer(Class<? extends Application> appClass) {
        this.app = new DeferredResourceConfig(appClass);
    }

    public ServletContainer(Application app) {
        this.app = app;
    }

    // GenericServlet

    /**
     * Get the servlet context for the servlet or filter, depending on
     * how this class is registered.
     * <p>
     * It is recommended that the {@link WebConfig} be utilized,
     * see the method {@link #getWebConfig() }, to obtain the servlet context
     * and initialization parameters for a servlet or filter.
     *
     * @return the servlet context for the servlet or filter.
     */
    @Override
    public ServletContext getServletContext() {
        if (filterConfig != null)
            return filterConfig.getServletContext();

        return super.getServletContext();
    }
    
    /**
     * Initiate the Web component.
     *
     * @param webConfig the Web configuration.
     *
     * @throws javax.servlet.ServletException
     */
    protected void init(WebConfig webConfig) throws ServletException {
        webComponent = (app == null)
                ? new InternalWebComponent()
                : new InternalWebComponent(app);
        webComponent.init(webConfig);
    }

    /**
     * Get the Web configuration.
     *
     * @return the Web configuration.
     */
    protected WebConfig getWebConfig() {
        return webComponent.getWebConfig();
    }

    /**
     * Create a new instance of a {@link WebApplication}.
     *
     * @return the {@link WebApplication} instance.
     */
    protected WebApplication create() {
        return WebApplicationFactory.createWebApplication();
    }
    
    /**
     * Get the default resource configuration if one is not declared in the
     * web.xml.
     * <p>
     * This implementaton returns an instance of {@link WebAppResourceConfig}
     * that scans in files and directories as declared by the
     * {@link ClasspathResourceConfig#PROPERTY_CLASSPATH} if present, otherwise
     * in the "WEB-INF/lib" and "WEB-INF/classes" directories.
     * <p>
     * An inheriting class may override this method to supply a different
     * default resource configuraton implementaton.
     *
     * @param props the properties to pass to the resource configuraton.
     * @param wc the web configuration.
     * @return the default resource configuraton.
     *
     * @throws javax.servlet.ServletException
     */
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
            WebConfig wc) throws ServletException  {
        return webComponent.getWebAppResourceConfig(props, wc);
    }

    /**
     * Configure the {@link ResourceConfig}.
     * <p>
     * The {@link ResourceConfig} is configured such that the following classes
     * may be injected onto the field of a root resource class or a parameter
     * of a method of root resource class that is annotated with
     * {@link javax.ws.rs.core.Context}: {@link HttpServletRequest}, {@link HttpServletResponse}
     * , {@link ServletContext} and {@link WebConfig}.
     * <p>
     * Any root resource class in registered in the resource configuration
     * that is an interface is processed as follows.
     * If the class is an interface and there exists a JNDI named object
     * with the fully qualified class name as the JNDI name then that named
     * object is added as a singleton root resource and the class is removed
     * from the set of root resource classes.
     * <p>
     * An inheriting class may override this method to configure the
     * {@link ResourceConfig} to provide alternative or additional instances
     * that are resource or provider classes or instances, and may modify the
     * features and properties of the {@link ResourceConfig}. For an inheriting
     * class to extend configuration behaviour the overriding method MUST call
     * <code>super.configure(servletConfig, rc, wa)</code> as the first statement
     * of that method.
     * <p>
     * This method will be called only once at initiation. Subsequent
     * reloads of the Web application will not result in subsequence calls to
     * this method.
     *
     * @param wc the Web configuration
     * @param rc the Resource configuration
     * @param wa the Web application
     */
    protected void configure(WebConfig wc, ResourceConfig rc, WebApplication wa) {
        if (getServletConfig() != null)
            configure(getServletConfig(), rc, wa);
        else if (filterConfig != null)
            configure(filterConfig, rc, wa);

        if (rc instanceof ReloadListener) {
            List<ContainerNotifier> notifiers = new ArrayList<ContainerNotifier>();

            Object o = rc.getProperties().get(ResourceConfig.PROPERTY_CONTAINER_NOTIFIER);

            if (o instanceof ContainerNotifier)
                notifiers.add((ContainerNotifier) o);
            else if (o instanceof List)
                for (Object elem : (List) o)
                    if (elem instanceof ContainerNotifier)
                        notifiers.add((ContainerNotifier) elem);

            for (ContainerNotifier cn : ServiceFinder.find(ContainerNotifier.class)) {
                notifiers.add(cn);
            }

            rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_NOTIFIER, notifiers);
        }
    }
    
    /**
     * Initiate the {@link WebApplication}.
     * <p>
     * This method will be called once at initiation and for
     * each reload of the Web application.
     * <p>
     * An inheriting class may override this method to initiate the
     * Web application with different parameters.
     *
     * @param rc the Resource configuration
     * @param wa the Web application
     */
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        wa.initiate(rc);
    }

    /**
     * Load the Web application. This will create, configure and initiate
     * the web application.
     */
    public void load() {
        webComponent.load();
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
    public void reload() {
        webComponent.onReload();
    }

    /**
     * Dispatch client requests to a resource class.
     *
     * @param baseUri the base URI of the request.
     * @param requestUri the URI of the request.
     * @param request the {@link HttpServletRequest} object that
     *        contains the request the client made to
     *	      the Web component.
     * @param response the {@link HttpServletResponse} object that
     *        contains the response the Web component returns
     *        to the client.
     * @return the status code of the response.
     * @exception IOException if an input or output error occurs
     *            while the Web component is handling the
     *            HTTP request.
     * @exception ServletException if the HTTP request cannot
     *            be handled.
     */
	public int service(URI baseUri, URI requestUri, final HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        return webComponent.service(baseUri, requestUri, request, response);
    }

    /**
     * Destroy this Servlet or Filter.
     * 
     */
    @Override
    public void destroy() {
        if (webComponent != null) {
            webComponent.destroy();
        }
    }

    
    // Servlet
    
    @Override
    public void init() throws ServletException {
        init(new WebServletConfig(this));
    }

    /**
     * Get the default resource configuration if one is not declared in the
     * web.xml.
     * <p>
     * This implementaton returns an instance of {@link WebAppResourceConfig}
     * that scans in files and directories as declared by the
     * {@link ClasspathResourceConfig#PROPERTY_CLASSPATH} if present, otherwise
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
     * @deprecated methods should implement {@link #getDefaultResourceConfig(java.util.Map, com.sun.jersey.spi.container.servlet.WebConfig) }.
     */
    @Deprecated
	protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
            ServletConfig servletConfig) throws ServletException  {
        return getDefaultResourceConfig(props, getWebConfig());
    }

    /**
     * Configure the {@link ResourceConfig} for a Servlet.
     * <p>
     * The {@link ResourceConfig} is configured such that the following classes
     * may be injected onto the field of a root resource class or a parameter
     * of a method of root resource class that is annotated with
     * {@link javax.ws.rs.core.Context}: {@link ServletConfig}.
     * <p>
     * An inheriting class may override this method to configure the
     * {@link ResourceConfig} to provide alternative or additional instances
     * that are resource or provider classes or instances, and may modify the
     * features and properties of the {@link ResourceConfig}. For an inheriting
     * class to extend configuration behaviour the overriding method MUST call
     * <code>super.configure(servletConfig, rc, wa)</code> as the first statement of that
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
        rc.getSingletons().add(new ContextInjectableProvider<ServletConfig>(
                ServletConfig.class, sc));
    }

    /**
     * Dispatches client requests to the {@link #service(java.net.URI, java.net.URI, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)  }
     * method.
     *
     * @param request the {@link HttpServletRequest} object that
     *        contains the request the client made to
     *	      the servlet.
     * @param response the {@link HttpServletResponse} object that
     *        contains the response the servlet returns
     *        to the client.
     * @exception IOException if an input or output error occurs
     *            while the servlet is handling the
     *            HTTP request.
     * @exception ServletException if the HTTP request cannot
     *            be handled.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /**
         * There is an annoying edge case where the service method is
         * invoked for the case when the URI is equal to the deployment URL
         * minus the '/', for example http://locahost:8080/HelloWorldWebApp
         */
        final String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        StringBuffer requestURL = request.getRequestURL();
        String requestURI = request.getRequestURI();
        final boolean checkPathInfo = pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/");
        final boolean checkServletPath = servletPath == null || servletPath.isEmpty();
        if (checkPathInfo && checkServletPath && !request.getRequestURI().endsWith("/")) {
            if (webComponent.getResourceConfig().getFeature(ResourceConfig.FEATURE_REDIRECT)) {
                URI l = UriBuilder.fromUri(request.getRequestURL().toString()).
                        path("/").
                        replaceQuery(request.getQueryString()).build();

                response.setStatus(307);
                response.setHeader("Location", l.toASCIIString());
                return;
            } else {
                pathInfo = "/";
                requestURL.append("/");
                requestURI += "/";
            }
        }

        /**
         * The HttpServletRequest.getRequestURL() contains the complete URI
         * minus the query and fragment components.
         */
        UriBuilder absoluteUriBuilder = UriBuilder.fromUri(
                requestURL.toString());

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
        final String decodedBasePath = (pathInfo != null)
                ? request.getContextPath() + servletPath + "/"
                : request.getContextPath() + "/";

        final String encodedBasePath = UriComponent.encode(decodedBasePath,
                UriComponent.Type.PATH);

        if (!decodedBasePath.equals(encodedBasePath)) {
            throw new ContainerException("The servlet context path and/or the " +
                    "servlet path contain characters that are percent enocded");
        }

        final URI baseUri = absoluteUriBuilder.replacePath(encodedBasePath).
                build();

        String queryParameters = request.getQueryString();
        if (queryParameters == null) {
            queryParameters = "";
        }

        final URI requestUri = absoluteUriBuilder.replacePath(requestURI).
                replaceQuery(queryParameters).
                build();

        service(baseUri, requestUri, request, response);
    }


    // Filter

    private String filterContextPath = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        init(new WebFilterConfig(filterConfig));
    }

    /**
     * @return the {@link Pattern} compiled from a regular expression that is
     * the property value of {@link #PROPERTY_WEB_PAGE_CONTENT_REGEX}.
     * A <code>null</code> value will be returned if the property is not present
     * is or an empty String.
     */
    public Pattern getStaticContentPattern() {
        return staticContentPattern;
    }

    /**
     * Configure the {@link ResourceConfig} for a Filter.
     * <p>
     * The {@link ResourceConfig} is configured such that the following classes
     * may be injected onto the field of a root resource class or a parameter
     * of a method of root resource class that is annotated with
     * {@link javax.ws.rs.core.Context}: {@link FilterConfig}.
     * <p>
     * An inheriting class may override this method to configure the
     * {@link ResourceConfig} to provide alternative or additional instances
     * that are resource or provider classes or instances, and may modify the
     * features and properties of the {@link ResourceConfig}. For an inheriting
     * class to extend configuration behaviour the overriding method MUST call
     * <code>super.configure(servletConfig, rc, wa)</code> as the first statement of that
     * method.
     * <p>
     * This method will be called only once at servlet initiation. Subsequent
     * reloads of the Web application will not result in subsequence calls to
     * this method.
     *
     * @param fc the Filter configuration
     * @param rc the Resource configuration
     * @param wa the Web application
     */
    protected void configure(final FilterConfig fc, ResourceConfig rc, WebApplication wa) {
        rc.getSingletons().add(new ContextInjectableProvider<FilterConfig>(
                FilterConfig.class, fc));

        String regex = (String)rc.getProperty(PROPERTY_WEB_PAGE_CONTENT_REGEX);
        if (regex != null && regex.length() > 0) {
            try {
                staticContentPattern = Pattern.compile(regex);
            } catch (PatternSyntaxException ex) {
                throw new ContainerException(
                        "The syntax is invalid for the regular expression, " + regex +
                        ", associated with the initialization parameter " + PROPERTY_WEB_PAGE_CONTENT_REGEX, ex);
            }
        }

        forwardOn404 = rc.getFeature(FEATURE_FILTER_FORWARD_ON_404);

        this.filterContextPath = filterConfig.getInitParameter(PROPERTY_FILTER_CONTEXT_PATH);
        if (filterContextPath != null) {
            if (filterContextPath.isEmpty()) {
                filterContextPath = null;
            } else {
                if (!filterContextPath.startsWith("/")) {
                    filterContextPath = '/' + filterContextPath;
                }
                if (filterContextPath.endsWith("/")) {
                    filterContextPath = filterContextPath.substring(0, filterContextPath.length() - 1);
                }
            }
        }
    }
    
    /**
     * Dispatches client requests to the {@link #doFilter(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain) }
     * method.
     *
     * @param request the {@link HttpServletRequest} object that
     *        contains the request the client made to
     *	      the servlet.
     * @param response the {@link HttpServletResponse} object that
     *        contains the response the servlet returns
     *        to the client.
     * @param chain the chain of filters from which the next filter can be invoked.
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
        } catch (ClassCastException e) {
            throw new ServletException("non-HTTP request or response");
        }
    }

    /**
     * Dispatches client requests to the {@link #service(java.net.URI, java.net.URI, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)  }
     * method.
     * <p>
     * If the servlet path matches the regular expression declared by the
     * property {@link #PROPERTY_WEB_PAGE_CONTENT_REGEX} then the request
     * is forwarded to the next filter in the filter chain so that the
     * underlying servlet engine can process the request otherwise Jersey
     * will process the request.
     * 
     * @param request the {@link HttpServletRequest} object that
     *        contains the request the client made to
     *	      the servlet.
     * @param response the {@link HttpServletResponse} object that
     *        contains the response the servlet returns
     *        to the client.
     * @param chain the chain of filters from which the next filter can be invoked.
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getAttribute("javax.servlet.include.request_uri") != null) {
            final String includeRequestURI = (String)request.getAttribute("javax.servlet.include.request_uri");

            if (!includeRequestURI.equals(request.getRequestURI())) {
                doFilter(request, response, chain,
                        includeRequestURI,
                        (String)request.getAttribute("javax.servlet.include.servlet_path"),
                        (String)request.getAttribute("javax.servlet.include.query_string"));
                return;
            }
        }

        doFilter(request, response, chain,
                request.getRequestURI(),
                request.getServletPath(),
                request.getQueryString());
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            String requestURI, String servletPath, String queryString) throws IOException, ServletException {
        // if we match the static content regular expression lets delegate to 
        // the filter chain to use the default container servlets & handlers
        final Pattern p = getStaticContentPattern();
        if (p != null && p.matcher(servletPath).matches()) {
            chain.doFilter(request, response);
            return;
        }

        if (filterContextPath != null) {
            if (!servletPath.startsWith(filterContextPath)) {
                throw new ContainerException("The servlet path, \"" + servletPath +
                        "\", does not start with the filter context path, \"" + filterContextPath + "\"");
            } else if (servletPath.length() == filterContextPath.length()) {
                // Path does not end in a slash, may need to redirect
                if (webComponent.getResourceConfig().getFeature(ResourceConfig.FEATURE_REDIRECT)) {
                    URI l = UriBuilder.fromUri(request.getRequestURL().toString()).
                            path("/").
                            replaceQuery(queryString).build();

                    response.setStatus(307);
                    response.setHeader("Location", l.toASCIIString());
                    return;
                } else {
                    requestURI += "/";
                }
            }
        }

        final UriBuilder absoluteUriBuilder = UriBuilder.fromUri(
                request.getRequestURL().toString());

        final URI baseUri = (filterContextPath == null)
                ? absoluteUriBuilder.replacePath(request.getContextPath()).
                        path("/").
                        build()
                : absoluteUriBuilder.replacePath(request.getContextPath()).
                        path(filterContextPath).
                        path("/").
                        build();

        final URI requestUri = absoluteUriBuilder.replacePath(requestURI).
                replaceQuery(queryString).
                build();

        final int status = service(baseUri, requestUri, request, response);

        // If forwarding is configured and response is a 404 with no entity
        // body then call the next filter in the chain
        if (forwardOn404 && status == 404 && !response.isCommitted()) {
            // lets clear the response to OK before we forward to the next in the chain
            // as OK is the default set by servlet containers before filters/servlets do any wor
            // so lets hide our footsteps and pretend we were never in the chain at all and let the 
            // next filter or servlet return the 404 if they can't find anything to return
            // 
            // We could add an optional flag to disable this step if anyone can ever find a case where
            // this causes a problem, though I suspect any problems will really be with downstream
            // servlets not correctly setting an error status if they cannot find something to return
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(request, response);
        }
    }
}
