package com.sun.jersey.osgi.servicemix;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;
import org.junit.Before;
import static org.ops4j.pax.exam.CoreOptions.felix;
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
public class JerseyTest {

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
    private static final String CONTEXT = "/osgified-webapp";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();

    final Semaphore semaphore = new Semaphore(0);

    @Inject
    protected BundleContext bundleContext;

    @Before
    public void registerEventHandler() {
        bundleContext.registerService(EventHandler.class.getName(), new WebEventHandler("Deploy Handler"), getHandlerServiceProperties("jersey/test/DEPLOYED"));
    }

    @Test
    public void testWebResources() throws Exception {

        final Bundle httpServiceBundle = bundleContext.installBundle("mvn:com.sun.jersey.test.osgi.osgified-war-tests/osgified-webapp/1.2-SNAPSHOT/war");
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

        Option[] options = options(
                //                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),//"DEBUG"),
                systemProperty("org.osgi.service.http.port").value(String.valueOf(port)),
                systemProperty("org.ops4j.pax.exam.rbc.rmi.port").value(String.valueOf(rmiPort)),
                // define maven repository
                repositories(
                "http://repo1.maven.org/maven2",
                "http://repository.apache.org/content/groups/snapshots-group",
                "http://repository.ops4j.org/maven2",
                "http://svn.apache.org/repos/asf/servicemix/m2-repo",
                "http://repository.springsource.com/maven/bundles/release",
                "http://repository.springsource.com/maven/bundles/external"),
                // felix config admin
                mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.2.4"),
                // felix event admin
                mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.2.2"),
                // load PAX Web bundles
                mavenBundle("org.ops4j.pax.web", "pax-web-jetty-bundle", "0.7.1"),
                mavenBundle("org.ops4j.pax.web", "pax-web-extender-war", "0.7.1"),
                mavenBundle("org.ops4j.pax.url", "pax-url-mvn"),
                // load Jersey bundles
                mavenBundle("javax.ws.rs", "jsr311-api", "1.1.1"),
                mavenBundle("com.sun.jersey", "jersey-core", "1.2-SNAPSHOT"),
                mavenBundle("com.sun.jersey", "jersey-server", "1.2-SNAPSHOT"),
                mavenBundle("com.sun.jersey", "jersey-client", "1.2-SNAPSHOT"),
                // start felix framework
                felix());

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

