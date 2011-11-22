package com.sun.jersey.oauth.tests;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;
import org.junit.Test;

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
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.MavenUtils.getArtifactVersion;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;
import org.osgi.framework.BundleContext;

import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.*;



@RunWith(JUnit4TestRunner.class)
public class OsgiIntegrationTest {

    private static final Logger LOGGER = Logger.getLogger(OsgiIntegrationTest.class.getName());
    private static final int port = Helper.getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final String CONTEXT = "/jersey";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();

    @Inject
    protected BundleContext bundleContext;

    @Configuration
    public static Option[] configuration() {
        String jerseyVersion = getArtifactVersion("com.sun.jersey", "jersey-core").replaceFirst("[0-9]{8}\\.[0-9]{6}-[0-9]+", "SNAPSHOT");

        Option[] options = options(
                //                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),//"DEBUG"),
                systemProperty("org.osgi.service.http.port").value(String.valueOf(port)),
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
                // load grizzly bundle
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-servlet-webserver").versionAsInProject(),
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-http").versionAsInProject(),
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-framework").versionAsInProject(),
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-rcm").versionAsInProject(),
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-portunif").versionAsInProject(),
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-utils").versionAsInProject(),
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-lzma").versionAsInProject(),
                mavenBundle().groupId("com.sun.grizzly").artifactId("grizzly-http-servlet").versionAsInProject(),

                // asm bundle
                mavenBundle().groupId("asm").artifactId("asm-all").versionAsInProject(),
                // load Jersey bundles
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-core").version(jerseyVersion),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-server").version(jerseyVersion),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-servlet").version(jerseyVersion),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-grizzly").version(jerseyVersion),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-client").version(jerseyVersion),
                mavenBundle().groupId("com.sun.jersey.contribs.jersey-oauth").artifactId("oauth-signature").version(jerseyVersion),
                mavenBundle().groupId("com.sun.jersey.contribs.jersey-oauth").artifactId("oauth-server").version(jerseyVersion),
                mavenBundle().groupId("com.sun.jersey.contribs.jersey-oauth").artifactId("oauth-client").version(jerseyVersion),

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
    public void testIntegration() throws Exception {
        String host = "localhost";
        Server.start(host, port);
        com.sun.jersey.oauth.tests.Client.execute(host, port);
        Server.stop();
    }
}
