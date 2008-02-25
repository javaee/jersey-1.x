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

package com.sun.ws.rest.impl.application;

import com.sun.ws.rest.spi.service.ComponentProvider;
import com.sun.ws.rest.spi.service.ComponentProvider.Scope;
import java.util.HashMap;
import java.util.HashSet;
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
public class ComponentProviderCache {
    private static final Logger LOGGER = Logger.getLogger(ComponentProviderCache.class.getName());
    
    private final ComponentProvider componentProvider;
    
    private final Set<Class> providers;
    
    private final Map<Class, Object> cache;
    
    public ComponentProviderCache(ComponentProvider componentProvider, 
            Set<Class> providers) {
        this.componentProvider = componentProvider;
        this.providers = providers;
        this.cache = new HashMap<Class, Object>();
    }
    
    public Set<Class> getProviderClasses(Class<?> service) {
        Set<Class> sp = new HashSet<Class>();
        for (Class p : providers) {
            if (service.isAssignableFrom(p))
                sp.add(p);
        }
        
        return sp;
    }
    
    public <T> Set<T> getProviders(Class<T> provider) {
        Set<Class> pcs = new HashSet<Class>();
        for (Class p : providers) {
            if (provider.isAssignableFrom(p))
                pcs.add(p);
        }
        
        Set<T> ps = new HashSet<T>();
        for (Class pc : pcs) {
            Object o = getComponent(pc);
            if (o != null) {
                ps.add(provider.cast(o));
            }
        }
        
        return ps;
    }
    
    public Object getComponent(Class provider) {
        Object o = cache.get(provider);
        if (o != null) return o;
            
        try {
            o = componentProvider.getInstance(Scope.WebApplication, provider);
        } catch (NoClassDefFoundError ex) {
            // Dependent class of provider not found
            if(LOGGER.isLoggable(Level.WARNING)) {
                // This assumes that ex.getLocalizedMessage() returns
                // the name of a dependent class that is not found
//                LOGGER.log(Level.WARNING, 
//                        SpiMessages.DEPENDENT_CLASS_OF_PROVIDER_NOT_FOUND(
//                        ex.getLocalizedMessage(), nextName, service));
            }
            LOGGER.log(Level.WARNING,
                    "The provider class, " + provider + 
                    ", could not be instantiated");
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,
                    "The provider class, " + provider + 
                    ", could not be instantiated");
            return null;
        }
        
        cache.put(provider, o);
        return o;
    }
}