package com.sun.jersey.test.framework.spi.container;

import com.sun.jersey.api.client.Client;
import java.net.URI;

/**
 * A test container.
 * 
 * @author Paul.Sandoz@Sun.COM
 */
public interface TestContainer {

    /**
     * Get a client specific to the test container.
     *
     * @return a client specific to the test container, otherwise null if there
     *         is no specific client required.
     */
    Client getClient();

    /**
     * Get the base URI of the application.
     * 
     * @return the base URI of the application.
     */
    URI getBaseUri();
    
    /**
     * Start the container.
     *
     */
    void start();

    /**
     * Stop the contaner.
     * 
     */
    void stop();
}
