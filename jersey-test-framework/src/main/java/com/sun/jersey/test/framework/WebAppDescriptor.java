package com.sun.jersey.test.framework;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author paulsandoz
 */
public class WebAppDescriptor extends AppDescriptor {

    public static class Builder
            extends AppDescriptorBuilder<Builder> {

        protected final Map<String, String> initParams;

        protected final Map<String, String> contextParams;

        protected Class<? extends HttpServlet> servletClass = ServletContainer.class;

        protected Class<? extends Filter> filterClass;

        protected Class<? extends ServletContextListener> contextListenerClass;

        protected String contextPath = "";

        protected String servletPath = "";

        public Builder(Map<String, String> initParams) {
            if (initParams == null)
                throw new IllegalArgumentException("The initialization parameters must not be null");
            
            this.initParams = initParams;
            this.contextParams = new HashMap<String, String>();
        }

        public Builder(String name, String value) {
            this.initParams = new HashMap<String, String>();
            this.contextParams = new HashMap<String, String>();

            initParam(name, value);
        }

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

        public Builder initParam(String name, String value) {
            this.initParams.put(name, value);

            return this;
        }

        public Builder contextParam(String name, String value) {
            this.contextParams.put(name, value);

            return this;
        }

        public Builder servletClass(Class<? extends HttpServlet> servletClass) {
            if (servletClass == null)
                throw new IllegalArgumentException("The servlet class must not be null");

            this.servletClass = servletClass;
            this.filterClass = null;
            return this;
        }

        public Builder filterClass(Class<? extends Filter> filterClass) {
            if (filterClass == null)
                throw new IllegalArgumentException("The filter class must not be null");

            this.filterClass = filterClass;
            this.servletClass = null;
            return this;
        }

        public Builder contextPath(String contextPath) {
            if (contextPath == null)
                throw new IllegalArgumentException("The context path must not be null");

            this.contextPath = contextPath;
            return this;
        }

        public Builder servletPath(String servletPath) {
            if (servletPath == null)
                throw new IllegalArgumentException("The servlet path must not be null");

            this.servletPath = servletPath;
            return this;
        }

        public Builder contextListenerClass(Class<? extends ServletContextListener> contextListenerClass) {
            if (contextListenerClass == null)
                throw new IllegalArgumentException("The servlet context listener class must not be null");

            this.contextListenerClass = contextListenerClass;
            return this;
        }

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

    public Map<String, String> getInitParams() {
        return initParams;
    }

    public Class<? extends HttpServlet> getServletClass() {
        return servletClass;
    }

    public Class<? extends Filter> getFilterClass() {
        return filterClass;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getServletPath() {
        return servletPath;
    }

    public Class<? extends ServletContextListener> getContextListenerClass() {
        return contextListenerClass;
    }

    public Map<String, String> getContextParams() {
        return contextParams;
    }

}
