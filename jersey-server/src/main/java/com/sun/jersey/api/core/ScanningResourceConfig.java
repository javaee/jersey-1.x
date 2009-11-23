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

package com.sun.jersey.api.core;

import com.sun.jersey.core.spi.scanning.Scanner;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;
import com.sun.jersey.spi.scanning.PathProviderScannerListener;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

/**
 * A resource configuration that performs scannning to find root resource
 * and provider classes.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ScanningResourceConfig extends DefaultResourceConfig {
    private static final Logger LOGGER = 
            Logger.getLogger(ScanningResourceConfig.class.getName());

    /**
     * Initialize and scan for root resource and provider classes
     * using a scanner.
     *
     * @param scanner the scanner.
     */
    public void init(final Scanner scanner) {
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