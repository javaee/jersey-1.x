package com.sun.jersey.test.framework.spi.container;

import com.sun.jersey.api.client.Client;
import java.net.URI;

/**
 *
 * @author paulsandoz
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
     * @return
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
