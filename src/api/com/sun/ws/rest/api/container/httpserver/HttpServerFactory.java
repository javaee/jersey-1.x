/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.api.container.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.ws.rest.api.container.ContainerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Factory for creating {@link HttpServer} instances.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpServerFactory {

    private HttpServerFactory() {}
    
    /**
     * Create a {@link HtppServer} that registers a HttpHandler that in turn
     * manages all root resource classes found by searching the classes
     * referenced in the java classpath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(HttpHandler)} method for creating
     * an HttpHandler that manages the root resources.
     *
     * @param the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path must not be null or an empty string, and must 
     *        not absolute (start with a '/' character). The URI path is used 
     *        as the context of the HTTP handler (and corresponds to the base 
     *        path). The URI query and fragment components are ignored.
     * @return the http server
     */
    public static HttpServer create(String u) throws IOException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u));
    }
    
    /**
     * Create a {@link HtppServer} that registers a HttpHandler that in turn
     * manages all root resource classes found by searching the classes
     * referenced in the java classath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(HttpHandler)} method for creating
     * an HttpHandler that manages the root resources.
     *
     * @param the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path must not be null or an empty string, and must 
     *        not absolute (start with a '/' character). The URI path is used 
     *        as the context of the HTTP handler (and corresponds to the base 
     *        path). The URI query and fragment components are ignored.
     * @return the http server
     */
    public static HttpServer create(URI u) throws IOException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");
            
        final String scheme = u.getScheme();
        if (!scheme.equalsIgnoreCase("http") &&  !scheme.equalsIgnoreCase("https"))
            throw new IllegalArgumentException("The URI scheme, of the URI " + u + 
                    ", must be equal (ignoring case) to 'http' or 'https'");            
        
        final String path = u.getPath();
        if (path == null)
            throw new IllegalArgumentException("The URI path, of the URI " + u + 
                    ", must be non-null");
        else if (path.length() == 0)
            throw new IllegalArgumentException("The URI path, of the URI " + u + 
                    ", must be present");
        else if (path.charAt(0) != '/')
            throw new IllegalArgumentException("The URI path, of the URI " + u + 
                    ". must start with a '/'");
        
        final HttpHandler handler = ContainerFactory.createContainer(
                HttpHandler.class);

        final int port = (u.getPort() == -1) ? 80 : u.getPort();    
        final HttpServer server = (scheme.equalsIgnoreCase("http")) ? 
            HttpServer.create(new InetSocketAddress(port), 0) :
            HttpsServer.create(new InetSocketAddress(port), 0);
        
        server.createContext(path, handler);        
        return server;
    }
}
