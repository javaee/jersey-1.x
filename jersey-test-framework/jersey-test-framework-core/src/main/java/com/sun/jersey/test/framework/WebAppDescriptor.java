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

package com.sun.jersey.test.framework;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.servlet.Filter;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.DispatcherType;

/**
 * A Web-based application descriptor.
 * <p>
 * An instance of this class is created by creating an instance of
 * {@link Builder}, invoking methods to add/modify state, and finally invoking 
 * the {@link Builder#build() } method.
 * <p>
 * This application descriptor is compatible with web-based test containers
 * that support Servlets. The following Web-based test container
 * factories are provided:
 * <ul>
 *  <li>{@link GrizzlyWebTestContainerFactory} for testing with the Grizzly
 *      Web container and Servlet support.</li>
 *  <li>{@link EmbeddedGlassFishTestContainerFactory} for testing with
 *      embedded GlassFish.</li>
 *  <li>{@link ExternalTestContainerFactory} for testing when the Web
 *      application is independently deployed in a separate JVM to that of the
 *      tests. For example, the application may be deployed to the
 *      Glassfish v2 or v3 application server.</li>
 * </ul>
 * 
 * @author Paul.Sandoz@Sun.COM
 */
public class WebAppDescriptor extends AppDescriptor {

    /**
     * The builder for building a Web-based application descriptor.
     * <p>
     * If properties of the builder are not modified default values be utilized.
     * The default value for initialization and context parameters is an
     * empty map.
     * The default value for the context and servlet path is an empty string.
     * The default value for the servlet class is the class
     * {@link ServletContainer}.
     * The default value for the filter class and the servlet context listener
     * class is <code>null</code>.
     * <p>
     * After the {@link #build() } has been invoked the state of the builder
     * will be reset to the default values.
     */
    public static class Builder
            extends AppDescriptorBuilder<Builder, WebAppDescriptor> {

        protected Map<String, String> initParams;

        protected Map<String, String> contextParams;

        protected Class<? extends HttpServlet> servletClass = ServletContainer.class;

        protected List<FilterDescriptor> filters;

        protected List<Class<? extends EventListener>> listeners;

        protected String contextPath = "";

        protected String servletPath = "";

        /**
         * Create a builder.
         *
         */
        public Builder() {
        }

        /**
         * Create a builder with initialization parameters.
         *
         * @param initParams a map of initialization parameters. The parameters
         *        will be copied.
         * @throws IllegalArgumentException if <code>initParams</code> is null.
         */
        public Builder(Map<String, String> initParams) throws IllegalArgumentException {
            if (initParams == null)
                throw new IllegalArgumentException("The initialization parameters must not be null");

            this.initParams = new HashMap<String, String>();
            this.initParams.putAll(initParams);
        }

        /**
         * Create a builder with one initialization parameter.
         *
         * @param name the parameter name.
         * @param value the parameter value.
         */
        public Builder(String name, String value) {
            initParam(name, value);
        }

        /**
         * Create a builder with one or more package names where
         * root resource and provider classes reside.
         *
         * @param packages one or more package names where
         *        root resource and provider classes reside.
         * @throws IllegalArgumentException if <code>packages</code> is null.
         */
        public Builder(String... packages) throws IllegalArgumentException {
            if (packages == null)
                throw new IllegalArgumentException("The packages must not be null");

            StringBuilder sb = new StringBuilder();
            for (String packageName : packages) {
                if (sb.length() > 0) sb.append(';');
                sb.append(packageName);
            }

            initParam(PackagesResourceConfig.PROPERTY_PACKAGES,
                    sb.toString());
        }

        /**
         * Add an initialization parameter.
         *
         * @param name the parameter name.
         * @param value the parameter value.
         * @return this builder.
         */
        public Builder initParam(String name, String value) {
            if (this.initParams == null)
                this.initParams = new HashMap<String, String>();
            this.initParams.put(name, value);

            return this;
        }

        /**
         * Add a context parameter.
         *
         * @param name the parameter name.
         * @param value the parameter value.
         * @return this builder.
         */
        public Builder contextParam(String name, String value) {
            if (this.contextParams == null)
                this.contextParams = new HashMap<String, String>();
            this.contextParams.put(name, value);

            return this;
        }

        /**
         * Set the servlet class.
         *
         * <p> Setting a servlet class resets the filter class to null.
         *
         * @param servletClass the servlet class to serve the application.
         * @return this builder.
         * @throws IllegalArgumentException if <code>servletClass</code> is null.
         */
        public Builder servletClass(Class<? extends HttpServlet> servletClass)
                throws IllegalArgumentException {
            if (servletClass == null)
                throw new IllegalArgumentException("The servlet class must not be null");

            this.servletClass = servletClass;
            this.filters = null;
            return this;
        }

        /**
         * Equivalent to {@code filterClass(filterClass, "jerseyfilter", Collections.emptyMap(), EnumSet.allOf(DispatcherType.class))}.
         *
         * @param filterClass the filter class to serve the application.
         * @return this builder.
         * @throws IllegalArgumentException if <code>filterClass</code> is null.
         */
        public Builder filterClass(Class<? extends Filter> filterClass)
                throws IllegalArgumentException {
            filterClass(filterClass, "jerseyfilter",
                    Collections.<String, String>emptyMap(),
                    EnumSet.allOf(DispatcherType.class));
            return this;
        }
        
        /**
         * Set the filter class.
         *
         * <p> Setting a filter class resets the servlet class to null
         *
         * @param filterClass filter class.
         * @param filterName filter name.
         * @param initParams filter init parameters.
         * @param dispatches indicates what kind of operations this filter should be applied to.
         * @return this builder.
         * @throws IllegalArgumentException if <code>filterClass</code>, <code>filterName</code>, <code>initParams</code> or <code>dispatches</code> are null.
         */
        public Builder filterClass(Class<? extends Filter> filterClass, String filterName,
                Map<String, String> initParams, EnumSet<DispatcherType> dispatches)
                throws IllegalArgumentException {
             this.servletClass = null;
            addFilter(filterClass, filterName, initParams, dispatches);
             return this;
         }

        /**
         * Adds filter class.
         *
         * <p> Adding a filter class DOES NOT reset the servlet or filter classes.
         * Filter will be instantiated without initialization parameters.
         *
         * @param filterClass filter class. Must not be null.
         * @param filterName filter name. Must not be null or empty string.
         * @return this builder.
         * @throws IllegalArgumentException if <code>filterClass</code> or <code>filterName</code> is null.
         */
        public Builder addFilter(Class<? extends Filter> filterClass, String filterName) throws IllegalArgumentException {
            return addFilter(filterClass, filterName, Collections.<String, String>emptyMap());
        }

        /**
         * Equivalent to {@code addFilter(filterClass, filterName, initParams, EnumSet.allOf(DispatcherType.class)}.
         *
         * @param filterClass filter class. Must not be null.
         * @param filterName filter name. Must not be null or empty string.
         * @param initParams filter init parameters. Must not be null.
         * @return this builder.
         * @throws IllegalArgumentException if <code>filterClass</code>, <code>filterName</code> or <code>initParams</code> is null.
         */
        public Builder addFilter(Class<? extends Filter> filterClass, String filterName,
                Map<String, String> initParams) throws IllegalArgumentException {
            return addFilter(filterClass, filterName, initParams, EnumSet.allOf(DispatcherType.class));
        }

        /**
         * Adds filter class.
         *
         * <p> Adding a filter class DOES NOT reset the servlet or filter classes
         *
         * @param filterClass filter class. Must not be null.
         * @param filterName filter name. Must not be null or empty string.
         * @param initParams filter init parameters. Must not be null.
         * @param dispatches indicates what kind of operations this filter should be applied to
         * @return this builder.
         * @throws IllegalArgumentException if <code>filterClass</code>, <code>filterName</code> or <code>initParams</code> is null.
         */
        public Builder addFilter(Class<? extends Filter> filterClass, String filterName,
                                 Map<String, String> initParams, EnumSet<DispatcherType> dispatches) throws IllegalArgumentException {
            if(this.filters == null)
                this.filters = new ArrayList<FilterDescriptor>();

            if(filterClass == null)
                throw new IllegalArgumentException("The filter class must not be null");

            if((filterName == null) || (filterName.isEmpty()))
                throw new IllegalArgumentException("The filter name must not be null or empty string");

            if(initParams == null)
                throw new IllegalArgumentException("Provided initParams are invalid; initParams must not be null");

            this.filters.add(new FilterDescriptor(filterClass, filterName, initParams, dispatches));
            return this;
        }

        /**
         * Set the context path.
         * 
         * @param contextPath the context path to the application. (See Servlet specification for definition of contextPath)
         * @return this builder.
         * @throws IllegalArgumentException if <code>contextPath</code> is null.
         */
        public Builder contextPath(String contextPath) {
            if (contextPath == null)
                throw new IllegalArgumentException("The context path must not be null");

            this.contextPath = contextPath;
            return this;
        }

        /**
         * Set the servlet path.
         *
         * @param servletPath the servlet path to the application. (See Servlet specification for definition of servletPath)
         * @return this builder.
         * @throws IllegalArgumentException if <code>servletPath</code> is null.
         */
        public Builder servletPath(String servletPath) {
            if (servletPath == null)
                throw new IllegalArgumentException("The servlet path must not be null");

            this.servletPath = servletPath;
            return this;
        }

        /**
         * Set a <code>ServletContextListener</code> class
         *
         * @param contextListenerClass the servlet context listener class.
         * @return this builder.
         * @throws IllegalArgumentException if <code>contextListenerClass</code> is null.
         */
        public Builder contextListenerClass(Class<? extends ServletContextListener> contextListenerClass) {
            if (contextListenerClass == null)
                throw new IllegalArgumentException("The servlet context listener class must not be null");

            if (this.listeners == null) {
                this.listeners = new ArrayList<Class<? extends EventListener>>();
            }

            this.listeners.add(contextListenerClass);
            return this;
        }

        /**
         * Set a <code>ServletContextListener</code> class
         *
         * @param contextAttributeListenerClass the servlet context listener class.
         * @return this builder.
         * @throws IllegalArgumentException if <code>contextListenerClass</code> is null.
         */
        public Builder contextAttributeListenerClass(Class<? extends ServletContextAttributeListener> contextAttributeListenerClass) {
            if (contextAttributeListenerClass == null)
                throw new IllegalArgumentException("The servlet context listener class must not be null");

            if (this.listeners == null) {
                this.listeners = new ArrayList<Class<? extends EventListener>>();
            }

            this.listeners.add(contextAttributeListenerClass);
            return this;
        }

        /**
         * Set a <code>ServletRequestListener</code> class
         *
         * @param requestListenerClass the servlet request listener class.
         * @return this builder.
         * @throws IllegalArgumentException if <code>requestListenerClass</code> is null.
         */
        public Builder requestListenerClass(Class<? extends ServletRequestListener> requestListenerClass) {
            if (requestListenerClass == null)
                throw new IllegalArgumentException("The servlet request listener class must not be null");

            if (this.listeners == null) {
                this.listeners = new ArrayList<Class<? extends EventListener>>();
            }

            this.listeners.add(requestListenerClass);
            return this;
        }

        /**
         * Set a <code>ServletRequestAttributeListener</code> class
         *
         * @param requestAttributeListenerClass the servlet request attribute listener class.
         * @return this builder.
         * @throws IllegalArgumentException if <code>requestAttributeListenerClass</code> is null.
         */
        public Builder requestAttributeListenerClass(Class<? extends ServletRequestAttributeListener> requestAttributeListenerClass) {
            if (requestAttributeListenerClass == null)
                throw new IllegalArgumentException("The servlet request attribute listener class must not be null");

            if (this.listeners == null) {
                this.listeners = new ArrayList<Class<? extends EventListener>>();
            }

            this.listeners.add(requestAttributeListenerClass);
            return this;
        }

        /**
         * Set a <code>HttpSessionListener</code> class
         *
         * @param httpSessionListenerClass the HTTP Session Listener class.
         * @return this builder.
         * @throws IllegalArgumentException if <code>httpSessionListenerClass</code> is null.
         */
        public Builder httpSessionListenerClass(Class<? extends HttpSessionListener> httpSessionListenerClass) {
            if (httpSessionListenerClass == null)
                throw new IllegalArgumentException("The http session listener class must not be null");

            if (this.listeners == null) {
                this.listeners = new ArrayList<Class<? extends EventListener>>();
            }

            this.listeners.add(httpSessionListenerClass);
            return this;
        }

        /**
         * Set a <code>HttpSessionActivationListener</code> class
         *
         * @param httpSessionActivationListenerClass the HTTP Session Activation Listener class.
         * @return this builder.
         * @throws IllegalArgumentException if <code>httpSessionActivationListenerClass</code> is null.
         */
        public Builder httpSessionActivationListenerClass(Class<?
            extends HttpSessionActivationListener> httpSessionActivationListenerClass) {
            if (httpSessionActivationListenerClass == null)
                throw new IllegalArgumentException("The http session activation listener " +
                        "class must not be null");

            if (this.listeners == null) {
                this.listeners = new ArrayList<Class<? extends EventListener>>();
            }

            this.listeners.add(httpSessionActivationListenerClass);
            return this;
        }

        /**
         * Set a <code>HttpSessionAttributeListener</code> class
         *
         * @param httpSessionAttributeListenerClass the HTTPSessionAttributeListener class.
         * @return this builder.
         * @throws IllegalArgumentException if <code>httpSessionAttributeListenerClass</code> is null.
         */
        public Builder httpSessionAttributeListenerClass(Class<?
            extends HttpSessionAttributeListener> httpSessionAttributeListenerClass) {
            if (httpSessionAttributeListenerClass == null)
                throw new IllegalArgumentException("The http session attribute listener " +
                        "class must not be null");

            if (this.listeners == null) {
                this.listeners = new ArrayList<Class<? extends EventListener>>();
            }

            this.listeners.add(httpSessionAttributeListenerClass);
            return this;
        }
        
        /**
         * Build the Web-based application descriptor.
         * .
         * @return the Web-based application descriptor.
         */
        public WebAppDescriptor build() {
            WebAppDescriptor wd = new WebAppDescriptor(this);
            reset();

            return wd;
        }

        @Override
        protected void reset() {
            super.reset();
            
            this.initParams = null;
            this.contextParams = null;
            this.servletClass = ServletContainer.class;
            this.filters = null;
            this.listeners = null;
            this.contextPath = "";
            this.servletPath = "";
        }
    }

