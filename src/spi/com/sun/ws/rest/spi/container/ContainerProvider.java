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

package com.sun.ws.rest.spi.container;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.ResourceConfig;

/**
 * Service-provider interface for creating container instances.
 * <p>
 * A container instance will be created according to the 
 * the supporting generic type of the container.
 * <p>
 * A provider shall support a one-to-one mapping between a type that is is not of 
 * the type Object. A provider may support 
 * more than one one-to-one mapping or a mapping of sub-types of a type
 * (that is not of the type Object). A provider shall not conflict with other
 * providers.
 * <p>
 * An implementation (a service-provider) identifies itself by placing a 
 * provider-configuration file (if not already present), 
 * "com.sun.ws.rest.spi.container.ContainerProvider" in the 
 * resource directory <tt>META-INF/services</tt>, and including the fully qualified
 * service-provider-class of the implementation in the file.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface ContainerProvider<T> {
    
    /**
     * Create an container of type T.
     * <p>
     * The container provider is responsible for initiating the Web application
     * with the resource configuration.
     * <p>
     * @return the container, otherwise null if the provider does not support
     *         the requested <code>type</code>.
     * @param type the type of the container.
     * @param resourceConfig the resource configuration.
     * @param application the Web application the container delegates to for 
     *         the handling of a HTTP request.
     * @throws ContainerException if there is an error creating the container.
     */
    T createContainer(Class<T> type, ResourceConfig resourceConfig, WebApplication application) 
    throws ContainerException;
}
