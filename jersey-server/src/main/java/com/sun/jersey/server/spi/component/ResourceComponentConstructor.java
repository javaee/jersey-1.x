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
package com.sun.jersey.server.spi.component;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceConstructor;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.spi.inject.Injectable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A utility class to obtain the most suitable constructor for a 
 * resource class.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ResourceComponentConstructor {
    private final Class c;
    
    private final Constructor constructor;

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
        private final Constructor con;

        /**
         * The list of injectables associated with the parameters of the
         * constructor;
         */
        private final List<Injectable> is;

        /**
         * Create a new tuple of a constructor and list of injectables.
         *
         * @param con the constructor
         * @param is the list of injectables.
         */
        private ConstructorInjectablePair(Constructor con, List<Injectable> is) {
            this.con = con;
            this.is = is;
        }
    }

    private static class ConstructorComparator<T> implements Comparator<ConstructorInjectablePair> {
        public int compare(ConstructorInjectablePair o1, ConstructorInjectablePair o2) {
            int p = Collections.frequency(o1.is, null) - Collections.frequency(o2.is, null);
            if (p != 0)
                return p;

            return o2.con.getParameterTypes().length - o1.con.getParameterTypes().length;
        }
    }

    public ResourceComponentConstructor(ServerInjectableProviderContext sipc,
            ComponentScope scope, AbstractResource ar) {
        this.c = ar.getResourceClass();

        ConstructorInjectablePair cip = getConstructor(sipc, scope, ar);
        if (cip == null || cip.is.size() == 0) {
            this.constructor = null;
            this.injectables = null;
        } else {
            this.constructor = cip.con;
            this.injectables = AbstractHttpContextInjectable.transform(cip.is);

            Class<?>[] types = this.constructor.getParameterTypes();
            for (int i = 0; i < this.injectables.size(); i++) {
                if (this.injectables.get(i) == null) {
                    final Object defaultValue = DEFAULT_VALUES.get(types[i]);
                    if (defaultValue != null) {
                        this.injectables.set(i, new AbstractHttpContextInjectable() {
                            @Override
                            public Object getValue(HttpContext c) {
                                return defaultValue;
                            }
                        });
                    }
                }
            }
        }
    }

    public Object getInstance(HttpContext hc)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if (constructor == null) {
            return c.newInstance();
        } else {
            Object[] params = new Object[injectables.size()];
            int i = 0;
            for (AbstractHttpContextInjectable injectable : injectables) {
                params[i++] = (injectable != null) ? injectable.getValue(hc) : null;
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
            List<Injectable> is = sipc.getInjectable(arc.getParameters(), scope);
            cs.add(new ConstructorInjectablePair(arc.getCtor(), is));
        }
                
        return cs.first();        
    }

    private static final Map<Class, Object> DEFAULT_VALUES = createDefaultValues();

    private static Map<Class, Object> createDefaultValues() {
        Map<Class, Object> defaultValues = new HashMap<Class, Object>();
        defaultValues.put(byte.class, (byte) 0);
        defaultValues.put(short.class, (short) 0);
        defaultValues.put(int.class, 0);
        defaultValues.put(long.class, (long) 0);
        defaultValues.put(float.class, (float)0.0);
        defaultValues.put(double.class, 0.0);
        defaultValues.put(char.class, '\0');
        defaultValues.put(boolean.class, Boolean.FALSE);
        return defaultValues;
    }
 }