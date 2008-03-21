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

import com.sun.ws.rest.spi.resource.Injectable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ContextResolverFactory {
    private static final Logger LOGGER = Logger.getLogger(ContextResolverFactory.class.getName());
    
    private static final class ContextResolverInjectable 
            extends Injectable<Context, ContextResolver> {
        
        private final ContextResolver cr;
                
        ContextResolverInjectable(ContextResolver cr) {
            this.cr = cr;
        }
        
        public Class<Context> getAnnotationClass() {
            return Context.class;
        }

        public ContextResolver getInjectableValue(Context a) {
            return cr;
        }
    }
    
    private final Map<Type, Injectable<Context, ContextResolver>> injectables = 
                new HashMap<Type, Injectable<Context, ContextResolver>>();
    
    public ContextResolverFactory(ComponentProviderCache componentProviderCache) {        
        Set<ContextResolver> providers = 
                componentProviderCache.getProviders(ContextResolver.class);
        Map<ParameterizedType, List<ContextResolver<?>>> typeMap = 
                new HashMap<ParameterizedType, List<ContextResolver<?>>>();
        for (ContextResolver provider : providers) {
            Set<ParameterizedType> types = getTypes(provider.getClass());
            
            addTypes(typeMap, types, provider);
        }
        
        reduceToInjectables(typeMap);
    }

    
    public Map<Type, Injectable<Context, ContextResolver>> getInjectables() {
        return injectables;
    }
    
    
    private Set<ParameterizedType> getTypes(Class providerClass) {
        Set<ParameterizedType> types = new HashSet<ParameterizedType>();
        
        outer: while (providerClass != null) {
            for (Type type : providerClass.getGenericInterfaces()) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType)type;
                    if (pType.getRawType() == ContextResolver.class) {
                        // TODO check if type argument is concrete 
                        types.add(pType);
                        break outer;
                    }
                }
            }
            providerClass = providerClass.getSuperclass();
        }
        
        return types;
    }
    
    private void addTypes(
            Map<ParameterizedType, List<ContextResolver<?>>> typeMap,
            Set<ParameterizedType> types, 
            ContextResolver<?> provider) {
        for (ParameterizedType type : types) {
            List<ContextResolver<?>> l = typeMap.get(type);
            if (l == null) {
                l = new ArrayList<ContextResolver<?>>();
                typeMap.put(type, l);
            }
            l.add(provider);
        }
    }
    
    private void reduceToInjectables(Map<ParameterizedType, List<ContextResolver<?>>> typeMap) {
        for (Map.Entry<ParameterizedType, List<ContextResolver<?>>> e : typeMap.entrySet()) {            
            injectables.put(e.getKey(), 
                    new ContextResolverInjectable(reduce(e.getValue())));
        }
    }
    
    private static final class ContextResolverAdapter implements ContextResolver {

        private final List<ContextResolver<?>> crs;
        
        ContextResolverAdapter(List<ContextResolver<?>> crs) {
            this.crs = crs;
        }
        
        public Object getContext(Class objectType) {
            for (ContextResolver<?> cr : crs) {
                Object c = cr.getContext(objectType);
                if (c != null) return c;
            }
            return null;
        }        
    }
    
    private ContextResolver reduce(List<ContextResolver<?>> crs) {
        if (crs.size() == 1) {
            return crs.get(0);
        } else {
            return new ContextResolverAdapter(crs);                
        }        
    }
}