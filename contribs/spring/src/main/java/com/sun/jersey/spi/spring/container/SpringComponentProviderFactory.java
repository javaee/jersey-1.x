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
package com.sun.jersey.spi.spring.container;

import com.sun.jersey.api.core.InjectParam;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.spring.Autowire;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
import com.sun.jersey.spi.inject.Inject;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Context;

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
    
    private final ConfigurableApplicationContext springContext;

    public SpringComponentProviderFactory(ResourceConfig rc, ConfigurableApplicationContext springContext) {

        this.springContext = springContext;

        addAppContextInjectableProvider(rc);
        registerSpringBeans(rc);
    }

    private void addAppContextInjectableProvider(final ResourceConfig rc) {
        rc.getSingletons().add(new SingletonTypeInjectableProvider<Context, ApplicationContext>(ApplicationContext.class, springContext) {});
    }

    private void registerSpringBeans(final ResourceConfig rc) {

        String[] names = BeanFactoryUtils.beanNamesIncludingAncestors(springContext);

        for (String name : names) {
            Class<?> type = ClassUtils.getUserClass(springContext.getType(name));
            if (ResourceConfig.isProviderClass(type)) {
                LOGGER.info("Registering Spring bean, " + name +
                        ", of type " + type.getName() +
                        " as a provider class");
                rc.getClasses().add(type);
            } else if (ResourceConfig.isRootResourceClass(type)) {
                LOGGER.info("Registering Spring bean, " + name +
                        ", of type " + type.getName() +
                        " as a root resource class");
                rc.getClasses().add(type);
            }
        }
    }

    @Override
    public IoCComponentProvider getComponentProvider(Class c) {
        return getComponentProvider(null, c);
    }

    @Override
    public IoCComponentProvider getComponentProvider(ComponentContext cc, Class c) {
        final Autowire autowire = (Autowire) c.getAnnotation(Autowire.class);
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

        final String scope = findBeanDefinition(beanName).getScope();
        return new SpringManagedComponentProvider(getComponentScope(scope), beanName, c);
    }

    /**
     * Fine the bean definition from a given context or from any of the parent
     * contexts.
     *
     * @param beanName the bean name.
     * @return the bean definition.
     * @throws NoSuchBeanDefinitionException if the bean definition could not
     *         be found.
     */
    private BeanDefinition findBeanDefinition(String beanName) {
        ConfigurableApplicationContext current = springContext;
        BeanDefinition beanDef = null;
        do {
            try {
                return current.getBeanFactory().getBeanDefinition(beanName);
            } catch (NoSuchBeanDefinitionException e) {
                final ApplicationContext parent = current.getParent();
                if (parent != null && parent instanceof ConfigurableApplicationContext) {
                    current = (ConfigurableApplicationContext) parent;
                } else {
                    throw e;
                }
            }
        } while (beanDef == null && current != null);
        return beanDef;
    }

    private ComponentScope getComponentScope(String scope) {
        ComponentScope cs = scopeMap.get(scope);
        return (cs != null) ? cs : ComponentScope.Undefined;
    }

    private final Map<String, ComponentScope> scopeMap = createScopeMap();

    private Map<String, ComponentScope> createScopeMap() {
        Map<String, ComponentScope> m = new HashMap<String, ComponentScope>();
        m.put(BeanDefinition.SCOPE_SINGLETON, ComponentScope.Singleton);
        m.put(BeanDefinition.SCOPE_PROTOTYPE, ComponentScope.PerRequest);
        m.put("request", ComponentScope.PerRequest);
        return m;
    }

    private class SpringInstantiatedComponentProvider implements IoCInstantiatedComponentProvider {

        private final Class c;
        private final Autowire a;

        SpringInstantiatedComponentProvider(Class c, Autowire a) {
            this.c = c;
            this.a = a;
        }

        @Override
        public Object getInstance() {
            return springContext.getBeanFactory().createBean(c,
                    a.mode().getSpringCode(), a.dependencyCheck());
        }

        @Override
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

        @Override
        public ComponentScope getScope() {
            return scope;
        }

        @Override
        public Object getInjectableInstance(Object o) {
            return SpringComponentProviderFactory.getInjectableInstance(o);
        }

        @Override
        public Object getInstance() {
            return springContext.getBean(beanName, c);
        }
    }

    private static Object getInjectableInstance(Object o) {
        if (AopUtils.isAopProxy(o)) {
            final Advised aopResource = (Advised) o;
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

            final InjectParam injectParam = getAnnotation(cc.getAnnotations(), InjectParam.class);
            if (injectParam != null) {
                annotatedWithInject = true;
                if (injectParam.value() != null && !injectParam.value().equals("")) {
                    return injectParam.value();
                }

            }
        }

        final String names[] = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(springContext, c);

        if (names.length == 0) {
            return null;
        } else if (names.length == 1) {
            return names[0];
        } else {
            // Check if types of the beans names are assignable
            // Spring auto-registration for a type A will include the bean
            // names for classes that extend A
            boolean inheritedNames = false;
            String beanName = null;
            for (String name : names) {
                Class<?> beanType = ClassUtils.getUserClass(springContext.getType(name));

                inheritedNames = c.isAssignableFrom(beanType);

                if (c == beanType)
                    beanName = name;
            }

            if (inheritedNames) {
                if (beanName != null)
                    return beanName;
            }
            
            final StringBuilder sb = new StringBuilder();
            sb.append("There are multiple beans configured in spring for the type ").
                    append(c.getName()).append(".");

            if (annotatedWithInject) {
                sb.append("\nYou should specify the name of the preferred bean with @InjectParam(\"name\") or @Inject(\"name\").");
            } else {
                sb.append("\nAnnotation information was not available, the reason might be because you're not using " +
                        "@InjectParam. You should use @InjectParam and specifiy the bean name via InjectParam(\"name\").");
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
