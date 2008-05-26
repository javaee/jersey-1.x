/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */
package com.sun.jersey.impl.container.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.test.util.TestHelper;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractHttpServerTester extends TestCase {

    public static final String CONTEXT = "/context";
    private HttpServer server;
    private int port = TestHelper.getEnvVariable("JERSEY_HTTP_PORT", 9998);

    public AbstractHttpServerTester(String name) {
        super(name);
    }

    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT);
    }

    public void startServer(Class... resources) {
        start(ContainerFactory.createContainer(HttpHandler.class, resources));
    }

    public void startServer(ResourceConfig config) {
        start(ContainerFactory.createContainer(HttpHandler.class, config));
    }

    public void startServer(String packageName) {
        start(ContainerFactory.createContainer(HttpHandler.class, packageName));
    }

    public void start(HttpHandler handler) {
        if (server != null) {
            stopServer();
        }
        
        // want to make the information available in hudson cli output
        System.out.println("Starting HttpServer port number = " + port);

        URI u = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).
                build();

        try {
            server = HttpServerFactory.create(u, handler);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        server.start();
        System.out.println("Started HttpServer");

        int timeToSleep = TestHelper.getEnvVariable("JERSEY_HTTP_SLEEP", 0);
        if (timeToSleep > 0) {
            System.out.println("Sleeping for " + timeToSleep + " ms");
            try {
                // Wait for the server to start
                Thread.sleep(timeToSleep);
            } catch (InterruptedException ex) {
                System.out.println("Sleeping interrupted: " + ex.getLocalizedMessage());
            }
        }
    }

    public void stopServer() {
        if (server != null) {
            System.out.println("Stopping HttpServer port number = " + server.getAddress().getPort());
            server.stop(TestHelper.getEnvVariable("JERSEY_HTTP_STOPSEC", 0));
            System.out.println("Stopped HttpServer");
        }
    }

    @Override
    public void tearDown() {
        stopServer();
    }
    

}
