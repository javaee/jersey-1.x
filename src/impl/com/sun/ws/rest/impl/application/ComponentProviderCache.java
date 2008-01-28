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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
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
    
    public Object getComponent(Class provider) {
        Object o = cache.get(provider);
        if (o != null) return o;
            
        try {
            o = componentProvider.provide(provider);
        } catch (Exception ex) {
            // TODO message
            throw new IllegalArgumentException("The provide class, " + provider + 
                    ", could not be instantiated", 
                    ex);
        }
        
        cache.put(provider, o);
        return o;
    }
}