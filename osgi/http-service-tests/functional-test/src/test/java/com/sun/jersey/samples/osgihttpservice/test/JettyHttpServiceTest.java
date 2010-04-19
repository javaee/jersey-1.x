package com.sun.jersey.samples.osgihttpservice.test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;
import javax.ws.rs.core.UriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;

import static org.junit.Assert.assertEquals;


@RunWith(JUnit4TestRunner.class)
public class JettyHttpServiceTest {

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

    final Semaphore semaphore = new Semaphore(0);

    private static final int port = getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final String CONTEXT = "/jersey-http-service";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();

    @Inject
    BundleContext bundleContext;

    @Configuration
    public Option[] configuration() {
                Option[] options = options(
                systemProperty("org.osgi.service.http.port").value(String.valueOf(port))
                , repositories("http://repo1.maven.org/maven2"
                            , "http://repository.apache.org/content/groups/snapshots-group"
                            , "http://repository.ops4j.org/maven2"
                            , "http://svn.apache.org/repos/asf/servicemix/m2-repo"
                            , "http://repository.springsource.com/maven/bundles/release"
                            , "http://repository.springsource.com/maven/bundles/external"
                            , "http://download.java.net/maven/2")

                , mavenBundle("org.ops4j.pax.url", "pax-url-mvn")

                , mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.2.4")
                , mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.2.2")
                , mavenBundle("org.ops4j.pax.web", "pax-web-jetty-bundle", "0.7.1")
                , mavenBundle("javax.ws.rs", "jsr311-api", "1.1.1")
                , mavenBundle("com.sun.jersey", "jersey-core", "1.2-SNAPSHOT")
                , mavenBundle("com.sun.jersey", "jersey-server", "1.2-SNAPSHOT")
                , mavenBundle("com.sun.jersey", "jersey-client", "1.2-SNAPSHOT")
                ,felix());

        return options;
    }

    @Before
    public void registerEventHandler() {
        bundleContext.registerService(EventHandler.class.getName(), new WebEventHandler("Deploy Handler"), getHandlerServiceProperties("jersey/test/DEPLOYED"));
    }

    @Test
    public void testServlets() throws Exception {

        bundleContext.installBundle("mvn:com.sun.jersey.samples.osgi-http-service/bundle/1.2-SNAPSHOT").start();

        semaphore.acquire();  // wait till the servlet gets really registered

        final WebResource r = resource();

        String result = r.path("/status").get(String.class);
        System.out.println("JERSEY RESULT = " + result);
        assertEquals("active", result);
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

    public WebResource resource() {
        final Client c = Client.create();
        final WebResource rootResource = c.resource(baseUri);
        return rootResource;
    }

    private Dictionary getHandlerServiceProperties(String... topics) {
         Dictionary result = new Hashtable();
         result.put(EventConstants.EVENT_TOPIC, topics);
         return result;
     }
}

