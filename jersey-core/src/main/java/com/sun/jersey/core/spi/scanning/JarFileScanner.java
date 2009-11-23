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
package com.sun.jersey.core.spi.scanning;

import com.sun.jersey.core.util.Closing;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A utility class that scans entries in jar files.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class JarFileScanner {

    /**
     * Scan entries in a jar file.
     * <p>
     * An entry will be reported to the scanning listener if the entry is a
     * child of the parent path.
     *
     * @param f the jar file.
     * @param parent the parent path.
     * @param sl the scanning lister to report jar entries.
     * @throws IOException if an error occurred scanning the jar entries
     */
    public static void scan(final File f, final String parent, final ScannerListener sl) throws IOException {
        new Closing(new FileInputStream(f)).f(new Closing.Closure() {

            public void f(final InputStream in) throws IOException {
                scan(in, parent, sl);
            }
        });
    }

    /**
     * Scan entries in a jar file.
     * <p>
     * An entry will be reported to the scanning listener if the entry is a
     * child of the parent path.
     * 
     * @param in the jar file as an input stream.
     * @param parent the parent path.
     * @param sl the scanning lister to report jar entries.
     * @throws IOException if an error occurred scanning the jar entries
     */
    public static void scan(final InputStream in, final String parent, final ScannerListener sl) throws IOException {
        JarInputStream jarIn = null;
        try {
            jarIn = new JarInputStream(in);
            JarEntry e = jarIn.getNextJarEntry();
            while (e != null) {
                if (!e.isDirectory() && e.getName().startsWith(parent) && sl.onAccept(e.getName())) {
                    sl.onProcess(e.getName(), jarIn);
                }
                jarIn.closeEntry();
                e = jarIn.getNextJarEntry();
            }
        } finally {
            if (jarIn != null) {
                jarIn.close();
            }
        }
    }
}
