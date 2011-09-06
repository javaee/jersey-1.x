/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.api.core.servlet;

import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.api.core.ScanningResourceConfig;
import com.sun.jersey.spi.scanning.servlet.WebAppResourcesScanner;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A mutable implementation of {@link DefaultResourceConfig} that dynamically 
 * searches for root resource and provider classes in the Web application 
 * resource paths declared by the property
 * {@link ClasspathResourceConfig#PROPERTY_CLASSPATH}.
 * If that property is not included in the map of initial properties passed to
 * the constructor then the Web application paths "WEB-INF/lib" and
 * "WEB-INF/classes" are utlized.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WebAppResourceConfig extends ScanningResourceConfig {

    private static final Logger LOGGER =
            Logger.getLogger(WebAppResourceConfig.class.getName());

    /**
     * @param props the property bag that contains the property 
     *        {@link ClasspathResourceConfig#PROPERTY_CLASSPATH}.
     * @param sc the servlet context.
     */
    public WebAppResourceConfig(Map<String, Object> props, ServletContext sc) {
        this(getPaths(props), sc);
        
        setPropertiesAndFeatures(props);
    }

    /**
     * @param paths the array paths consisting of either jar files or
     *        directories containing jar files for class files.
     * @param sc the servlet context.
     */
    public WebAppResourceConfig(String[] paths, ServletContext sc) {
        if (paths == null || paths.length == 0)
            throw new IllegalArgumentException(
                    "Array of paths must not be null or empty");

        init(paths, sc);
    }

    private void init(String[] paths, ServletContext sc) {
        if (LOGGER.isLoggable(Level.INFO)) {
            StringBuilder b = new StringBuilder();
            b.append("Scanning for root resource and provider classes in the Web app resource paths:");
            for (String p : paths)
                b.append('\n').append("  ").append(p);

            LOGGER.log(Level.INFO, b.toString());
        }

        init(new WebAppResourcesScanner(paths, sc));
    }

    private static String[] getPaths(Map<String, Object> props) {
        Object v = props.get(ClasspathResourceConfig.PROPERTY_CLASSPATH);
        if (v == null) {
            return new String[]{ "/WEB-INF/lib", "/WEB-INF/classes" };
        }

        String[] paths = getPaths(v);
        if (paths.length == 0)
            throw new IllegalArgumentException(
                    ClasspathResourceConfig.PROPERTY_CLASSPATH +
                    " contains no paths");
        
        return paths;
    }
    
    private static String[] getPaths(Object param) {
        if (param instanceof String) {
            return getElements(new String[] { (String)param });
        } else if (param instanceof String[]) {
            return getElements((String[])param);
        } else {
            throw new IllegalArgumentException(
                    ClasspathResourceConfig.PROPERTY_CLASSPATH + " must " +
                    "have a property value of type String or String[]");
        }
    }
}