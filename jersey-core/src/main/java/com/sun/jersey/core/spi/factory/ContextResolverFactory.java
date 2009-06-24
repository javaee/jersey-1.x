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
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.reflection.ReflectionHelper.DeclaringClassInterfacePair;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.core.util.KeyComparatorHashMap;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<Type, Map<MediaType, ContextResolver>> resolver;
    
    private final Map<Type, ConcurrentHashMap<MediaType, ContextResolver>> cache;

    public ContextResolverFactory(ProviderServices providersServices,
            InjectableProviderFactory ipf) {
        Map<Type, Map<MediaType, List<ContextResolver>>> rs =
                new HashMap<Type, Map<MediaType, List<ContextResolver>>>();
        
        Set<ContextResolver> providers = 
                providersServices.getProviders(ContextResolver.class);
        for (ContextResolver provider : providers) {
            List<MediaType> ms = MediaTypes.createMediaTypes(
                    provider.getClass().getAnnotation(Produces.class));

            Type type = getParameterizedType(provider.getClass());
            
            Map<MediaType, List<ContextResolver>> mr = rs.get(type);
            if (mr == null) {
                mr = new HashMap<MediaType, List<ContextResolver>>();
                rs.put(type, mr);
            }
            for (MediaType m : ms) {
                List<ContextResolver> crl = mr.get(m);
                if (crl == null) {
                    crl = new ArrayList<ContextResolver>();
                    mr.put(m, crl);
                }
                crl.add(provider);
            }
        }
        
        // Reduce set of two or more context resolvers for same type and
        // media type
        
        this.resolver = new HashMap<Type, Map<MediaType, ContextResolver>>(4);
        this.cache = new HashMap<Type, ConcurrentHashMap<MediaType, ContextResolver>>(4);
        for (Map.Entry<Type, Map<MediaType, List<ContextResolver>>> e : rs.entrySet()) {
            Map<MediaType, ContextResolver> mr = new KeyComparatorHashMap<MediaType, ContextResolver>(
                    4, MessageBodyFactory.MEDIA_TYPE_COMPARATOR);
            resolver.put(e.getKey(), mr);

            cache.put(e.getKey(), new ConcurrentHashMap<MediaType, ContextResolver>(4));

            for (Map.Entry<MediaType, List<ContextResolver>> f : e.getValue().entrySet()) {
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
                Map<MediaType, ContextResolver> x = resolver.get(type);
                if (x == null)
                    return null;
                
                List<MediaType> ms = getMediaTypes(ic);
                if (ms.size() == 1) {
                    return resolve(type, ms.get(0));
                } else {                    
                    Set<MediaType> ml = new TreeSet<MediaType>(MediaTypes.MEDIA_TYPE_COMPARATOR);
                    for (MediaType m : ms) {
                        if (m.isWildcardType()) {
                            ml.add(MediaTypes.GENERAL_MEDIA_TYPE);
                        } else if (m.isWildcardSubtype()) {
                            ml.add(new MediaType(m.getType(), "*"));
                            ml.add(MediaTypes.GENERAL_MEDIA_TYPE);
                        } else {
                            ml.add(new MediaType(m.getType(), m.getSubtype()));
                            ml.add(new MediaType(m.getType(), "*"));
                            ml.add(MediaTypes.GENERAL_MEDIA_TYPE);                        }
                    }

                    List<ContextResolver> crl = new ArrayList<ContextResolver>(ml.size());
                    for (MediaType m : ms) {
                        ContextResolver cr = x.get(m);
                        if (cr != null) crl.add(cr);
                    }
                    if (crl.isEmpty())
                        return null;

                    return new ContextResolverAdapter(crl);
                }        
            }
            
            List<MediaType> getMediaTypes(ComponentContext ic) {
                Produces p = null;
                for (Annotation a : ic.getAnnotations()) {
                    if (a instanceof Produces) {
                        p = (Produces)a;
                        break;
                    }
                }

                return MediaTypes.createMediaTypes(p);
            }
        });
    }

    private Type getParameterizedType(Class c) {
        DeclaringClassInterfacePair p = ReflectionHelper.getClass(
                c, ContextResolver.class);

        Type[] as = ReflectionHelper.getParameterizedTypeArguments(p);

        return (as != null) ? as[0] : Object.class;
    }

    private static final NullContextResolverAdapter NULL_CONTEXT_RESOLVER =
            new NullContextResolverAdapter();

    private static final class NullContextResolverAdapter implements ContextResolver {
        public Object getContext(Class type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static final class ContextResolverAdapter implements ContextResolver {
        
        private final ContextResolver[] cra;
        
        ContextResolverAdapter(ContextResolver... cra) {
            this(removeNull(cra));
        }

        ContextResolverAdapter(List<ContextResolver> crl) {
            this.cra = crl.toArray(new ContextResolver[crl.size()]);
        }
        
        public Object getContext(Class objectType) {
            for (ContextResolver cr : cra) {
                Object c = cr.getContext(objectType);
                if (c != null) return c;
            }
            return null;
        }

        ContextResolver reduce() {
            if (cra.length == 0) {
                return NULL_CONTEXT_RESOLVER;
            } if (cra.length == 1) {
                return cra[0];
            } else {
                return this;
            }
        }
        
        private static List<ContextResolver> removeNull(ContextResolver... cra) {
            List<ContextResolver> crl = new ArrayList<ContextResolver>(cra.length);
            for (ContextResolver cr : cra) {
                if (cr != null) {
                    crl.add(cr);
                }
            }
            return crl;
        }
    }
    
    private ContextResolver reduce(List<ContextResolver> r) {
        if (r.size() == 1) {
            return r.iterator().next();
        } else {
            return new ContextResolverAdapter(r);                
        }                
    }
    
    public <T> ContextResolver<T> resolve(Type t, MediaType m) {
        final ConcurrentHashMap<MediaType, ContextResolver> crMapCache = cache.get(t);
        if (crMapCache == null) return null;

        if (m == null)
            m = MediaTypes.GENERAL_MEDIA_TYPE;

        ContextResolver<T> cr = crMapCache.get(m);
        if (cr == null) {
            final Map<MediaType, ContextResolver> crMap = resolver.get(t);

            if (m.isWildcardType()) {
                cr = crMap.get(MediaTypes.GENERAL_MEDIA_TYPE);
                if (cr == null) {
                    cr = NULL_CONTEXT_RESOLVER;
                }
            } else if (m.isWildcardSubtype()) {
                // Include x, x/* and */*
                final ContextResolver<T> subTypeWildCard = crMap.get(m);
                final ContextResolver<T> wildCard = crMap.get(MediaTypes.GENERAL_MEDIA_TYPE);

                cr = new ContextResolverAdapter(subTypeWildCard, wildCard).reduce();
            } else {
                // Include x, x/* and */*
                final ContextResolver<T> type = crMap.get(m);
                final ContextResolver<T> subTypeWildCard = crMap.get(new MediaType(m.getType(), "*"));
                final ContextResolver<T> wildCard = crMap.get(MediaType.WILDCARD_TYPE);

                cr = new ContextResolverAdapter(type, subTypeWildCard, wildCard).reduce();
            }

            crMapCache.putIfAbsent(m, cr);
        }

        return (cr != NULL_CONTEXT_RESOLVER) ? cr : null;
    }
}