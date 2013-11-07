/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.jersey.server.spi.component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.WebApplicationException;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceConstructor;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.spi.inject.Errors;
import com.sun.jersey.spi.inject.Injectable;

/**
 * A constructor of a resource class.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ResourceComponentConstructor {
    private final Class clazz;

    private final ResourceComponentInjector resourceComponentInjector;

    private final Constructor constructor;
    private final Constructor nonPublicConstructor;

    private final List<Method> postConstructs = new ArrayList<Method>();

    private final List<AbstractHttpContextInjectable> injectables;

    /**
     * A tuple of a constructor and the list of injectables associated with
     * the parameters of the constructor.
     *
     * @param <T> the type to construct.
     */
    private static class ConstructorInjectablePair {
        /**
         * The constructor.
         */
        private final Constructor constructor;

        /**
         * The list of injectables associated with the parameters of the
         * constructor;
         */
        private final List<Injectable> injectables;

        /**
         * Create a new tuple of a constructor and list of injectables.
         *
         * @param constructor the constructor
         * @param injectables the list of injectables.
         */
        private ConstructorInjectablePair(Constructor constructor, List<Injectable> injectables) {
            this.constructor = constructor;
            this.injectables = injectables;
        }
    }

    private static class ConstructorComparator<T> implements Comparator<ConstructorInjectablePair> {
        @Override
        public int compare(ConstructorInjectablePair o1, ConstructorInjectablePair o2) {

            int p = Collections.frequency(o1.injectables, null) - Collections.frequency(o2.injectables, null);
            if (p != 0) {
                return p;
            }

            return o2.constructor.getParameterTypes().length - o1.constructor.getParameterTypes().length;
        }
    }

    public ResourceComponentConstructor(ServerInjectableProviderContext serverInjectableProviderCtx,
            ComponentScope scope, AbstractResource abstractResource) {

        this.clazz = abstractResource.getResourceClass();

        final int modifiers = clazz.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            Errors.nonPublicClass(clazz);
        }

        if (Modifier.isAbstract(modifiers)) {
            if (Modifier.isInterface(modifiers)) {
                Errors.interfaceClass(clazz);
            } else {
                Errors.abstractClass(clazz);
            }
        }

        if (clazz.getEnclosingClass() != null && !Modifier.isStatic(modifiers)) {
            Errors.innerClass(clazz);
        }

        if (Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers)) {
            if (clazz.getConstructors().length == 0) {
                Errors.nonPublicConstructor(clazz);
            }
        }

        this.resourceComponentInjector = new ResourceComponentInjector(
                serverInjectableProviderCtx,
                scope,
                abstractResource);

        this.postConstructs.addAll(abstractResource.getPostConstructMethods());

        ConstructorInjectablePair ctorInjectablePair = getConstructor(serverInjectableProviderCtx, scope, abstractResource);

        if (ctorInjectablePair == null) {
            this.constructor = null;
            this.nonPublicConstructor = getNonPublicConstructor();
            this.injectables = null;
        } else if (ctorInjectablePair.injectables.isEmpty()) {
            this.constructor = ctorInjectablePair.constructor;
            this.nonPublicConstructor = null;
            this.injectables = null;
        } else {
            if (ctorInjectablePair.injectables.contains(null)) {
                // Missing dependency
                for (int i = 0; i < ctorInjectablePair.injectables.size(); i++) {
                    if (ctorInjectablePair.injectables.get(i) == null) {
                        Errors.missingDependency(ctorInjectablePair.constructor, i);
                    }
                }
            }

            this.constructor = ctorInjectablePair.constructor;
            this.injectables = AbstractHttpContextInjectable.transform(ctorInjectablePair.injectables);

            if (constructor != null){
                setAccessible(constructor);
                nonPublicConstructor = null;
            } else {
                nonPublicConstructor = (injectables == null) ? getNonPublicConstructor() : null;
            }
        }
    }

    private Constructor getNonPublicConstructor() {
        try {
            Constructor result = AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor>(){

                @Override
                public Constructor run() throws NoSuchMethodException {
                    return clazz.getDeclaredConstructor();
                }
            });
            setAccessible(result);
            return result;
        } catch (PrivilegedActionException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof NoSuchMethodException) {
                return null;
            }
            throw new WebApplicationException(cause);
        }
    }

    private void setAccessible(final Constructor constructor) {
        AccessController.doPrivileged(new PrivilegedAction(){

            @Override
            public Object run() {
                constructor.setAccessible(true);
                return null;
            }
        });
    }

    public Class getResourceClass() {
        return clazz;
    }

    public Object construct(HttpContext hc)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final Object o = _construct(hc);
        resourceComponentInjector.inject(hc, o);
        for (Method postConstruct : postConstructs) {
            postConstruct.invoke(o);
        }
        return o;
    }

    private Object _construct(HttpContext httpContext)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if (injectables == null) {
            return constructor != null ? constructor.newInstance() :
                    nonPublicConstructor != null ? nonPublicConstructor.newInstance() : clazz.newInstance();
        } else {
            Object[] params = new Object[injectables.size()];
            int i = 0;
            for (AbstractHttpContextInjectable injectable : injectables) {
                params[i++] = (injectable != null) ? injectable.getValue(httpContext) : null;
            }
            return constructor.newInstance(params);
        }
    }

    /**
     * Get the most suitable constructor. The constructor with the most
     * parameters and that has the most parameters associated with
     * Injectable instances will be chosen.
     *
     * @param <T> the type of the resource.
     * @param c the class to instantiate.
     * @param ar the abstract resource.
     * @param s the scope for which the injectables will be used.
     * @return a list constructor and list of injectables for the constructor
     *         parameters.
     */
    @SuppressWarnings("unchecked")
    private <T> ConstructorInjectablePair getConstructor(
            ServerInjectableProviderContext sipc,
            ComponentScope scope,
            AbstractResource ar) {
        if (ar.getConstructors().isEmpty())
            return null;

        SortedSet<ConstructorInjectablePair> cs = new TreeSet<ConstructorInjectablePair>(
                new ConstructorComparator());
        for (AbstractResourceConstructor arc : ar.getConstructors()) {
            List<Injectable> is = sipc.getInjectable(arc.getCtor(), arc.getParameters(), scope);
            cs.add(new ConstructorInjectablePair(arc.getCtor(), is));
        }

        return cs.first();
    }
 }