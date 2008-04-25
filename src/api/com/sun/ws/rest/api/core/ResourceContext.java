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

/**
 * The resource context provides access to instances of specified 
 * resource classes (dependencies).
 * It can be injected into resource and provider classes using the {@link Context} 
 * annotation.
 * <br>Created on: Apr 4, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public interface ResourceContext {
    
    /**
     * Provides an instance of the given resource class.
     * @param <T> the type of the resource class
     * @param c the resource class
     * @return an instance if it could be resolved, otherwise null.
     * @throws com.sun.ws.rest.api.container.ContainerException if the resource
     *         class cannot be found.
     * @author Martin Grotzke
     */
    <T> T getResource(Class<T> c) throws ContainerException;   
}