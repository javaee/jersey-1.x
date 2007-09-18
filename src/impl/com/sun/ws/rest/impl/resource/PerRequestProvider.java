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

    private Class<?> resourceClass;
    
    public void init(Class<?> resourceClass,
            Map<String, Boolean> resourceFeatures,
            Map<String, Object> resourceProperties) {
        this.resourceClass = resourceClass;
    }

    public Object getInstance(ResourceProviderContext context) {
        try {
            Object resource = null;
            // get the public constructors
            Constructor<?> constructors[] = resourceClass.getConstructors();
            if (constructors.length==0)
                resource = resourceClass.newInstance();
            else {
                // take the first constructor
                // TODO be more systematic about choosing a constructor
                Constructor<?> ctor = constructors[0];
                Object[] params = context.getParameterValues(ctor);
                resource=ctor.newInstance(params);
            }
            context.injectDependencies(resource);
            return resource;
        } catch (InstantiationException ex) {
            throw new ContainerException("Unable to create resource", ex);
        } catch (IllegalAccessException ex) {
            throw new ContainerException("Unable to create resource", ex);
        } catch (InvocationTargetException ex) {
            throw new ContainerException("Unable to create resource", ex);
        }
    }
}
