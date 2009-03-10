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
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Enumeration;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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


/**
 * A {@link Servlet} or {@link Filter} for deploying root resource classes.
 * <p>
 * If this class is declared as a filter and the initialization parameter
 * {@link #PROPERTY_WEB_PAGE_CONTENT_REGEX} is not set, then the filter must be
 * declared at the last position in the filter chain as the filter will not
 * forward any request to a next filter (if any) in the chain.
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
 * All initialization parameters are added as properties of the created
 * {@link ResourceConfig}.
 *
 * <p>
 * A new {@link WebApplication} instance will be created and configured such
 * that the following classes may be injected onto the field of a root 
 * resource class or a parameter of a method of root resource class that is 
 * annotated with {@link javax.ws.rs.core.Context}: {@link HttpServletRequest}, 
 * {@link HttpServletResponse}, {@link ServletContext}, and {@link ServletConfig}.
 * If this class is used as a Servlet then the {@link ServletConfig} class may be
 * injected. If this class is used as a Filter then the {@link FilterConfig} class may be
 * injected.
 * 
 * <p>
 * A {@link IoCComponentProviderFactory} instance may be registered by extending this class
 * and overriding the method {@link #initiate(ResourceConfig, WebApplication)}
 * to initiate the {@link WebApplication} with the {@link IoCComponentProviderFactory}
 * instance.
 * 
 */
public class ServletContainer extends HttpServlet implements Filter {
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

    /**
     * If set the regular expression used to match an incoming servlet path URI
     * to some web page content such as static resources or JSPs to be handled
     * by the underlying servlet engine.
     * <p>
     * The type of this property must be a String and the value must be a valid
     * regular expression.
     * <p>
     * This property is only applicable when this class is used as a
     * {@link Filter}, otherwise this property will be ingored and not
     * processed.
     * <p>
     * If a servlet path matches this regular expression then the filter
     * forwards the request to the next filter in the filter chain so that the
     * underlying servlet engine can process the request otherwise Jersey
     * will process the request.
     * <p>
     * For example if you set the value to
     * <code>/(image|css)/.*</code>
     * then you can serve up images and CSS files for your Implicit or Explicit Views
     * while still processing your JAX-RS resources.
     */
    public static final String PROPERTY_WEB_PAGE_CONTENT_REGEX
            = "com.sun.jersey.config.property.WebPageContentRegex";

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

    private class InternalWebComponent extends WebComponent {
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
        webComponent = new InternalWebComponent();
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
     * This implementaton returns an instance of {@link ClasspathResourceConfig}
     * that scans in files and directories as declared by the
     * {@link ClasspathResourceConfig.PROPERTY_CLASSPATH} if present, otherwise
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
        return webComponent.getClassPathResourceConfig(props, wc);
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
        webComponent.reload();
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
     * @exception IOException if an input or output error occurs
     *            while the Web component is handling the
     *            HTTP request.
     * @exception ServletException if the HTTP request cannot
     *            be handled.
     */
    public void service(URI baseUri, URI requestUri, final HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        webComponent.service(baseUri, requestUri, request, response);
    }

    /**
     * Destroy this Servlet or Filter.
     * 
     */
    @Override
    public void destroy() {
        webComponent.destroy();
    }

    
    // Servlet
    
    @Override
    public void init() throws ServletException {
        init(new WebConfig() {

            public String getName() {
                return ServletContainer.this.getServletName();
            }

            public String getInitParameter(String name) {
                return ServletContainer.this.getInitParameter(name);
            }

            public Enumeration getInitParameterNames() {
                return ServletContainer.this.getInitParameterNames();
            }

            public ServletContext getServletContext() {
                return ServletContainer.this.getServletContext();
            }

            public ResourceConfig getDefaultResourceConfig(Map<String, Object> props) throws ServletException {
                return ServletContainer.this.getDefaultResourceConfig(props, ServletContainer.this.getServletConfig());
            }
        });
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
        if (request.getPathInfo() != null &&
                request.getPathInfo().equals("/") && !request.getRequestURI().endsWith("/")) {
            response.setStatus(404);
            return;
        }

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

        final URI baseUri = absoluteUriBuilder.replacePath(encodedBasePath).
                build();

        String queryParameters = request.getQueryString();
        if (queryParameters == null) {
            queryParameters = "";
        }

        final URI requestUri = absoluteUriBuilder.replacePath(request.getRequestURI()).
                replaceQuery(queryParameters).
                build();

        service(baseUri, requestUri, request, response);
    }


    // Filter

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        init(new WebConfig() {

            public String getName() {
                return ServletContainer.this.filterConfig.getFilterName();
            }

            public String getInitParameter(String name) {
                return ServletContainer.this.filterConfig.getInitParameter(name);
            }

            public Enumeration getInitParameterNames() {
                return ServletContainer.this.filterConfig.getInitParameterNames();
            }

            public ServletContext getServletContext() {
                return ServletContainer.this.filterConfig.getServletContext();
            }

            public ResourceConfig getDefaultResourceConfig(Map<String, Object> props) throws ServletException {
                return null;
            }
        });
    }

    /**
     * @return the {@link Pattern} compiled from a regular expression that is
     * the property value of {@link #PROPERTY_STATIC_CONTENT_REGEX}.
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
     * property {@link #PROPERTY_STATIC_CONTENT_REGEX} then the request
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
        String servletPath = request.getServletPath();

        // if we match the static content regular expression lets delegate to the filter chain
        // to use the default container servlets & handlers
        Pattern p = getStaticContentPattern();
        if (p != null && p.matcher(servletPath).matches()) {
            chain.doFilter(request, response);
            return;
        }

        final UriBuilder absoluteUriBuilder = UriBuilder.fromUri(
                request.getRequestURL().toString());

        final URI baseUri = absoluteUriBuilder.replacePath(request.getContextPath()).
                path("/").
                build();

        final URI requestUri = absoluteUriBuilder.replacePath(request.getRequestURI()).
                replaceQuery(request.getQueryString()).
                build();

        service(baseUri, requestUri, request, response);
    }
}