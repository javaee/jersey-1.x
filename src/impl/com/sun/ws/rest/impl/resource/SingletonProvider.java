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

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.spi.resource.ResourceProviderContext;
import com.sun.ws.rest.spi.resource.ResourceProvider;
import java.util.Map;

/**
 * A simple provider that maintains a singleton resource class instance
 */
public final class SingletonProvider implements ResourceProvider {

    private Class c;
    
    private Object resource;
    
    public void init(AbstractResource abstractResource,
            Map<String, Boolean> resourceFeatures,
            Map<String, Object> resourceProperties) {
        this.c = abstractResource.getResourceClass();
    }

    public Object getInstance(ResourceProviderContext context) {
        if (resource != null)
            return resource;

        try {
            resource = c.newInstance();
            context.injectDependencies(resource);
            return resource;
        } catch (InstantiationException ex) {
            throw new ContainerException("Unable to create resource", ex);
        } catch (IllegalAccessException ex) {
            throw new ContainerException("Unable to create resource", ex);
        }
    }
}