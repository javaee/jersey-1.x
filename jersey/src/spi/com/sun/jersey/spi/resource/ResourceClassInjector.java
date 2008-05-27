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
package com.sun.jersey.spi.resource;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.spi.resource.InjectableProviderContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An injector that injects onto properties of a resource.
 * 
 * Analysis of the class will be performed using reflection to find 
 * {@link Injectable} instances and as a result the use of reflection is 
 * minimized when performing injection (to that of getting and setting
 * field values).
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceClassInjector {
    private Field[] singletonFields;    
    private Object[] singletonValues;
    
    private Field[] perRequestFields;
    private Injectable<?>[] perRequestInjectables;
   
    /**
     * Create a new resource class injector.
     * 
     * @param ipc the injectable provider context to obtain injectables
     * @param s the scope underwhich injection will be performed
     * @param resource the abstract resource model
     */
    public ResourceClassInjector(InjectableProviderContext ipc, Scope s, AbstractResource resource) {
        processFields(ipc, s, resource.getResourceClass());
    }

    private void processFields(InjectableProviderContext ipc, Scope s, Class c) {
        Map<Field, Injectable<?>> singletons = new HashMap<Field, Injectable<?>>();
        Map<Field, Injectable<?>> perRequest = new HashMap<Field, Injectable<?>>();
        
        while (c != Object.class) {
            for (final Field f : c.getDeclaredFields()) {
                final Annotation[] as = f.getAnnotations();
                for (Annotation a : as) {
                    if (s == Scope.PerRequest) {
                        Injectable i = ipc.getInjectable(
                                a.annotationType(), null, a, f.getGenericType(), 
                                Arrays.asList(Scope.PerRequest, Scope.Undefined));
                        if (i != null) {
                            configureField(f);
                            perRequest.put(f, i);
                        } else {
                            i = ipc.getInjectable(
                                    a.annotationType(), null, a, f.getGenericType(), 
                                    Scope.Singleton);
                            if (i != null) {
                                configureField(f);
                                singletons.put(f, i);
                            }   
                        }                       
                    } else {
                        Injectable i = ipc.getInjectable(
                                a.annotationType(), null, a, f.getGenericType(), 
                                Arrays.asList(Scope.Undefined, Scope.Singleton));
                        if (i != null) {
                            configureField(f);
                            singletons.put(f, i);
                        }
                    }                                        
                }
            }
            c = c.getSuperclass();
        }
        
        int size = singletons.entrySet().size();
        singletonFields = new Field[size];
        singletonValues = new Object[size];        
        int i = 0;
        for (Map.Entry<Field, Injectable<?>> e : singletons.entrySet()) {
            singletonFields[i] = e.getKey();
            singletonValues[i++] = e.getValue().getValue(null);
        }
        
        size = perRequest.entrySet().size();
        perRequestFields = new Field[size];
        perRequestInjectables = new Injectable<?>[size];        
        i = 0;
        for (Map.Entry<Field, Injectable<?>> e : perRequest.entrySet()) {
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
    
    /**
     * Inject onto an instance of a resource class.
     * 
     * @param c the HTTP context, may be set to null if not available for the
     *        current scope.
     * @param o the resource.
     */
    public void inject(HttpContext c, Object o) {
        int i = 0;
        for (Field f : singletonFields) {
            try {
                if (f.get(o) == null) {
                    f.set(o, singletonValues[i]);
                }
                i++;
            } catch (IllegalAccessException ex) {
                throw new ContainerException(ex);
            }
        }
        
        i = 0;
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