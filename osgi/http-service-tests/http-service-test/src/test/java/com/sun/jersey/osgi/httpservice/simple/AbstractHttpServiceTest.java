/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.osgi.httpservice.simple;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.BundleContext;

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;

import static org.junit.Assert.assertEquals;


/**
 *
 * @author japod
 */
public abstract class AbstractHttpServiceTest {

    private static final int port = getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final int timeToSleep = getEnvVariable("JERSEY_HTTP_SLEEP", 0);
    private static final String CONTEXT = "/jersey-http-service";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();

    public AbstractHttpServiceTest() {
    }

    @Configuration
    public Option[] configuration() {
                Option[] basicOptions = options(
                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO")
                , systemProperty("org.osgi.service.http.port").value(String.valueOf(port))
                , repositories("http://repo1.maven.org/maven2"
                            , "http://repository.apache.org/content/groups/snapshots-group"
                            , "http://repository.ops4j.org/maven2"
                            , "http://svn.apache.org/repos/asf/servicemix/m2-repo"
                            , "http://repository.springsource.com/maven/bundles/release"
                            , "http://repository.springsource.com/maven/bundles/external"
                            , "http://download.java.net/maven/2")
                , mavenBundle("org.ops4j.pax.logging", "pax-logging-api", "1.4")
                , mavenBundle("org.ops4j.pax.logging", "pax-logging-service", "1.4")
                , mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.2.4")
                , mavenBundle("org.apache.felix", "org.apache.felix.http.bundle", "2.0.4")
                , mavenBundle("javax.ws.rs", "jsr311-api", "1.1.1")
                , mavenBundle("com.sun.jersey", "jersey-core", "1.2-SNAPSHOT")
                , mavenBundle("com.sun.jersey", "jersey-server", "1.2-SNAPSHOT")
                , mavenBundle("com.sun.jersey", "jersey-client", "1.2-SNAPSHOT")
                , mavenBundle("com.sun.jersey.test.osgi.http-service-tests", "http-service-test-bundle", "1.2-SNAPSHOT")
                );

        Option[] customOptions = httpServiceProviderConfiguration();
        
        Option[] allOptions = new Option[basicOptions.length + customOptions.length + 1];
        System.arraycopy(basicOptions, 0, allOptions, 0, basicOptions.length);
        System.arraycopy(customOptions, 0, allOptions, basicOptions.length, customOptions.length);

        allOptions[allOptions.length -1] = felix();

        return allOptions;
    }

    public abstract Option[] httpServiceProviderConfiguration();


    protected void defaultJerseyServletTestMethod() throws Exception {

        timeout();

        WebResource r = resource().path("/status");
        String result = r.get(String.class);
        System.out.println("RESULT = " + result);
        assertEquals("active", result);
    }
    

    protected void defaultNonJerseyServletTestMethod() throws Exception {

        timeout();

        WebResource r = resource().path("../non-jersey-http-service/status");
        String result = r.get(String.class);
        System.out.println("RESULT = " + result);
        assertEquals("also active", result);
    }

    private void timeout() {
        if (timeToSleep > 0) {
            System.out.println("Sleeping for " + timeToSleep + " ms");
            try {
                Thread.sleep(timeToSleep);
            } catch (InterruptedException ex) {
                System.out.println("Sleeping interrupted: " + ex.getLocalizedMessage());
            }
        }
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
    @Inject
    protected BundleContext bundleContext;

    public WebResource resource() {
        final Client c = Client.create();
        final WebResource rootResource = c.resource(baseUri);
        return rootResource;
    }

}
