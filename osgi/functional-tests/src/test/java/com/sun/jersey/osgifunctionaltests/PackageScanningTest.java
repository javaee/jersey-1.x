package com.sun.jersey.osgifunctionaltests;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.osgi.tests.util.Helper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.*;

import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.junit.Test;


@RunWith(JUnit4TestRunner.class)
public class PackageScanningTest {
    private static final Logger LOGGER = Logger.getLogger(PackageScanningTest.class.getName());

    private static final int port = Helper.getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final String CONTEXT = "/jersey";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();
    
    @Inject
    protected BundleContext bundleContext;

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

                // asm bundle
                mavenBundle().groupId("asm").artifactId("asm-all").versionAsInProject(),
                // load grizzly bundle
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-servlet-webserver").versionAsInProject(),
                // load Jersey bundles
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

    @Test
    public void testSimpleResource() throws Exception {

        GrizzlyWebServer gws = new GrizzlyWebServer(port);

        ServletAdapter jerseyAdapter = new ServletAdapter();
        jerseyAdapter.addInitParameter("com.sun.jersey.config.property.packages",
                this.getClass().getPackage().getName());
        jerseyAdapter.setContextPath("/jersey");
        jerseyAdapter.setServletInstance(new ServletContainer());

        gws.addGrizzlyAdapter(jerseyAdapter, new String[] {"/jersey"});

        gws.start();

        WebResource r = resource().path("/simple");
        String result = r.get(String.class);

        System.out.println("RESULT = " + result);
        assertEquals("OK", result);


        gws.stop();
    }

    public WebResource resource() {
        final Client c = Client.create();
        final WebResource rootResource = c.resource(baseUri);
        return rootResource;
    }
}
