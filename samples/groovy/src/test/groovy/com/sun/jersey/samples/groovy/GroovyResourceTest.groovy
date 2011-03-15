package com.sun.jersey.samples.groovy

import com.sun.jersey.api.client.Client
import com.sun.jersey.api.container.ContainerFactory
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory
import javax.ws.rs.core.UriBuilder
import org.glassfish.grizzly.http.server.HttpHandler

class GroovyResourceTest extends GroovyTestCase {
    private static getPort(defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    private static final BASE_URI =
        UriBuilder.fromUri("http://localhost/").port(getPort(9998)).build()

    private static startServer() throws IOException {
        def handler = ContainerFactory.createContainer(
                    HttpHandler.class,
                    GroovyResource.class)
        return GrizzlyServerFactory.createHttpServer(BASE_URI, handler)
    }

    private httpServer
    
    private r

    void setUp() {
        super.setUp();

        httpServer = startServer();
        r = Client.create().resource(BASE_URI);
    }

    void tearDown() {
        super.tearDown();

        httpServer.stop();
    }

    void testGet() {
        assertEquals("groovy", r.path("groovy").get(String.class));
    }
}