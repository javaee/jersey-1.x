/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.server.impl.resource;

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
import com.sun.jersey.core.spi.component.ioc.IoCDestroyable;
import com.sun.jersey.server.spi.component.ResourceComponentDestructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class PerRequestFactory implements ResourceComponentProviderFactory {
    private static final Logger LOGGER = Logger.getLogger(PerRequestFactory.class.getName());

    private final ServerInjectableProviderContext sipc;

    private final HttpContext threadLocalHc;

    public static void destroy(HttpContext hc) {
        Map<AbstractPerRequest, Object> m = (Map<AbstractPerRequest, Object>)hc.getProperties().
                get(SCOPE_PER_REQUEST);
        if (m != null) {
            for (Map.Entry<AbstractPerRequest, Object> e : m.entrySet()) {
                try {
                    e.getKey().destroy(e.getValue());
                } catch (ContainerException ex) {
                    LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
                }
            }
        }
    }

    public PerRequestFactory(
            @Context ServerInjectableProviderContext sipc,
            @Context HttpContext threadLocalHc) {
        this.sipc = sipc;
        this.threadLocalHc = threadLocalHc;
    }
    
    public ComponentScope getScope(Class c) {
        return ComponentScope.PerRequest;
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

    private static final String SCOPE_PER_REQUEST = "com.sun.jersey.scope.PerRequest";

    private abstract class AbstractPerRequest implements ResourceComponentProvider {

        private ResourceComponentDestructor rcd;
        
        public final Object getInstance() {
            return getInstance(threadLocalHc);
        }

        public final ComponentScope getScope() {
            return ComponentScope.PerRequest;
        }

        public void init(AbstractResource abstractResource) {
            rcd = new ResourceComponentDestructor(abstractResource);
        }

        public final Object getInstance(HttpContext hc) {
            Map<AbstractPerRequest, Object> m = (Map<AbstractPerRequest, Object>)hc.getProperties().
                    get(SCOPE_PER_REQUEST);
            if (m == null) {
                m = new HashMap<AbstractPerRequest, Object>();
                hc.getProperties().put(SCOPE_PER_REQUEST, m);
            } else {
                Object o = m.get(this);
                if (o != null) return o;
            }

            Object o = _getInstance(hc);
            m.put(this, o);
            return o;
        }

        public final void destroy() {
        }
        
        protected abstract Object _getInstance(HttpContext hc);

        public void destroy(Object o) {
            try {
                rcd.destroy(o);
            } catch (IllegalAccessException ex) {
                throw new ContainerException("Unable to destroy resource", ex);
            } catch (InvocationTargetException ex) {
                throw new ContainerException("Unable to destroy resource", ex);
            } catch (RuntimeException ex) {
                throw new ContainerException("Unable to destroy resource", ex);
            }
        }
    }

    private final class PerRequest extends AbstractPerRequest {
        private ResourceComponentConstructor rcc;

        @Override
        public void init(AbstractResource abstractResource) {
            super.init(abstractResource);

            this.rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.PerRequest,
                    abstractResource);
        }

        protected Object _getInstance(HttpContext hc) {
            try {
                return rcc.construct(hc);
            } catch (InstantiationException ex) {
                throw new ContainerException("Unable to create resource " + rcc.getResourceClass(), ex);
            } catch (IllegalAccessException ex) {
                throw new ContainerException("Unable to create resource " + rcc.getResourceClass(), ex);
            } catch (InvocationTargetException ex) {
                // Propagate the target exception so it may be mapped to a response
                throw new MappableContainerException(ex.getTargetException());
            } catch (WebApplicationException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new ContainerException("Unable to create resource " + rcc.getResourceClass(), ex);
            }
        }
    }

    private final class PerRequestInstantiated extends AbstractPerRequest {
        private final IoCInstantiatedComponentProvider iicp;

        private final IoCDestroyable destroyable;
        
        private ResourceComponentInjector rci;

        PerRequestInstantiated(IoCInstantiatedComponentProvider iicp) {
            this.iicp = iicp;
            this.destroyable = (iicp instanceof IoCDestroyable)
                    ? (IoCDestroyable) iicp : null;
        }

        @Override
        public void init(AbstractResource abstractResource) {
            super.init(abstractResource);
            if (destroyable == null) {
                this.rci = new ResourceComponentInjector(
                        sipc,
                        ComponentScope.PerRequest,
                        abstractResource);
            }
        }

        public Object _getInstance(HttpContext hc) {
            Object o = iicp.getInstance();
            if (destroyable == null) {
                rci.inject(hc, iicp.getInjectableInstance(o));
            }
            return o;
        }

        @Override
        public void destroy(Object o) {
            if (destroyable != null) {
                destroyable.destroy(o);
            } else {
                super.destroy(o);
            }
        }
    }

    private final class PerRequestProxied extends AbstractPerRequest {
        private final IoCProxiedComponentProvider ipcp;

        private ResourceComponentConstructor rcc;

        PerRequestProxied(IoCProxiedComponentProvider ipcp) {
            this.ipcp = ipcp;
        }

        @Override
        public void init(AbstractResource abstractResource) {
            super.init(abstractResource);

            this.rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.PerRequest,
                    abstractResource);
        }

        public Object _getInstance(HttpContext hc) {
            try {
                return ipcp.proxy(rcc.construct(hc));
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