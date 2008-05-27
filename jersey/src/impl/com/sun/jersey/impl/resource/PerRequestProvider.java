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

package com.sun.jersey.impl.resource;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceConstructor;
import com.sun.jersey.spi.resource.InjectableProviderContext;
import com.sun.jersey.spi.resource.ResourceClassInjector;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.resource.ResourceProvider;
import com.sun.jersey.spi.service.ComponentProvider;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.ws.rs.core.Context;

/**
 *
 * @author mh124079
 */
public final class PerRequestProvider implements ResourceProvider {
    @Context InjectableProviderContext ipc;

    private Class<?> c;
    
    private ResourceClassInjector rci;
    
    private Constructor<?> constructor;
    
    private List<Injectable> constructorInjectableParams;
    
    public void init(ComponentProvider provider,
            AbstractResource abstractResource) {
        this.c = abstractResource.getResourceClass();
        
        this.rci = new ResourceClassInjector(ipc, Scope.PerRequest, 
                abstractResource);
                
        // TODO select the most appropriate constructor 
        // instead of just picking up the first one
        if (abstractResource.getConstructors().isEmpty()) {
            this.constructor = null;
            this.constructorInjectableParams = null;
        } else {
            AbstractResourceConstructor abstractConstructor = 
                    abstractResource.getConstructors().get(0);
            
            this.constructor = abstractConstructor.getCtor();
            if (this.constructor.getParameterTypes().length > 0) {
                this.constructorInjectableParams = ipc.getInjectable(
                        abstractConstructor.getParameters());
            } else {
                this.constructor = null;
                this.constructorInjectableParams = null;                
            }
        }
    }

    public Object getInstance(ComponentProvider provider, HttpContext context) {
        try {
            Object o = null;
            if (constructor == null) {
                o = provider.getInstance(Scope.PerRequest, c);
            } else {
                final Object[] params = new Object[constructorInjectableParams.size()];
                int index = 0;
                for (Injectable i : constructorInjectableParams) {
                    params[index++] = (i != null) ? i.getValue(context) : null;
                }
                
                o = provider.getInstance(Scope.PerRequest, 
                    constructor, params);
            }
            rci.inject(context, provider.getInjectableInstance(o));
            return o;
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