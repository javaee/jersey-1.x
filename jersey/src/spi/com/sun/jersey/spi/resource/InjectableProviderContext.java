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

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableContext;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * The context to obtain {@link Injectable} instances.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface InjectableProviderContext {
    /**
     * Get an injectable.
     * 
     * @param ac the annotation class.
     * @param ic the injectable context.
     * @param a the annotation instance.
     * @param c the context type.
     * @param s the scope.
     * @return the injectable, otherwise null if an injectable could 
     *         not be found.
     */
    <A extends Annotation, C> Injectable getInjectable(
            Class<? extends Annotation> ac,             
            InjectableContext ic,
            A a,
            C c,
            Scope s);
    
    /**
     * Get an injectable.
     * 
     * @param ac the annotation class.
     * @param ic the injectable context.
     * @param a the annotation instance.
     * @param c the context type.
     * @param s the list of scope, ordered by preference.
     * @return the injectable, otherwise null if an injectable could 
     *         not be found.
     */
    <A extends Annotation, C> Injectable getInjectable(
            Class<? extends Annotation> ac,             
            InjectableContext ic,
            A a,
            C c,
            List<Scope> s);
    
    /**
     * Get an injectable given a parameter.
     * 
     * @param p the parameter.
     * @return the injectable, otherwise null if an injectable could 
     *         not be found.
     */
    Injectable getInjectable(Parameter p);
    
    /**
     * Get a list of injectable given a list of parameter.
     * 
     * @param ps the list of parameter.
     * @return the list of injectable, if an injectable for a parameter
     *         could not be found the corresponding element in the 
     *         list will be null.
     */
    List<Injectable> getInjectable(List<Parameter> ps);
}