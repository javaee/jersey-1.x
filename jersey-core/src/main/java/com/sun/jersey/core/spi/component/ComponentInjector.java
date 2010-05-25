/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.core.spi.component;

import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProviderContext;
import com.sun.jersey.spi.inject.Errors;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;

/**
 * An injector to inject on a component.
 * 
 * @param <T> the type to inject on.
 * @author Paul.Sandoz@Sun.Com
 */
public class ComponentInjector<T> {
    protected final InjectableProviderContext ipc;

    protected final Class<T> c;

    /**
     * Create a component injector.
     * 
     * @param ipc the injector provider context to ontain injectables.
     * @param c the class of the type to inject on.
     */
    public ComponentInjector(InjectableProviderContext ipc, Class<T> c) {
        this.ipc = ipc;
        this.c = c;
    }

    /**
     * Inject on an instance.
     *
     * @param t the instance to inject on.
     */
    public void inject(T t) {
        AccessibleObjectContext aoc = new AccessibleObjectContext();

        Class oClass = c;
        while (oClass != Object.class) {
            for (final Field f : oClass.getDeclaredFields()) {
                if (getFieldValue(t, f) != null) continue;

                aoc.setAccesibleObject(f);
                final Annotation[] as = f.getAnnotations();
                boolean missingDependency = false;
                for (Annotation a : as) {
                    Injectable i = ipc.getInjectable(
                            a.annotationType(), aoc, a, f.getGenericType(),
                            ComponentScope.UNDEFINED_SINGLETON);
                    if (i != null) {
                        missingDependency = false;
                        setFieldValue(t, f, i.getValue());
                        break;
                    } else if (ipc.isAnnotationRegistered(a.annotationType(), f.getGenericType().getClass())) {
                        missingDependency = true;
                    }
                }

                if (missingDependency) {
                    // Missing dependency
                    Errors.missingDependency(f);
                }

            }
            oClass = oClass.getSuperclass();
        }

        MethodList ml = new MethodList(c.getMethods());
        int methodIndex = 0;
        for (AnnotatedMethod m : ml.
                hasNotMetaAnnotation(HttpMethod.class).
                hasNotAnnotation(Path.class).
                hasNumParams(1).
                hasReturnType(void.class).
                nameStartsWith("set")) {
            final Annotation[] as = m.getAnnotations();
            aoc.setAccesibleObject(m.getMethod(), as);
            final Type gpt = m.getGenericParameterTypes()[0];

            boolean missingDependency = false;
            for (Annotation a : as) {
                Injectable i = ipc.getInjectable(
                        a.annotationType(), aoc, a, gpt,
                        ComponentScope.UNDEFINED_SINGLETON);
                if (i != null) {
                    missingDependency = false;
                    setMethodValue(t, m, i.getValue());
                    break;
                } else if (ipc.isAnnotationRegistered(a.annotationType(), gpt.getClass())) {
                    missingDependency = true;
                }
            }

            if (missingDependency) {
                // Missing dependency
                Errors.missingDependency(m.getMethod(), methodIndex);
            }

            methodIndex++;
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
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
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
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private void setMethodValue(Object o, AnnotatedMethod m, Object value) {
        try {
            m.getMethod().invoke(o, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}