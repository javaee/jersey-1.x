package com.sun.ws.rest.impl.client;

import java.io.IOException;
import java.net.URI;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface ResourceProxyInvoker {
    ResponseInBound invoke(URI u, String method, RequestOutBound ro) throws IOException;
}
