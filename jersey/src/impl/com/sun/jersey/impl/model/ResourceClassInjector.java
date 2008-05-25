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
package com.sun.jersey.impl.model;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.impl.application.InjectableProviderContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestInjectable;
import com.sun.jersey.spi.inject.SingletonInjectable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceClassInjector {
    private Field[] singletonFields;    
    private Object[] singletonValues;
    private SingletonInjectable<?>[] singletonInjectables;
    
    private Field[] perRequestFields;
    private PerRequestInjectable<?>[] perRequestInjectables;
    
    public ResourceClassInjector(InjectableProviderContext ipc, AbstractResource resource) {
        processFields(ipc, resource.getResourceClass());
    }

    private void processFields(InjectableProviderContext ipc, Class c) {        
        Map<Field, SingletonInjectable<?>> singletons = new HashMap<Field, SingletonInjectable<?>>();
        Map<Field, PerRequestInjectable<?>> perRequest = new HashMap<Field, PerRequestInjectable<?>>();
        
        while (c != Object.class) {
            for (final Field f : c.getDeclaredFields()) {
                final Annotation[] as = f.getAnnotations();
                for (Annotation a : as) {
                    final Injectable i = ipc.getInjectable(
                            a.annotationType(), null, a, f.getGenericType());
                    if (i == null)
                        continue;
                    
                    configureField(f);
                    if (i instanceof SingletonInjectable) {
                        SingletonInjectable si = (SingletonInjectable)i;
                        singletons.put(f, si);
                    } else if (i instanceof PerRequestInjectable) {
                        PerRequestInjectable pri = (PerRequestInjectable)i;
                        perRequest.put(f, pri);
                    }
                }
            }
            c = c.getSuperclass();
        }
        
        int size = singletons.entrySet().size();
        singletonFields = new Field[size];
        singletonInjectables = new SingletonInjectable<?>[size];
        singletonValues = new Object[size];        
        int i = 0;
        for (Map.Entry<Field, SingletonInjectable<?>> e : singletons.entrySet()) {
            singletonFields[i] = e.getKey();
            singletonInjectables[i] = e.getValue();
            singletonValues[i++] = e.getValue().getValue(null);
        }
        
        size = perRequest.entrySet().size();
        perRequestFields = new Field[size];
        perRequestInjectables = new PerRequestInjectable<?>[size];        
        i = 0;
        for (Map.Entry<Field, PerRequestInjectable<?>> e : perRequest.entrySet()) {
            perRequestFields[i] = e.getKey();
            perRequestInjectables[i++] = e.getValue();
        }        
    }

    private void configureField(final Field f) {
        if (!f.isAccessible()) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    f.setAccessible(true);
                    return null;
                }
            });
        }
    }
    
    public void injectSingleton(Object o) {
        int i = 0;
        for (Field f : singletonFields) {
            try {
                if (f.get(o) == null) {
                    f.set(o, singletonInjectables[i].getValue(null));
                    // TODO use constant values when fully supporting
                    // injection on per-request resources
                    // especially with @Inject
                    // f.set(o, singletonValues[i]);
                }
                i++;
            } catch (IllegalAccessException ex) {
                throw new ContainerException(ex);
            }
        }
    }
    
    public void injectPerRequest(HttpContext c, Object o) {
        injectSingleton(o);
        
        int i = 0;
        for (Field f : perRequestFields) {
            try {
                if (f.get(o) == null) {
                    f.set(o, perRequestInjectables[i].getValue(c));
                }
                i++;
            } catch (IllegalAccessException ex) {
                throw new ContainerException(ex);
            }
        }
    }
}