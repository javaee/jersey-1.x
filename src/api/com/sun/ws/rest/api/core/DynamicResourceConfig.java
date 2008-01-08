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

import com.sun.ws.rest.impl.container.config.ResourceClassScanner;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A mutable implementation of {@link DefaultResourceConfig} that dynamically 
 * searches for root resource classes in the paths declared by the property 
 * {@link ResourceConfig#PROPERTY_RESOURCE_PATHS}. That property MUST be 
 * included in the map of initial properties passed to the constructor.
 * <p>
 * Root resource classes MUST be present in the java class path.
 * 
 * @author Frank D. Martinez. fmartinez@asimovt.com
 */
public final class DynamicResourceConfig extends DefaultResourceConfig {

    /**
     * @param paths the array paths consisting of either jar files or
     *        directories containing jar files for class files.
     */
    public DynamicResourceConfig(String[] paths) {
        if (paths == null || paths.length == 0)
            throw new IllegalArgumentException("Array of paths must not be null or empty");
        
        init(paths);
    }
    /**
     * @param props the property bag that contains the property 
     *        {@link ResourceConfig#PROPERTY_RESOURCE_PATHS}. 
     */
    public DynamicResourceConfig(Map<String, Object> props) {
        super();
        getProperties().putAll(props);
        String[] paths = getPaths(props.get(PROPERTY_RESOURCE_PATHS));
        if (paths == null) {
            throw new IllegalArgumentException(PROPERTY_RESOURCE_PATHS + " Property is missing.");
        }
        else {
            init(paths);
        }
    }

    private void init(String[] paths) {    
        File[] roots = new File[paths.length];
        for (int i=0; i<paths.length; i++) {
            roots[i] = new File(paths[i]);
        }

        ResourceClassScanner scanner = new ResourceClassScanner(javax.ws.rs.Path.class);
        Set<Class> classes = scanner.scan(roots);

        Logger logger = Logger.getLogger(DynamicResourceConfig.class.getName());
        Set<Class> resources = getResourceClasses();
        logger.log(Level.INFO, "Scanning for resource classes ...");
        for (Class cls : classes) {
            resources.add(cls);
            logger.log(Level.INFO, "Resource class: " + cls.getName() + " Loaded.");
        }
        logger.log(Level.INFO, "All resource classes loaded.");
    }
    
    private String[] getPaths(Object param) {
        if (param == null) return null;
        if (param instanceof String) {
            return getPaths((String) param);
        }
        else if (param instanceof String[]) {
            return getPaths((String[]) param);
        }
        else {
            return null;
        }
    }
    
    private String[] getPaths(String[] param) {
        List<String> paths = new LinkedList<String>();
        for (String group : param) {
            String[] explode = getPaths(group);
            for (String e : explode) {
                paths.add(e);
            }
        }
        return paths.toArray(new String[paths.size()]);
    }
    
    private String[] getPaths(String param) {
        return param.split(";");
    }
    
}
