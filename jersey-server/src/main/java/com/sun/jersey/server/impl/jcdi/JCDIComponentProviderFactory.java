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

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessor;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactory;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactoryInitializer;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCDestroyable;
import com.sun.jersey.core.spi.component.ioc.IoCFullyManagedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
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
        final JCDIComponentExtension e = getRef(JCDIComponentExtension.class);
        if (e == null) {
            throw new ContainerException("Reference to " + JCDIComponentExtension.class + " is null");
        }
        
        for (final ProcessInjectionTarget pit : e.getProcessInjectionTargets()) {
            final Class<?> c = pit.getAnnotatedType().getJavaClass();
            final Bean<?> b = getBean(c);
            if (b == null)
                continue;

            final Class<? extends Annotation> s = b.getScope();
            if (s == Dependent.class &&
                    !c.isAnnotationPresent(ManagedBean.class))
                continue;

            final IoCComponentProcessor icp = cpf.get(c, getComponentScope(b));
            if (icp == null)
                continue;

            LOGGER.info("Adapting InjectionTarget for " + c.getName() + " in the scope " + b.getScope());

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
        e.clear();
    }

    // IoCComponentProviderFactory

    public IoCComponentProvider getComponentProvider(Class<?> c) {
        return getComponentProvider(null, c);
    };

    public IoCComponentProvider getComponentProvider(ComponentContext cc, final Class<?> c) {
        final Bean<?> b = getBean(c);
        if (b == null)
            return null;
        
        final Class<? extends Annotation> s = b.getScope();
        final ComponentScope cs = getComponentScope(b);

        if (s == Dependent.class) {
            if (!c.isAnnotationPresent(ManagedBean.class))
                return null;

            LOGGER.info("Binding the JCDI managed-bean class " + c.getName() +
                    " in the scope " + s.getName() +
                    " to JCDIComponentProviderFactory");

            return new ComponentProviderDestroyable() {

                // IoCInstantiatedComponentProvider

                public Object getInjectableInstance(Object o) {
                    return o;
                }

                public Object getInstance() {
                    final CreationalContext<?> bcc = bm.createCreationalContext(b);
                    return c.cast(bm.getReference(b, c, bcc));
                }

                // IoCDestroyable

                public void destroy(Object o) {
                    final CreationalContext cc = bm.createCreationalContext(b);
                    ((Bean)b).destroy(o, cc);
                }
            };
        } else {
            LOGGER.info("Binding the JCDI managed class " + c.getName() +
                    " in the scope " + s.getName() +
                    " to JCDIComponentProviderFactory in the scope " + cs);

            return new IoCFullyManagedComponentProvider() {
                public Object getInstance() {
                    final CreationalContext<?> bcc = bm.createCreationalContext(b);
                    return c.cast(bm.getReference(b, c, bcc));
                }
            };
        }
    }

    private interface ComponentProviderDestroyable extends IoCInstantiatedComponentProvider, IoCDestroyable {
    };

    private Bean<?> getBean(Class<?> c) {
        final Set<Bean<?>> bs = bm.getBeans(c);
        if (bs.isEmpty()) {
            return null;
        }

        try {
            return bm.resolve(bs);
        } catch(AmbiguousResolutionException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private <T> T getRef(Class<T> c) {
        Bean<?> b = getBean(c);
        if (b == null) {
            return null;
        }
        
        CreationalContext<?> cc = bm.createCreationalContext(b);
        return c.cast(bm.getReference(b, c, cc));
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
        m.put(Dependent.class, ComponentScope.PerRequest);
        return m;
    }
}