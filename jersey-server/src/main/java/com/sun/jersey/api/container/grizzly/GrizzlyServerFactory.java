/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.jersey.api.container.grizzly;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.tcp.Adapter;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import java.io.IOException;
import java.net.URI;

/**
 * Factory for creating and starting Grizzly {@link SelectorThread} instances.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class GrizzlyServerFactory {
    
    private GrizzlyServerFactory() {}
    
    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that 
     * in turn manages all root resource and provder classes found by searching the classes
     * referenced in the java classath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Class)} method for creating
     * an Adapter that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @return the select thread, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static SelectorThread create(String u)
            throws IOException, IllegalArgumentException {            
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u));
    }
    
    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that 
     * in turn manages all root resource and provder classes found by searching 
     * the classes referenced in the java classath.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Class)} method for creating
     * an Adapter that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @return the select thread, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static SelectorThread create(URI u)
            throws IOException, IllegalArgumentException {            
        return create(u, ContainerFactory.createContainer(Adapter.class));
    }
        
    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an Adapter that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param rc the resource configuration.
     * @return the select thread, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static SelectorThread create(String u, ResourceConfig rc)
            throws IOException, IllegalArgumentException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u), rc);
    }

    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method
     * for creating an Adapter that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param rc the resource configuration.
     * @return the select thread, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static SelectorThread create(URI u, ResourceConfig rc)
            throws IOException, IllegalArgumentException {
        return create(u, ContainerFactory.createContainer(Adapter.class, rc));
    }

    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig, IoCComponentProviderFactory)} method
     * for creating an Adapter that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param rc the resource configuration.
     * @param factory the IoC component provider factory the web application
     *        delegates to for obtaining instances of resource and provider
     *        classes. May be null if the web application is responsible for
     *        instantiating resource and provider classes.
     * @return the select thread, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static SelectorThread create(String u, ResourceConfig rc,
            IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u), rc, factory);
    }

    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that
     * in turn manages all root resource and provder classes declared by the
     * resource configuration.
     * <p>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig, IoCComponentProviderFactory)} method
     * for creating an Adapter that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be
     *        used. The URI path, query and fragment components are ignored.
     * @param rc the resource configuration.
     * @param factory the IoC component provider factory the web application
     *        delegates to for obtaining instances of resource and provider
     *        classes. May be null if the web application is responsible for
     *        instantiating resource and provider classes.
     * @return the select thread, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static SelectorThread create(URI u, ResourceConfig rc,
            IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException {
        return create(u, ContainerFactory.createContainer(Adapter.class, rc, factory));
    }

    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that 
     * in turn manages all root resource and provder classes found by searching the classes
     * referenced in the java classath.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param adapter the Adapter
     * @return the select thread, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static SelectorThread create(String u, Adapter adapter)
            throws IOException, IllegalArgumentException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u), adapter);
    }
    
    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that 
     * in turn manages all root resource and provder classes found by searching the classes
     * referenced in the java classath.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param adapter the Adapter
     * @return the select thread, with the endpoint started
     * @throws IOException if an error occurs creating the container.
     * @throws IllegalArgumentException if <code>u</code> is null
     */
    public static SelectorThread create(URI u, Adapter adapter) 
            throws IOException, IllegalArgumentException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");
            
        // TODO support https
        final String scheme = u.getScheme();
        if (!scheme.equalsIgnoreCase("http"))
            throw new IllegalArgumentException("The URI scheme, of the URI " + u + 
                    ", must be equal (ignoring case) to 'http'");            
        
        final SelectorThread selectorThread = new SelectorThread();

        selectorThread.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
        
        final int port = (u.getPort() == -1) ? 80 : u.getPort();            
        selectorThread.setPort(port);

        selectorThread.setAdapter(adapter);
        
        try {
            selectorThread.listen();
        } catch (InstantiationException e) {
            IOException _e = new IOException();
            _e.initCause(e);
            throw _e;
        }
        return selectorThread;
    }    
}