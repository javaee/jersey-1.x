package com.sun.jersey.test.framework;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * An abstract class which allows the definition of {@link ClientConfig}.
 *
 * @author Paul.Sandoz@Sun.COM
 */
public abstract class AppDescriptor {
    
    protected static abstract class AppDescriptorBuilder<T extends AppDescriptorBuilder> {
        protected ClientConfig cc = new DefaultClientConfig();

        public T clientConfig(ClientConfig cc) {
            if (cc == null)
                throw new IllegalArgumentException("The client configuration must not be null");

            this.cc = cc;
            return (T)this;
        }
    }

    private final ClientConfig cc;

    protected AppDescriptor(AppDescriptorBuilder<?> b) {
        this.cc = b.cc;
    }

    /**
     * Returns an instance of {@link ClientConfig}
     * @return An instance of {@link ClientConfig}
     */
    public ClientConfig getClientConfig() {
        return cc;
    }

}