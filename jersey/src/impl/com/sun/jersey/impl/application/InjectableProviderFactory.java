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

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.model.ReflectionHelper;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableContext;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.PerRequestInjectable;
import com.sun.jersey.spi.inject.SingletonInjectable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public final class InjectableProviderFactory implements InjectableProviderContext {
    private static final class MetaInjectableProvider {
        final InjectableProvider ip;
        final Class<? extends Annotation> ac;
        final Class<? extends Injectable> ic;
        final Class<?> cc;
        
        MetaInjectableProvider(
                InjectableProvider ip,
                Class<? extends Annotation> ac, 
                Class<?> cc,
                Class<? extends Injectable> ic) {
            this.ip = ip;
            this.ac = ac;
            this.cc = cc;
            this.ic = ic;
        }
    }
    
    private Map<Class<? extends Annotation>, LinkedList<MetaInjectableProvider>> ipm = 
            new HashMap<Class<? extends Annotation>, LinkedList<MetaInjectableProvider>>();
        
    public void add(InjectableProvider ip) {
        Type[] args = getMetaArguments(ip.getClass());
        if (args != null) {
            MetaInjectableProvider mip = new MetaInjectableProvider(ip, 
                    (Class)args[0], (Class)args[1], (Class)args[2]);
            
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
    
    private MetaInjectableProvider getMeta(InjectableProvider ip) {
        Type[] args = getMetaArguments(ip.getClass());
        if (args != null)
            return new MetaInjectableProvider(ip, (Class)args[0], (Class)args[1], (Class)args[2]);

        // TODO throw exception
        return null;
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
                                (args[1] == Type.class || args[1] == Parameter.class) &&
                                args[2] instanceof Class)
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
            Class<? extends Injectable> ic, 
            Class<?> cc) {
        List<MetaInjectableProvider> subips = new ArrayList<MetaInjectableProvider>();        
        for (MetaInjectableProvider i : getList(ac)) {
            if (ic.isAssignableFrom(i.ic)) {
                if (i.cc.isAssignableFrom(cc)) {
                    subips.add(i);                        
                }
            }
        }
        
        return subips;    
    }
    
    private List<MetaInjectableProvider> findInjectableProviders(
            Class<? extends Annotation> ac, 
            Class<?> cc) {
        List<MetaInjectableProvider> subips = new ArrayList<MetaInjectableProvider>();
        for (MetaInjectableProvider i : getList(ac)) {
            if (i.cc.isAssignableFrom(cc)) {
                subips.add(i);                   
            }
        }
            
        return subips;    
    }
    
    // InjectableProviderContext
    
    public <A extends Annotation, C> Injectable getInjectable(
            Class<? extends Annotation> ac,
            InjectableContext ic,
            A a,
            C c) {
        for (MetaInjectableProvider mip : findInjectableProviders(ac, c.getClass())) {
            Object i = mip.ip.getInjectable(ic, a, c);
            if (i != null)
                return (Injectable)i;
        }
        return null;
    }    
    
    public <A extends Annotation, I extends Injectable, C> I getInjectable(
            Class<? extends Annotation> ac,             
            InjectableContext ic,
            A a,
            C c,
            Class<? extends Injectable> iclass) {
        for (MetaInjectableProvider mip : findInjectableProviders(ac, iclass, c.getClass())) {
            Object i = mip.ip.getInjectable(ic, a, c);
            if (i != null)
                return (I)i;
        }
        return null;
    }
        
    public Injectable getInjectable(Parameter p) {
        if (p.getAnnotation() == null) return null;
        
        InjectableContext ic = new ParameterIC(p);
        
        // Find a per request injectable with Parameter
        Injectable i = getInjectable(p.getAnnotation().annotationType(), ic, p.getAnnotation(), 
                p, PerRequestInjectable.class);
        if (i != null) return i;
        
        // Find a singleton or per request injetable with parameter Type
        return getInjectable(p.getAnnotation().annotationType(), ic, p.getAnnotation(), 
                p.getParameterType());        
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
                    final Injectable i = getInjectable(
                            a.annotationType(), null, a, f.getGenericType());
                    if (i != null && i instanceof SingletonInjectable) {
                        SingletonInjectable si = (SingletonInjectable)i;
                        
                        Object v = si.getValue(null);
                        
                        setFieldValue(o, f, v);
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