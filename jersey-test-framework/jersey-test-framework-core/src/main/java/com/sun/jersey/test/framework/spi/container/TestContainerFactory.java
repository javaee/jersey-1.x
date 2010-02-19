package com.sun.jersey.test.framework.spi.container;

import com.sun.jersey.test.framework.AppDescriptor;
import java.net.URI;

/**
 * A test container factory responsible for creating test containers.
 * 
 * @author Paul.Sandoz@Sun.COM
 */
public interface TestContainerFactory  {
    /**
     * Get the application descriptor class supported by this test container
     * factory.
     *
     * @param <T> the type of application descriptor.
     * @return the application descriptor class supported by this test container
     * factory.
     */
    <T extends AppDescriptor> Class<T> supports();

    /**
     * Create a test container.
     *
     * @param baseUri the base URI of the application.
     * 
     * @param ad the application descriptor.
     * @return the test container.
     * @throws IllegalArgumentException if <code>ad</code> is not an
     *         appropriate instance of an application descriptor supported
     *         by this test container factory.
     */
    TestContainer create(URI baseUri, AppDescriptor ad) throws IllegalArgumentException;
    
}