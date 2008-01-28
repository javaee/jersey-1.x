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
import com.sun.ws.rest.api.model.AbstractResourceConstructor;
import com.sun.ws.rest.spi.resource.ResourceProvider;
import com.sun.ws.rest.spi.resource.ResourceProviderContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 * @author mh124079
 */
public final class PerRequestProvider  implements ResourceProvider {

    private Class c;
    private AbstractResourceConstructor ctor;
    
    public void init(AbstractResource abstractResource,
            Map<String, Boolean> resourceFeatures,
            Map<String, Object> resourceProperties) {
        this.c = abstractResource.getResourceClass();
        // TODO select the most appropriate constructor 
        // instead of just picking up the first one
        this.ctor = (abstractResource.getConstructors().isEmpty()) 
                ? null : abstractResource.getConstructors().get(0);
    }

    public Object getInstance(ResourceProviderContext context) {
        final Object resource = getResource(context);
        context.injectDependencies(resource);
        return resource;
    }
    
    private Object getResource(ResourceProviderContext context) {
        try {
            return (ctor == null)
                ? c.newInstance()
                : ctor.getCtor().newInstance(context.getParameterValues(ctor));
        } catch (InstantiationException ex) {
            throw new ContainerException("Unable to create resource", ex);
        } catch (IllegalAccessException ex) {
            throw new ContainerException("Unable to create resource", ex);
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            if (t instanceof RuntimeException) {
                // Rethrow the runtime exception
                throw (RuntimeException)t;
            } else {
                // TODO should a checked exception be wrapped in 
                // WebApplicationException ?
                throw new ContainerException("Unable to create resource", t);
            }
        }        
    }
}