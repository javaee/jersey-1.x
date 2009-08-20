package com.sun.jersey.test.framework;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

/**
 *  This class provides the necessary APIs for defining an application, so that
 * it could be deployed and tested on containers like Grizzly Web Server, Embedded
 * GlassFish, etc..
 * <p> It follows the Builder design pattern.
 * @author Paul.Sandoz@Sun.COM
 */
public class WebAppDescriptor extends AppDescriptor {

    /**
     * The Builder class for building an instance of {@link WebAppDescriptor}.
     */
    public static class Builder
            extends AppDescriptorBuilder<Builder> {

        protected final Map<String, String> initParams;

        protected final Map<String, String> contextParams;

        protected Class<? extends HttpServlet> servletClass = ServletContainer.class;

        protected Class<? extends Filter> filterClass;

        protected Class<? extends ServletContextListener> contextListenerClass;

        protected String contextPath = "";

        protected String servletPath = "";

        /**
         * Create an instance of the {@link Builder} from a map of application init-params.
         * @param A map of initParams, with the parameter name and value as key-value pair.
         */
        public Builder(Map<String, String> initParams) {
            if (initParams == null)
                throw new IllegalArgumentException("The initialization parameters must not be null");
            
            this.initParams = initParams;
            this.contextParams = new HashMap<String, String>();
        }

        /**
         * Create an instance of the {@link Builder} from an init-param name and value.
         * @param Init-param name
         * @param Initi-param value.
         */
        public Builder(String name, String value) {
            this.initParams = new HashMap<String, String>();
            this.contextParams = new HashMap<String, String>();

            initParam(name, value);
        }

        /**
         * Create an instance of the {@link Builder} from a fully qualified root resource
         * package name or an array of package names.
         * @param Root resource package name or an array of package names
         */
        public Builder(String... packages) {
            if (packages == null)
                throw new IllegalArgumentException("The packages must not be null");

            StringBuilder sb = new StringBuilder();
            for (String packageName : packages) {
                if (sb.length() > 0) sb.append(';');
                sb.append(packageName);
            }

            this.initParams = new HashMap<String, String>();
            this.initParams.put(PackagesResourceConfig.PROPERTY_PACKAGES,
                    sb.toString());

            this.contextParams = new HashMap<String, String>();
        }

        /**
         * Sets an init-param with the passed name and value.
         * @param Name of the init-param
         * @param Value of the init-param
         * @return The {@link Builder} instance.
         */
        public Builder initParam(String name, String value) {
            this.initParams.put(name, value);

            return this;
        }

        /**
         * Sets a context-param with the passed name and value.
         * @param Name of the context-param
         * @param Value of the context-param
         * @return The {@link Builder} instance
         */
        public Builder contextParam(String name, String value) {
            this.contextParams.put(name, value);

            return this;
        }

        /**
         * Sets the servlet-class.
         * @param The servlet class of the resource application
         * @return The {@link Builder} instance
         */
        public Builder servletClass(Class<? extends HttpServlet> servletClass) {
            if (servletClass == null)
                throw new IllegalArgumentException("The servlet class must not be null");

            this.servletClass = servletClass;
            this.filterClass = null;
            return this;
        }

        /**
         * Sets the filter-class.
         * @param The filter class of the resource application
         * @return The {@link Builder} instance
         */
        public Builder filterClass(Class<? extends Filter> filterClass) {
            if (filterClass == null)
                throw new IllegalArgumentException("The filter class must not be null");

            this.filterClass = filterClass;
            this.servletClass = null;
            return this;
        }

        /**
         * Sets the application context-path.
         * @param The context-path of the application
         * @return The {@link Builder} instance
         */
        public Builder contextPath(String contextPath) {
            if (contextPath == null)
                throw new IllegalArgumentException("The context path must not be null");

            this.contextPath = contextPath;
            return this;
        }

        /**
         * Sets the servlet-path
         * @param The servlet-path of the application
         * @return The {@link Builder} instance
         */
        public Builder servletPath(String servletPath) {
            if (servletPath == null)
                throw new IllegalArgumentException("The servlet path must not be null");

            this.servletPath = servletPath;
            return this;
        }

        /**
         * Sets the {@link ServletContextListener} class
         * @param The ServletContextListener class to set
         * @return The {@link Builder} instance
         */
        public Builder contextListenerClass(Class<? extends ServletContextListener> contextListenerClass) {
            if (contextListenerClass == null)
                throw new IllegalArgumentException("The servlet context listener class must not be null");

            this.contextListenerClass = contextListenerClass;
            return this;
        }

        /**
         * Builds an instance of {@link WebAppDescriptor} from the parameters set
         * using the various methods of the {@link Builder} instance.
         * @return An instance of {@link WebAppDescriptor}
         */
        public WebAppDescriptor build() {
            return new WebAppDescriptor(this);
        }
    }

    private final Map<String, String> initParams;

    private final Map<String, String> contextParams;

    private final Class<? extends HttpServlet> servletClass;

    private final Class<? extends Filter> filterClass;

    private final Class<? extends ServletContextListener> contextListenerClass;
    
    private final String contextPath;

    private final String servletPath;

    /**
     * Creates an instance of {@link WebAppDescriptor} from the passed {@link Builder}
     * instance.
     * @param {@link Builder} instance
     */
    private WebAppDescriptor(Builder b) {
        super(b);

        this.initParams = b.initParams;
        this.servletClass = b.servletClass;
        this.filterClass = b.filterClass;
        this.contextPath = b.contextPath;
        this.servletPath = b.servletPath;
        this.contextParams = b.contextParams;
        this.contextListenerClass = b.contextListenerClass;
    }

    /**
     * Returns the map of application init-params.
     * @return A map with the various init-param names and values as key-value pairs.
     */
    public Map<String, String> getInitParams() {
        return initParams;
    }

    /**
     * Returns the application servlet-class
     * @return The servlet-class
     */
    public Class<? extends HttpServlet> getServletClass() {
        return servletClass;
    }

    /**
     * Returns the application filter-class
     * @return The filter-class
     */
    public Class<? extends Filter> getFilterClass() {
        return filterClass;
    }

    /**
     * Returns the application context-path
     * @return The context-path
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Returns the application servlet-path
     * @return The servlet-path
     */
    public String getServletPath() {
        return servletPath;
    }

    /**
     * Returns the application's configured {@link ServletContextListener} class.
     * @return The ServletContextListerner class.
     */
    public Class<? extends ServletContextListener> getContextListenerClass() {
        return contextListenerClass;
    }

    public Map<String, String> getContextParams() {
        return contextParams;
    }

}