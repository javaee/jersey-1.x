import com.sun.jersey.api.client.{Client, WebResource};
import java.io.File;
import java.net.URI;
import java.util.Collections;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;
import org.glassfish.embed.{ScatteredWar, GlassFish};

package com.sun.jersey.samples.helloworld {

object HelloWorldWebAppTest {
    def getPort(defaultPort: int) : int = {
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

    // TODO Scala 2.7.2 compiler crashes when WebResource is referred to
    // 
//    var r : WebResource = _

    override def setUp() : unit = {
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
//        r = c.resource(HelloWorldWebAppTest.BASE_URI)
    }

    override def tearDown() : unit = {
        super.tearDown()

        glassfish.stop()
    }

    def testHelloWorld() : unit = {
//        val responseMsg = r.path("helloworld").get(ClassOf[String]);
//        assertEquals("Hello World", responseMsg);
    }

}

}
