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
package com.sun.jersey.core.spi.scanning.uri;

import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.core.util.Closing;
import com.sun.jersey.core.spi.scanning.JarFileScanner;
import com.sun.jersey.core.spi.scanning.ScannerException;
import com.sun.jersey.core.spi.scanning.ScannerListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A "jar" and "zip" scheme URI scanner that recursively jar files.
 * Jar entries are reported to a {@link ScannerListener}.
 *
 * @author Paul.Sandoz@Sun.Com
 * @author gerard.davison@oracle.com
 */
public class JarZipSchemeScanner implements UriSchemeScanner {

    public Set<String> getSchemes() {
        return new HashSet<String>(Arrays.asList("jar", "zip"));
    }

    public void scan(final URI u, final ScannerListener cfl) {
        final String ssp = u.getRawSchemeSpecificPart();
        final String jarUrlString = ssp.substring(0, ssp.lastIndexOf('!'));
        final String parent = ssp.substring(ssp.lastIndexOf('!') + 2);
        try {
            closing(jarUrlString).f(new Closing.Closure() {

                public void f(final InputStream in) throws IOException {
                    JarFileScanner.scan(in, parent, cfl);
                }
            });
        } catch (IOException ex) {
            throw new ScannerException("IO error when scanning jar " + u, ex);
        }
    }

    /**
     * Obtain a {@link Closing} of the jar file.
     * <p>
     * For most platforms the format for the zip or jar follows the form of
     * the <a href="http://docs.sun.com/source/819-0913/author/jar.html#jarprotocol"jar protcol.</a></p>
     * <ul>
     *   <li><code>jar:file:///tmp/fishfingers.zip!/example.txt</code></li>
     *   <li><code>zip:http://www.example.com/fishfingers.zip!/example.txt</code></li>
     * </ul>
     * <p>
     * On versions of the WebLogic application server a proprietary format is
     * supported of the following form, which assumes a zip file located on
     * the local file system:
     * </p>
     * <ul>
     *   <li><code>zip:/tmp/fishfingers.zip!/example.txt</code></li>
     *   <li><code>zip:d:/tempfishfingers.zip!/example.txt</code></li>
     * </ul>
     * <p>
     * This method will first attempt to create a {@link Closing} as follows:
     * <pre>
     *   new Closing(new URL(jarUrlString).openStream());
     * </pre>
     * if that fails with a {@link MalformedURLException} then the method will
     * attempt to create a {@link Closing} instance as follows:
     * <pre>
     *  return new Closing(new FileInputStream(
     *      UriComponent.decode(jarUrlString, UriComponent.Type.PATH)));
     * </pre>
     *
     * @param jarUrlString the raw scheme specific part of a URI minus the jar
     *        entry
     * @return a {@link Closing}.
     * @throws IOException if there is an error opening the stream.
     */
    protected Closing closing(String jarUrlString) throws IOException {
        try {
            return new Closing(new URL(jarUrlString).openStream());
        } catch (MalformedURLException ex) {
            return new Closing(new FileInputStream(
                    UriComponent.decode(jarUrlString, UriComponent.Type.PATH)));
        }
    }
}
