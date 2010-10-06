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

import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.spi.factory.InjectableProviderFactory;
import com.sun.jersey.spi.inject.ConstrainedTo;
import com.sun.jersey.spi.inject.ConstrainedToType;
import com.sun.jersey.spi.service.ServiceFinder;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Combines access to provider instances given a set of provider classes,
 * a set of provider instances and providers registered in META-INF/services.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ProviderServices {
    private static final Logger LOGGER = Logger.getLogger(ProviderServices.class.getName());

    private final Class<? extends ConstrainedToType> constraintToType;
    
    private final ProviderFactory componentProviderFactory;
    
    private final Set<Class<?>> providers;
    
    private final Set providerInstances;

    /**
     * Create the provider services.
     *
     * @param componentProviderFactory
     * @param providers
     * @param providerInstances
     */
    public ProviderServices(
            ProviderFactory componentProviderFactory,
            Set<Class<?>> providers,
            Set<?> providerInstances) {
        this(ConstrainedToType.class, componentProviderFactory, providers, providerInstances);
    }

    /**
     * Create the provider services.
     *
     * @param constraintToType 
     * @param componentProviderFactory
     * @param providers
     * @param providerInstances
     */
    public ProviderServices(
            Class<? extends ConstrainedToType> constraintToType,
            ProviderFactory componentProviderFactory,
            Set<Class<?>> providers,
            Set<?> providerInstances) {
        this.constraintToType = constraintToType;
        this.componentProviderFactory = componentProviderFactory;
        this.providers = providers;
        this.providerInstances = providerInstances;
    }

    public void update(Set<Class<?>> providers, Set<?> providerInstances, InjectableProviderFactory ipf) {
        final Set<Class<?>> addedProviders = diff(this.providers, providers);
        final Set<?> addedProviderInstances = diff(this.providerInstances, providerInstances);

        this.providers.clear();
        this.providers.addAll(providers);

        this.providerInstances.clear();
        this.providerInstances.addAll(providerInstances);

        final ProviderServices _ps = new ProviderServices(componentProviderFactory, addedProviders, addedProviderInstances);
        final InjectableProviderFactory _ipf = new InjectableProviderFactory();
        _ipf.configureProviders(_ps);
        ipf.update(_ipf);
    }

    private <T> Set<T> diff(Set<T> s1, Set<T> s2) {
        Set<T> diff = new LinkedHashSet<T>();

        for (T t : s1) {
            if (!s2.contains(t)) {
                diff.add(t);
            }
        }

        for (T t : s2) {
            if (!s1.contains(t)) {
                diff.add(t);
            }
        }

        return diff;
    }

    public ProviderFactory getComponentProviderFactory() {
        return componentProviderFactory;
    }
    
    public <T> Set<T> getProviders(Class<T> provider) {
        Set<T> ps = new LinkedHashSet<T>();
        ps.addAll(getProviderInstances(provider));
        for (Class pc : getProviderClasses(provider)) {
            Object o = getComponent(pc);
            if (o != null) {
                ps.add(provider.cast(o));
            }
        }

        return ps;
    }

    public <T> Set<T> getServices(Class<T> provider) {
        Set<T> ps = new LinkedHashSet<T>();
        for (ProviderClass pc : getServiceClasses(provider)) {
            Object o = getComponent(pc);
            if (o != null) {
                ps.add(provider.cast(o));
            }
        }

        return ps;
    }

    public <T> Set<T> getProvidersAndServices(Class<T> provider) {
        Set<T> ps = new LinkedHashSet<T>();
        ps.addAll(getProviderInstances(provider));
        for (ProviderClass pc : getProviderAndServiceClasses(provider)) {
            Object o = getComponent(pc);
            if (o != null) {
                ps.add(provider.cast(o));
            }
        }

        return ps;
    }

    public static interface ProviderListener<T> {
        void onAdd(T t);
    }
    
    public <T> void getProviders(Class<T> provider, ProviderListener listener) {
        for (T t : getProviderInstances(provider)) {
            listener.onAdd(t);
        }

        for (ProviderClass pc : getProviderOnlyClasses(provider)) {
            Object o = getComponent(pc);
            if (o != null) {
                listener.onAdd(provider.cast(o));
            }
        }
    }

    public <T> void getProvidersAndServices(Class<T> provider, ProviderListener listener) {
        for (T t : getProviderInstances(provider)) {
            listener.onAdd(t);
        }

        for (ProviderClass pc : getProviderAndServiceClasses(provider)) {
            Object o = getComponent(pc);
            if (o != null) {
                listener.onAdd(provider.cast(o));
            }
        }
    }

    public <T> List<T> getInstances(Class<T> provider, String[] classNames) {
        List<T> ps = new LinkedList<T>();
        for (String className : classNames) {
            try {
               Class<?> c = ReflectionHelper.classForNameWithException(className);
               if (provider.isAssignableFrom(c)) {
                   Object o = getComponent(c);
                   if (o != null)
                       ps.add(provider.cast(o));
               } else {
                   LOGGER.severe("The class " + 
                           className +
                           " is not assignable to the class " +
                           provider.getName() + 
                           ". This class is ignored.");
               }
            } catch (ClassNotFoundException e) {
               LOGGER.severe("The class " + 
                       className +
                       " could not be found" +
                       ". This class is ignored.");                
            }
        }
        
        return ps;
    }
        
    public <T> List<T> getInstances(Class<T> provider, Class<? extends T>[] classes) {
        List<T> ps = new LinkedList<T>();
        for (Class<? extends T> c : classes) {
           Object o = getComponent(c);
           if (o != null)
               ps.add(provider.cast(o));
        }

        return ps;
    }

    private Object getComponent(Class provider) {
        ComponentProvider cp = componentProviderFactory.getComponentProvider(provider);
        return (cp != null) ? cp.getInstance() : null;
    }
    
    private Object getComponent(ProviderClass provider) {
        ComponentProvider cp = componentProviderFactory.getComponentProvider(provider);
        return (cp != null) ? cp.getInstance() : null;
    }

    private <T> Set<T> getProviderInstances(Class<T> service) {
        Set<T> sp = new LinkedHashSet<T>();
        for (Object p : providerInstances) {
            if (service.isInstance(p) && constrainedTo(p.getClass()))
                sp.add(service.cast(p));
        }

        return sp;
    }

    private Set<Class> getProviderClasses(Class<?> service) {
        Set<Class> sp = new LinkedHashSet<Class>();
        for (Class p : providers) {
            if (service.isAssignableFrom(p) && constrainedTo(p))
                sp.add(p);
        }
        
        return sp;
    }

    public class ProviderClass {
        final boolean isServiceClass;
        final Class c;
        
        ProviderClass(Class c) {
            this.c = c;
            this.isServiceClass = false;
        }

        ProviderClass(Class c, boolean isServiceClass) {
            this.c = c;
            this.isServiceClass = isServiceClass;
        }
    }

    private Set<ProviderClass> getProviderAndServiceClasses(Class<?> service) {
        Set<ProviderClass> sp = getProviderOnlyClasses(service);
        getServiceClasses(service, sp);
        return sp;
    }    
    
    private Set<ProviderClass> getProviderOnlyClasses(Class<?> service) {
        Set<ProviderClass> sp = new LinkedHashSet<ProviderClass>();
        for(Class c : getProviderClasses(service)) {
            sp.add(new ProviderClass(c));
        }
        return sp;
    }

    private Set<ProviderClass> getServiceClasses(Class<?> service) {
        Set<ProviderClass> sp = new LinkedHashSet<ProviderClass>();
        getServiceClasses(service, sp);
        return sp;
    }    
    
    private void getServiceClasses(Class<?> service, Set<ProviderClass> sp) {
        // Get the service-defined provider classes that implement serviceClass
        LOGGER.log(Level.CONFIG, "Searching for providers that implement: " + service);
        Class<?>[] pca = ServiceFinder.find(service, true).toClassArray();
        for (Class pc : pca) {
            if (constrainedTo(pc)) {
                LOGGER.log(Level.CONFIG, "    Provider found: " + pc);
            }
        }
        // Add service-defined providers to the set after application-defined
        for (Class pc : pca) {
            if (constrainedTo(pc)) {
                if(service.isAssignableFrom(pc)) {
                    sp.add(new ProviderClass(pc, true));
                } else {
                    LOGGER.log(Level.CONFIG, "Provider " + pc.getName() +
                            " won't be used because its not assignable to " +
                            service.getName() + ". This might be caused by clashing " +
                            "container-provided and application-bundled Jersey classes.");
                }
            }
        }
    }

    private boolean constrainedTo(Class<?> p) {
        final ConstrainedTo ct = p.getAnnotation(ConstrainedTo.class);
        return (ct != null)
                ? ct.value().isAssignableFrom(constraintToType)
                : true;
    }
}