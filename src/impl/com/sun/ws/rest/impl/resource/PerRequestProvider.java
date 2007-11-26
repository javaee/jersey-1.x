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

/*
 * PerRequestProvider.java
 *
 * Created on August 2, 2007, 12:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractResourceConstructor;
import com.sun.ws.rest.spi.resource.ResourceProvider;
import com.sun.ws.rest.spi.resource.ResourceProviderContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 * @author mh124079
 */
public class PerRequestProvider  implements ResourceProvider {

    private AbstractResource abstractResource;
    
    public void init(AbstractResource abstractResource,
            Map<String, Boolean> resourceFeatures,
            Map<String, Object> resourceProperties) {
        this.abstractResource = abstractResource;
    }

    public Object getInstance(ResourceProviderContext context) {
        try {
            Object resource = null;
            // get the public constructors
            // TODO abstract resource keeps just one constructor, is it ok?
            AbstractResourceConstructor arCtor = this.abstractResource.getConstructor();
            if (null == arCtor)
                resource = abstractResource.getResourceClass().newInstance();
            else {
                // take the first constructor
                Object[] params = context.getParameterValues(arCtor);
                resource = arCtor.getCtor().newInstance(params);
            }
            context.injectDependencies(resource);
            return resource;
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
