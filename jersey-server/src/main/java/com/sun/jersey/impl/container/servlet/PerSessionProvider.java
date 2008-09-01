/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.container.servlet;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.resource.InjectableProviderContext;
import com.sun.jersey.spi.resource.ResourceClassInjector;
import com.sun.jersey.spi.resource.ResourceConstructor;
import com.sun.jersey.spi.resource.ResourceProvider;
import com.sun.jersey.spi.service.ComponentConstructor.ConstructorInjectablePair;
import com.sun.jersey.spi.service.ComponentProvider;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

/**
 * A provider that maintains a per session resource class instance
 */
public final class PerSessionProvider implements ResourceProvider {
    @Context InjectableProviderContext ipc;

    private Class<?> c;
    
    private ResourceClassInjector rci;
    
    private Constructor<?> constructor;
    
    private List<Injectable> constructorInjectableParams;

    
    public void init(ComponentProvider provider,
            ComponentProvider resourceProvider, AbstractResource abstractResource) {
        this.c = abstractResource.getResourceClass();
        
        this.rci = new ResourceClassInjector(ipc, Scope.PerRequest, 
                abstractResource);
                
        ResourceConstructor rc = new ResourceConstructor(ipc);
        ConstructorInjectablePair<?> cip = rc.getConstructor(c, abstractResource, 
                Scope.Singleton);
        if (cip == null || cip.is.size() == 0) {
            this.constructor = null;
            this.constructorInjectableParams = null;
        } else {
            this.constructor = cip.con;
            this.constructorInjectableParams = cip.is;
        }
    }

    @Context HttpServletRequest hsr;
    
    public synchronized Object getInstance(ComponentProvider provider, HttpContext context) {
        Object o = hsr.getSession().getAttribute(c.getName());
        if (o != null) return o;

        try {
            if (constructor == null) {
                o = provider.getInstance(Scope.Singleton, c);
            } else {
                final Object[] params = new Object[constructorInjectableParams.size()];
                int index = 0;
                for (Injectable i : constructorInjectableParams) {
                    params[index++] = (i != null) ? i.getValue(context) : null;
                }

                o = provider.getInstance(Scope.Singleton,
                    constructor, params);
            }
            rci.inject(context, provider.getInjectableInstance(o));
            hsr.getSession().setAttribute(c.getName(), o);
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
