/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2014 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.server.impl.component;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCFullyManagedComponentProvider;
import com.sun.jersey.core.util.PriorityUtil;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.spi.component.ResourceComponentInjector;
import com.sun.jersey.server.spi.component.ResourceComponentProvider;
import com.sun.jersey.server.spi.component.ResourceComponentProviderFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Factory for individual JAX-RS resources.
 *
 * @author Paul Sandoz (paul.sandoz at oracle.com)
 */
public class IoCResourceFactory extends ResourceFactory {

    private final List<IoCComponentProviderFactory> factories;

    public IoCResourceFactory(
            ResourceConfig config,
            ServerInjectableProviderContext ipc,
            List<IoCComponentProviderFactory> factories) {
        super(config, ipc);
        List<IoCComponentProviderFactory> myFactories = new ArrayList<IoCComponentProviderFactory>(factories);
        Collections.sort(myFactories, PriorityUtil.INSTANCE_COMPARATOR);
        this.factories = Collections.unmodifiableList(myFactories);
    }

    @Override
    public ResourceComponentProvider getComponentProvider(ComponentContext cc, Class c) {
        IoCComponentProvider icp = null;
        for (IoCComponentProviderFactory f : factories) {
            icp = f.getComponentProvider(cc, c);
            if (icp != null)
                break;
        }
        return (icp == null) ? super.getComponentProvider(cc, c) : wrap(c, icp);
    }

    private ResourceComponentProvider wrap(Class c, IoCComponentProvider icp) {
        if (icp instanceof IoCManagedComponentProvider) {
            IoCManagedComponentProvider imcp = (IoCManagedComponentProvider)icp;
            if (imcp.getScope() == ComponentScope.PerRequest) {
                return new PerRequestWrapper(getInjectableProviderContext(), imcp);
            } else if (imcp.getScope() == ComponentScope.Singleton) {
                return new SingletonWrapper(getInjectableProviderContext(), imcp);
            } else {
                return new UndefinedWrapper(getInjectableProviderContext(), imcp);
            }
        } else if (icp instanceof IoCFullyManagedComponentProvider) {
            IoCFullyManagedComponentProvider ifmcp = (IoCFullyManagedComponentProvider)icp;
            return new FullyManagedWrapper(ifmcp);
        } else {
            ResourceComponentProviderFactory rcpf = getComponentProviderFactory(c);
            return rcpf.getComponentProvider(icp, c);
        }
    }

    private static class FullyManagedWrapper implements ResourceComponentProvider {
        private final IoCFullyManagedComponentProvider ifmcp;

        FullyManagedWrapper(IoCFullyManagedComponentProvider ifmcp) {
            this.ifmcp = ifmcp;
        }

        public void init(AbstractResource abstractResource) {
        }

        public ComponentScope getScope() {
            return ifmcp.getScope();
        }

        public Object getInstance(HttpContext hc) {
            return ifmcp.getInstance();
        }

        public Object getInstance() {
            throw new IllegalStateException();
        }

        public void destroy() {
        }
    }

    private static class PerRequestWrapper implements ResourceComponentProvider {
        private final ServerInjectableProviderContext ipc;
        private final IoCManagedComponentProvider imcp;
        private ResourceComponentInjector rci;

        PerRequestWrapper(ServerInjectableProviderContext ipc, IoCManagedComponentProvider imcp) {
            this.ipc = ipc;
            this.imcp = imcp;
        }

        public void init(AbstractResource abstractResource) {
            rci = new ResourceComponentInjector(
                    ipc,
                    ComponentScope.PerRequest,
                    abstractResource);
        }

        public ComponentScope getScope() {
            return ComponentScope.PerRequest;
        }

        public Object getInstance(HttpContext hc) {
            Object o = imcp.getInstance();
            rci.inject(hc, imcp.getInjectableInstance(o));
            return o;
        }

        public Object getInstance() {
            throw new IllegalStateException();
        }

        public void destroy() {
        }
    }

    private static class SingletonWrapper implements ResourceComponentProvider {
        private final ServerInjectableProviderContext ipc;
        private final IoCManagedComponentProvider imcp;
        private Object o;

        SingletonWrapper(ServerInjectableProviderContext ipc, IoCManagedComponentProvider imcp) {
            this.ipc = ipc;
            this.imcp = imcp;
        }

        public void init(AbstractResource abstractResource) {
            ResourceComponentInjector rci = new ResourceComponentInjector(
                    ipc,
                    ComponentScope.Singleton,
                    abstractResource);
            o = imcp.getInstance();
            rci.inject(null, imcp.getInjectableInstance(o));
        }

        public ComponentScope getScope() {
            return ComponentScope.Singleton;
        }

        public Object getInstance(HttpContext hc) {
            return o;
        }

        public Object getInstance() {
            throw new IllegalStateException();
        }

        public void destroy() {
        }
    }

    private static class UndefinedWrapper implements ResourceComponentProvider {
        private final ServerInjectableProviderContext ipc;
        private final IoCManagedComponentProvider imcp;
        private ResourceComponentInjector rci;

        UndefinedWrapper(ServerInjectableProviderContext ipc, IoCManagedComponentProvider imcp) {
            this.ipc = ipc;
            this.imcp = imcp;
        }

        public void init(AbstractResource abstractResource) {
            rci = new ResourceComponentInjector(
                    ipc,
                    ComponentScope.Undefined,
                    abstractResource);
        }

        public ComponentScope getScope() {
            return ComponentScope.Undefined;
        }

        public Object getInstance(HttpContext hc) {
            Object o = imcp.getInstance();
            rci.inject(hc, imcp.getInjectableInstance(o));
            return o;
        }

        public Object getInstance() {
            throw new IllegalStateException();
        }

        public void destroy() {
        }
    }
}