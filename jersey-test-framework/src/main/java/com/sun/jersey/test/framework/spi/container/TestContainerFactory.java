package com.sun.jersey.test.framework.spi.container;

import com.sun.jersey.test.framework.AppDescriptor;
import java.net.URI;

/**
 *
 * @author paulsandoz
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
