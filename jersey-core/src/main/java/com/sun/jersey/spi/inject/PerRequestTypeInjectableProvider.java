/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.spi.inject;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A utility class that may implemented to support a per-request injectable 
 * provider for a specific type T.
 * 
 * @param <A> the annotation type
 * @param <T> the type returned by {@link Injectable#getValue}
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class PerRequestTypeInjectableProvider <A extends Annotation, T> 
    implements InjectableProvider<A, Type> {
    
    private final Type t;
    
    /**
     * Construct a new instance with the Type
     *
     * @param t the type of T.
     */
    public PerRequestTypeInjectableProvider(Type t) {
        this.t = t;
    }

    public final ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    public final Injectable getInjectable(ComponentContext ic, A a, Type c) {
        if (c.equals(t)) {
            return getInjectable(ic, a);
        } else
            return null;
    }

    /**
     * Get an injectable for type T.
     * 
     * @param ic the injectable context
     * @param a the annotation instance
     * @return an Injectable instance, otherwise null if an instance cannot
     *         be created.
     */
    public abstract Injectable<T> getInjectable(ComponentContext ic, A a);
}
