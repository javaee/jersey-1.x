/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.container.grizzly2;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.test.util.TestHelper;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractGrizzlyServerTester extends TestCase {
    public static final String CONTEXT = "/test";

    private HttpServer httpServer;

    private int port = TestHelper.getEnvVariable("JERSEY_HTTP_PORT", 9997);
    
    public AbstractGrizzlyServerTester(String name) {
        super(name);
    }
    
    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).path("/");
    }
    
    public void startServer(Class... resources) {
        start(ContainerFactory.createContainer(HttpHandler.class, resources));
    }
    
    public void startServer(ResourceConfig config) {
        start(ContainerFactory.createContainer(HttpHandler.class, config));
    }
    
    private void start(HttpHandler httpHandler) {
        if (httpServer != null && httpServer.isStarted()){
            stopServer();
        }

        System.out.println("Starting GrizzlyServer port number = " + port);
        
        URI u = getUri().build();
        try {
            httpServer = GrizzlyServerFactory.createHttpServer(u, httpHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Started GrizzlyServer");

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
        if (httpServer.isStarted()) {
            httpServer.stop();
        }
    }
    
    @Override
    public void tearDown() {
        stopServer();
    }
}
