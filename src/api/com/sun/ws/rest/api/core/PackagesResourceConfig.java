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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A mutable implementation of {@link DefaultResourceConfig} that dynamically 
 * searches for root resource classes given a set of package names.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class PackagesResourceConfig extends DefaultResourceConfig {

    /**
     * @param packages the array packages
     */
    public PackagesResourceConfig(String[] packages) {
        if (packages == null || packages.length == 0)
            throw new IllegalArgumentException("Array of paths must not be null or empty");
        
        init(packages);
    }

    private void init(String[] packages) {
        ResourceClassScanner scanner = new ResourceClassScanner(javax.ws.rs.Path.class);
        Set<Class> classes = scanner.scan(packages);

        Logger logger = Logger.getLogger(PackagesResourceConfig.class.getName());
        Set<Class> resources = getResourceClasses();
        logger.log(Level.INFO, "Scanning for resource classes ...");
        for (Class cls : classes) {
            resources.add(cls);
            logger.log(Level.INFO, "Resource class: " + cls.getName() + " Loaded.");
        }
        logger.log(Level.INFO, "All resource classes loaded.");
    }
}
