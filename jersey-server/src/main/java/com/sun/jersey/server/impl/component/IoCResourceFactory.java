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
package com.sun.jersey.server.impl.component;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.spi.component.ResourceComponentInjector;
import com.sun.jersey.server.spi.component.ResourceComponentProvider;
import com.sun.jersey.server.spi.component.ResourceComponentProviderFactory;
import com.sun.jersey.spi.inject.InjectableProviderContext;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class IoCResourceFactory extends ResourceFactory {
    private final IoCComponentProviderFactory icpf;
    
    public IoCResourceFactory(
            ResourceConfig config,
            InjectableProviderContext ipc,
            IoCComponentProviderFactory icpf) {
        super(config, ipc);
        this.icpf = icpf;
    }

    @Override
    public ResourceComponentProvider getComponentProvider(Class c) {
        IoCComponentProvider icp = icpf.getComponentProvider(c);
        return (icp == null) ? super.getComponentProvider(c) : wrap(c, icp);
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
        } else {
            ResourceComponentProviderFactory rcpf = getComponentProviderFactory(c);
            return rcpf.getComponentProvider(icp, c);
        }
    }

    private static class PerRequestWrapper implements ResourceComponentProvider {
        private final InjectableProviderContext ipc;
        private final IoCManagedComponentProvider imcp;
        private ResourceComponentInjector rci;
        
        PerRequestWrapper(InjectableProviderContext ipc, IoCManagedComponentProvider imcp) {
            this.ipc = ipc;
            this.imcp = imcp;
        }

        public void init(AbstractResource abstractResource) {
            rci = new ResourceComponentInjector(
                    ipc,
                    ComponentScope.PerRequest,
                    abstractResource);
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
        private final InjectableProviderContext ipc;
        private final IoCManagedComponentProvider imcp;
        private Object o;

        SingletonWrapper(InjectableProviderContext ipc, IoCManagedComponentProvider imcp) {
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
        private final InjectableProviderContext ipc;
        private final IoCManagedComponentProvider imcp;
        private ResourceComponentInjector rci;

        UndefinedWrapper(InjectableProviderContext ipc, IoCManagedComponentProvider imcp) {
            this.ipc = ipc;
            this.imcp = imcp;
        }

        public void init(AbstractResource abstractResource) {
            rci = new ResourceComponentInjector(
                    ipc,
                    ComponentScope.Undefined,
                    abstractResource);
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