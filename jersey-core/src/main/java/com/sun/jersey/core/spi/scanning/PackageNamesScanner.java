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

import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.api.uri.UriComponent.Type;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.spi.scanning.uri.FileSchemeScanner;
import com.sun.jersey.core.spi.scanning.uri.JarZipSchemeScanner;
import com.sun.jersey.core.spi.scanning.uri.UriSchemeScanner;
import com.sun.jersey.core.spi.scanning.uri.VfsSchemeScanner;
import com.sun.jersey.spi.service.ServiceFinder;
import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * A scanner that recursively scans URI-based resources present in a set of
 * package names, and sub-package names of that set.
 * <p>
 * The URIs for a package name are obtained by invoking
 * {@link ClassLoader#getResources(java.lang.String) } with the parameter that
 * is the package name with "." replaced by "/".
 * <p>
 * Each URI is then scanned using a registered {@link UriSchemeScanner} that
 * supports the URI scheme.
 * <p>
 * The following are registered.
 * The {@link FileSchemeScanner} for "file" URI schemes.
 * The {@link JarZipSchemeScanner} for "jar" or "zip" URI schemes to jar
 * resources.
 * The {@link VfsSchemeScanner} for the JBoss-based "vfsfile" and "vfszip"
 * URI schemes.
 * <p>
 * If a URI scheme is not supported a {@link ScannerException} will be thrown
 * and package scanning deployment will fail.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class PackageNamesScanner implements Scanner {

    private final String[] packages;
    private final ClassLoader classloader;
    private final Map<String, UriSchemeScanner> scanners;

    /**
     * Scan from a set of packages using the context class loader.
     *
     * @param packages an array of package names.
     */
    public PackageNamesScanner(final String[] packages) {
        this(ReflectionHelper.getContextClassLoader(), packages);
    }

    /**
     * Scan from a set of packages using declared class loader.
     * 
     * @param classloader the class loader to load classes from.
     * @param packages an array of package names.
     */
    public PackageNamesScanner(final ClassLoader classloader, final String[] packages) {
        this.packages = packages;
        this.classloader = classloader;

        this.scanners = new HashMap<String, UriSchemeScanner>();
        add(new JarZipSchemeScanner());
        add(new FileSchemeScanner());
        add(new VfsSchemeScanner());

        for (UriSchemeScanner s : ServiceFinder.find(UriSchemeScanner.class)) {
            add(s);
        }
    }

    private void add(final UriSchemeScanner ss) {
        for (final String s : ss.getSchemes()) {
            scanners.put(s.toLowerCase(), ss);
        }
    }

    public void scan(final ScannerListener cfl) {
        for (final String p : packages) {
            try {
                final Enumeration<URL> urls = PackageURLProvider.getInstance().getPackageURLs(classloader, p.replace('.', '/'));
                while (urls.hasMoreElements()) {
                    try {
                        scan(toURI(urls.nextElement()), cfl);
                    } catch (URISyntaxException ex) {
                        throw new ScannerException("Error when converting a URL to a URI", ex);
                    }
                }
            } catch (IOException ex) {
                throw new ScannerException("IO error when package scanning jar", ex);
            }
        }
    }

    public static abstract class PackageURLProvider {

        private static volatile PackageURLProvider provider;

        private static PackageURLProvider getInstance() {
            // Double-check idiom for lazy initialization
            PackageURLProvider result = provider;

            if (result == null) { // first check without locking
                synchronized (PackageURLProvider.class) {
                    result = provider;
                    if (result == null) { // second check with locking
                        provider = result = new PackageURLProvider() {

                            @Override
                            public Enumeration<URL> getPackageURLs(ClassLoader cl, String pkgName) throws IOException {
                                return cl.getResources(pkgName);
                            }
                        };

                    }
                }

            }
            return result;
        }

        private static void setInstance(PackageURLProvider provider) throws SecurityException {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                ReflectPermission rp = new ReflectPermission("suppressAccessChecks");
                security.checkPermission(rp);
            }
            synchronized (PackageURLProvider.class) {
                PackageURLProvider.provider = provider;
            }
        }

        /*
         * creates an enumeration of URLs, where the package with given pkgName could be found
         */
        public abstract Enumeration<URL> getPackageURLs(ClassLoader cl, String pkgName) throws IOException;
    }

    /**
     * Gives you a chance to change the default package URL lookup mechanism, by registering a custom
     * <tt>PackageURLProvider</tt>. The call has to be made prior to any lookup attempts,
     * otherwise the default method would be used.
     *
     * @param provider
     * @throws SecurityException
     */
    public static void setPackageURLProvider(PackageURLProvider provider) throws SecurityException {
        PackageURLProvider.setInstance(provider);
    }


    private void scan(final URI u, final ScannerListener cfl) {
        final UriSchemeScanner ss = scanners.get(u.getScheme().toLowerCase());
        if (ss != null) {
            ss.scan(u, cfl);
        } else {
            throw new ScannerException("The URI scheme " + u.getScheme() +
                    " of the URI " + u +
                    " is not supported. Package scanning deployment is not" + 
                    " supported for such URIs." +
                    "\nTry using a different deployment mechanism such as" +
                    " explicitly declaring root resource and provider classes" +
                    " using an extension of javax.ws.rs.core.Application");
        }
    }

    private URI toURI(URL url) throws URISyntaxException {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            // Work around bug where some URLs are incorrectly encoded.
            // This can occur when certain class loaders are utilized
            // to obtain URLs for resources.
            return URI.create(toExternalForm(url));
        }
    }

    private String toExternalForm(URL u) {

        // pre-compute length of StringBuffer
        int len = u.getProtocol().length() + 1;
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            len += 2 + u.getAuthority().length();
        }
        if (u.getPath() != null) {
            len += u.getPath().length();
        }
        if (u.getQuery() != null) {
            len += 1 + u.getQuery().length();
        }
        if (u.getRef() != null) {
            len += 1 + u.getRef().length();
        }

        StringBuffer result = new StringBuffer(len);
        result.append(u.getProtocol());
        result.append(":");
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            result.append("//");
            result.append(u.getAuthority());
        }
        if (u.getPath() != null) {
            result.append(UriComponent.contextualEncode(u.getPath(), Type.PATH));
        }
        if (u.getQuery() != null) {
            result.append('?');
            result.append(UriComponent.contextualEncode(u.getQuery(), Type.QUERY));
        }
        if (u.getRef() != null) {
            result.append("#");
            result.append(u.getRef());
        }
        return result.toString();
    }

}
