/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.osgifunctionaltests;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sun.jersey.core.osgi.OsgiRegistry;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.core.spi.scanning.Scanner;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;
import com.sun.jersey.spi.scanning.PathProviderScannerListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.modifyBundle;

@RunWith(JUnit4TestRunner.class)
public class OsgiRegistryTest {
    @Inject
    @SuppressWarnings("UnusedDeclaration")
    protected BundleContext bundleContext;
    private boolean failed;

    @Configuration
    public static Option[] configuration() {
        return options(
                systemPackage("sun.misc"),

                // define maven repository
                repositories(
                        "http://repo1.maven.org/maven2",
                        "http://repository.apache.org/content/groups/snapshots-group",
                        "http://repository.ops4j.org/maven2",
                        "http://svn.apache.org/repos/asf/servicemix/m2-repo",
                        "http://repository.springsource.com/maven/bundles/release",
                        "http://repository.springsource.com/maven/bundles/external",
                        "http://maven.java.net/content/repositories/snapshots"),

                mavenBundle("org.apache.geronimo.specs", "geronimo-servlet_2.5_spec", "1.1.2"),

                mavenBundle().groupId("javax.ws.rs").artifactId("jsr311-api").versionAsInProject(),

                // load Jersey bundles
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-core").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-server").versionAsInProject(),

                // customize the export header
                new Customizer() {
                    @Override
                    public InputStream customizeTestProbe(InputStream testProbe) throws IOException {
                        return modifyBundle(testProbe).set("Export-Package", this.getClass().getPackage().getName())
                                                      .build();
                    }
                },

                // start felix framework
                felix());
    }

    private static void verifyClassNames() throws ClassNotFoundException {
        Scanner nameScanner = new PackageNamesScanner(new String[]{"com.sun.jersey.osgifunctionaltests"});
        final AnnotationScannerListener asl = new PathProviderScannerListener();
        nameScanner.scan(asl);

        OsgiRegistry instance = OsgiRegistry.getInstance();

        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.JsonResource$NameBean");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.OsgiRegistryTest$2");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.OsgiRegistryTest");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.JsonResource");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.OsgiRegistryTest$1");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.JsonTest");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.SimpleResource");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.BasicOsgiIntegrationTest$SuperSimpleResource");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.JsonTest$1");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.BasicOsgiIntegrationTest$1");
        instance.classForNameWithException("com.sun.jersey.osgifunctionaltests.BasicOsgiIntegrationTest");
    }

    @Test
    public void testScansInMultipleThreads() throws Exception {
        OsgiRegistry instance = OsgiRegistry.getInstance();
        Runnable registryRunner = new Runnable() {
            public void run() {
                try {
                    OsgiRegistryTest.verifyClassNames();
                } catch (ClassNotFoundException e) {
                    failed = true;
                }
            }
        };

        List<Thread> threadList = new ArrayList<Thread>();
        for (int i = 0; i < 200; i++) {
            Thread thread = new Thread(registryRunner);
            threadList.add(thread);
            thread.start();
            Thread.sleep(5);
        }

        for (Thread activeThread : threadList) {
            activeThread.join();
        }

        if (failed) fail();
    }
}
