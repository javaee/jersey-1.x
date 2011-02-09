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

package com.sun.jersey.api.container;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.container.ContainerProvider;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;
import com.sun.jersey.spi.service.ServiceFinder;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Factory for creating specific HTTP-based containers.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ContainerFactory {
    
    private ContainerFactory() {
    }
    
    /**
     * Create a container according to the class requested.
     * <p>
     * The list of service-provider supporting the {@link ContainerProvider}
     * service-provider will be iterated over until one returns a non-null
     * container instance.
     * <p>
     * @param <A> the type of the container.
     * @param type the type of the container.
     * @param resourceClasses the list of Web resources to be managed by the
     *        Web application.
     * @return the container.
     * @throws ContainerException if there is an error creating the container.
     * @throws IllegalArgumentException if no container provider supports the type.
     */
    @SuppressWarnings("unchecked")
    public static <A> A createContainer(Class<A> type, Class<?>... resourceClasses)
    throws ContainerException, IllegalArgumentException {
        Set<Class<?>> resourceClassesSet = new HashSet<Class<?>>(
                Arrays.asList(resourceClasses));
        
        return createContainer(type, new DefaultResourceConfig(resourceClassesSet),
                null);
    }
    
    /**
     * Create a container according to the class requested.
     * <p>
     * The list of service-provider supporting the {@link ContainerProvider}
     * service-provider will be iterated over until one returns a non-null
     * container instance.
     * <p>
     * @param <A> the type of the container.
     * @param type the type of the container.
     * @param resourceClasses the set of Web resources to be managed by the
     *        Web application.
     * @return the container.
     * @throws ContainerException if there is an error creating the container.
     * @throws IllegalArgumentException if no container provider supports the type.
     */
    @SuppressWarnings("unchecked")
    public static <A> A createContainer(Class<A> type, Set<Class<?>> resourceClasses)
    throws ContainerException, IllegalArgumentException {
        return createContainer(type, new DefaultResourceConfig(resourceClasses),
                null);
    }
    
    /**
     * Create a container according to the class requested.
     * <p>
     * The list of service-provider supporting the {@link ContainerProvider}
     * service-provider will be iterated over until one returns a non-null
     * container instance.
     * <p>
     * @param <A> the type of the container.
     * @param type the type of the container.
     * @param resourceConfig the resource configuration containing the set
     *        of Web resources to be managed by the Web application.
     * @return the container.
     * @throws ContainerException if there is an error creating the container.
     * @throws IllegalArgumentException if no container provider supports the type.
     */
    public static <A> A createContainer(Class<A> type, ResourceConfig resourceConfig)
    throws ContainerException, IllegalArgumentException {
        return createContainer(type, resourceConfig, null);
    }

    /**
     * Create a container according to the class requested.
     * <p>
     * The list of service-provider supporting the {@link ContainerProvider}
     * service-provider will be iterated over until one returns a non-null
     * container instance.
     * <p>
     * @param <A> the type of the container.
     * @param type the type of the container.
     * @param resourceConfig the resource configuration containing the set
     *        of Web resources to be managed by the Web application.
     * @param factory the IoC component provider factory the web application
     *        delegates to for obtaining instances of resource and provider
     *        classes. May be null if the web application is responsible for
     *        instantiating resource and provider classes.
     * @return the container.
     * @throws ContainerException if there is an error creating the container.
     * @throws IllegalArgumentException if no container provider supports the type.
     */
    @SuppressWarnings("unchecked")
    public static <A> A createContainer(Class<A> type, ResourceConfig resourceConfig,
            IoCComponentProviderFactory factory)
    throws ContainerException, IllegalArgumentException {
        WebApplication wa = WebApplicationFactory.createWebApplication();
        
        // Reverse the order so that applications may override
        LinkedList<ContainerProvider> cps = new LinkedList<ContainerProvider>();
        for (ContainerProvider cp : ServiceFinder.find(ContainerProvider.class, true))
            cps.addFirst(cp);
        
        for (ContainerProvider<A> cp : cps) {
            A c = cp.createContainer(type, resourceConfig, wa);
            if (c != null) {
                // Initiate the web application
                if (!wa.isInitiated()) {
                    wa.initiate(resourceConfig, factory);
                }

                // Register a container listener
                Object o = resourceConfig.getProperties().get(
                        ResourceConfig.PROPERTY_CONTAINER_NOTIFIER);
                if (o instanceof List) {
                    List list = (List) o;
                    for (Object elem : list) {
                        if (elem instanceof ContainerNotifier &&
                                c instanceof ContainerListener) {
                            ContainerNotifier crf = (ContainerNotifier) elem;
                            crf.addListener((ContainerListener) c);
                        }
                    }
                } else if (o instanceof ContainerNotifier &&
                        c instanceof ContainerListener) {
                    ContainerNotifier crf = (ContainerNotifier) o;
                    crf.addListener((ContainerListener) c);
                }
                return c;
            }
        }
        
        throw new IllegalArgumentException("No container provider supports the type " + type);
    }
    
    /**
     * Create an instance of a container according to the class requested.
     *
     * @param <A> the type of the container.
     * @param type the type of the container.
     * @param packageName the name of the package where to find the resource configuration
     *        class.
     * @return the HTTP handler, if a handler could not be created then null is
     * returned.
     * @throws ContainerException if the resource configuration class could not
     *         be found and instantiated or there is an error creating the container.
     * @throws IllegalArgumentException if no container provider supports the type.
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <A> A createContainer(Class<A> type, String packageName)
    throws ContainerException, IllegalArgumentException {
        String resourcesClassName = packageName + ".WebResources";
        try {
            Class<?> resourcesClass = ContainerFactory.class.getClassLoader().loadClass(resourcesClassName);
            ResourceConfig config = (ResourceConfig) resourcesClass.newInstance();
            return createContainer(type, config, null);
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        } catch (InstantiationException e) {
            throw new ContainerException(e);
        } catch (IllegalAccessException e) {
            throw new ContainerException(e);
        }
    }
    
    /**
     * Create an instance of a container according to the class requested.
     * <p>
     * All java classpath will be scanned for Root Resource Classes.
     * </p>
     * @param <A> the type of the container.
     * @param type the type of the container.
     *
     * @return the HTTP handler, if a handler could not be created then null is
     * returned.
     * @throws IllegalArgumentException if no container provider supports the type.
     */
    public static <A> A createContainer(Class<A> type) {
        String classPath = System.getProperty("java.class.path");
        String[] paths = classPath.split(File.pathSeparator);
        return createContainer(type, paths);
    }
    
    /**
     * Create an instance of a container according to the class requested.
     * <p>
     * Root Resource Classes will be scanned in paths.
     * </p>
     * @param <A> the type of the container.
     * @param type the type of the container.
     * @param paths a list of paths to be scanned for resource classes.
     *
     * @return the HTTP handler, if a handler could not be created then null is
     * returned.
     * @throws IllegalArgumentException if no container provider supports the type.
     */
    public static <A> A createContainer(Class<A> type, String... paths) {
        ClasspathResourceConfig config = new ClasspathResourceConfig(paths);
        return createContainer(type, config, null);
    }
    
}