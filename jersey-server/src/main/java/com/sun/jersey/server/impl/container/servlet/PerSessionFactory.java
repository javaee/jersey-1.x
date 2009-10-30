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

package com.sun.jersey.server.impl.container.servlet;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCProxiedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCDestroyable;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.spi.component.ResourceComponentConstructor;
import com.sun.jersey.server.spi.component.ResourceComponentDestructor;
import com.sun.jersey.server.spi.component.ResourceComponentInjector;
import com.sun.jersey.server.spi.component.ResourceComponentProvider;
import com.sun.jersey.server.spi.component.ResourceComponentProviderFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

/**
 * A provider that maintains a per session resource class instance
 */
public final class PerSessionFactory implements ResourceComponentProviderFactory {

    private final ServerInjectableProviderContext sipc;

    private final HttpServletRequest hsr;

    private final HttpContext threadLocalHc;

    public PerSessionFactory(
            @Context ServerInjectableProviderContext sipc,
            @Context HttpServletRequest hsr,
            @Context HttpContext threadLocalHc) {
        this.hsr = hsr;
        this.sipc = sipc;
        this.threadLocalHc = threadLocalHc;
    }

    public ResourceComponentProvider getComponentProvider(Class c) {
        return new PerSesson();
    }

    public ResourceComponentProvider getComponentProvider(IoCComponentProvider icp, Class c) {
        if (icp instanceof IoCInstantiatedComponentProvider) {
            return new PerSessonInstantiated((IoCInstantiatedComponentProvider)icp);
        } else if (icp instanceof IoCProxiedComponentProvider) {
            return new PerSessonProxied((IoCProxiedComponentProvider)icp);
        }
        throw new IllegalStateException();
    }

    private static class ProviderObjectPair {
        private final AbstractPerSession s;
        private final Object o;

        public ProviderObjectPair(AbstractPerSession s, Object o) {
            this.s = s;
            this.o = o;
        }
    }
    
    private static class SessionMap extends HashMap<String, ProviderObjectPair>
            implements HttpSessionBindingListener {

        public void valueBound(HttpSessionBindingEvent hsbe) {
        }

        public void valueUnbound(HttpSessionBindingEvent hsbe) {
            for (ProviderObjectPair x : values()) {
                x.s.destroy(x.o);
            }
        }
    }

    private abstract class AbstractPerSession implements ResourceComponentProvider {
        private static final String SCOPE_PER_SESSION = "com.sun.jersey.scope.PerSession";

        private ResourceComponentDestructor rcd;

        private Class c;
        
        public void init(AbstractResource abstractResource) {
            this.rcd = new ResourceComponentDestructor(abstractResource);
            this.c = abstractResource.getResourceClass();
        }

        public final Object getInstance() {
            return getInstance(threadLocalHc);
        }

        public final Object getInstance(HttpContext hc) {
            HttpSession hs = hsr.getSession();

            synchronized(hs) {
                SessionMap sm = (SessionMap)hs.getAttribute(SCOPE_PER_SESSION);
                if (sm == null) {
                    sm = new SessionMap();
                    hs.setAttribute(SCOPE_PER_SESSION, sm);
                }
                ProviderObjectPair x = sm.get(c.getName());
                if (x != null)
                    return x.o;

                Object o = _getInstance(hc);
                sm.put(c.getName(), new ProviderObjectPair(this, o));
                return o;
            }
        }

        protected abstract Object _getInstance(HttpContext hc);

        public final void destroy() {
        }

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

    private final class PerSesson extends AbstractPerSession {
        private ResourceComponentConstructor rcc;

        @Override
        public void init(AbstractResource abstractResource) {
            super.init(abstractResource);
            
            this.rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.Undefined,
                    abstractResource);
        }

        protected Object _getInstance(HttpContext hc) {
            try {
                return rcc.construct(hc);
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

    private final class PerSessonInstantiated extends AbstractPerSession {
        private final IoCInstantiatedComponentProvider iicp;

        private final IoCDestroyable destroyable;
        
        private ResourceComponentInjector rci;

        PerSessonInstantiated(IoCInstantiatedComponentProvider iicp) {
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
                        ComponentScope.Undefined,
                        abstractResource);
            }
        }

        protected Object _getInstance(HttpContext hc) {
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

    private final class PerSessonProxied extends AbstractPerSession {
        private final IoCProxiedComponentProvider ipcp;

        private ResourceComponentConstructor rcc;

        PerSessonProxied(IoCProxiedComponentProvider ipcp) {
            this.ipcp = ipcp;
        }

        @Override
        public void init(AbstractResource abstractResource) {
            super.init(abstractResource);
            
            this.rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.Undefined,
                    abstractResource);
        }

        protected Object _getInstance(HttpContext hc) {
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