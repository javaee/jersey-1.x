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

package com.sun.ws.rest.spi.resource;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.model.AbstractResourceConstructor;

/**
 * Context for resource providers.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface ResourceProviderContext {
    /**
     * Inject dependencies into a newly instantiated resource class instance.
     * Only dependencies defined by JAX-RS will be injected, unknown
     * dependencies will be ignored.
     * 
     * @param resource the  newly instantiated resource class instance
     * @throws ContainerException if an error occurs during dependency injection.
     */
    void injectDependencies(Object resource) throws ContainerException;

    /**
     * Get values for a resource class constructors arguments. The position in
     * the returned array corresponds to the position of the argument in the
     * argument list. 
     * Only arguments whose annotations are defined by JAX-RS will assigned a
     * non-null value, unknown parameters will have a value of null.
     * @param abstractResourceConstructor the resource class constructor that the resource provider
     * wishes to invoke
     * @return an array of values corresponding to the constructor parameters
     */
    Object[] getParameterValues(AbstractResourceConstructor abstractResourceConstructor);
}
