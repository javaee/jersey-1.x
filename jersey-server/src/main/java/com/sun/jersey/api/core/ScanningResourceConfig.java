/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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

import com.sun.jersey.core.spi.scanning.Scanner;
import com.sun.jersey.spi.container.ReloadListener;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;
import com.sun.jersey.spi.scanning.PathProviderScannerListener;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A resource configuration that performs scanning to find root resource
 * and provider classes.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ScanningResourceConfig extends DefaultResourceConfig implements ReloadListener {
    private static final Logger LOGGER = 
            Logger.getLogger(ScanningResourceConfig.class.getName());

    private Scanner scanner;

    private final Set<Class<?>> cachedClasses = new HashSet<Class<?>>();

    /**
     * Initialize and scan for root resource and provider classes
     * using a scanner.
     *
     * @param scanner the scanner.
     */
    public void init(final Scanner scanner) {
        this.scanner = scanner;

        final AnnotationScannerListener asl = new PathProviderScannerListener();
        scanner.scan(asl);

        getClasses().addAll(asl.getAnnotatedClasses());
        
        if (LOGGER.isLoggable(Level.INFO) && !getClasses().isEmpty()) {
            final Set<Class> rootResourceClasses = get(Path.class);
            if (rootResourceClasses.isEmpty()) {
                LOGGER.log(Level.INFO, "No root resource classes found.");
            } else {
                logClasses("Root resource classes found:", rootResourceClasses);
            }

            final Set<Class> providerClasses = get(Provider.class);
            if (providerClasses.isEmpty()) {
                LOGGER.log(Level.INFO, "No provider classes found.");
            } else {
                logClasses("Provider classes found:", providerClasses);
            }

        }

        cachedClasses.clear();
        cachedClasses.addAll(getClasses());
    }

    /**
     * Perform a new search for resource classes and provider classes.
     * <p/>
     * Deprecated, use onReload instead.
     */
    @Deprecated
    public void reload() {
        onReload();
    }

    /**
     * Perform a new search for resource classes and provider classes.
     */
    @Override
    public void onReload() {
        Set<Class<?>> classesToRemove = new HashSet<Class<?>>();
        Set<Class<?>> classesToAdd = new HashSet<Class<?>>();

        for(Class c : getClasses())
            if(!cachedClasses.contains(c))
                classesToAdd.add(c);

        for(Class c : cachedClasses)
            if(!getClasses().contains(c))
                classesToRemove.add(c);

        getClasses().clear();

        init(scanner);

        getClasses().addAll(classesToAdd);
        getClasses().removeAll(classesToRemove);
    }

    private Set<Class> get(Class<? extends Annotation> ac) {
        Set<Class> s = new HashSet<Class>();
        for (Class c : getClasses())
            if (c.isAnnotationPresent(ac))
                s.add(c);
        return s;
    }

    private void logClasses(String s, Set<Class> classes) {
        final StringBuilder b = new StringBuilder();
        b.append(s);
        for (Class c : classes)
            b.append('\n').append("  ").append(c);

        LOGGER.log(Level.INFO, b.toString());
    }
}