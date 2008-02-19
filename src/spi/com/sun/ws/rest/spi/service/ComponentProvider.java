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

package com.sun.ws.rest.spi.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A provider for the instantiation and management of components.
 * <p>
 * The runtime will defer to a registered component provider (if present)
 * for every component (application-defined or infrastructure-defined) that
 * needs to be instantiated. If the component provider does
 * not support the requested component it should return a null value and the
 * runtime will attempt to directly instantiate and manage the component.
 * <p>
 * A {@link ComponentProvider} instance may be registed by passing the 
 * instance to the 
 * {@link com.sun.ws.rest.spi.container.WebApplication#initiate(com.sun.ws.rest.api.core.ResourceConfig, ComponentProvider)}
 * method.
 * <p>
 * Applications may extend the {@link com.sun.ws.rest.spi.container.servlet.ServletContainer}
 * and override the method {@link com.sun.ws.rest.spi.container.servlet.ServletContainer#initiate(ResourceConfig, WebApplication)}
 * to initiate the {@link com.sun.ws.rest.spi.container.WebApplication} with the {@link ComponentProvider} instance.
 * 
 * @see com.sun.ws.rest.spi.container.WebApplication
 * @see com.sun.ws.rest.spi.container.servlet.ServletContainer
 * @author Paul.Sandoz@Sun.Com
 */
public interface ComponentProvider {
    /**
     * The scope contract for the instantiation of a component.
     */
    public enum Scope {
        /**
         * Declares that only one instance of a component shall exist 
         * per-web application instance. The runtime will manage the component
         * in the scope of the web application.
         */
        WebApplication,
        
        /**
         * Declares that the scope is application defined and instances will 
         * be managed by the runtime according to this scope. This requires 
         * that a new instance be created for each invocation of 
         * <code>getInstance</code>.
         */
        ApplicationDefined,
    }
    
    /**
     * Get the instance of a class. Injection will be performed on the
     * instance.
     * 
     * @param scope the scope of the instance
     * @param c the class
     * @return the instance, or null if the component cannot be instantaited
     *         and managed.
     * 
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    Object getInstance(Scope scope, Class c) 
            throws InstantiationException, IllegalAccessException;
    
    /**
     * Get the instance of a class using a constructor and a corresponding
     * array of parameter values. Injection will be performed on the instance.
     * <p>
     * The array of parameter values must be the same length as that required
     * by the constructor. Some parameter values may be null, indicating that
     * the values are not set and must be set by the component provider before
     * construction occurs.
     * 
     * @param scope the scope of the instance
     * @param contructor the constructor to instantiate the class
     * @param parameters the array parameter values passed to the constructor
     * @return the instance, or null if the component cannot be instantaited
     *         and managed.
     * 
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalArgumentException 
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    Object getInstance(Scope scope, Constructor contructor, Object[] parameters) 
            throws InstantiationException, IllegalArgumentException, 
            IllegalAccessException, InvocationTargetException;

    /**
     * Perform injection on an instance. This may be used when a
     * component is instantiated by means other than the component
     * provider.
     * 
     * @param instance the instance to perform injection on.
     */
    void inject(Object instance);
}
