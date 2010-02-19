package com.sun.jersey.test.framework;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * The base application descriptor.
 *
 * @author Paul.Sandoz@Sun.COM
 */
public abstract class AppDescriptor {
    
    /**
     * The base builder for building an application descriptor.
     * <p>
     * If properties of the builder are not modified default values be utilized.
     * The default value for client configuration is an instance of
     * {@link DefaultClientConfig}.
     * <p>
     * After the {@link #build() } has been invoked the state of the builder
     * will be reset to the default values.
     * @param <T> the type of the builder.
     * @param <V> the type of the descriptor
     */
    protected static abstract class AppDescriptorBuilder<T extends AppDescriptorBuilder, V extends AppDescriptor> {
        protected ClientConfig cc;

        /**
         * Set the client configuration.
         *
         * @param cc the client configuration.
         * @return this builder.
         */
        public T clientConfig(ClientConfig cc) {
            if (cc == null)
                throw new IllegalArgumentException("The client configuration must not be null");

            this.cc = cc;
            return (T)this;
        }

        public abstract V build();

        protected void reset() {
           this.cc = null;

        }
    }

    private final ClientConfig cc;

    protected AppDescriptor(AppDescriptorBuilder<?, ?> b) {
        this.cc = (b.cc == null)
                ? new DefaultClientConfig() : b.cc;
    }

    /**
     * Get the client configuration.
     *
     * @return the client configuration.
     */
    public ClientConfig getClientConfig() {
        return cc;
    }

}