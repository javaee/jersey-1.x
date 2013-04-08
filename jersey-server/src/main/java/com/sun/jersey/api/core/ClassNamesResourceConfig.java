/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.api.core;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A mutable implementation of {@link DefaultResourceConfig} that explicitly
 * declares for root resource and provider classes.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ClassNamesResourceConfig extends DefaultResourceConfig {

    /**
     * The property value MUST be an instance String or String[]. Each String
     * instance represents one or more class names that MUST be separated by ';',
     * ',' or ' ' (space).
     */
    public static final String PROPERTY_CLASSNAMES = "com.sun.jersey.config.property.classnames";

    /**
     * Declare an array of root resource and provider classes.
     *
     * @param classes the array of classes.
     */
    public ClassNamesResourceConfig(Class... classes) {
        super();
        
        for (Class c : classes)
            getClasses().add(c);
    }
    
    /**
     * Declare root resource and provider class as an array of class names.
     *
     * @param classNames the array of classes.
     */
    public ClassNamesResourceConfig(String... classNames) {
        super(getClasses(classNames));
    }
    
    /**
     * Declare root resource and provider classes declaring the class names as a
     * property of {@link ResourceConfig}.
     *
     * @param props the property bag that contains the property
     *        {@link #PROPERTY_CLASSNAMES}.
     */
    public ClassNamesResourceConfig(Map<String, Object> props) {
        super(getClasses(props));
        setPropertiesAndFeatures(props);
    }

    private static Set<Class<?>> getClasses(Map<String, Object> props) {
        Object v = props.get(PROPERTY_CLASSNAMES);
        if (v == null) {
            throw new IllegalArgumentException(PROPERTY_CLASSNAMES + " property is missing");
        }
        Set<Class<?>> s = getClasses(v);
        if (s.isEmpty()) {
            throw new IllegalArgumentException(PROPERTY_CLASSNAMES + " contains no classes");
        }
        return s;
    }

    private static Set<Class<?>> getClasses(Object param) {
        return convertToSet(_getClasses(param));
    }
    
    private static Set<Class<?>> getClasses(String[] elements) {
        return convertToSet(getElements(elements, ResourceConfig.COMMON_DELIMITERS));
    }

    private static Set<Class<?>> convertToSet(String[] classes) {
        Set<Class<?>> s = new LinkedHashSet<Class<?>>();
        for (String c : classes) {
            try {
                s.add(getClassLoader().loadClass(c));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return s;
    }
    
    private static String[] _getClasses(Object param) {
        if (param instanceof String) {
            return getElements(new String[] { (String)param }, ResourceConfig.COMMON_DELIMITERS);
        } else if (param instanceof String[]) {
            return getElements((String[])param, ResourceConfig.COMMON_DELIMITERS);
        } else {
            throw new IllegalArgumentException(PROPERTY_CLASSNAMES + " must " +
                    "have a property value of type String or String[]");
        }
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();        
        return (classLoader == null) ? ClassNamesResourceConfig.class.getClassLoader() : classLoader;
    }    
}
