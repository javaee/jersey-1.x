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

package com.sun.ws.rest.impl.container.grizzly;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.tcp.Adapter;
import com.sun.ws.rest.api.container.ContainerFactory;
import com.sun.ws.rest.api.container.grizzly.GrizzlyServerFactory;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.test.util.TestHelper;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractGrizzlyServerTester extends TestCase {
    public static final String CONTEXT = "";

    private SelectorThread selectorThread;

    private int port = TestHelper.getEnvVariable("JERSEY_HTTP_PORT", 9997);
    
    public AbstractGrizzlyServerTester(String name) {
        super(name);
    }
    
    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT);
    }
    
    public void startServer(Class... resources) {
        start(ContainerFactory.createContainer(Adapter.class, resources));
    }
    
    public void startServer(ResourceConfig config) {
        start(ContainerFactory.createContainer(Adapter.class, config));
    }
    
    private void start(Adapter adapter) {
        if (selectorThread != null && selectorThread.isRunning()){
            stopServer();
        }

        System.out.println("Starting GrizzlyServer port number = " + port);
        
        URI u = UriBuilder.fromUri("http://localhost").port(port).build();
        selectorThread = GrizzlyServerFactory.create(u, adapter);
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
        if (selectorThread.isRunning()) {
            selectorThread.stopEndpoint();
        }
    }
    
    @Override
    public void tearDown() {
        stopServer();
    }
}
