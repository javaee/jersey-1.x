/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.jersey.core.spi.component.ioc;

import com.sun.jersey.core.spi.component.ComponentDestructor;
import com.sun.jersey.core.spi.component.ComponentInjector;
import com.sun.jersey.core.spi.component.ComponentProvider;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ProviderFactory;
import com.sun.jersey.spi.inject.InjectableProviderContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * An extension of {@link ProviderFactory} that defers to an
 * {@link IoCComponentProviderFactory}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class IoCProviderFactory extends ProviderFactory {
    private final List<IoCComponentProviderFactory> factories;

    /**
     *
     * @param ipc the injectable provider context.
     * @param icpf the IoC component provider factory.
     */
    public IoCProviderFactory(
            InjectableProviderContext ipc,
            IoCComponentProviderFactory icpf) {
        this(ipc, Collections.singletonList(icpf));
    }

    /**
     *
     * @param ipc the injectable provider context.
     * @param factories the list of IoC component provider factory.
     */
    public IoCProviderFactory(
            InjectableProviderContext ipc,
            List<IoCComponentProviderFactory> factories) {
        super(ipc);
        this.factories = factories;
    }

    @Override
    public ComponentProvider _getComponentProvider(Class c) {
        IoCComponentProvider icp = null;
        for (IoCComponentProviderFactory f : factories) {
            icp = f.getComponentProvider(c);
            if (icp != null)
                break;
        }
        return (icp == null) ? super._getComponentProvider(c) : wrap(c, icp);
    }

    private ComponentProvider wrap(Class c, IoCComponentProvider icp) {
        if (icp instanceof IoCManagedComponentProvider) {
            IoCManagedComponentProvider imcp = (IoCManagedComponentProvider)icp;
            if (imcp.getScope() == ComponentScope.Singleton) {
                return new ManagedSingleton(getInjectableProviderContext(), imcp, c);
            } else {
                throw new RuntimeException("The scope of the component " + c + " must be a singleton");
            }
        } else if (icp instanceof IoCFullyManagedComponentProvider) {
            IoCFullyManagedComponentProvider ifmcp = (IoCFullyManagedComponentProvider)icp;
            return new FullyManagedSingleton(ifmcp.getInstance());
        } else if (icp instanceof IoCInstantiatedComponentProvider) {
            IoCInstantiatedComponentProvider iicp = (IoCInstantiatedComponentProvider)icp;
            return new InstantiatedSingleton(getInjectableProviderContext(), iicp, c);
        } else if (icp instanceof IoCProxiedComponentProvider) {
            IoCProxiedComponentProvider ipcp = (IoCProxiedComponentProvider)icp;
            ComponentProvider cp = super._getComponentProvider(c);
            // Problem creating the component provider
            if (cp == null)
                return null;

            return new ProxiedSingletonWrapper(ipcp, cp, c);
        }
        throw new UnsupportedOperationException();
    }

    private static class InstantiatedSingleton implements ComponentProvider, Destroyable {
        private final Object o;

        private final IoCDestroyable destroyable;
        
        private final ComponentDestructor cd;

        InstantiatedSingleton(InjectableProviderContext ipc,
                IoCInstantiatedComponentProvider iicp,
                Class c) {
            this.destroyable = (iicp instanceof IoCDestroyable)
                    ? (IoCDestroyable) iicp : null;
            
            o = iicp.getInstance();

            this.cd = (destroyable == null) ? new ComponentDestructor(c) : null;

            if (destroyable == null) {
                ComponentInjector ci = new ComponentInjector(
                        ipc,
                        c);
                ci.inject(iicp.getInjectableInstance(o));
            }
        }

        @Override
        public Object getInstance() {
            return o;
        }

        @Override
        public void destroy() {
            if (destroyable != null) {
                destroyable.destroy(o);
            } else {
                try {
                    cd.destroy(o);
                } catch (IllegalAccessException ex) {
                    LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
                } catch (InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
                }
            }
        }
    }

    private static class ManagedSingleton implements ComponentProvider {
        private final Object o;

        ManagedSingleton(InjectableProviderContext ipc,
                IoCInstantiatedComponentProvider iicp,
                Class c) {
            ComponentInjector rci = new ComponentInjector(
                    ipc,
                    c);
            o = iicp.getInstance();
            rci.inject(iicp.getInjectableInstance(o));
        }

        @Override
        public Object getInstance() {
            return o;
        }
    }

    private static class FullyManagedSingleton implements ComponentProvider {
        private final Object o;

        FullyManagedSingleton(Object o) {
            this.o = o;
        }

        @Override
        public Object getInstance() {
            return o;
        }
    }

    private static class ProxiedSingletonWrapper implements ComponentProvider, Destroyable {
        private final Destroyable destroyable;

        private final Object proxy;

        ProxiedSingletonWrapper(IoCProxiedComponentProvider ipcp,
                ComponentProvider cp,
                Class c) {

            this.destroyable = (cp instanceof Destroyable)
                    ? (Destroyable) cp : null;
            
            Object o = cp.getInstance();
            this.proxy = ipcp.proxy(o);
            if (!this.proxy.getClass().isAssignableFrom(o.getClass()))
                throw new IllegalStateException("Proxied object class " + this.proxy.getClass() +
                        " is not assignable from object class " + o.getClass());
        }

        @Override
        public Object getInstance() {
            return proxy;
        }

        @Override
        public void destroy() {
            if (destroyable != null) {
                destroyable.destroy();
            }
        }
    }
 }