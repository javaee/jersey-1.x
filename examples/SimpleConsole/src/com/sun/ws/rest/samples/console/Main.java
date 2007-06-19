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

package com.sun.ws.rest.samples.console;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.ws.rest.api.container.ContainerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class,
                "com.sun.ws.rest.samples.console.resources");
        
        HttpServer server = startServerInNewThread("/resources", handler);
        
        System.out.println("Server running, visit: http://127.0.0.1:9998/resources/form, hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");
        
        server.stop(0);
        System.out.println("Server stopped");
    }
    
    private static HttpServer startServerInNewThread(String context, HttpHandler handler) throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(9998), 0);
        server.createContext(context, handler);
        server.setExecutor(null);
        
        Runnable r = new Runnable() {
            public void run() {
                server.start();
            }
        };
        
        new Thread(r).start();
        
        return server;
    }
}
