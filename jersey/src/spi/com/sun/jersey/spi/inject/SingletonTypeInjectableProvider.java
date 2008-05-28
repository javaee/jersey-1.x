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

package com.sun.jersey.spi.inject;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A utility class that may implemented to support an singleton injectable 
 * provider for a specific type T, and an instance of type T, that is to be 
 * injected.
 * 
 * @param A the annotation type
 * @param T the type returned by {@link Injectable#getValue}
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class SingletonTypeInjectableProvider<A extends Annotation, T> 
    implements InjectableProvider<A, Type>, Injectable<T> {

    private final Type t;
    private final T instance;
        
    /**
     * Construct a new instance with the Type and the instance.
     * 
     * @param t the type of T.
     * @param instance the instance.
     */
    public SingletonTypeInjectableProvider(Type t, T instance) {
        this.t = t;
        this.instance = instance;
    }
    
    public Scope getScope() {
        return Scope.Singleton;
    }
    
    public Injectable getInjectable(InjectableContext ic, A a, Type c) {
        if (c.equals(t)) {
            return this;
        } else
            return null;
    }

    public T getValue(HttpContext c) {
        return instance;
    }

}