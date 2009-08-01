package com.sun.jersey.test.framework;

import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 *
 * @author paulsandoz
 */
public class LowLevelAppDescriptor extends AppDescriptor {
    
    public static class Builder
            extends AppDescriptorBuilder<Builder> {

        protected final ResourceConfig rc;

        protected String contextPath = "";

        public Builder(String... packages) {
            if (packages == null)
                throw new IllegalArgumentException("The packages must not be null");
            
            this.rc = new PackagesResourceConfig(packages);
        }

        public Builder(Class... classes) {
            if (classes == null)
                throw new IllegalArgumentException("The classes must not be null");

            this.rc = new ClassNamesResourceConfig(classes);
        }

        public Builder(ResourceConfig rc) {
            if (rc == null)
                throw new IllegalArgumentException("The resource configuration must not be null");
            
            this.rc = rc;
        }

        public Builder contextPath(String contextPath) {
            if (contextPath == null)
                throw new IllegalArgumentException("The context path must not be null");
            
            this.contextPath = contextPath;
            return this;
        }

        public LowLevelAppDescriptor build() {
            return new LowLevelAppDescriptor(this);
        }
    }

    private final ResourceConfig rc;

    private final String contextPath;

    private LowLevelAppDescriptor(Builder b) {
        super(b);

        this.contextPath = b.contextPath;
        this.rc = b.rc;
    }

    public ResourceConfig getResourceConfig() {
        return rc;
    }

    public String getContextPath() {
        return contextPath;
    }


    public static LowLevelAppDescriptor transform(WebAppDescriptor wad) {
        // TODO need to check contraints on wad
        String packages = wad.getInitParams().get(PackagesResourceConfig.PROPERTY_PACKAGES);
        return new LowLevelAppDescriptor.Builder(packages).build();
    }
}
