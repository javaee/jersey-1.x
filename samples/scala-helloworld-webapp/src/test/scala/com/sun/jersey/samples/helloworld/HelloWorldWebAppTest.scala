package com.sun.jersey.samples.helloworld

import com.sun.jersey.api.client.{Client, WebResource};
import java.io.File;
import java.net.URI;
import java.util.Collections;
import javax.ws.rs.core.UriBuilder;
import junit.framework.{Assert, TestCase};
import org.glassfish.embed.{ScatteredWar, GlassFish};


object HelloWorldWebAppTest {
    def getPort(defaultPort: Int) : Int = {
        val port = System.getenv("JERSEY_HTTP_PORT");

        if (null != port)
            try  {
                Integer.parseInt(port);
            } catch {
                case ex: NumberFormatException => defaultPort;
            }
        else
            defaultPort;
    }

    val baseUri = UriBuilder.
            fromUri("http://localhost/").
            port(getPort(9998)).
            path("scala-helloworld-webapp").build();
}

class HelloWorldWebAppTest extends TestCase {

    var glassfish : GlassFish = _

    var r : WebResource = _

    override def setUp() : Unit = {
        super.setUp();

        // Start Glassfish
        glassfish = new GlassFish(HelloWorldWebAppTest.baseUri.getPort())
        // Deploy Glassfish referencing the web.xml
        val war = new ScatteredWar(HelloWorldWebAppTest.baseUri.getRawPath(),
                new File("src/main/webapp"),
                new File("src/main/webapp/WEB-INF/web.xml"),
                Collections.singleton(new File("target/classes").toURI().toURL()))
        glassfish.deploy(war)

        val c = Client.create();
        r = c.resource(HelloWorldWebAppTest.baseUri)
    }

    override def tearDown() : Unit = {
        super.tearDown()

        glassfish.stop()
    }

    def testHelloWorld() : Unit = {
        val responseMsg = r.path("helloworld").get(classOf[String]);
        Assert.assertEquals("Hello World", responseMsg);
    }
}
