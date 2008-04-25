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

package com.sun.ws.rest.spi.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Injectable functionality to a field that is annotated with a particular 
 * annotation. The field types supported are determined by implementations of 
 * this class.
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 * @param <T> the type of the annotation class.
 */
public abstract class AnnotationInjectable<T extends Annotation> extends Injectable<T, Object> {
    
    /* (non-Javadoc)
     * @see com.sun.ws.rest.spi.resource.Injectable#getInjectableValue(java.lang.Object, java.lang.reflect.Field, java.lang.annotation.Annotation)
     */
    @Override
    public Object getInjectableValue(Object o, Field f, T a) {
        return getInjectableValue(o, f);
    }

    /**
     * Get the object to inject onto the field.
     * 
     * @param o the instance of the class containing the field
     * @param f the field
     * @return the instance to inject onto the field.
     */
    public abstract Object getInjectableValue(Object o, Field f);   
}