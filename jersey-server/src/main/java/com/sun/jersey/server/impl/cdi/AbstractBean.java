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

package com.sun.jersey.server.impl.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Base class implementing the CDI Bean interface.
 *
 * Subclasses must implement the create method.
 *
 * Currently, AbstractBean only supports a single qualifier and a single bean type,
 * although it could easily be extended to support more.
 *
 * @author robc
 */
public abstract class AbstractBean<T> implements Bean<T> {

    private Class<?> klass;
    private Set<Annotation> qualifiers;
    private Set<Type> types;

    public AbstractBean(Class<?> klass, Annotation qualifier) {
        this(klass, klass, qualifier);
    }

    public AbstractBean(Class<?> klass, Set<Annotation> qualifiers) {
        this(klass, klass, qualifiers);
    }


    public AbstractBean(Class<?> klass, Type type, Annotation qualifier) {
        this.klass = klass;

        qualifiers = new HashSet<Annotation>();
        qualifiers.add(qualifier);
        types = new HashSet<Type>();
        types.add(type);
    }

    public AbstractBean(Class<?> klass, Type type, Set<Annotation> qualifiers) {
        this.klass = klass;
        this.qualifiers = qualifiers;
        types = new HashSet<Type>();
        types.add(type);
    }

    public Class<?> getBeanClass() {
        return klass;
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return (Set<InjectionPoint>)Collections.EMPTY_SET;
    }

    public String getName() {
        return null;
    }

    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    public Set<Class<? extends Annotation>> getStereotypes() {
        return (Set<Class<? extends Annotation>>)Collections.EMPTY_SET;
    }

    public Set<Type> getTypes() {
        return types;
    }

    public boolean isAlternative() {
        return false;
    }

    public boolean isNullable() {
        return false;
    }

    public abstract T create(CreationalContext<T> creationalContext);

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        // no-op
    }

}
