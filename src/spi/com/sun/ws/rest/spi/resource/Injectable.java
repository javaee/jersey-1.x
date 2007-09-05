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

/*
 * Injectable.java
 *
 * Created on August 2, 2007, 3:59 PM
 *
 */

package com.sun.ws.rest.spi.resource;

import com.sun.ws.rest.api.container.ContainerException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A utility base class for implementing injectable resources.
 * 
 */
public abstract class Injectable<T extends Annotation, V> {
    public abstract Class<T> getAnnotationClass();
    
    public void inject(Object resource, Field f) {
        if (getFieldValue(resource, f) != null) {
            // skip fields that already have a value
            // (may have been injected by the container impl)
            return;
        }
        
        T a = f.getAnnotation(getAnnotationClass());
        if (a == null) {
            // skip if the annotation is not declared
            return;
        }
        
        V value = getInjectableValue(a);
        if (value != null)
            setFieldValue(resource, f, value);
    }
    
    public abstract V getInjectableValue(T a);
    
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
}
