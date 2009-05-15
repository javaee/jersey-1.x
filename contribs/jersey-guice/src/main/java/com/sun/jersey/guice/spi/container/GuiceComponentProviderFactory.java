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
package com.sun.jersey.guice.spi.container;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.spi.BindingScopingVisitor;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Guice-based {@link IoCComponentProviderFactory}.
 *
 * @author Gili Tzabari
 * @author Paul Sandoz
 */
public class GuiceComponentProviderFactory implements IoCComponentProviderFactory {

    private static final Logger LOGGER =
            Logger.getLogger(GuiceComponentProviderFactory.class.getName());

    private final Map<Scope, ComponentScope> scopeMap = createScopeMap();

    private final Injector injector;

    /**
     * Creates a new GuiceComponentProviderFactory.
     *
     * @param config the resource configuration
     * @param injector the Guice injector
     */
    public GuiceComponentProviderFactory(ResourceConfig config, Injector injector) {
        this.injector = injector;
        register(config, injector);
    }

    /**
     * Registers any Guice-bound providers or root resources.
     *
     * @param config the resource config
     * @param injector the Guice injector
     */
    private void register(ResourceConfig config, Injector injector) {
        while (injector != null) {
            for (Key<?> key : injector.getBindings().keySet()) {
                Type type = key.getTypeLiteral().getType();
                if (type instanceof Class) {
                    Class<?> c = (Class) type;
                    if (ResourceConfig.isProviderClass(c)) {
                        LOGGER.info("Registering " + c.getName() + " as a provider class");
                        config.getClasses().add(c);
                    } else if (ResourceConfig.isRootResourceClass(c)) {
                        LOGGER.info("Registering " + c.getName() + " as a root resource class");
                        config.getClasses().add(c);
                    }

                }
            }
            injector = injector.getParent();
        }
    }

    public IoCComponentProvider getComponentProvider(Class c) {
        return getComponentProvider(null, c);
    }

    public IoCComponentProvider getComponentProvider(ComponentContext cc, Class clazz) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("getComponentProvider(" + clazz.getName() + ")");
        }

        Key<?> key = Key.get(clazz);
        Injector i = findInjector(key);
        // If there is no explicit binding
        if (i == null) {
            // If an @Inject is explicitly declared
            if (!isImplicitGuiceComponent(clazz)) {
                return null;
            }

            try {
                // If a binding is possible
                if (injector.getBinding(key) != null) {
                    LOGGER.info("Binding " + clazz.getName() + " to GuiceInstantiatedComponentProvider");
                    return new GuiceInstantiatedComponentProvider(injector, clazz);
                }
            } catch (ConfigurationException e) {
                // The class cannot be injected. 
                // For example, the constructor might contain parameters that
                // cannot be injected
                LOGGER.log(Level.INFO, "Cannot bind " + clazz.getName(), e);
                // Guice should have picked this up. We fail-fast to prevent 
                // Jersey from trying to handle injection.
                throw e;
            }

        }

        final Scope[] scope = new Scope[1];
        i.getBinding(key).acceptScopingVisitor(new BindingScopingVisitor<Void>() {

            public Void visitEagerSingleton() {
                scope[0] = Scopes.SINGLETON;
                return null;
            }

            public Void visitScope(Scope theScope) {
                scope[0] = theScope;
                return null;
            }

            public Void visitScopeAnnotation(Class scopeAnnotation) {
                // This method is not invoked for Injector bindings
                throw new UnsupportedOperationException();
            }

            public Void visitNoScoping() {
                scope[0] = Scopes.NO_SCOPE;
                return null;
            }
        });
        assert (scope[0] != null);

        ComponentScope componentScope = getComponentScope(scope[0]);
        LOGGER.info("Binding " + clazz.getName() +
                " to GuiceManagedComponentProvider with the scope \"" +
                componentScope + "\"");
        return new GuiceManagedComponentProvider(i, componentScope, clazz);
    }

    private Injector findInjector(Key<?> key) {
        Injector i = injector;
        while (i != null) {
            if (i.getBindings().containsKey(key))
                return i;

            i = i.getParent();
        }
        return null;
    }

    /**
     * Converts a Guice scope to Jersey scope.
     *
     * @param scope the guice scope
     * @return the Jersey scope
     */
    private ComponentScope getComponentScope(Scope scope) {
        ComponentScope cs = scopeMap.get(scope);
        return (cs != null) ? cs : ComponentScope.Undefined;
    }

    /**
     * Determine if a class is an implicit Guice component that can be
     * instatiated by Guice and the life-cycle managed by Jersey.
     * 
     * @param c the class.
     * @return true if the class is an implicit Guice component.
     */
    public boolean isImplicitGuiceComponent(Class<?> c) {
        for (Constructor<?> con : c.getConstructors()) {
            if (con.isAnnotationPresent(Inject.class))
                return true;
        }

        return false;
    }

    /**
     * Maps a Guice scope to a Jersey scope.
     *
     * @return the map
     */
    public Map<Scope, ComponentScope> createScopeMap() {
        Map<Scope, ComponentScope> result = new HashMap<Scope, ComponentScope>();
        result.put(Scopes.SINGLETON, ComponentScope.Singleton);
        result.put(Scopes.NO_SCOPE, ComponentScope.PerRequest);
        return result;
    }

    /**
     * Guice injects instances while Jersey manages their scope.
     *
     * @author Gili Tzabari
     */
    private static class GuiceInstantiatedComponentProvider
            implements IoCInstantiatedComponentProvider {

        private final Injector injector;
        private final Class<?> clazz;

        /**
         * Creates a new GuiceManagedComponentProvider.
         *
         * @param injector the injector
         * @param clazz the class
         */
        public GuiceInstantiatedComponentProvider(Injector injector, Class<?> clazz) {
            this.injector = injector;
            this.clazz = clazz;
        }

        public Class<?> getInjectableClass(Class<?> c) {
            return c.getSuperclass();
        }

        // IoCInstantiatedComponentProvider
        public Object getInjectableInstance(Object o) {
            return o;
        }

        public Object getInstance() {
            return injector.getInstance(clazz);
        }
    }

    /**
     * Guice injects instances and manages their scope.
     *
     * @author Gili Tzabari
     */
    private static class GuiceManagedComponentProvider extends GuiceInstantiatedComponentProvider
            implements IoCManagedComponentProvider {

        private final ComponentScope scope;

        /**
         * Creates a new GuiceManagedComponentProvider.
         *
         * @param injector the injector
         * @param scope the Jersey scope
         * @param clazz the class
         */
        public GuiceManagedComponentProvider(Injector injector, ComponentScope scope, Class<?> clazz) {
            super(injector, clazz);
            this.scope = scope;
        }

        public ComponentScope getScope() {
            return scope;
        }
    }
}
