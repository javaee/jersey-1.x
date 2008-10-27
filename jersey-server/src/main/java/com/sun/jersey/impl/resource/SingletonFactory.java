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

package com.sun.jersey.impl.resource;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCProxiedComponentProvider;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.spi.component.ResourceComponentConstructor;
import com.sun.jersey.server.spi.component.ResourceComponentInjector;
import com.sun.jersey.server.spi.component.ResourceComponentProvider;
import com.sun.jersey.server.spi.component.ResourceComponentProviderFactory;
import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.Context;

/**
 * A simple provider that maintains a singleton resource class instance
 */
public final class SingletonFactory implements ResourceComponentProviderFactory  {
    private final ServerInjectableProviderContext sipc;

    private final HttpContext threadLocalHc;

    public SingletonFactory(
            @Context ServerInjectableProviderContext sipc,
            @Context HttpContext threadLocalHc) {
        this.sipc = sipc;
        this.threadLocalHc = threadLocalHc;
    }

    public ResourceComponentProvider getComponentProvider(Class c) {
        return new Singleton();
    }
    
    public ResourceComponentProvider getComponentProvider(IoCComponentProvider icp, Class c) {
        if (icp instanceof IoCInstantiatedComponentProvider) {
            return new SingletonInstantiated((IoCInstantiatedComponentProvider)icp);
        } else if (icp instanceof IoCProxiedComponentProvider) {
            return new SingletonProxied((IoCProxiedComponentProvider)icp);
        }
        throw new IllegalStateException();
    }

    private class Singleton implements ResourceComponentProvider {
        private Object resource;

        public void init(AbstractResource abstractResource) {
            ResourceComponentConstructor rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.Singleton,
                    abstractResource);
            ResourceComponentInjector rci = new ResourceComponentInjector(
                    sipc,
                    ComponentScope.Singleton,
                    abstractResource);

            try {
                this.resource = rcc.getInstance(null);
                rci.inject(null, resource);
            } catch (InvocationTargetException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (InstantiationException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (IllegalAccessException ex) {
                throw new ContainerException("Unable to create resource", ex);
            }
        }

        public Object getInstance(HttpContext hc) {
            return resource;
        }

        public Object getInstance() {
            return resource;
        }
    }

    private class SingletonInstantiated implements ResourceComponentProvider {
        private final IoCInstantiatedComponentProvider iicp;
        
        private Object resource;

        SingletonInstantiated(IoCInstantiatedComponentProvider iicp) {
            this.iicp = iicp;
        }

        public void init(AbstractResource abstractResource) {
            ResourceComponentInjector rci = new ResourceComponentInjector(
                    sipc,
                    ComponentScope.Singleton,
                    abstractResource);

            resource = iicp.getInstance();
            rci.inject(null, iicp.getInjectableInstance(resource));
        }

        public Object getInstance(HttpContext hc) {
            return resource;
        }

        public Object getInstance() {
            return resource;
        }
    }

    private class SingletonProxied implements ResourceComponentProvider {
        private final IoCProxiedComponentProvider ipcp;

        private Object resource;

        SingletonProxied(IoCProxiedComponentProvider ipcp) {
            this.ipcp = ipcp;
        }

        public void init(AbstractResource abstractResource) {
            ResourceComponentConstructor rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.Singleton,
                    abstractResource);
            ResourceComponentInjector rci = new ResourceComponentInjector(
                    sipc,
                    ComponentScope.Singleton,
                    abstractResource);

            try {
                Object o = rcc.getInstance(null);
                rci.inject(null, o);
                resource = ipcp.proxy(o);
            } catch (InvocationTargetException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (InstantiationException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (IllegalAccessException ex) {
                throw new ContainerException("Unable to create resource", ex);
            }
        }

        public Object getInstance(HttpContext hc) {
            return resource;
        }

        public Object getInstance() {
            return resource;
        }
    }
}