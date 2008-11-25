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
package com.sun.jersey.core.spi.factory;

import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.core.util.KeyComparator;
import com.sun.jersey.core.util.KeyComparatorHashMap;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;

/**
 * A factory for managing {@link ContextResolver} instances.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ContextResolverFactory {
    private static final KeyComparator<MediaType> MEDIA_TYPE_COMPARATOR = 
            new KeyComparator<MediaType>() {
        public boolean equals(MediaType x, MediaType y) {
            return x.getType().equalsIgnoreCase(y.getType())
                    && x.getSubtype().equalsIgnoreCase(y.getSubtype());
        }

        public int hash(MediaType k) {
            return k.getType().toLowerCase().hashCode() + 
                    k.getSubtype().toLowerCase().hashCode();
        }

        public int compare(MediaType o1, MediaType o2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }        
    };
    
    private final Map<Type, Map<MediaType, ContextResolver>> resolver;
    
    public ContextResolverFactory(ProviderServices providersServices,
            InjectableProviderFactory ipf) {
        Map<Type, Map<MediaType, Set<ContextResolver>>> rs = 
                new HashMap<Type, Map<MediaType, Set<ContextResolver>>>();
        
        Set<ContextResolver> providers = 
                providersServices.getProviders(ContextResolver.class);
        for (ContextResolver provider : providers) {
            MediaType[] ms = getAnnotationValues(provider.getClass(), Produces.class);
            ParameterizedType pType = getType(provider.getClass());
            Type type = pType.getActualTypeArguments()[0];
            // TODO check if concrete type
            
            Map<MediaType, Set<ContextResolver>> mr = rs.get(type);
            if (mr == null) {
                mr = new HashMap<MediaType, Set<ContextResolver>>();
                rs.put(type, mr);
            }
            for (MediaType m : ms) {
                Set<ContextResolver> sr = mr.get(m);
                if (sr == null) {
                    sr = new HashSet<ContextResolver>();
                    mr.put(m, sr);
                }
                sr.add(provider);                
            }
        }
        
        // Reduce set of two or more context resolvers for same type and
        // media type
        
        this.resolver = new HashMap<Type, Map<MediaType, ContextResolver>>(4);
        for (Map.Entry<Type, Map<MediaType, Set<ContextResolver>>> e : rs.entrySet()) {
            Map<MediaType, ContextResolver> mr = new KeyComparatorHashMap<MediaType, ContextResolver>(
                    4, MEDIA_TYPE_COMPARATOR);
            resolver.put(e.getKey(), mr);
            
            for (Map.Entry<MediaType, Set<ContextResolver>> f : e.getValue().entrySet()) {
                mr.put(f.getKey(), reduce(f.getValue()));
            }
        }
        
        // Add injectable
        
        ipf.add(new InjectableProvider<Context, Type>() {
            public ComponentScope getScope() {
                return ComponentScope.Singleton;
            }

            public Injectable getInjectable(ComponentContext ic, Context ac, Type c) {
                if (!(c instanceof ParameterizedType))
                    return null;                
                ParameterizedType pType = (ParameterizedType)c;
                if (pType.getRawType() != ContextResolver.class)
                    return null;
                Type type = pType.getActualTypeArguments()[0];
                // TODO check if concrete type
                
                final ContextResolver cr = getResolver(ic, type);
                if (cr == null) return null;
                
                return new Injectable() {
                    public Object getValue() {
                        return cr;
                    }
                };
            }
            
            ContextResolver getResolver(ComponentContext ic, Type type) {
                ContextResolver cr = null;
                MediaType[] ms = getMediaTypes(ic);
                if (ms.length == 1) {
                    cr = resolve(type, ms[0]);
                    if (cr == null)
                        return null;
                } else {
                    Set<ContextResolver> scr = new HashSet<ContextResolver>();
                    for (MediaType m : ms) {
                        cr = resolve(type, ms[0]);
                        if (cr != null) scr.add(cr);
                    }
                    if (scr.isEmpty())
                        return null;
                    cr = new ContextResolverAdapter(scr);
                }        
                return cr;
            }
            
            MediaType[] getMediaTypes(ComponentContext ic) {
                String[] mts = null;
                for (Annotation a : ic.getAnnotations()) {
                    if (a instanceof Produces) {
                        Produces p = (Produces)a;
                        mts = p.value();
                    }
                }
                
                MediaType[] mt = null;
                if (mts == null) {
                    mt = new MediaType[1];
                    mt[0] = MediaTypes.GENERAL_ACCEPT_MEDIA_TYPE;
                } else {
                    mt = new MediaType[mts.length];
                    for (int i = 0; i < mts.length; i++)
                        mt[i] = MediaType.valueOf(mts[i]);
                }
                return mt;
            }            
        });
    }
    
    private ParameterizedType getType(Class providerClass) {
        while (providerClass != null) {
            for (Type type : providerClass.getGenericInterfaces()) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType)type;
                    if (pType.getRawType() == ContextResolver.class) {
                        return pType;
                    }
                }
            }
            providerClass = providerClass.getSuperclass();
        }
        
        throw new IllegalArgumentException();
    }
    
    private MediaType[] getAnnotationValues(Class<?> clazz, Class<?> annotationClass) {
        String[] mts = _getAnnotationValues(clazz, annotationClass);
        if (mts == null) {
            MediaType[] mt = new MediaType[1];
            mt[0] = MediaTypes.GENERAL_ACCEPT_MEDIA_TYPE;
            return mt;
        }
        
        MediaType[] mt = new MediaType[mts.length];
        for (int i = 0; i < mts.length; i++)
            mt[i] = MediaType.valueOf(mts[i]);
        
        return mt;
    }
    
    private String[] _getAnnotationValues(Class<?> clazz, Class<?> annotationClass) {
        String values[] = null;
        if (annotationClass.equals(Consumes.class)) {
            Consumes consumes = clazz.getAnnotation(Consumes.class);
            if (consumes != null)
                values = consumes.value();
        } else if (annotationClass.equals(Produces.class)) {
            Produces produces = clazz.getAnnotation(Produces.class);
            if (produces != null)
                values = produces.value();
        }
        return values;
    }
    
    private static final class ContextResolverAdapter implements ContextResolver {
        private final Set<ContextResolver> crs;
        
        ContextResolverAdapter(Set<ContextResolver> crs) {
            this.crs = crs;
        }
        
        public Object getContext(Class objectType) {
            for (ContextResolver cr : crs) {
                Object c = cr.getContext(objectType);
                if (c != null) return c;
            }
            return null;
        }        
    }
    
    private ContextResolver reduce(Set<ContextResolver> r) {
        if (r.size() == 1) {
            return r.iterator().next();
        } else {
            return new ContextResolverAdapter(r);                
        }                
    }

    
    public <T> ContextResolver<T> resolve(Type t, MediaType m) {
        Map<MediaType, ContextResolver> x = resolver.get(t);
        if (x == null) return null;
        if (m == null) m = MediaTypes.GENERAL_ACCEPT_MEDIA_TYPE;
        return x.get(m);
    }    
}