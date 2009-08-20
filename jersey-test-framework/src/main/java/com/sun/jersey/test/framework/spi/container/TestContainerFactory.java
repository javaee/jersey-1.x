package com.sun.jersey.test.framework.spi.container;

import com.sun.jersey.test.framework.AppDescriptor;
import java.net.URI;

/**
 * An interface which defines methods for creating instances of {@link TestContainer}.
 * @author Paul.Sandoz@Sun.COM
 */
public interface TestContainerFactory  {
    /**
     *
     * @return
     */
    <T extends AppDescriptor> Class<T> supports();

    /**
     * 
     * @param baseUri
     * @param ad
     * @return
     */
    TestContainer create(URI baseUri, AppDescriptor ad);
    
}