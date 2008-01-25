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

package com.sun.ws.rest.api.core;

import com.sun.ws.rest.impl.container.config.AnnotatedClassScanner;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

/**
 * A mutable implementation of {@link DefaultResourceConfig} that dynamically 
 * searches for root resource classes given a set of package names.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class PackagesResourceConfig extends DefaultResourceConfig {
    /**
     * The property value MUST be an instance String or String[]. Each String
     * instance represents one or more paths that MUST be separated by ';'. 
     * Each path MUST be an absolute or relative directory, or a Jar file. 
     * The contents of a directory, including Java class files, jars files 
     * and sub-directories (recusively) are scanned. The Java class files of 
     * a jar file are scanned.
     * <p>
     * Root resource clases MUST be present in the Java class path.
     */
    public static final String PROPERTY_PACKAGES
            = "com.sun.ws.rest.config.property.packages";
    
    private static final Logger LOGGER = 
            Logger.getLogger(PackagesResourceConfig.class.getName());

    /**
     * @param packages the array packages
     */
    public PackagesResourceConfig(String[] packages) {
        if (packages == null || packages.length == 0)
            throw new IllegalArgumentException("Array of packages must not be null or empty");
        
        init(packages);
    }

    /**
     * @param props the property bag that contains the property 
     *        {@link PackagesResourceConfig#PROPERTY_PACKAGES}. 
     */
    public PackagesResourceConfig(Map<String, Object> props) {
        this(getPackages(props));
        
        getProperties().putAll(props);
    }
    
    private void init(String[] packages) {
        if (LOGGER.isLoggable(Level.INFO)) {
            StringBuilder b = new StringBuilder();
            b.append("Scanning for root resource and provider classes in the packages:");
            for (String p : packages)
                b.append('\n').append("  ").append(p);
            
            LOGGER.log(Level.INFO, b.toString());
        }
        
        AnnotatedClassScanner scanner = new AnnotatedClassScanner(
                Path.class, Provider.class);
        scanner.scan(packages);        
        
        getResourceClasses().addAll(scanner.getMatchingClasses(Path.class));
        getProviderClasses().addAll(scanner.getMatchingClasses(Provider.class));
        
        if (LOGGER.isLoggable(Level.INFO) && !getResourceClasses().isEmpty()) {
            StringBuilder b = new StringBuilder();
            b.append("Root resource classes found:");
            for (Class c : getResourceClasses())
                b.append('\n').append("  ").append(c);
            
            LOGGER.log(Level.INFO, b.toString());
            
            b = new StringBuilder();
            b.append("Provider classes found:");
            for (Class c : getProviderClasses())
                b.append('\n').append("  ").append(c);
            
            LOGGER.log(Level.INFO, b.toString());            
        }
    }
    
    private static String[] getPackages(Map<String, Object> props) {
        Object v = props.get(PROPERTY_PACKAGES);
        if (v == null)
            throw new IllegalArgumentException(PROPERTY_PACKAGES + 
                    " property is missing");
        
        String[] packages = getPackages(v);
        if (packages.length == 0)
            throw new IllegalArgumentException(PROPERTY_PACKAGES + 
                    " contains no packages");
        
        return packages;
    }
    
    private static String[] getPackages(Object param) {
        if (param instanceof String) {
            return getPackages((String)param);
        } else if (param instanceof String[]) {
            return getPackages((String[])param);
        } else {
            throw new IllegalArgumentException(PROPERTY_PACKAGES + " must " +
                    "have a property value of type String or String[]");
        }
    }
    
    private static String[] getPackages(String[] elements) {
        List<String> paths = new LinkedList<String>();
        for (String element : elements) {
            if (element == null || element.length() == 0) continue;
            Collections.addAll(paths, element);
        }
        return paths.toArray(new String[paths.size()]);
    }
    
    private static String[] getPackages(String paths) {
        return paths.split(";");
    }     
}
