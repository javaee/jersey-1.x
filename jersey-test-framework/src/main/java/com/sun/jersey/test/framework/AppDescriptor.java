package com.sun.jersey.test.framework;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 *
 * @author paulsandoz
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

    public ClientConfig getClientConfig() {
        return cc;
    }

}