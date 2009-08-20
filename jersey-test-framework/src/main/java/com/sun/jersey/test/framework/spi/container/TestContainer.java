package com.sun.jersey.test.framework.spi.container;

import com.sun.jersey.api.client.Client;
import java.net.URI;

/**
 * An interface which defines the methods for starting and stopping a test container.
 * @author Paul.Sandoz@Sun.COM
 */
public interface TestContainer {

    /**
     * Obtain a Client specific to the test container.
     *
     * @return a client specific to the test container, otherwise null if there
     *         is no specific client required.
     */
    Client getClient();

    /**
     * 
     * @return The application BASE URI
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
