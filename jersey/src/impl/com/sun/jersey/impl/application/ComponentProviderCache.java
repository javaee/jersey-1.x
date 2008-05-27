/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.jersey.impl.application;

import com.sun.jersey.spi.service.ComponentProvider;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import com.sun.jersey.spi.service.ServiceFinder;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * TODO also use service finder and combine.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class ComponentProviderCache {
    private static final Logger LOGGER = Logger.getLogger(ComponentProviderCache.class.getName());
    
    private final ComponentProvider componentProvider;
    
    private final Set<Class<?>> providers;
    
    private final Map<Class, Object> cache;
    
    public ComponentProviderCache(ComponentProvider componentProvider, 
            Set<Class<?>> providers) {
        this.componentProvider = componentProvider;
        this.providers = providers;
        this.cache = new HashMap<Class, Object>();
    }
    
    public <T> Set<T> getProviders(Class<T> provider) {
        Set<T> ps = new LinkedHashSet<T>();
        for (Class pc : getProviderClasses(provider)) {
            Object o = getComponent(pc);
            if (o != null) {
                ps.add(provider.cast(o));
            }
        }
        
        return ps;
    }
    
    public <T> Set<T> getProvidersAndServices(Class<T> provider) {
        Set<T> ps = new LinkedHashSet<T>();
        for (Class pc : getProviderAndServiceClasses(provider)) {
            Object o = getComponent(pc);
            if (o != null) {
                ps.add(provider.cast(o));
            }
        }
        
        return ps;        
    }
    
    private Object getComponent(Class<?> provider) {
        Object o = cache.get(provider);
        if (o != null) return o;
            
        try {
            o = componentProvider.getInstance(Scope.Singleton, provider);
        } catch (NoClassDefFoundError ex) {
            // Dependent class of provider not found
            // This assumes that ex.getLocalizedMessage() returns
            // the name of a dependent class that is not found
            LOGGER.log(Level.CONFIG,
                    "A dependent class, " + ex.getLocalizedMessage() + 
                    ", of the component " + provider + " is not found." +
                    " The component is ignored.");
            return null;
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            LOGGER.log(Level.CONFIG,
                    "The provider class, " + provider + 
                    ", could not be instantiated");
            return null;
        }
        
        cache.put(provider, o);
        return o;
    }
    
    private Set<Class> getProviderClasses(Class<?> service) {
        Set<Class> sp = new LinkedHashSet<Class>();
        for (Class p : providers) {
            if (service.isAssignableFrom(p))
                sp.add(p);
        }
        
        return sp;
    }
    
    private Set<Class> getProviderAndServiceClasses(Class<?> service) {
        Set<Class> sp = new LinkedHashSet<Class>(getProviderClasses(service)); 
        
        // Get the service-defined provider classes that implement serviceClass
        LOGGER.log(Level.CONFIG, "Searching for providers that implement: " + service);
        Class<?>[] pca = ServiceFinder.find(service, true).toClassArray();
        for (Class pc : pca)
            LOGGER.log(Level.CONFIG, "    Provider found: " + pc);
        
        // Add service-defined providers to the set after application-defined
        for (Class pc : pca)
            sp.add(pc);
        
        return sp;
    }    
}