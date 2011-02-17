/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.impl.container.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.test.util.JerseyTestHelper;
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
    private int port = JerseyTestHelper.getEnvVariable("JERSEY_HTTP_PORT", 9998);

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

        int timeToSleep = JerseyTestHelper.getEnvVariable("JERSEY_HTTP_SLEEP", 0);
        timeToSleep = 2000;
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
            server.stop(JerseyTestHelper.getEnvVariable("JERSEY_HTTP_STOPSEC", 0));
            System.out.println("Stopped HttpServer");
        }
    }

    @Override
    public void tearDown() {
        stopServer();
    }
    

}
