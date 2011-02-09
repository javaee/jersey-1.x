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
package com.sun.jersey.core.spi.scanning.uri;

import com.sun.jersey.core.spi.scanning.JarFileScanner;
import com.sun.jersey.core.spi.scanning.ScannerException;
import com.sun.jersey.core.spi.scanning.ScannerListener;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.UriBuilder;

/**
 * A JBoss-based "vfsfile" and "vfszip" scheme URI scanner.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class VfsSchemeScanner implements UriSchemeScanner {

    public Set<String> getSchemes() {
        return new HashSet<String>(Arrays.asList("vfsfile", "vfszip"));
    }

    // UriSchemeScanner
    
    public void scan(final URI u, final ScannerListener sl) {
        if (u.getScheme().equalsIgnoreCase("vfsfile")) {
            new FileSchemeScanner().scan(
                    UriBuilder.fromUri(u).scheme("file").build(),
                    sl);
        } else {
            final String su = u.toString();
            final int webInfIndex = su.indexOf("/WEB-INF/classes");
            if (webInfIndex != -1) {
                final String war = su.substring(0, webInfIndex);
                final String path = su.substring(webInfIndex + 1);

                final int warParentIndex = war.lastIndexOf('/');
                final String warParent = su.substring(0, warParentIndex);

                // Check is there is a war within an ear
                // If so we need to load the ear then obtain the InputStream
                // of the entry to the war
                if (warParent.endsWith(".ear")) {
                    final String warName = su.substring(warParentIndex + 1, war.length());
                    try {
                        JarFileScanner.scan(new URL(warParent.replace("vfszip", "file")).openStream(), "",
                                new ScannerListener() {
                            public boolean onAccept(String name) {
                                return name.equals(warName);
                            }

                            public void onProcess(String name, InputStream in) throws IOException {
                                // This is required so that the underlying ear
                                // is not closed
                                in = new FilterInputStream(in) {
                                    public void close() throws IOException {};
                                };
                                try {
                                    JarFileScanner.scan(in, path, sl);
                                } catch (IOException ex) {
                                    throw new ScannerException("IO error when scanning war " + u, ex);
                                }
                            }
                        });
                    } catch (IOException ex) {
                        throw new ScannerException("IO error when scanning war " + u, ex);
                    }
                } else {
                    try {
                        JarFileScanner.scan(new URL(war.replace("vfszip", "file")).openStream(), path, sl);
                    } catch (IOException ex) {
                        throw new ScannerException("IO error when scanning war " + u, ex);
                    }
                }
            } else {
                try {
                    JarFileScanner.scan(new URL(su).openStream(), "", sl);
                } catch (IOException ex) {
                    throw new ScannerException("IO error when scanning jar " + u, ex);
                }
            }
        }
    }
}
