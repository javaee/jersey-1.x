package com.sun.jersey.osgifunctionaltests;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.osgi.tests.util.Helper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import static org.junit.Assert.assertEquals;

import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.UriBuilder;

import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.*;


@RunWith(JUnit4TestRunner.class)
public class JsonTest {
    private static final Logger LOGGER = Logger.getLogger(JsonTest.class.getName());

    private static final int port = Helper.getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final String CONTEXT = "/jersey";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();
    
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
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-core").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-server").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-grizzly").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-client").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-json").versionAsInProject(),

                // jersey-json deps
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-core-asl").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-mapper-asl").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-jaxrs").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jettison").artifactId("jettison").versionAsInProject(),

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

    @Test
    public void testSimpleResource() throws Exception {

        GrizzlyWebServer gws = new GrizzlyWebServer(port);

        // Jersey web resources
        ServletAdapter jerseyAdapter = new ServletAdapter();
        jerseyAdapter.addInitParameter("com.sun.jersey.config.property.classnames",
                JsonResource.class.getName());
        jerseyAdapter.addInitParameter("com.sun.jersey.config.property.resourceConfigClass",
                ClassNamesResourceConfig.class.getName());
        jerseyAdapter.setContextPath("/jersey");
        jerseyAdapter.setServletInstance(new ServletContainer());

        gws.addGrizzlyAdapter(jerseyAdapter, new String[] {"/jersey"});

        gws.start();

        WebResource r = resource().path("/json");
        JsonResource.NameBean result = r.accept(MediaType.APPLICATION_JSON).get(JsonResource.NameBean.class);

        System.out.println("RESULT.name = " + result.name);
        assertEquals("Jim", result.name);

        gws.stop();


        assertEquals("one", "one");
    }

    public WebResource resource() {
        final Client c = Client.create();
        final WebResource rootResource = c.resource(baseUri);
        return rootResource;
    }
}
