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

import com.sun.jersey.spi.service.ComponentProvider;
import java.lang.annotation.Annotation;

/**
 * An injectable provider provides an injectable which in turn may be used
 * to obtain the instance to inject onto a field, bean setter method, parameter
 * of a constructor, or parameter of a method.
 * 
 * @param A the annotation type
 * @param C the context type. Types of the {@link java.lang.reflect.Type} and 
 *        {@link com.sun.jersey.api.model.Parameter} are the only types that
 *        are supported.
 * @author Paul.Sandoz@Sun.Com
 */
public interface InjectableProvider<A extends Annotation, C> {
    
    ComponentProvider.Scope getScope();
    
    /**
     * Get an injectable.
     * 
     * @param ic the injectable context
     * @param a the annotation instance
     * @param c the context instance
     * @return an Injectable instance, otherwise null if an instance cannot
     *         be created.
     */
    Injectable getInjectable(InjectableContext ic, A a, C c);
}