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
package com.sun.jersey.server.impl.cdi;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCDestroyable;
import com.sun.jersey.core.spi.component.ioc.IoCFullyManagedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationListener;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import static com.sun.jersey.server.impl.cdi.Utils.getBean;

/**
 * Factory for IoCComponentProvider for CDI beans.
 *
 * Crucially, it passes the WebApplication and ResourceConfig on to the CDIExtension,
 * allowing beans registered by the extension to (finally) do some work.
 *
 * @author Paul.Sandoz@Sun.Com
 * @author robc
 */
public class CDIComponentProviderFactory implements
        IoCComponentProviderFactory, WebApplicationListener {

    private static final Logger LOGGER = Logger.getLogger(
            CDIComponentProviderFactory.class.getName());
    
    private final BeanManager beanManager;
    private final CDIExtension extension;

    public CDIComponentProviderFactory(Object bm, ResourceConfig rc, WebApplication wa) {
        beanManager = (BeanManager)bm;
        // work around proxying bug in Weld
        if (CDIExtension.lookupExtensionInBeanManager) {
            extension = Utils.getInstance(beanManager, CDIExtension.class);
        }
        else {
            extension = CDIExtension.getInitializedExtension();
        }
        extension.setWebApplication(wa);
        extension.setResourceConfig(rc);
    }
    
    public void onWebApplicationReady() {
        extension.lateInitialize();
    }

    public IoCComponentProvider getComponentProvider(Class<?> c) {
        return getComponentProvider(null, c);
    }

    public IoCComponentProvider getComponentProvider(ComponentContext cc, final Class<?> c) {
        final Bean<?> b = getBean(beanManager, c);
        if (b == null) {
            return null;
        }

        final Class<? extends Annotation> s = b.getScope();
        final ComponentScope cs = getComponentScope(b);

        if (s == Dependent.class) {
            if (!c.isAnnotationPresent(ManagedBean.class)) {
                return null;
            }

            LOGGER.fine("Binding the CDI managed bean " + c.getName() +
                    " in scope " + s.getName() +
                    " to CDIComponentProviderFactory");

            return new ComponentProviderDestroyable() {

                // IoCInstantiatedComponentProvider

                public Object getInjectableInstance(Object o) {
                    return o;
                }

                public Object getInstance() {
                    final CreationalContext<?> bcc = beanManager.createCreationalContext(b);
                    return c.cast(beanManager.getReference(b, c, bcc));
                }

                // IoCDestroyable

                public void destroy(Object o) {
                    final CreationalContext cc = beanManager.createCreationalContext(b);
                    ((Bean)b).destroy(o, cc);
                }
            };
        }
        else {
            LOGGER.fine("Binding the CDI managed bean " + c.getName() +
                    " in scope " + s.getName() +
                    " to CDIComponentProviderFactory in scope " + cs);

            return new IoCFullyManagedComponentProvider() {
                public ComponentScope getScope() {
                    return cs;
                }

                public Object getInstance() {
                    final CreationalContext<?> bcc = beanManager.createCreationalContext(b);
                    return c.cast(beanManager.getReference(b, c, bcc));
                }
            };
        }
    }
    
    private interface ComponentProviderDestroyable extends IoCInstantiatedComponentProvider, IoCDestroyable {
    };

    private ComponentScope getComponentScope(Bean<?> b) {
        ComponentScope cs = scopeMap.get(b.getScope());
        return (cs != null) ? cs : ComponentScope.Undefined;
    }

    private final Map<Class<? extends Annotation>, ComponentScope> scopeMap = createScopeMap();

    private Map<Class<? extends Annotation>, ComponentScope> createScopeMap() {
        Map<Class<? extends Annotation>, ComponentScope> m = 
                new HashMap<Class<? extends Annotation>, ComponentScope>();
        m.put(ApplicationScoped.class, ComponentScope.Singleton);
        m.put(RequestScoped.class, ComponentScope.PerRequest);
        m.put(Dependent.class, ComponentScope.PerRequest);
        return Collections.unmodifiableMap(m);
    }
}
