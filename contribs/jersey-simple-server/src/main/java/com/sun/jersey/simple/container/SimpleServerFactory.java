/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.simple.container;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;

import javax.net.ssl.SSLContext;

import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

/**
 * Factory for creating and starting Simple server containers. This returns
 * a handle to the started server as {@link Closeable} instances, which allows
 * the server to be stopped by invoking the {@link Closeable#close} method.
 * <p>
 * To start the server in HTTPS mode an {@link SSLContext} can be provided.
 * This will be used to decrypt and encrypt information sent over the 
 * connected TCP socket channel.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class SimpleServerFactory {
    
    private SimpleServerFactory() {}
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that 
     * in turn manages all root resource and provder classes found by searching the 
     * classes referenced in the java classpath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Class)} method for creating
     * an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(String address) 
          throws IOException, IllegalArgumentException {            
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        return create(URI.create(address));
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that 
     * in turn manages all root resource and provder classes found by searching the 
     * classes referenced in the java classpath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Class)} method for creating
     * an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "https". The URI user information and host
     *        are ignored If the URI port is not present then port 143 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(String address, SSLContext context) 
          throws IOException, IllegalArgumentException {            
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        return create(URI.create(address), context);
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that 
     * in turn manages all root resource and provder classes found by searching 
     * the classes referenced in the java classpath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Class)} method for creating
     * an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(URI address) 
          throws IOException, IllegalArgumentException {            
        return create(address, ContainerFactory.createContainer(Container.class));
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that 
     * in turn manages all root resource and provder classes found by searching 
     * the classes referenced in the java classpath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Class)} method for creating
     * an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "https". The URI user information and host
     *        are ignored If the URI port is not present then port 143 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(URI address, SSLContext context) 
          throws IOException, IllegalArgumentException {            
        return create(address, context, ContainerFactory.createContainer(Container.class));
    }
        
    /**
     * Create a {@link Closeable} that registers an {@link Container} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param config the resource configuration.
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(String address, ResourceConfig config) 
          throws IOException, IllegalArgumentException {
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        return create(URI.create(address), config);
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "https". The URI user information and host
     *        are ignored If the URI port is not present then port 143 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections
     * @return the closeable connection, with the endpoint started
     * @return the connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(String address, SSLContext context, ResourceConfig config) 
          throws IOException, IllegalArgumentException {
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        return create(URI.create(address), context, config);
    }


    /**
     * Create a {@link Closeable} that registers an {@link Container} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param config the resource configuration.
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(URI address, ResourceConfig config)
          throws IOException, IllegalArgumentException {
        return create(address, ContainerFactory.createContainer(Container.class, config));
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "https". The URI user information and host
     *        are ignored If the URI port is not present then port 143 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections
     * @param config the resource configuration.
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(URI address, SSLContext context, ResourceConfig config)
          throws IOException, IllegalArgumentException {
        return create(address, context, ContainerFactory.createContainer(Container.class, config));
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig, IoCComponentProviderFactory)} 
     * method for creating an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param config the resource configuration.
     * @param factory the IoC component provider factory the web application
     *        delegates to for obtaining instances of resource and provider
     *        classes. May be null if the web application is responsible for
     *        instantiating resource and provider classes.
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(String address, ResourceConfig config, 
            IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException {
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        return create(URI.create(address), config, factory);
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig, IoCComponentProviderFactory)} 
     * method for creating an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "https". The URI user information and host
     *        are ignored If the URI port is not present then port 143 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections
     * @param config the resource configuration.
     * @param factory the IoC component provider factory the web application
     *        delegates to for obtaining instances of resource and provider
     *        classes. May be null if the web application is responsible for
     *        instantiating resource and provider classes.
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(String address, SSLContext context, ResourceConfig config, 
            IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException {
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        return create(URI.create(address), context, config, factory);
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig, IoCComponentProviderFactory)} 
     * method for creating an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param config the resource configuration.
     * @param factory the IoC component provider factory the web application
     *        delegates to for obtaining instances of resource and provider
     *        classes. May be null if the web application is responsible for
     *        instantiating resource and provider classes.
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(URI address, ResourceConfig config,
            IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException {
        return create(address, ContainerFactory.createContainer(Container.class, config, factory));
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig, IoCComponentProviderFactory)} method
     * for creating an Container that manages the root resources.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "https". The URI user information and host
     *        are ignored If the URI port is not present then port 143 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections
     * @param config the resource configuration.
     * @param factory the IoC component provider factory the web application
     *        delegates to for obtaining instances of resource and provider
     *        classes. May be null if the web application is responsible for
     *        instantiating resource and provider classes.
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(URI address, SSLContext context, ResourceConfig config,
            IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException {
        return create(address, context, ContainerFactory.createContainer(Container.class, config, factory));
    }

    /**
     * Create a {@link Closeable} that registers an {@link Container} that 
     * in turn manages all root resource and provder classes found by searching the 
     * classes referenced in the java classpath.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param container the container that handles all HTTP requests
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(String address, Container container)
            throws IOException, IllegalArgumentException {
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        return create(URI.create(address), container);
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that 
     * in turn manages all root resource and provder classes found by searching the 
     * classes referenced in the java classpath.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "https". The URI user information and host
     *        are ignored If the URI port is not present then port 143 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections
     * @param container the container that handles all HTTP requests
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(String address, SSLContext context, Container container)
            throws IOException, IllegalArgumentException {
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        return create(URI.create(address), context, container);
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that 
     * in turn manages all root resource and provder classes found by searching the 
     * classes referenced in the java classpath.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param container the container that handles all HTTP requests
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(URI address, Container container) 
            throws IOException, IllegalArgumentException {
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        return create(address, null, container);           
    }
    
    /**
     * Create a {@link Closeable} that registers an {@link Container} that 
     * in turn manages all root resource and provder classes found by searching the 
     * classes referenced in the java classpath.
     *
     * @param address the URI to create the http server. The URI scheme must be
     *        equal to "https". The URI user information and host
     *        are ignored If the URI port is not present then port 143 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param context this is the SSL context used for SSL connections
     * @param container the container that handles all HTTP requests
     * @return the closeable connection, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>address</code> is null
     */
    public static Closeable create(URI address, SSLContext context, Container container) 
            throws IOException, IllegalArgumentException {
        if (address == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }   
        String scheme = address.getScheme();
        int defaultPort = 80;
        
        if(context == null) {
          if (!scheme.equalsIgnoreCase("http")) {
             throw new IllegalArgumentException("The URI scheme should be 'http' when not using SSL");
          } 
        } else {
          if (!scheme.equalsIgnoreCase("https")) {
             throw new IllegalArgumentException("The URI scheme should be 'https' when using SSL");
          }
          defaultPort = 143; // default HTTPS port
        }  
        int port = address.getPort();
        
        if(port == -1) {
           port = defaultPort;
        }         
        SocketAddress listen = new InetSocketAddress(port);
        Connection connection = new SocketConnection(container);
        
        connection.connect(listen, context);
        
        return connection;
    }  
}
