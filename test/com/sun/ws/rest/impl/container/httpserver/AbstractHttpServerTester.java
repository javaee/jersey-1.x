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

package com.sun.ws.rest.impl.container.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.ws.rest.api.container.ContainerFactory;
import com.sun.ws.rest.api.core.ResourceConfig;
import java.io.IOException;
import java.net.InetSocketAddress;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractHttpServerTester extends TestCase {
    public static final String CONTEXT = "/context";
    
    private HttpServer server;
    private int port = 9998;
    
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
    
    private void start(HttpHandler handler) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException ex) {
            RuntimeException e = new RuntimeException();
            e.initCause(ex);
            throw e;
        }
            
        server.createContext(CONTEXT, handler);
        server.start();        
    }
    
    public void stopServer() {
        server.stop(0);
    }
}
