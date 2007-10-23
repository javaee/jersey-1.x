package com.sun.ws.rest.impl.client;

import java.io.IOException;
import java.net.URI;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ResourceProxyFilter implements ResourceProxyInvoker {
    private ResourceProxyInvoker next;
    
    public final void setNext(ResourceProxyInvoker next) {
        this.next = next;
    }
    
    public final ResourceProxyInvoker getNext() {
        return next;
    }
    
    public abstract ResponseInBound invoke(URI u, String method, RequestOutBound ro) throws IOException;
}
