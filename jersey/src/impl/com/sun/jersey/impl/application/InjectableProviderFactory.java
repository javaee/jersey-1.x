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

import com.sun.jersey.spi.resource.InjectableProviderContext;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.model.ReflectionHelper;
import com.sun.jersey.impl.modelapi.annotation.AnnotatedMethod;
import com.sun.jersey.impl.modelapi.annotation.MethodList;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableContext;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;

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
    
    private Type[] getMetaArguments(Class<? extends InjectableProvider> c) {
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
    
    /**
     * Inject onto a singleton provider.
     * 
     * @param o the singleton instance
     */
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
        
        MethodList ml = new MethodList(o.getClass().getMethods());
        for (AnnotatedMethod m : ml.
                hasNotMetaAnnotation(HttpMethod.class).
                hasNotAnnotation(Path.class).
                hasNumParams(1).
                hasReturnType(void.class).
                nameStartsWith("set")) {
            final Annotation[] as = m.getAnnotations();
            final Type t = m.getGenericParameterTypes()[0];
            for (Annotation a : as) {
                Injectable i = getInjectable(
                        a.annotationType(), null, a, t, 
                        Arrays.asList(Scope.Singleton, Scope.Undefined));
                if (i != null) {
                    setMethodValue(o, m, i.getValue(null));
                }
            }
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

    private void setMethodValue(Object o, AnnotatedMethod m, Object value) {
        try {
            m.getMethod().invoke(o, value);
        } catch (Exception ex) {
            throw new ContainerException(ex);
        }
    }
    
    public static class ConstructorInjectablePair<T> {
        Constructor<T> con;
        List<Injectable> is;
        
        ConstructorInjectablePair(Constructor<T> con, List<Injectable> is) {
            this.con = con;
            this.is = is;
        }
    }
    
    /**
     * Get the most suitable constructor. The constructor with the most
     * parameters and that has the most parameters associated with 
     * Injectable instances will be chosen.
     * 
     * @param c the class to instantiate
     * @return a constructor and list of injectables for the constructor 
     *         parameters.
     */
    @SuppressWarnings("unchecked")
    public <T> ConstructorInjectablePair<T> getConstructor(Class<T> c) {
        if (c.getConstructors().length == 0)
            return null;
        
        SortedSet<ConstructorInjectablePair<T>> cs = new TreeSet<ConstructorInjectablePair<T>>(
                new Comparator<ConstructorInjectablePair<T>>() {
            public int compare(ConstructorInjectablePair<T> o1, ConstructorInjectablePair<T> o2) {
                int p = o2.con.getParameterTypes().length - o1.con.getParameterTypes().length;
                if (p != 0)
                    return p;
                
                return Collections.frequency(o2.is, null) - Collections.frequency(o1.is, null);
            }
        });
        
        for (Constructor<T> con : c.getConstructors()) {
            List<Injectable> is = new ArrayList<Injectable>();
            int ps = con.getParameterTypes().length;
            for (int p = 0; p < ps; p++) {
                Type pgtype = con.getGenericParameterTypes()[p];
                Annotation[] as = con.getParameterAnnotations()[p];
                
                Injectable i = null;
                for (Annotation a : as) {
                    i = getInjectable(
                            a.annotationType(), null, a, pgtype, 
                            Arrays.asList(Scope.Singleton, Scope.Undefined));
                }
                is.add(i);
            }
            cs.add(new ConstructorInjectablePair<T>(con, is));
        }
                
        return cs.first();
    }
}