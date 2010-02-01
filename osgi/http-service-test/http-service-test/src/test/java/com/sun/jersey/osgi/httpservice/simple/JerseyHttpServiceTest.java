package com.sun.jersey.osgi.httpservice.simple;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.UriBuilder;

import java.net.URI;
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
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4TestRunner.class)
public class JerseyHttpServiceTest {

    private static final int port = getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final String CONTEXT = "/jersey-http-service";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();

    @Inject
    protected BundleContext bundleContext;

    @Test
    public void testHello() throws Exception {
        WebResource r = resource().path("/status");
        String result = r.get(String.class);

        System.out.println("RESULT = " + result);
        assertEquals("active", result);
    }


    @Configuration
    public static Option[] configuration() {

        Option[] options = options(
        		
                // this is how you set the default log level when using pax logging (logProfile)
                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),
                systemProperty("org.osgi.service.http.port").value(String.valueOf(port)),
                
                // define maven repository
                repositories(
                        "http://repo1.maven.org/maven2", 
                        "http://repository.apache.org/content/groups/snapshots-group",
                        "http://repository.ops4j.org/maven2",
                        "http://svn.apache.org/repos/asf/servicemix/m2-repo",
                        "http://repository.springsource.com/maven/bundles/release",
                        "http://repository.springsource.com/maven/bundles/external"
                   ),

                // log
                mavenBundle("org.ops4j.pax.logging", "pax-logging-api", "1.4"),
                mavenBundle("org.ops4j.pax.logging", "pax-logging-service", "1.4"),

                //mvn: url handler
                mavenBundle("org.ops4j.pax.url", "pax-url-mvn", "1.1.2"),

                // felix config admin
                mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.2.4"),
                
                // felix preference service
                mavenBundle("org.apache.felix", "org.apache.felix.prefs","1.0.2"),

                // felix http service
                mavenBundle("org.apache.felix", "org.apache.felix.http.bundle", "2.0.4"),
                
                // blueprint
                mavenBundle("org.apache.geronimo.blueprint", "geronimo-blueprint", "1.0.0"),
                
                // bundles
                mavenBundle("org.apache.mina", "mina-core", "2.0.0-RC1"),
                mavenBundle("org.apache.sshd", "sshd-core", "0.3.0"),
                
                // HTTP SPEC
                mavenBundle("org.apache.geronimo.specs","geronimo-servlet_2.5_spec","1.1.2"),
                mavenBundle("org.apache.servicemix.bundles","org.apache.servicemix.bundles.jetty-bundle","6.1.14_2"),
                 
                // load PAX Web bundles
                mavenBundle("org.ops4j.pax.web","pax-web-api", "0.7.1"),
                mavenBundle("org.ops4j.pax.web","pax-web-spi","0.7.1"),
                mavenBundle("org.ops4j.pax.web","pax-web-runtime","0.7.1"),
                mavenBundle("org.ops4j.pax.web","pax-web-jetty","0.7.1"),
                mavenBundle("org.ops4j.pax.web","pax-web-jsp","0.7.1"),
                mavenBundle("org.ops4j.pax.web","pax-web-extender-war","0.7.1"),
                mavenBundle("org.ops4j.pax.web","pax-web-extender-whiteboard","0.7.1"),
                
                // load PAX url war
                mavenBundle("org.ops4j.pax.url","pax-url-war","1.1.2"),
                
                mavenBundle("com.sun.jersey.osgi","jsr311-api","1.2-SNAPSHOT"),
                mavenBundle("com.sun.jersey.osgi","jersey-core","1.2-SNAPSHOT"),
        	mavenBundle("com.sun.jersey.osgi","jersey-server", "1.2-SNAPSHOT"),
        	mavenBundle("com.sun.jersey.osgi","jersey-client", "1.2-SNAPSHOT"),

                mavenBundle("com.sun.jersey.osgi.http-service-test", "jersey-http-service-bundle", "1.2-SNAPSHOT"),

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
            }catch (NumberFormatException e) {
                // will return default value bellow
            }
        }
        return defaultValue;
    }

}