    public static class FilterDescriptor {
        private Class<? extends Filter> filterClass;
        private String filterName;
        private Map<String, String> initParams;
        private EnumSet<DispatcherType> dispatches;

        FilterDescriptor(Class<? extends Filter> filterClass, String filterName, Map<String, String> initParams, EnumSet<DispatcherType> dispatches) {
            this.filterClass = filterClass;
            this.filterName = filterName;
            this.initParams = initParams;
            this.dispatches = dispatches;
        }

        public Class<? extends Filter> getFilterClass() {
            return filterClass;
        }

        public String getFilterName() {
            return filterName;
        }

        public Map<String, String> getInitParams() {
            return initParams;
        }

        public EnumSet<DispatcherType> getDispatches() {
            return dispatches;
        }    
    }

    private final Map<String, String> initParams;

    private final Map<String, String> contextParams;

    private final Class<? extends HttpServlet> servletClass;

    private final List<FilterDescriptor> filters;

    private final List<Class<? extends EventListener>> listeners;
    
    private final String contextPath;

    private final String servletPath;

    /**
     * Creates an instance of {@link WebAppDescriptor} from the passed {@link Builder}
     * instance.
     * @param b {@link Builder} instance
     */
    private WebAppDescriptor(Builder b) {
        super(b);

        this.initParams = (b.initParams == null)
            ? new HashMap<String, String>()
            : b.initParams;
        this.contextParams = (b.contextParams == null)
            ? new HashMap<String, String>()
            : b.contextParams;
        this.servletClass = b.servletClass;
        this.filters = b.filters;
        this.contextPath = b.contextPath;
        this.servletPath = b.servletPath;
        if (b.listeners == null) {
                this.listeners = Collections.emptyList();
        } else {
                this.listeners = Collections.unmodifiableList(b.listeners);
        }
        
    }

    /**
     * Get the initialization parameters.
     *
     * @return the initialization parameters.
     */
    public Map<String, String> getInitParams() {
        return initParams;
    }

    /**
     * Get the context parameters.
     * 
     * @return the context parameters.
     */
    public Map<String, String> getContextParams() {
        return contextParams;
    }

    /**
     * Get the servlet class.
     * 
     * @return the servlet class.
     */
    public Class<? extends HttpServlet> getServletClass() {
        return servletClass;
    }

    /**
     * Get the filter class.
     *
     * @return the filter classes.
     */
    public List<FilterDescriptor> getFilters() {
        return filters;
    }

    /**
     * Get the context path.
     * 
     * @return the context path.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Get the servlet path.
     *
     * @return the servlet path.
     */
    public String getServletPath() {
        return servletPath;
    }

    /**
     * Get all the registered Listener classes
     * @return the registered listener classes, or <code>null</code>
     *         if none is registered.
     */
    public List<Class<? extends EventListener>> getListeners() {
        return listeners;
    }
    
}