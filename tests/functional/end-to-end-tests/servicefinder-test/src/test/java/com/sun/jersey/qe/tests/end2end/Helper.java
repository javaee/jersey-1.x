/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.qe.tests.end2end;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.jar.Manifest;

import com.sun.jersey.spi.service.ServiceFinder;

/**
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class Helper {

    private static final String MANIFEST = "META-INF/MANIFEST.MF";

    private static final String MODULE_VERSION = "META-INF/jersey-module-version";

    private Helper() {
    }

    public static Manifest getManifest(Class c) throws IOException {
        String resource = c.getName().replace(".", "/") + ".class";
        URL url = getResource(c.getClassLoader(), resource);
        if (url == null)
            throw new IOException("Resource not found: " + url);

        return getManifest(resource, url);
    }

    public static String getJerseyModuleVersion(final Class<?> service) {
        try {
            final String serviceClassName = service.getName().replace(".", "/") + ".class";
            URL moduleVersionURL = new URL(getResource(serviceClassName).toString().replace(serviceClassName, MODULE_VERSION));

            return new BufferedReader(new InputStreamReader(moduleVersionURL.openStream())).readLine();
        } catch (IOException ioe) {
            return null;
        }
    }

    private static URL getResource(ClassLoader loader, String name) throws IOException {
        if (loader == null)
            return getResource(name);
        else {
            final URL resource = loader.getResource(name);
            if (resource != null) {
                return resource;
            } else {
                return getResource(name);
            }
        }
    }

    private static Manifest getManifest(String name, URL serviceURL) throws IOException {
        return getManifest(getManifestURL(name, serviceURL));
    }

    private static URL getManifestURL(String name, URL serviceURL) throws IOException {
        return new URL(serviceURL.toString().replace(name, MANIFEST));
    }

    private static Manifest getManifest(URL url) throws IOException {
        final InputStream in = url.openStream();
        try {
            return new Manifest(in);
        } finally {
            in.close();
        }
    }

    private static URL getResource(String name) throws IOException {
        if (ServiceFinder.class.getClassLoader() != null)
            return ServiceFinder.class.getClassLoader().getResource(name);
        else
            return ClassLoader.getSystemResource(name);
    }

}
