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
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
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
public class ServletContainer extends WebComponent implements Servlet, ServletConfig, Filter, Serializable {

    private transient ServletConfig servletConfig;

    private transient FilterConfig filterConfig;

    // ServletConfig

    public String getServletName() {
        return _getServletConfig().getServletName();
    }

    public ServletContext getServletContext() {
        return _getServletConfig().getServletContext();
    }

    public String getInitParameter(String name) {
        return _getServletConfig().getInitParameter(name);
    }

    public Enumeration getInitParameterNames() {
        return _getServletConfig().getInitParameterNames();
    }

    // Servlet

    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    private ServletConfig _getServletConfig() {
        if (servletConfig == null) {
            throw new IllegalStateException("Servlet is not initialized");
        }
        return servletConfig;
    }

    /**
     * Returns information about the servlet, such as
     * author, version, and copyright.
     * By default, this method returns an empty string.  Override this method
     * to have it return a meaningful value.  See {@link
     * Servlet#getServletInfo}.
     *
     * @return String information about this servlet, by default an
     *         empty string
     */
    public String getServletInfo() {
        return "";
    }

    public final void init(ServletConfig servletConfig) throws ServletException {
        this.servletConfig = servletConfig;

        init(new WebConfig() {

            public String getName() {
                return ServletContainer.this.servletConfig.getServletName();
            }

            public String getInitParameter(String name) {
                return ServletContainer.this.servletConfig.getInitParameter(name);
            }

            public Enumeration getInitParameterNames() {
                return ServletContainer.this.servletConfig.getInitParameterNames();
            }

            public ServletContext getServletContext() {
                return ServletContainer.this.servletConfig.getServletContext();
            }

            public ResourceConfig getDefaultResourceConfig(Map<String, Object> props) throws ServletException {
                return ServletContainer.this.getDefaultResourceConfig(props, ServletContainer.this.servletConfig);
            }
        });
    }

    /**
     * Dispatches client requests to the {@link #service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) }
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
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        try {
            service((HttpServletRequest) request, (HttpServletResponse) response);
        } catch (ClassCastException e) {
            throw new ServletException("non-HTTP request or response");
        }
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
    }

    //

    @Override
    protected void configure(WebConfig wc, ResourceConfig rc, WebApplication wa) {
        super.configure(wc, rc, wa);
        if (servletConfig != null)
            configure(servletConfig, rc, wa);
        else if (filterConfig != null)
            configure(filterConfig, rc, wa);
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
}