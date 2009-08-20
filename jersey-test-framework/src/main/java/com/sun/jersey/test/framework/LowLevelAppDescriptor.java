package com.sun.jersey.test.framework;

import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * This class provides the necessary APIs for defining an application, so that
 * it could be deployed and tested on any of the light weight containers like HTTP Server,
 * Grizzly Server, etc.
 * <p> It follows the Builder design pattern.
 * @author Paul.Sandoz@Sun.COM
 */
public class LowLevelAppDescriptor extends AppDescriptor {

    /**
     * The Builder class for building the application descriptor.
     */
    public static class Builder
            extends AppDescriptorBuilder<Builder> {

        protected final ResourceConfig rc;

        protected String contextPath = "";

        /**
         * Build the descriptor from a fully qualified name of a resource package
         * or an array of packages.
         * @param A resource package or an array of packages, if there are more than one.
         */
        public Builder(String... packages) {
            if (packages == null)
                throw new IllegalArgumentException("The packages must not be null");
            
            this.rc = new PackagesResourceConfig(packages);
        }

        /**
         * Build the descriptor from an array of resource classes.
         * @param An array of resource classes
         */
        public Builder(Class... classes) {
            if (classes == null)
                throw new IllegalArgumentException("The classes must not be null");

            this.rc = new ClassNamesResourceConfig(classes);
        }

        /**
         * Build the descriptor from an instance of {@link ResourceConfig}.
         * @param An instance of {@link ResourceConfig}
         */
        public Builder(ResourceConfig rc) {
            if (rc == null)
                throw new IllegalArgumentException("The resource configuration must not be null");
            
            this.rc = rc;
        }

        /**
         * Set the context-path of the application.
         * @param A string which defines tha application context name.
         * @return The application descriptor builder instance
         */
        public Builder contextPath(String contextPath) {
            if (contextPath == null)
                throw new IllegalArgumentException("The context path must not be null");
            
            this.contextPath = contextPath;
            return this;
        }

        /**
         * Builds the application descriptor from the Builder instance.
         * @return An instance of {@link LowLevelAppDescriptor}
         */
        public LowLevelAppDescriptor build() {
            return new LowLevelAppDescriptor(this);
        }
    }

    private final ResourceConfig rc;

    private final String contextPath;

    /**
     * The private constructor.
     * @param A {@link Builder} instance.
     */
    private LowLevelAppDescriptor(Builder b) {
        super(b);

        this.contextPath = b.contextPath;
        this.rc = b.rc;
    }

    /**
     * Returns the {@link ResourceConfig}
     * @return The application's ResourceConfig
     */
    public ResourceConfig getResourceConfig() {
        return rc;
    }

    /**
     * Returns the context path of the application
     * @return The application context path
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Creates an instance of {@link LowLevelAppDescriptor} from {@link WebAppDescriptor}
     * @param An instance of {@link WebAppDescriptor}
     * @return An instance of {@link LowLevelAppDescriptor}
     */
    public static LowLevelAppDescriptor transform(WebAppDescriptor wad) {
        // TODO need to check contraints on wad
        String packages = wad.getInitParams().get(PackagesResourceConfig.PROPERTY_PACKAGES);
        return new LowLevelAppDescriptor.Builder(packages).build();
    }
}
