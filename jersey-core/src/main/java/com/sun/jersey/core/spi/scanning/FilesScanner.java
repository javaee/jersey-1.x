/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.core.spi.scanning;

import com.sun.jersey.core.util.Closing;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A scanner that recursively scans directories and jar files. 
 * Files or jar entries are reported to a {@link ScannerListener}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class FilesScanner implements Scanner {

    private final File[] files;

    /**
     * Scan from a set of files.
     * 
     * @param files an array of files that are either directories or jar files
     *        ending in the suffix '.jar' or '.zip'. Any other type of file
     *        is ignored.
     */
    public FilesScanner(final File[] files) {
        this.files = files;
    }

    // Scanner
    
    public void scan(final ScannerListener cfl) {
        for (final File f : files) {
            scan(f, cfl);
        }
    }

    private void scan(final File f, final ScannerListener cfl) {
        if (f.isDirectory()) {
            scanDir(f, cfl);
        } else if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) {
            try {
                JarFileScanner.scan(f, "", cfl);
            } catch (IOException ex) {
                throw new ScannerException("IO error when scanning jar file " + f, ex);
            }
        } else {
            // TODO log
        }
    }

    private void scanDir(final File root, final ScannerListener cfl) {
        for (final File child : root.listFiles()) {
            if (child.isDirectory()) {
                scanDir(child, cfl);
            } else if (child.getName().endsWith(".jar")) {
                try {
                    JarFileScanner.scan(child, "", cfl);
                } catch (IOException ex) {
                    throw new ScannerException("IO error when scanning jar file " + child, ex);
                }
            } else if (cfl.onAccept(child.getName())) {
                try {
                    new Closing(new BufferedInputStream(new FileInputStream(child))).f(new Closing.Closure() {

                        public void f(InputStream in) throws IOException {
                            cfl.onProcess(child.getName(), in);
                        }
                    });
                } catch (IOException ex) {
                    throw new ScannerException("IO error when scanning file " + child, ex);
                }
            }
        }
    }
}
