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

package com.sun.ws.rest.spi.resolver;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;

/**
 * Resolver of a Web resource given an HTTP request.
 * <p>
 * A resolver can be used to associate state with a Web resource. For example,
 * if a Web resource is associated with the state of the HTTP request
 * an authenticated session.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface WebResourceResolver {
    
    /**
     * Get the Web resource class. Resolving will return instances
     * of the class.
     *
     * @return the Web resource class.
     */
    Class<?> getWebResourceClass();

    /**
     * Resolve a Web resource to in instance.
     * <p>
     * If resolving results in the instantiation of a new Web resource
     * instance then the resolver shall call the 
     * {@link WebResourceResolverListener#onInstantiation} method.
     * 
     * @param request the HTTP request to use to resolve the Web resource.
     * @param listener the resolver listener
     * @return the resolved Web resource
     * @throws ContainerException if there is an error resolving the object instance.
     */
    Object resolve(HttpRequestContext request, 
            WebResourceResolverListener listener) throws ContainerException;
}
