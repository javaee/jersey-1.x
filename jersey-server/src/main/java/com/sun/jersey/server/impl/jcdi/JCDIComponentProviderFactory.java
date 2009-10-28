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
package com.sun.jersey.server.impl.jcdi;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessor;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactory;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactoryInitializer;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCFullyManagedComponentProvider;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class JCDIComponentProviderFactory implements
        IoCComponentProviderFactory,
        IoCComponentProcessorFactoryInitializer {

    private static final Logger LOGGER = Logger.getLogger(
            JCDIComponentProviderFactory.class.getName());

    private final BeanManager bm;

    public JCDIComponentProviderFactory(Object bm) {
        this.bm = (BeanManager)bm;
    }

    // IoCComponentProcessorFactoryInitializer

    public void init(final IoCComponentProcessorFactory cpf) {
        // TODO get list of registered ProcessInjectionTarget
        final Collection<ProcessInjectionTarget> pitc = Collections.emptyList();

        for (final ProcessInjectionTarget pit : pitc) {
            final Class<?> c = pit.getAnnotatedType().getJavaClass();
            final Bean<?> b = getBean(c);
            if (b == null)
                continue;
            
            final IoCComponentProcessor icp = cpf.get(c, getComponentScope(b));
            if (icp == null)
                continue;

            final InjectionTarget it = pit.getInjectionTarget();
            final InjectionTarget nit = new InjectionTarget() {
                public void inject(Object t, CreationalContext cc) {
                    it.inject(t, cc);
                    icp.postConstruct(t);
                }

                public void postConstruct(Object t) {
                    it.postConstruct(t);
                }

                public void preDestroy(Object t) {
                    it.preDestroy(t);
                }

                public Object produce(CreationalContext cc) {
                    return it.produce(cc);
                }

                public void dispose(Object t) {
                    it.dispose(t);
                }

                public Set getInjectionPoints() {
                    return it.getInjectionPoints();
                }
            };
            pit.setInjectionTarget(nit);
        }
    }

    // IoCComponentProviderFactory

    public IoCComponentProvider getComponentProvider(Class<?> c) {
        return getComponentProvider(null, c);
    }

    public IoCComponentProvider getComponentProvider(ComponentContext cc, final Class<?> c) {
        final Set<Bean<?>> bs = bm.getBeans(c);
        if (bs.isEmpty()) {
            return null;
        }

        final Bean<?> b = bm.resolve(bs);
        final ComponentScope cs = getComponentScope(b);

        LOGGER.info("Binding the JCDI managed class " + c.getName() +
                " to JCDIComponentProviderFactory in the scope " + cs);

        return new IoCFullyManagedComponentProvider() {
            public Object getInstance() {
                final CreationalContext<?> bcc = bm.createCreationalContext(b);
                return c.cast(bm.getReference(b, c, bcc));
            }
        };
    }

    private Bean<?> getBean(Class<?> c) {
        final Set<Bean<?>> bs = bm.getBeans(c);
        if (bs.isEmpty()) {
            return null;
        }

        return bm.resolve(bs);
    }

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
        return m;
    }
}