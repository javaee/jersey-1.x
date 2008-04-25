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
import java.io.File;
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
 * searches for root resource classes and providers in the paths declared by 
 * the property {@link ClasspathResourceConfig#PROPERTY_CLASSPATH}. 
 * That property MUST be included in the map of initial properties passed to 
 * the constructor.
 * 
 * @author Frank D. Martinez. fmartinez@asimovt.com
 */
public class ClasspathResourceConfig extends DefaultResourceConfig {
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
    public static final String PROPERTY_CLASSPATH
            = "com.sun.ws.rest.config.property.classpath";
    
    private static final Logger LOGGER = 
            Logger.getLogger(ClasspathResourceConfig.class.getName());

    private final String[] paths;
    
    public ClasspathResourceConfig() {
        this(getPaths());
    }
    
    /**
     * @param props the property bag that contains the property 
     *        {@link ClasspathResourceConfig#PROPERTY_CLASSPATH}. 
     */
    public ClasspathResourceConfig(Map<String, Object> props) {
        this(getPaths(props));
        
        getProperties().putAll(props);
    }

    /**
     * @param paths the array paths consisting of either jar files or
     *        directories containing jar files for class files.
     */
    public ClasspathResourceConfig(String[] paths) {
        super();
        
        if (paths == null || paths.length == 0)
            throw new IllegalArgumentException(
                    "Array of paths must not be null or empty");

        this.paths = paths.clone();
        
        init(paths);
    }
    
    /**
     * Perform a new search for resource classes and provider classes.
     */
    public void reload() {
        getResourceClasses().clear();
        getProviderClasses().clear();
        init(paths);
    }
    
    private void init(String[] paths) {    
        File[] roots = new File[paths.length];
        for (int i = 0;  i< paths.length; i++) {
            roots[i] = new File(paths[i]);
        }

        if (LOGGER.isLoggable(Level.INFO)) {
            StringBuilder b = new StringBuilder();
            b.append("Scanning for root resource and provider classes in the paths:");
            for (String p : paths)
                b.append('\n').append("  ").append(p);
            
            LOGGER.log(Level.INFO, b.toString());            
        }
        
        AnnotatedClassScanner scanner = new AnnotatedClassScanner(
                Path.class, Provider.class);
        scanner.scan(roots);

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
    
    private static String[] getPaths() {
        String classPath = System.getProperty("java.class.path");
        return classPath.split(File.pathSeparator);                
    }
    
    private static String[] getPaths(Map<String, Object> props) {
        Object v = props.get(PROPERTY_CLASSPATH);
        if (v == null)
            throw new IllegalArgumentException(PROPERTY_CLASSPATH + 
                    " property is missing");
        
        String[] paths = getPaths(v);
        if (paths.length == 0)
            throw new IllegalArgumentException(PROPERTY_CLASSPATH + 
                    " contains no paths");
        
        return paths;
    }
    
    private static String[] getPaths(Object param) {
        if (param instanceof String) {
            return getPaths((String)param);
        } else if (param instanceof String[]) {
            return getPaths((String[])param);
        } else {
            throw new IllegalArgumentException(PROPERTY_CLASSPATH + " must " +
                    "have a property value of type String or String[]");
        }
    }
    
    private static String[] getPaths(String[] elements) {
        List<String> paths = new LinkedList<String>();
        for (String element : elements) {
            if (element == null || element.length() == 0) continue;
            Collections.addAll(paths, getPaths(element));
        }
        return paths.toArray(new String[paths.size()]);
    }
    
    private static String[] getPaths(String paths) {
        return paths.split(";");
    } 
}