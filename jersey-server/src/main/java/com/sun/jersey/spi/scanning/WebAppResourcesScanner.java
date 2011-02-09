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
package com.sun.jersey.spi.scanning;

import com.sun.jersey.core.spi.scanning.JarFileScanner;
import com.sun.jersey.core.spi.scanning.Scanner;
import com.sun.jersey.core.spi.scanning.ScannerException;
import com.sun.jersey.core.spi.scanning.ScannerListener;
import com.sun.jersey.core.util.Closing;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * A scanner that recursively scans resources within a Web application.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WebAppResourcesScanner implements Scanner {

    private final String[] paths;
    private final ServletContext sc;

    /**
     * Scan from a set of web resource paths.
     * <p>
     *
     * @param paths an array of web resource paths.
     * @param sc
     */
    public WebAppResourcesScanner(final String[] paths, final ServletContext sc) {
        this.paths = paths;
        this.sc = sc;
    }

    // Scanner
    
    public void scan(final ScannerListener cfl) {
        for (final String path : paths) {
            scan(path, cfl);
        }
    }

    private void scan(final String root, final ScannerListener cfl) {
        final Set<String> resourcePaths = sc.getResourcePaths(root);
        if(resourcePaths == null)
            return;
        for (final String resourcePath : resourcePaths) {
            if (resourcePath.endsWith("/")) {
                scan(resourcePath, cfl);
            } else if (resourcePath.endsWith(".jar")) {
                try {
                    new Closing(sc.getResourceAsStream(resourcePath)).f(new Closing.Closure() {
                        public void f(final InputStream in) throws IOException {
                            JarFileScanner.scan(in, "", cfl);
                        }
                    });
                } catch (IOException ex) {
                    throw new ScannerException("IO error scanning jar " + resourcePath, ex);
                }
            } else if (cfl.onAccept(resourcePath)) {
                try {
                    new Closing(sc.getResourceAsStream(resourcePath)).f(new Closing.Closure() {
                        public void f(final InputStream in) throws IOException {
                            cfl.onProcess(resourcePath, in);
                        }
                    });
                } catch (IOException ex) {
                    throw new ScannerException("IO error scanning resource " + resourcePath, ex);
                }
            }
        }
    }
}
