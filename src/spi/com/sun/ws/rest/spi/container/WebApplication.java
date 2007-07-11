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
import com.sun.ws.rest.spi.resolver.WebResourceResolverFactory;

/**
 * A Web application that manages a set of Web resource.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface WebApplication {
    /**
     * Initiate the Web application.
     * @param containerMemento the object representing the container
     * @param resourceConfig the resource configuration containing the set
     *        of Web resources to be managed by the Web application.
     * @param resolverFactory the Web resource resolver factory to be used
     *        for creating Web resource resolvers for the managed set of 
     *        Web resources.
     * @throws IllegalArgumentException if resourceConfig is null.
     */
    void initiate(Object containerMemento,
            ResourceConfig resourceConfig, 
            WebResourceResolverFactory resolverFactory) throws IllegalArgumentException;
    
    /**
     * Handle an HTTP request by dispatching the request to the appropriate
     * matching Web resource that produces the response or otherwise producing 
     * the appropriate HTTP error response.
     * <p>
     * @param request the HTTP container request.
     * @param response the HTTP container response.
     * @throws ContainerException if there is an error that the container 
     * should manage.
     */
    void handleRequest(ContainerRequest request, ContainerResponse response)
    throws ContainerException;
}