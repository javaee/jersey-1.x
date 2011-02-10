/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.api.container.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

/**
 * Factory for creating {@link HttpServer} instances.
 * <p>
 * The {@link HttpServer} executor will be configued with instance returned from
 * {@link Executors#newCachedThreadPool() }. This behaviour may be overridden
 * before {@link HttpServer#start() } is called.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpServerFactory {

    private HttpServerFactory() {}
    
    /**
     * Create a {@link HttpServer} that registers a HttpHandler that in turn
     * manages all root resource classes found by searching the classes
     * referenced in the java classpath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Class)} method for creating
     * an HttpHandler that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path must not be null or an empty string, and must 
     *        not absolute (start with a '/' character). The URI path is used 
     *        as the context of the HTTP handler (and corresponds to the base 
     *        path). The URI query and fragment components are ignored.
     * @return the http server
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static HttpServer create(String u) 
            throws IOException, IllegalArgumentException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u));
    }
    
    /**
     * Create a {@link HttpServer} that registers a HttpHandler that in turn
     * manages all root resource classes found by searching the classes
     * referenced in the java classath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Class)} method for creating
     * an HttpHandler that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path must not be null or an empty string, and must 
     *        not absolute (start with a '/' character). The URI path is used 
     *        as the context of the HTTP handler (and corresponds to the base 
     *        path). The URI query and fragment components are ignored.
     * @return the http server
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static HttpServer create(URI u) 
            throws IOException, IllegalArgumentException {
        return create(u, ContainerFactory.createContainer(HttpHandler.class));
    }

    /**
     * Create a {@link HttpServer} that registers a HttpHandler that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an HttpHandler that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path must not be null or an empty string, and must
     *        not absolute (start with a '/' character). The URI path is used
     *        as the context of the HTTP handler (and corresponds to the base
     *        path). The URI query and fragment components are ignored.
     * @param rc the resource configuration.
     * @return the http server
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static HttpServer create(String u, ResourceConfig rc)
            throws IOException, IllegalArgumentException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u), rc);
    }

    /**
     * Create a {@link HttpServer} that registers a HttpHandler that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an HttpHandler that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path must not be null or an empty string, and must
     *        not absolute (start with a '/' character). The URI path is used
     *        as the context of the HTTP handler (and corresponds to the base
     *        path). The URI query and fragment components are ignored.
     * @param rc the resource configuration.
     * @return the http server
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static HttpServer create(URI u, ResourceConfig rc)
            throws IOException, IllegalArgumentException {
        return create(u, ContainerFactory.createContainer(HttpHandler.class, rc));
    }

    /**
     * Create a {@link HttpServer} that registers a HttpHandler that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an HttpHandler that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path must not be null or an empty string, and must
     *        not absolute (start with a '/' character). The URI path is used
     *        as the context of the HTTP handler (and corresponds to the base
     *        path). The URI query and fragment components are ignored.
     * @param rc the resource configuration.
     * @param factory the IoC component provider factory the web application
     *        delegates to for obtaining instances of resource and provider
     *        classes. May be null if the web application is responsible for
     *        instantiating resource and provider classes.
     * @return the http server
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static HttpServer create(String u, ResourceConfig rc,
            IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u), rc, factory);
    }

    /**
     * Create a {@link HttpServer} that registers a HttpHandler that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an HttpHandler that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path must not be null or an empty string, and must
     *        not absolute (start with a '/' character). The URI path is used
     *        as the context of the HTTP handler (and corresponds to the base
     *        path). The URI query and fragment components are ignored.
     * @param rc the resource configuration.
     * @param factory the IoC component provider factory the web application
     *        delegates to for obtaining instances of resource and provider
     *        classes. May be null if the web application is responsible for
     *        instantiating resource and provider classes.
     * @return the http server
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static HttpServer create(URI u, ResourceConfig rc,
            IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException {
        return create(u, ContainerFactory.createContainer(HttpHandler.class, rc, factory));
    }

    /**
     * Create a {@link HttpServer} that registers a HttpHandler that in turn
     * manages all root resource classes found by searching the classes
     * referenced in the java classath.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path must not be null or an empty string, and must 
     *        not absolute (start with a '/' character). The URI path is used 
     *        as the context of the HTTP handler (and corresponds to the base 
     *        path). The URI query and fragment components are ignored.
     * @param handler the HTTP handler
     * @return the http server
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static HttpServer create(String u, HttpHandler handler) 
            throws IOException, IllegalArgumentException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u), handler);
    }
    
    /**
     * Create a {@link HttpServer} that registers a HttpHandler that in turn
     * manages all root resource classes found by searching the classes
     * referenced in the java classath.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http" or "https". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path must not be null or an empty string, and must 
     *        not absolute (start with a '/' character). The URI path is used 
     *        as the context of the HTTP handler (and corresponds to the base 
     *        path). The URI query and fragment components are ignored.
     * @param handler the HTTP handler
     * @return the http server
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static HttpServer create(URI u, HttpHandler handler) 
            throws IOException, IllegalArgumentException {
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
        
        final int port = (u.getPort() == -1) ? 80 : u.getPort();    
        final HttpServer server = (scheme.equalsIgnoreCase("http")) ? 
            HttpServer.create(new InetSocketAddress(port), 0) :
            HttpsServer.create(new InetSocketAddress(port), 0);

        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext(path, handler);        
        return server;
    }
}