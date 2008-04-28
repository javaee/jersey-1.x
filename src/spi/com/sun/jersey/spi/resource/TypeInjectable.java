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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Injectable functionality to a field that is annotated with a particular 
 * annotation and supports a particular type.
 * 
 * @param <T> The type of the annotation class.
 * @param <V> the return type of the injectable value.
 * 
 */
public abstract class TypeInjectable<T extends Annotation, V> extends Injectable<T, V> {

    /* (non-Javadoc)
     * @see com.sun.jersey.spi.resource.Injectable#getInjectableValue(java.lang.Object, java.lang.reflect.Field, java.lang.annotation.Annotation)
     */
    @Override
    public V getInjectableValue(Object o, Field f, T a) {
        return getInjectableValue(a);
    }
        
    /**
     * Get the object to inject onto the field.
     * 
     * @param a the annotation.
     * @return the instance to inject onto the field.
     */
    public abstract V getInjectableValue(T a);    
}