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

package com.sun.jersey.spi.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A provider for the instantiation and management of components.
 * <p>
 * The runtime will defer to a registered component provider (if present)
 * for every component (application-defined or infrastructure-defined) that
 * needs to be instantiated. If the component provider does
 * not support the requested component it should return a null value and the
 * runtime will attempt to directly instantiate and manage the component.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface ComponentProvider {
    /**
     * The scope contract for the instantiation of a component.
     */
    public enum Scope {
        /**
         * Declares that only one instance of a component shall exist 
         * per-runtime instance.
         */
        Singleton,
        
        /**
         * Declares that the scope is application defined and instances will 
         * be managed by the runtime according to this scope. This requires 
         * that a new instance be created for each invocation of 
         * <code>getInstance</code>.
         */
        PerRequest,
        
        /**
         * The runtime does not care what the scope is,
         * the component provider can decide which to choose - the component
         * provider is responsible for managing instances of a type.
         */
        Undefined;

        /**
         * A immutable list comprising of the scopes Undefined and
         * Singleton, in that order.
         */
        public static final List<Scope> UNDEFINED_SINGLETON = Collections.unmodifiableList(
                Arrays.asList(Scope.Undefined, Scope.Singleton));
        
        /**
         * A immutable list comprising of the scopes PerRequest, Undefined and
         * Singleton, in that order.
         */
        public static final List<Scope> PERREQUEST_UNDEFINED_SINGLETON = Collections.unmodifiableList(
                Arrays.asList(Scope.PerRequest, Scope.Undefined, Scope.Singleton));
        
        /**
         * A immutable list comprising of the scopes PerRequest and  
         * Undefined, in that order.
         */
        public static final List<Scope> PERREQUEST_UNDEFINED = Collections.unmodifiableList(
                Arrays.asList(Scope.PerRequest, Scope.Undefined));
    }
    
        
    /**
     * Get the instance of a class. Injection will be performed on the
     * instance.
     * 
     * @param <T> the type to inject.
     * @param scope the scope of the instance
     * @param c the class
     * @return the instance, or null if the component cannot be instantaited
     *         and managed.
     * 
     * @throws java.lang.InstantiationException if the component could not be
     *         instantiated.
     * @throws java.lang.IllegalAccessException if there was an error accessing
     *         the definition of the component class.
     */
    <T> T getInstance(Scope scope, Class<T> c) 
            throws InstantiationException, IllegalAccessException;
    
    /**
     * Get the instance of a class using a constructor and a corresponding
     * array of parameter values. Injection will be performed on the instance.
     * <p>
     * The array of parameter values must be the same length as that required
     * by the constructor. Some parameter values may be null, indicating that
     * the values are not set and must be set by the component provider before
     * construction occurs.
     * 
     * @param <T> the type to inject.
     * @param scope the scope of the instance
     * @param contructor the constructor to instantiate the class
     * @param parameters the array parameter values passed to the constructor
     * @return the instance, or null if the component cannot be instantaited
     *         and managed.
     * 
     * @throws java.lang.InstantiationException if the component could not be
     *         instantiated.
     * @throws java.lang.IllegalAccessException if there was an error accessing
     *         the definition of the component constructor.
     * @throws java.lang.IllegalArgumentException if the number of parameters
     *          differ or a parameter instance does not conform to the
     *          approprate type.
     * @throws java.lang.reflect.InvocationTargetException
     */
    <T> T getInstance(Scope scope, Constructor<T> contructor, Object[] parameters) 
            throws InstantiationException, IllegalArgumentException, 
            IllegalAccessException, InvocationTargetException;

    /**
     * Get the instance of a class. Injection will be performed on the
     * instance. Additional context is provided that may be used to determine
     * the instance to return.
     * <p>
     * Implementations wishing to ignore the component context may defer 
     * to the implemented method {@link #getInstance(Scope, Class)}.
     * 
     * @param <T> the type to inject.
     * @param cc the component context
     * @param scope the scope of the instance
     * @param c the class
     * @return the instance, or null if the component cannot be instantaited
     *         and managed.
     * 
     * @throws java.lang.InstantiationException if the component could not be
     *         instantiated.
     * @throws java.lang.IllegalAccessException if there was an error accessing
     *         the definition of the component class.
     */
    <T> T getInstance(ComponentContext cc, Scope scope, Class<T> c) 
            throws InstantiationException, IllegalAccessException;
    
    /**
     * Get the injectable instance to inject onto fields of the instance.
     * <p>
     * If the injectable instance is the same as the instance that was passed in
     * then the provider MUST return that instance.
     * 
     * @param <T> the type to inject.
     * @param instance the instance returned by one of the getInstance methods.
     * @return the injectable instance.
     */
    <T> T getInjectableInstance(T instance);
    
    /**
     * Perform injection on an instance. This may be used when a
     * component is instantiated by means other than the component
     * provider.
     * 
     * @param instance the instance to perform injection on.
     */
    void inject(Object instance);
}
