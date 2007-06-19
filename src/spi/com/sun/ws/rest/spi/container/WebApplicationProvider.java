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

/**
 * Service-provider interface for creating Web application instances.
 * <p>
 * An implementation (a service-provider) identifies itself by placing a 
 * provider-configuration file (if not already present), 
 * "com.sun.research.ws.rest.spi.container.WebApplicationProvider" in the 
 * resource directory <tt>META-INF/services</tt>, and including the fully qualified
 * service-provider-class of the implementation in the file.
 * <p>
 * Only the first registered provider in the provider-configuration file will
 * be used any subsequent providers (if present) will be ignored.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface WebApplicationProvider {

    /**
     * Instantiate a new {@link WebApplication}.
     * 
     * @return the Web application.
     * @throws ContainerException if there is an error creating the Web application.
     */
    public abstract WebApplication createWebApplication()
        throws ContainerException;    
}
