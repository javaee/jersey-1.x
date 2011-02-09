/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.server.impl.ejb;

import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessor;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.interceptor.InvocationContext;
import javax.ws.rs.ext.Provider;

final class EJBInjectionInterceptor {

    private IoCComponentProcessorFactory cpf;

    private final ConcurrentMap<Class, IoCComponentProcessor> componentProcessorMap =
            new ConcurrentHashMap<Class, IoCComponentProcessor>();
    
    public void setFactory(IoCComponentProcessorFactory cpf) {
        this.cpf = cpf;
    }

    @PostConstruct
    private void init(final InvocationContext context) throws Exception {
        if (cpf == null) {
            // Not initialized
            return;
        }

        final Object beanInstance = context.getTarget();
        final IoCComponentProcessor icp = get(beanInstance.getClass());
        if (icp != null)
            icp.postConstruct(beanInstance);
        
        // Invoke next interceptor in chain
        context.proceed();
    }

    private static final IoCComponentProcessor NULL_COMPONENT_PROCESSOR = new IoCComponentProcessor() {
        public void preConstruct() {
        }

        public void postConstruct(Object o) {
        }
    };
    
    private IoCComponentProcessor get(final Class c) {
        IoCComponentProcessor cp = componentProcessorMap.get(c);
        if (cp != null) {
            return (cp == NULL_COMPONENT_PROCESSOR) ? null : cp;
        }

        synchronized (componentProcessorMap) {
            cp = componentProcessorMap.get(c);
            if (cp != null) {
                return (cp == NULL_COMPONENT_PROCESSOR) ? null : cp;
            }

            final ComponentScope cs = c.isAnnotationPresent(ManagedBean.class)
                    ? c.isAnnotationPresent(Provider.class)
                        ? ComponentScope.Singleton
                        : cpf.getScope(c)
                    : ComponentScope.Singleton;
            cp = cpf.get(c, cs);
            if (cp != null) {
                componentProcessorMap.put(c, cp);
            } else {
                componentProcessorMap.put(c, NULL_COMPONENT_PROCESSOR);
            }
        }
        return cp;
    }
}
