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
import com.sun.ws.rest.spi.resource.Injectable;
import com.sun.ws.rest.spi.service.ComponentProvider;
import java.lang.reflect.Type;

/**
 * A Web application that manages a set of Web resource.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface WebApplication {
    /**
     * Initiate the Web application.
     * <p>
     * This method can only be called once. Further calls will result in an
     * exception.
     * @param resourceConfig the resource configuration containing the set
     *        of Web resources to be managed by the Web application.
     * @throws IllegalArgumentException if resourceConfig is null.
     * @throws ContainerException if a second or further call to the method 
     *         is invoked.
     */
    void initiate(ResourceConfig resourceConfig) 
            throws IllegalArgumentException, ContainerException;
    
    /**
     * Initiate the Web application.
     * <p>
     * This method can only be called once. Further calls will result in an
     * exception.
     * @param resourceConfig the resource configuration containing the set
     *        of Web resources to be managed by the Web application.
     * @param provider the component provider to use, if null the default
     *        component provider will be used.
     * @throws IllegalArgumentException if resourceConfig is null.
     * @throws ContainerException if a second or further call to the method 
     *         is invoked.
     */
    void initiate(ResourceConfig resourceConfig, ComponentProvider provider) 
            throws IllegalArgumentException, ContainerException;
    
    /**
     * Get the message body context that can be used for getting
     * message body readers and writers. 
     * 
     * @return the message body context. The return value is 
     * undefined before the web applicaiton is initialized.
     */
    MessageBodyContext getMessageBodyContext();

    /**
     * Get the component provider that can be used for instantiating
     * components.
     * 
     * @return the component provider. The return value is 
     * undefined before the web applicaiton is initialized.
     */
    ComponentProvider getComponentProvider();
    
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
    
    /**
     * Add an injectable resource to the set maintained by the application.
     * The fieldType is used as a unique key and therefore adding an injectable
     * for a type already supported will override the existing one.
     * @param fieldType the type of the field that will be injected
     * @param injectable the injectable for the field
     */
    void addInjectable(Type fieldType, Injectable injectable);
}