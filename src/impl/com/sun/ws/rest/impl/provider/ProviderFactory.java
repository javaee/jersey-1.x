/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * ProviderFactory.java
 *
 * Created on May 16, 2007, 12:01 PM
 *
 */

package com.sun.ws.rest.impl.provider;
 
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import sun.misc.Service;

/**
 * Factory for creating instances of provider classes.
 */
public abstract class ProviderFactory {
    
    private static AtomicReference<ProviderFactory> pfr = new AtomicReference<ProviderFactory>();
    
    /** 
     * Get an instance of ProviderFactory. The implementation of
     * ProviderFactory that will be instantiated is determined using the 
     * Services API (as detailed in the JAR specification) to determine
     * the classname. The Services API will look for a classname in the file
     * META-INF/services/javax.ws.rs.ext.ProviderFactory in jars available
     * to the runtime.
     */
    public static ProviderFactory getInstance() {
        ProviderFactory pf = pfr.get();
        if (pf != null)
            return pf;
        synchronized(pfr) {
            pf = pfr.get();
            if (pf != null)
                return pf;
            Iterator ps = Service.providers(ProviderFactory.class); 
            while (ps.hasNext()) { 
                pf = (ProviderFactory)ps.next();
                pfr.set(pf);
                break;
            }
        }
        return pf;
    }
    
    /**
     * Create a new instance of a MessageBodyReader for the specified class.
     * @param type the type of value class used to represent the message body
     * @param mediaType the media type to be read
     * @return a new provider instance
     */
    public abstract <T> MessageBodyReader<T> createMessageBodyReader(
            Class<T> type, MediaType mediaType);

    /**
     * Create a new instance of a MessageBodyWriter for the specified class.
     * @param type the type of value class used to represent the message body
     * @param mediaType the media type to be written
     * @return a new provider instance
     */
    public abstract <T> MessageBodyWriter<T> createMessageBodyWriter(
            Class<T> type, MediaType mediaType);
}
