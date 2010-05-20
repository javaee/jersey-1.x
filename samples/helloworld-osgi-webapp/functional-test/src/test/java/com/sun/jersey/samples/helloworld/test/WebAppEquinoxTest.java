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

package com.sun.jersey.samples.helloworld.test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;
import org.junit.Before;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4TestRunner.class)
public class WebAppEquinoxTest {

    public class WebEventHandler implements EventHandler {

        @Override
        public void handleEvent(Event event) {
            semaphore.release();
        }

        public WebEventHandler(String handlerName) {
            this.handlerName = handlerName;
        }
        private final String handlerName;

        protected String getHandlerName() {
            return handlerName;
        }
    }
    
    private static final int port = getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final int rmiPort = getEnvVariable("JERSEY_RMI_PORT", 1099);
    private static final String CONTEXT = "/helloworld";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();
    private static final String BundleLocationProperty = "jersey.bundle.location";

    final Semaphore semaphore = new Semaphore(0);

    @Inject
    protected BundleContext bundleContext;

    @Before
    public void registerEventHandler() {
        bundleContext.registerService(EventHandler.class.getName(), new WebEventHandler("Deploy Handler"), getHandlerServiceProperties("jersey/test/DEPLOYED"));
    }

    @Test
    public void testWebResources() throws Exception {

        final Bundle httpServiceBundle = bundleContext.installBundle(System.getProperty(BundleLocationProperty));
        httpServiceBundle.start();

        semaphore.acquire();

        WebResource r = resource();
        String result = r.path("/webresources/helloworld").get(String.class);

        System.out.println("HELLO RESULT = " + result);
        assertEquals("Hello World", result);

        String result2 = r.path("/webresources/another").get(String.class);

        System.out.println("ANOTHER RESULT = " + result2);
        assertEquals("Another", result2);
    }

    @Configuration
    public static Option[] configuration() {

        String bundleLocation = mavenBundle().groupId("com.sun.jersey.samples.helloworld-osgi-webapp").artifactId("war-bundle").type("war").versionAsInProject().getURL().toString();

        Option[] options = options(
                //                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),//"DEBUG"),
                systemProperty("org.osgi.service.http.port").value(String.valueOf(port)),
                systemProperty(BundleLocationProperty).value(bundleLocation),
                // define maven repository
                repositories(
                "http://repo1.maven.org/maven2",
                "http://repository.apache.org/content/groups/snapshots-group",
                "http://repository.ops4j.org/maven2",
                "http://svn.apache.org/repos/asf/servicemix/m2-repo",
                "http://repository.springsource.com/maven/bundles/release",
                "http://repository.springsource.com/maven/bundles/external"),
                // load PAX Web bundles
                mavenBundle("org.ops4j.pax.web", "pax-web-jetty-bundle", "0.7.1"),
                mavenBundle("org.ops4j.pax.web", "pax-web-extender-war", "0.7.1"),
                mavenBundle("org.ops4j.pax.url", "pax-url-mvn"),
                // equinox event and admin bundles
                mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").version("4.2.0"),
                mavenBundle("org.eclipse.equinox", "event", "1.0.100-v20070516"),
                mavenBundle("org.eclipse.equinox", "cm", "3.2.0-v20070116"),
                // load Jersey bundles
                mavenBundle().groupId("javax.ws.rs").artifactId("jsr311-api").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-core").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-server").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-client").versionAsInProject(),
                // start equinox framework
                equinox());

        return options;
    }

    public WebResource resource() {
        final Client c = Client.create();
        final WebResource rootResource = c.resource(baseUri);
        return rootResource;
    }

    public static int getEnvVariable(final String varName, int defaultValue) {
        if (null == varName) {
            return defaultValue;
        }
        String varValue = System.getenv(varName);
        if (null != varValue) {
            try {
                return Integer.parseInt(varValue);
            } catch (NumberFormatException e) {
                // will return default value bellow
            }
        }
        return defaultValue;
    }

    private Dictionary getHandlerServiceProperties(String... topics) {
         Dictionary result = new Hashtable();  
         result.put(EventConstants.EVENT_TOPIC, topics);  
         return result;  
     }  
       
}

