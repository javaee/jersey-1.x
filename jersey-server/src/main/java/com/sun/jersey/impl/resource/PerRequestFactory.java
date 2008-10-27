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
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCProxiedComponentProvider;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.spi.component.ResourceComponentConstructor;
import com.sun.jersey.server.spi.component.ResourceComponentInjector;
import com.sun.jersey.server.spi.component.ResourceComponentProvider;
import com.sun.jersey.server.spi.component.ResourceComponentProviderFactory;
import com.sun.jersey.core.spi.component.ComponentScope;
import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class PerRequestFactory implements ResourceComponentProviderFactory {
    private final ServerInjectableProviderContext sipc;

    private final HttpContext threadLocalHc;

    public PerRequestFactory(
            @Context ServerInjectableProviderContext sipc,
            @Context HttpContext threadLocalHc) {
        this.sipc = sipc;
        this.threadLocalHc = threadLocalHc;
    }
    
    public ResourceComponentProvider getComponentProvider(Class c) {
        return new PerRequest();
    }
    
    public ResourceComponentProvider getComponentProvider(IoCComponentProvider icp, Class c) {
        if (icp instanceof IoCInstantiatedComponentProvider) {
            return new PerRequestInstantiated((IoCInstantiatedComponentProvider)icp);
        } else if (icp instanceof IoCProxiedComponentProvider) {
            return new PerRequestProxied((IoCProxiedComponentProvider)icp);
        }
        throw new IllegalStateException();
    }

    private class PerRequest implements ResourceComponentProvider {
        private ResourceComponentConstructor rcc;

        private ResourceComponentInjector rci;

        public void init(AbstractResource abstractResource) {
            this.rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.PerRequest,
                    abstractResource);
            this.rci = new ResourceComponentInjector(
                    sipc,
                    ComponentScope.PerRequest,
                    abstractResource);
        }

        public Object getInstance() {
            return getInstance(threadLocalHc);
        }

        public Object getInstance(HttpContext hc) {
            try {
                Object o = rcc.getInstance(hc);
                rci.inject(hc, o);
                return o;
            } catch (InstantiationException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (IllegalAccessException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (InvocationTargetException ex) {
                // Propagate the target exception so it may be mapped to a response
                throw new MappableContainerException(ex.getTargetException());
            } catch (WebApplicationException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new ContainerException("Unable to create resource", ex);
            }
        }
    }

    private class PerRequestInstantiated implements ResourceComponentProvider {
        private final IoCInstantiatedComponentProvider iicp;
        
        private ResourceComponentInjector rci;

        PerRequestInstantiated(IoCInstantiatedComponentProvider iicp) {
            this.iicp = iicp;
        }

        public void init(AbstractResource abstractResource) {
            this.rci = new ResourceComponentInjector(
                    sipc,
                    ComponentScope.PerRequest,
                    abstractResource);
        }

        public Object getInstance() {
            return getInstance(threadLocalHc);
        }

        public Object getInstance(HttpContext hc) {
            Object o = iicp.getInstance();
            rci.inject(hc, iicp.getInjectableInstance(o));
            return o;
        }
    }

    private class PerRequestProxied implements ResourceComponentProvider {
        private final IoCProxiedComponentProvider ipcp;

        private ResourceComponentConstructor rcc;

        private ResourceComponentInjector rci;

        PerRequestProxied(IoCProxiedComponentProvider ipcp) {
            this.ipcp = ipcp;
        }

        public void init(AbstractResource abstractResource) {
            this.rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.PerRequest,
                    abstractResource);
            this.rci = new ResourceComponentInjector(
                    sipc,
                    ComponentScope.PerRequest,
                    abstractResource);
        }

        public Object getInstance() {
            return getInstance(threadLocalHc);
        }

        public Object getInstance(HttpContext hc) {
            try {
                Object o = rcc.getInstance(hc);
                rci.inject(hc, o);
                Object po = ipcp.proxy(o);
                    return po;
            } catch (InstantiationException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (IllegalAccessException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (InvocationTargetException ex) {
                // Propagate the target exception so it may be mapped to a response
                throw new MappableContainerException(ex.getTargetException());
            } catch (WebApplicationException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new ContainerException("Unable to create resource", ex);
            }
        }
    }
}