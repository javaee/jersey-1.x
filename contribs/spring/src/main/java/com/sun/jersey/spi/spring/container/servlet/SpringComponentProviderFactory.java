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
package com.sun.jersey.spi.spring.container.servlet;

import com.sun.jersey.api.spring.Autowire;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
import com.sun.jersey.spi.inject.Inject;

/**
 * The Spring-based {@link IoCComponentProviderFactory}.
 * <p>
 * Resource and provider classes can be registered Spring-based beans using
 * XML-based registration or auto-wire-based registration.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class SpringComponentProviderFactory implements IoCComponentProviderFactory {

    private static final Logger LOGGER = Logger.getLogger(SpringComponentProviderFactory.class.getName());

    private ConfigurableApplicationContext springContext;

    public SpringComponentProviderFactory(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    public IoCComponentProvider getComponentProvider(Class c) {
        return getComponentProvider(null, c);
    }

    public IoCComponentProvider getComponentProvider(ComponentContext cc, Class c) {
        final Autowire autowire = (Autowire)c.getAnnotation(Autowire.class);
        if (autowire != null) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Creating resource class " +
                        c.getSimpleName() +
                        " annotated with @" +
                        Autowire.class.getSimpleName() +
                        " as spring bean.");
            }
            return new SpringInstantiatedComponentProvider(c, autowire);
        }
        
        final String beanName = getBeanName(cc, c, springContext);
        if (beanName == null) {
            return null;
        }

        if (springContext.isSingleton(beanName)) {
            return new SpringManagedComponentProvider(ComponentScope.Singleton, beanName, c);
        } else if (springContext.isPrototype(beanName)) {
            return new SpringManagedComponentProvider(ComponentScope.PerRequest, beanName, c);
        } else {
            return new SpringManagedComponentProvider(ComponentScope.Undefined, beanName, c);
        }
    }

    private class SpringInstantiatedComponentProvider implements IoCInstantiatedComponentProvider {
        private final Class c;
        private final Autowire a;

        SpringInstantiatedComponentProvider(Class c, Autowire a) {
            this.c = c;
            this.a = a;
        }

        public Object getInstance() {
            return springContext.getBeanFactory().createBean(c,
                    a.mode().getSpringCode(), a.dependencyCheck());
        }
        
        public Object getInjectableInstance(Object o) {
            return SpringComponentProviderFactory.getInjectableInstance(o);
        }
    }
    
    private class SpringManagedComponentProvider implements IoCManagedComponentProvider {
        private final ComponentScope scope;
        private final String beanName;
        private final Class c;
        
        SpringManagedComponentProvider(ComponentScope scope, String beanName, Class c) {
            this.scope = scope;
            this.beanName = beanName;
            this.c = c;
        }

        public ComponentScope getScope() {
            return scope;
        }

        public Object getInjectableInstance(Object o) {
            return SpringComponentProviderFactory.getInjectableInstance(o);
        }

        public Object getInstance() {
            return springContext.getBean(beanName, c);
        }
    }

    private static Object getInjectableInstance(Object o) {
        if (AopUtils.isAopProxy(o)) {
            final Advised aopResource = (Advised)o;
            try {
                return aopResource.getTargetSource().getTarget();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not get target object from proxy.", e);
                throw new RuntimeException("Could not get target object from proxy.", e);
            }
        } else {
            return o;
        }
    }

    private static String getBeanName(ComponentContext cc, Class<?> c, ApplicationContext springContext) {
        boolean annotatedWithInject = false;
        if (cc != null) {
            final Inject inject = getAnnotation(cc.getAnnotations(), Inject.class);
            if (inject != null) {
                annotatedWithInject = true;
                if (inject.value() != null && !inject.value().equals("")) {
                    return inject.value();
                }

            }
        }

        final String names[] = springContext.getBeanNamesForType(c);

        if (names.length == 0) {
            return null;
        } else if (names.length == 1) {
            return names[0];
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append("There are multiple beans configured in spring for the type ").
                    append(c.getName()).append(".");

            if (annotatedWithInject) {
                sb.append("\nYou should specify the name of the preferred bean at @Inject: Inject(\"yourBean\").");
            } else {
                sb.append("\nAnnotation information was not available, the reason might be because you're not using " +
                        "@Inject. You should use @Inject and specifiy the bean name via Inject(\"yourBean\").");
            }

            sb.append("\nAvailable bean names: ").append(toCSV(names));

            throw new RuntimeException(sb.toString());
        }
    }

    private static <T extends Annotation> T getAnnotation(Annotation[] annotations,
            Class<T> clazz) {
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(clazz)) {
                    return clazz.cast(annotation);
                }
            }
        }
        return null;
    }

    private static <T> String toCSV(T[] items) {
        if (items == null) {
            return null;
        }
        return toCSV(Arrays.asList(items));
    }

    private static <I> String toCSV(Collection<I> items) {
        return toCSV(items, ", ", null);
    }

    private static <I> String toCSV(Collection<I> items, String separator, String delimiter) {
        if (items == null) {
            return null;
        }
        if (items.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final Iterator<I> iter = items.iterator(); iter.hasNext();) {
            if (delimiter != null) {
                sb.append(delimiter);
            }
            final I item = iter.next();
            sb.append(item);
            if (delimiter != null) {
                sb.append(delimiter);
            }
            if (iter.hasNext()) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
}