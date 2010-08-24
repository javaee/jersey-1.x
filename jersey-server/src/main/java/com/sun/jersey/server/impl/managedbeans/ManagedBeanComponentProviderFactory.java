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
package com.sun.jersey.server.impl.managedbeans;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCDestroyable;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import javax.annotation.ManagedBean;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class ManagedBeanComponentProviderFactory implements
        IoCComponentProviderFactory {
    
    private static final Logger LOGGER = Logger.getLogger(
            ManagedBeanComponentProviderFactory.class.getName());

    private final Object injectionMgr;
    
    private final Method createManagedObjectMethod;

    private final Method destroyManagedObjectMethod;
    
    public ManagedBeanComponentProviderFactory(Object injectionMgr, 
            Method createManagedObjectMethod, Method destroyManagedObjectMethod) {
        this.injectionMgr = injectionMgr;
        this.createManagedObjectMethod = createManagedObjectMethod;
        this.destroyManagedObjectMethod = destroyManagedObjectMethod;
    }

    // IoCComponentProviderFactory
    
    public IoCComponentProvider getComponentProvider(Class<?> c) {
        return getComponentProvider(null, c);
    }

    public IoCComponentProvider getComponentProvider(ComponentContext cc, Class<?> c) {
        if (!isManagedBean(c))
            return null;

        LOGGER.info("Binding the Managed bean class " + c.getName() +
                " to ManagedBeanComponentProvider");

        return new ManagedBeanComponentProvider(c);
    }

    private boolean isManagedBean(Class<?> c) {
        return c.isAnnotationPresent(ManagedBean.class);
    }

    private class ManagedBeanComponentProvider implements 
            IoCInstantiatedComponentProvider, IoCDestroyable {
        private final Class<?> c;

        ManagedBeanComponentProvider(Class<?> c) {
            this.c = c;
        }

        // IoCInstantiatedComponentProvider
        
        public Object getInstance() {
            try {
                return createManagedObjectMethod.invoke(injectionMgr, c);
            } catch (Exception ex) {
                throw new ContainerException(ex);
            }
        }

        public Object getInjectableInstance(Object o) {
            return o;
        }

        // IoCDestroyable

        public void destroy(Object o) {
            try {
                destroyManagedObjectMethod.invoke(injectionMgr, o);
            } catch (Exception ex) {
                throw new ContainerException(ex);
            }
        }
    }
}