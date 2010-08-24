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
package com.sun.jersey.core.spi.component;

import com.sun.jersey.spi.inject.InjectableProviderContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A component provider factory for provider components (which are singletons).
 * <p>
 * A cache of component providers is managed. When a component provider for
 * a class is obtained it is cached such that the same instance on subsequent
 * requests.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ProviderFactory implements ComponentProviderFactory<ComponentProvider> {
    protected static final Logger LOGGER = Logger.getLogger(ProviderFactory.class.getName());

    protected interface Destroyable {
        void destroy();
    }

    private static final class SingletonComponentProvider implements ComponentProvider, Destroyable {
        private final Object o;

        private final ComponentDestructor cd;

        private final ComponentInjector ci;

        SingletonComponentProvider(ComponentInjector ci, Object o) {
            this.cd = new ComponentDestructor(o.getClass());
            this.ci = ci;
            this.o = o;
        }
        
        public Object getInstance() {
            return o;
        }

        public void inject() {
            ci.inject(o);
        }

        public void destroy() {
            try {
                cd.destroy(o);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
            }
        }
    }
    
    private final Map<Class, ComponentProvider> cache =
            new HashMap<Class, ComponentProvider>();

    private final InjectableProviderContext ipc;

    /**
     * Create the provider factory.
     * 
     * @param ipc the injectable provider context to get injectables.
     */
    public ProviderFactory(InjectableProviderContext ipc) {
        this.ipc = ipc;
    }

    /**
     * Get the injectable provider context.
     *
     * @return the injectable provider context
     */
    public InjectableProviderContext getInjectableProviderContext() {
        return ipc;
    }

    /**
     * Get a component provider for a class.
     *
     * @param pc the provider class.
     * @return the component provider.
     */
    public final ComponentProvider getComponentProvider(ProviderServices.ProviderClass pc) {
        if (!pc.isServiceClass) {
            return getComponentProvider(pc.c);
        }

        ComponentProvider cp = cache.get(pc.c);
        if (cp != null) return cp;

        cp = __getComponentProvider(pc.c);
        
        if (cp != null) cache.put(pc.c, cp);
        return cp;
    }

    /**
     * Get a component provider for a class.
     * 
     * @param c the class.
     * @return the component provider.
     */
    public final ComponentProvider getComponentProvider(Class c) {
        ComponentProvider cp = cache.get(c);
        if (cp != null) return cp;

        cp = _getComponentProvider(c);
        if (cp != null) cache.put(c, cp);
        return cp;
    }

    protected ComponentProvider _getComponentProvider(Class c) {
        return __getComponentProvider(c);
    }
    
    private ComponentProvider __getComponentProvider(Class c) {
        try {
            ComponentInjector ci = new ComponentInjector(ipc, c);
            ComponentConstructor cc = new ComponentConstructor(ipc, c, ci);
            Object o = cc.getInstance();

            return new SingletonComponentProvider(ci, o);
        } catch (NoClassDefFoundError ex) {
            // Dependent class of provider not found
            // This assumes that ex.getLocalizedMessage() returns
            // the name of a dependent class that is not found
            LOGGER.log(Level.CONFIG,
                    "A dependent class, " + ex.getLocalizedMessage() +
                    ", of the component " + c + " is not found." +
                    " The component is ignored.");
            return null;
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof NoClassDefFoundError) {
                NoClassDefFoundError ncdf = (NoClassDefFoundError)ex.getCause();
                LOGGER.log(Level.CONFIG,
                        "A dependent class, " + ncdf.getLocalizedMessage() +
                        ", of the component " + c + " is not found." +
                        " The component is ignored.");
                return null;
            } else {
                LOGGER.log(Level.SEVERE,
                        "The provider class, " + c +
                        ", could not be instantiated. Processing will continue but the class will not be utilized", ex.getTargetException());
                return null;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE,
                    "The provider class, " + c +
                    ", could not be instantiated. Processing will continue but the class will not be utilized", ex);
            return null;
        }
    }

    /**
     * Inject on all cached components.
     */
    public void injectOnAllComponents() {
        for (ComponentProvider cp : cache.values()) {
            if (cp instanceof SingletonComponentProvider) {
                SingletonComponentProvider scp = (SingletonComponentProvider)cp;
                scp.inject();
            }
        }
    }

    /**
     * Destroy all cached components.
     */
    public void destroy() {
        for (ComponentProvider cp : cache.values()) {
            if (cp instanceof Destroyable) {
                Destroyable d = (Destroyable)cp;
                d.destroy();
            }
        }
    }

    /**
     * Inject on a collection of providers.
     * 
     * @param providers the collection of providers.
     */
    public void injectOnProviderInstances(Collection<?> providers) {
        for (Object o : providers) {
            injectOnProviderInstance(o);
        }
    }

    /**
     * Inject on a provider.
     *
     * @param provider the provider.
     */
    public void injectOnProviderInstance(Object provider) {
        Class c = provider.getClass();
        ComponentInjector ci = new ComponentInjector(ipc, c);
        ci.inject(provider);
    }

}