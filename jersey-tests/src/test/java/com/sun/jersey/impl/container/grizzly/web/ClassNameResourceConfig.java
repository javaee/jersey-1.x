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
package com.sun.jersey.impl.container.grizzly.web;

import com.sun.jersey.api.core.DefaultResourceConfig;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A mutable implementation of {@link DefaultResourceConfig} that explicitly
 * references classes.
 * 
 * TODO move to the API.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ClassNameResourceConfig extends DefaultResourceConfig {

    public static final String PROPERTY_CLASSNAMES = "com.sun.jersey.config.property.classnames";

    public ClassNameResourceConfig(Class... classes) {
        super();
        
        for (Class c : classes)
            getClasses().add(c);
    }
    
    public ClassNameResourceConfig(String[] names) {
        super(getClasses(names));
    }
    
    public ClassNameResourceConfig(Map<String, Object> props) {
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
            throw new IllegalArgumentException(PROPERTY_CLASSNAMES + " contains no paths");
        }
        return s;
    }

    private static Set<Class<?>> getClasses(Object param) {
        return convertToSet(_getClasses(param));
    }
    
    private static Set<Class<?>> getClasses(String[] elements) {
        return convertToSet(_getClasses(elements));
    }

    private static Set<Class<?>> convertToSet(String[] classes) {
        Set<Class<?>> s = new HashSet<Class<?>>();
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
            return _getClasses((String) param);
        } else if (param instanceof String[]) {
            return _getClasses((String[]) param);
        } else {
            throw new IllegalArgumentException(PROPERTY_CLASSNAMES + " must " + "have a property value of type String or String[]");
        }
    }

    private static String[] _getClasses(String[] elements) {
        List<String> paths = new LinkedList<String>();
        for (String element : elements) {
            if (element == null || element.length() == 0) {
                continue;
            }
            Collections.addAll(paths, _getClasses(element));
        }
        return paths.toArray(new String[paths.size()]);
    }

    private static String[] _getClasses(String paths) {
        return paths.split(";");
    }
    
    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();        
        return (classLoader == null) ? ClassNameResourceConfig.class.getClassLoader() : classLoader;
    }    
}
