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

package com.sun.jersey.osgi.tests.grizzly;

import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import com.sun.jersey.osgi.tests.util.Helper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Servlet;
import javax.ws.rs.core.UriBuilder;
import org.junit.After;
import org.junit.Before;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;

import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@RunWith(JUnit4TestRunner.class)
public abstract class AbstractGrizzlyWebContainerTester {
    public static final String CONTEXT = "";

    private SelectorThread selectorThread;

    private static int port = Helper.getEnvVariable("JERSEY_HTTP_PORT", 9997);
    
    private String contextPath;

    private Class<? extends Servlet> sc;

    @Configuration
    public static Option[] configuration() {

        Option[] options = options(
                //                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),//"DEBUG"),
                systemProperty("org.osgi.service.http.port").value(String.valueOf(port)),
                // define maven repository
                repositories(
                "http://repo1.maven.org/maven2",
                "http://repository.apache.org/content/groups/snapshots-group",
                "http://repository.ops4j.org/maven2",
                "http://svn.apache.org/repos/asf/servicemix/m2-repo",
                "http://repository.springsource.com/maven/bundles/release",
                "http://repository.springsource.com/maven/bundles/external"),
                // felix config admin
                //mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.2.4"),
                // felix event admin
                //mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.2.2"),
                // load PAX Web bundles
                //mavenBundle("org.ops4j.pax.web", "pax-web-jetty-bundle", "0.7.1"),
                //mavenBundle("org.ops4j.pax.web", "pax-web-extender-war", "0.7.1"),
                //mavenBundle("org.ops4j.pax.url", "pax-url-mvn"),
//                // tiny bundle
//                mavenBundle().groupId("org.ops4j.pax.swissbox").artifactId("pax-swissbox-tinybundles").versionAsInProject(),
                // load grizzly bundle
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-servlet-webserver").versionAsInProject(),
                // load Jersey bundles
                mavenBundle().groupId("javax.ws.rs").artifactId("jsr311-api").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-core").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-server").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-client").versionAsInProject(),

                // customize the export header

                new Customizer() {

                    @Override
                    public InputStream customizeTestProbe(InputStream testProbe)
                            throws IOException {
                        return modifyBundle(testProbe).set("Export-Package", this.getClass().getPackage().getName()).build();
                    }
                },

                // start felix framework
                felix());

        return options;
    }


    public AbstractGrizzlyWebContainerTester() {
        this(CONTEXT);
    }

    protected AbstractGrizzlyWebContainerTester(String contextPath) {
        this.contextPath = contextPath;
    }

    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(port).path(contextPath);
    }
    
    public void setServletClass(Class<? extends Servlet> sc) {
        this.sc = sc;
    }
    
    public void startServer(Class... resources) {
        Map<String, String> initParams = getInitParams(resources);
        start(initParams);
    }
    
    public void startServer(Map<String, String> initParams) {
        start(initParams);
    }
    
    public void startServer(Map<String, String> params, Class... resources) {
        Map<String, String> initParams = getInitParams(resources);
        initParams.putAll(params);
        start(initParams);
    }
    
    private Map<String, String> getInitParams(Class... resources) {
        Map<String, String> initParams = new HashMap<String, String>();
        
        StringBuilder sb = new StringBuilder();
        for (Class r : resources) {            
            if (sb.length() > 0)
                sb.append(';');
            sb.append(r.getName());
        }
        
        initParams.put(ServletContainer.RESOURCE_CONFIG_CLASS, 
                ClassNamesResourceConfig.class.getName());
                        initParams.put(ClassNamesResourceConfig.PROPERTY_CLASSNAMES, sb.toString());
        return initParams;
    }
    
    private void start(Map<String, String> initParams) {
        if (selectorThread != null && selectorThread.isRunning()){
            stopServer();
        }

        System.out.println("Starting GrizzlyServer port number = " + port);
        
        URI u = getUri().path("/").build();
        try {
            if (sc == null) {
                selectorThread = GrizzlyWebContainerFactory.create(u, initParams);
            } else {
                selectorThread = GrizzlyWebContainerFactory.create(u, sc, initParams);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Started GrizzlyServer");

        int timeToSleep = Helper.getEnvVariable("JERSEY_HTTP_SLEEP", 0);
        if (timeToSleep > 0) {
            System.out.println("Sleeping for " + timeToSleep + " ms");
            try {
                // Wait for the server to start
                Thread.sleep(timeToSleep);
            } catch (InterruptedException ex) {
                System.out.println("Sleeping interrupted: " + ex.getLocalizedMessage());
            }
        }
    }
    
    public void stopServer() {
        if (selectorThread.isRunning()) {
            selectorThread.stopEndpoint();
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        stopServer();
    }
}
