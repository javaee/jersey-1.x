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

package com.sun.jersey.impl.application;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
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
    
    private static final class ContextResolverInjectableProvider
            extends SingletonTypeInjectableProvider<Context, ContextResolver> {
        ContextResolverInjectableProvider(Type t, ContextResolver instance) {
            super(t, instance);
        }
    }
    
    private final List<ContextResolverInjectableProvider> injectables = 
            new ArrayList<ContextResolverInjectableProvider>();
    
    public ContextResolverFactory(ComponentProviderCache componentProviderCache,
            InjectableProviderFactory ipf) {        
        Set<ContextResolver> providers = 
                componentProviderCache.getProviders(ContextResolver.class);
        Map<ParameterizedType, List<ContextResolver<?>>> typeMap = 
                new HashMap<ParameterizedType, List<ContextResolver<?>>>();
        for (ContextResolver provider : providers) {
            Set<ParameterizedType> types = getTypes(provider.getClass());
            
            addTypes(typeMap, types, provider);
        }
        
        reduceToInjectables(typeMap);
        
        for (ContextResolverInjectableProvider i : injectables) {
            ipf.add(i);
        }
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
            injectables.add(
                    new ContextResolverInjectableProvider(e.getKey(), reduce(e.getValue())));
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