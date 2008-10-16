package com.sun.jersey.samples.groovy

import groovy.util.GroovyTestCase

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.tcp.Adapter;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.grizzly.GrizzlyServerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;

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
        def adapter = ContainerFactory.createContainer(
                    Adapter.class,
                    GroovyResource.class)
        return GrizzlyServerFactory.create(BASE_URI, adapter)
    }

    private threadSelector
    
    private r

    void setUp() {
        super.setUp();

        threadSelector = startServer();
        r = Client.create().resource(BASE_URI);
    }

    void tearDown() {
        super.tearDown();

        threadSelector.stopEndpoint();
    }

    void testGet() {
        assertEquals("groovy", r.path("groovy").get(String.class));
    }
}