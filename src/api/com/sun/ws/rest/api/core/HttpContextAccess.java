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

package com.sun.ws.rest.api.core;

import com.sun.ws.rest.api.container.ContainerException;
import javax.ws.rs.core.Response;

/**
 * A HttpContextAccess makes it possible for a web resource implementation class to 
 * access and manipulate HTTP request and response information directly. Typically
 * a HttpContextAccess is injected into web resource implementation class using the 
 * Resource annotation.
 */
public interface HttpContextAccess {
    /**
     * Get the HTTP request information.
     * @return the HTTP request information
     */
    HttpRequestContext getHttpRequestContext();

    /**
     * Get the HTTP response information.
     * @return the HTTP response information
     */
    HttpResponseContext getHttpResponseContext();
    
    /**
     * Get the current resource.
     */
    Object getCurrentResource();
    
    /**
     * Create a response that will forward the HTTP request to another component
     * within the same container for further processing.
     * @param path the path that identifies the component. The path may be relative or absolute,
     * relative paths are relative to the request URI.
     * @return a response object that, when returned, will cause the container to forward
     * the request to another component.
     * @throws ContainerException if the path cannot be reached by a local forward
     * @throws UnsupportedOperationException if the current container does not support local forwards
     */
    Response createLocalForward(String path) throws ContainerException, UnsupportedOperationException;    
}
