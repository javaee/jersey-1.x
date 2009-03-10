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
package com.sun.jersey.core.spi.component.ioc;

import com.sun.jersey.core.spi.component.ComponentInjector;
import com.sun.jersey.core.spi.component.ComponentProvider;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ProviderFactory;
import com.sun.jersey.spi.inject.InjectableProviderContext;

/**
 * An extension of {@link ProviderFactory} that defers to an
 * {@link IoCComponentProviderFactory}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class IoCProviderFactory extends ProviderFactory {
    private final IoCComponentProviderFactory icpf;

    /**
     *
     * @param ipc the injectable provider context.
     * @param icpf the IoC component provider factory.
     */
    public IoCProviderFactory(
            InjectableProviderContext ipc,
            IoCComponentProviderFactory icpf) {
        super(ipc);
        this.icpf = icpf;
    }

    @Override
    public ComponentProvider _getComponentProvider(Class c) {
        IoCComponentProvider icp = icpf.getComponentProvider(c);
        return (icp == null) ? super._getComponentProvider(c) : wrap(c, icp);
    }

    private ComponentProvider wrap(Class c, IoCComponentProvider icp) {
        if (icp instanceof IoCManagedComponentProvider) {
            IoCManagedComponentProvider imcp = (IoCManagedComponentProvider)icp;
            if (imcp.getScope() == ComponentScope.Singleton) {
                return new SingletonWrapper(getInjectableProviderContext(), imcp, c);
            } else {
                throw new RuntimeException("The scope of the component " + c + " must be a singleton");
            }
        } else if (icp instanceof IoCFullyManagedComponentProvider) {
            IoCFullyManagedComponentProvider ifmcp = (IoCFullyManagedComponentProvider)icp;
            return new FullyManagedSingleton(ifmcp.getInstance());
        } else if (icp instanceof IoCInstantiatedComponentProvider) {
            IoCInstantiatedComponentProvider iicp = (IoCInstantiatedComponentProvider)icp;
            return new SingletonWrapper(getInjectableProviderContext(), iicp, c);
        } else if (icp instanceof IoCProxiedComponentProvider) {
            IoCProxiedComponentProvider ipcp = (IoCProxiedComponentProvider)icp;
            ComponentProvider cp = super._getComponentProvider(c);
            return new ProxiedSingletonWrapper(ipcp, cp, c);
        }
        throw new UnsupportedOperationException();
    }

    private static class SingletonWrapper implements ComponentProvider {
        private final Object o;

        SingletonWrapper(InjectableProviderContext ipc, 
                IoCInstantiatedComponentProvider iicp,
                Class c) {
            ComponentInjector rci = new ComponentInjector(
                    ipc,
                    c);
            o = iicp.getInstance();
            rci.inject(iicp.getInjectableInstance(o));
        }

        public Object getInstance() {
            return o;
        }
    }

    private static class FullyManagedSingleton implements ComponentProvider {
        private final Object o;

        FullyManagedSingleton(Object o) {
            this.o = o;
        }

        public Object getInstance() {
            return o;
        }
    }

    private static class ProxiedSingletonWrapper implements ComponentProvider {
        private final Object o;

        ProxiedSingletonWrapper(IoCProxiedComponentProvider ipcp,
                ComponentProvider cp,
                Class c) {

            Object _o = cp.getInstance();
            this.o = ipcp.proxy(_o);
            if (!this.o.getClass().isAssignableFrom(_o.getClass()))
                throw new IllegalStateException("Proxied object class " + this.o.getClass() +
                        " is not assignable from object class " + _o.getClass());
        }

        public Object getInstance() {
            return o;
        }
    }
 }