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

import com.sun.jersey.spi.resource.InjectableProviderContext;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.model.ReflectionHelper;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableContext;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class InjectableProviderFactory implements InjectableProviderContext {
    private static final class MetaInjectableProvider {
        final InjectableProvider ip;
        final Class<? extends Annotation> ac;
        final Class<?> cc;
        
        MetaInjectableProvider(
                InjectableProvider ip,
                Class<? extends Annotation> ac, 
                Class<?> cc) {
            this.ip = ip;
            this.ac = ac;
            this.cc = cc;
        }
    }
    
    private Map<Class<? extends Annotation>, LinkedList<MetaInjectableProvider>> ipm = 
            new HashMap<Class<? extends Annotation>, LinkedList<MetaInjectableProvider>>();
        
    @SuppressWarnings("unchecked")
    public void add(InjectableProvider ip) {
        Type[] args = getMetaArguments(ip.getClass());
        if (args != null) {
            MetaInjectableProvider mip = new MetaInjectableProvider(ip, 
                    (Class)args[0], (Class)args[1]);
            
            // TODO change to add first
            getList(mip.ac).add(mip);
        } else {
            // TODO throw exception or log error            
        }
    }

    public void configure(ComponentProviderCache componentProviderCache) {
        for (InjectableProvider ip : 
            componentProviderCache.getProvidersAndServices(InjectableProvider.class)) {
            add(ip);
        }
    }
    
    private LinkedList<MetaInjectableProvider> getList(Class<? extends Annotation> c) {
        LinkedList<MetaInjectableProvider> l = ipm.get(c);
        if (l == null) {
            l = new LinkedList<MetaInjectableProvider>();
            ipm.put(c, l);
        }
        return l;
    }
    
    private Type[] getMetaArguments(Class<?> c) {
        Class _c = c;
        while (_c != Object.class) {
            Type[] ts = _c.getGenericInterfaces();
            for (Type t : ts) {
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)t;
                    if (pt.getRawType() == InjectableProvider.class) {
                        Type[] args = pt.getActualTypeArguments();
                        for (int i = 0; i < args.length; i++)
                            args[i] = getResolvedType(args[i], c, _c);
                            
                        if (args[0] instanceof Class &&
                                args[1] instanceof Class &&
                                (args[1] == Type.class || args[1] == Parameter.class))
                            return args;
                    }
                }
            }
            
            _c = _c.getSuperclass();
        }
        
        return null;        
    }
    
    private Type getResolvedType(Type t, Class c, Class dc) {
        if (t instanceof Class)
            return t;
        else if (t instanceof TypeVariable) {
            ReflectionHelper.ClassTypePair ct = ReflectionHelper.
                    resolveTypeVariable(c, dc, (TypeVariable)t);
            if (ct != null)
                return ct.c;
            else 
                return t;
        } else if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            return pt.getRawType();
        } else
            return t;
    }
    
    private List<MetaInjectableProvider> findInjectableProviders(
            Class<? extends Annotation> ac, 
            Class<?> cc,
            Scope s) {
        List<MetaInjectableProvider> subips = new ArrayList<MetaInjectableProvider>();        
        for (MetaInjectableProvider i : getList(ac)) {
            if (s == i.ip.getScope()) {
                if (i.cc.isAssignableFrom(cc)) {
                    subips.add(i);                        
                }
            }
        }
        
        return subips;    
    }
    
    // InjectableProviderContext

    public <A extends Annotation, C> Injectable getInjectable(
            Class<? extends Annotation> ac,             
            InjectableContext ic,
            A a,
            C c,
            Scope s) {
        return getInjectable(ac, ic, a, c, Collections.singletonList(s));
    }
    
    public <A extends Annotation, C> Injectable getInjectable(
            Class<? extends Annotation> ac,             
            InjectableContext ic,
            A a,
            C c,
            List<Scope> ls) {
        for (Scope s : ls) {
            Injectable i = _getInjectable(ac, ic, a, c, s);
            if (i != null)
                return i;
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private <A extends Annotation, C> Injectable _getInjectable(
            Class<? extends Annotation> ac,             
            InjectableContext ic,
            A a,
            C c,
            Scope s) {
        for (MetaInjectableProvider mip : findInjectableProviders(ac, c.getClass(), s)) {
            Injectable i = mip.ip.getInjectable(ic, a, c);
            if (i != null)
                return i;
        }
        return null;
    }
        
    public Injectable getInjectable(Parameter p) {
        if (p.getAnnotation() == null) return null;
        
        InjectableContext ic = new ParameterIC(p);
        
        // Find a per request injectable with Parameter
        Injectable i = getInjectable(p.getAnnotation().annotationType(), ic, p.getAnnotation(), 
                p, Scope.PerRequest);
        if (i != null) return i;
        
        // Find a per request, undefined or singleton injectable with parameter Type
        return getInjectable(
                p.getAnnotation().annotationType(), 
                ic, 
                p.getAnnotation(), 
                p.getParameterType(),
                Arrays.asList(Scope.PerRequest, Scope.Undefined, Scope.Singleton)
                );
    }
    
    public List<Injectable> getInjectable(List<Parameter> ps) {
        List<Injectable> is = new ArrayList<Injectable>();
        for (Parameter p : ps)
            is.add(getInjectable(p));        
        return is;
    }
        
    private static final class ParameterIC implements InjectableContext {
        private final Parameter p;

        ParameterIC(Parameter p) {
            this.p = p;
        }
        
        public <A extends Annotation> A getAnnotation(Class<A> ca) {
            return p.getParameterClass().getAnnotation(ca);
        }

        public Annotation[] getAnnotations() {
            return p.getClass().getAnnotations();
        }
    }
    
    //
    
    public void injectResources(final Object o) {
        Class oClass = o.getClass();
        while (oClass != Object.class) {
            for (final Field f : oClass.getDeclaredFields()) {                
                if (getFieldValue(o, f) != null) continue;
                
                final Annotation[] as = f.getAnnotations();
                for (Annotation a : as) {
                    Injectable i = getInjectable(
                            a.annotationType(), null, a, f.getGenericType(), 
                            Arrays.asList(Scope.Singleton, Scope.Undefined));
                    if (i != null) {
                        setFieldValue(o, f, i.getValue(null));
                    }
                }
                
            }
            oClass = oClass.getSuperclass();
        }
    }
    
    private void setFieldValue(final Object resource, final Field f, final Object value) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    f.set(resource, value);
                    return null;
                } catch (IllegalAccessException e) {
                    throw new ContainerException(e);
                }
            }
        });
    }
    
    private Object getFieldValue(final Object resource, final Field f) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    return f.get(resource);
                } catch (IllegalAccessException e) {
                    throw new ContainerException(e);
                }
            }
        });
    }
}