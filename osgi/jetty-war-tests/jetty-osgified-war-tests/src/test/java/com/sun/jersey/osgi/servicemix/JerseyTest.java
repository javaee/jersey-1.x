package com.sun.jersey.osgi.servicemix;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.provision;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4TestRunner.class)
public class JerseyTest {

    private static MavenArtifactProvisionOption execUrl;
    private static final int port = getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final String CONTEXT = "/helloworld-webapp";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();

    @Inject
    protected BundleContext bundleContext;

    @Test
    public void testHello() throws Exception {
        WebResource r = resource().path("/webresources/helloworld");
        String result = r.get(String.class);

        System.out.println("RESULT = " + result);
        assertEquals("Hello World", result);
    }

    @Test
    public void testAnother() throws Exception {
        WebResource r = resource().path("/webresources/another");
        String result = r.get(String.class);

        System.out.println("RESULT = " + result);
        assertEquals("Another", result);
    }

    @Configuration
    public static Option[] configuration() {

        Option[] options = options(
        		
        		// More info about debug czn be dind here : 
        		// http://wiki.ops4j.org/display/paxexam/Pax+Exam+-+Tutorial+1
                //new VMOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
        		 //vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005" ),
        		
        		// Not necessary if maven repo is installed in the default location
        		//localRepository("file:///c:/.m2/repository"),
        		
                // this is how you set the default log level when using pax logging (logProfile)
                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),//"DEBUG"),
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
                
                // blueprint
                mavenBundle("org.apache.geronimo.blueprint", "geronimo-blueprint", "1.0.0"),
                
                // bundles
                mavenBundle("org.apache.mina", "mina-core", "2.0.0-RC1"),
                mavenBundle("org.apache.sshd", "sshd-core", "0.3.0"),
//                mavenBundle("org.apache.felix.gogo", "org.apache.felix.gogo.runtime","0.2.2"),
//                mavenBundle("org.apache.felix","org.apache.felix.fileinstall","2.0.4"),
                
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
                
                // Load Additional Spring stuffs
//                mavenBundle("org.springframework","spring-web","2.5.6.SEC01"),
//                mavenBundle("org.springframework.osgi","spring-osgi-web","1.2.0"),
                
                mavenBundle("javax.ws.rs","jsr311-api","1.1.1"),
                mavenBundle("com.sun.jersey.osgi","jersey-core","1.2-SNAPSHOT"),
        	mavenBundle("com.sun.jersey.osgi","jersey-server", "1.2-SNAPSHOT"),
        	mavenBundle("com.sun.jersey.osgi","jersey-client", "1.2-SNAPSHOT"),
                //mavenBundle("org.codehaus.jackson","jackson-core-asl","1.2.0"),
		//mavenBundle("org.codehaus.jettison","jettison","1.1"),
		//mavenBundle("com.sun.jersey.osgi","jersey-json","1.1.5-ea-SNAPSHOT"),
//		provision(wrappedBundle(mavenBundle().groupId("com.sun.jersey.contribs").artifactId("jersey-spring").version("1.1.5-ea-SNAPSHOT"))),
				
//			    mavenBundle("org.apache.servicemix.specs","org.apache.servicemix.specs.activation-api-1.1","1.4.0"),
//			    mavenBundle("org.apache.servicemix.specs","org.apache.servicemix.specs.jaxb-api-2.1","1.4.0"),
//			    mavenBundle("org.apache.servicemix.specs","org.apache.servicemix.specs.stax-api-1.0","1.4.0"),
//			    mavenBundle("org.apache.servicemix.bundles","org.apache.servicemix.bundles.jaxb-impl","2.1.12_1"),
//			    mavenBundle("org.fusesource.commonman","commons-management","1.0"),
			    
//			    mavenBundle("org.apache.geronimo.specs","geronimo-jta_1.1_spec/1.1.1"),
//		        mavenBundle("org.springframework","spring-tx","2.5.6.SEC01"),
		        
	        // And finally the WAR
		provision("war:file:../helloworld-webapp/target/helloworld-webapp.war"),

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

