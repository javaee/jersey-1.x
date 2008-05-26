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

package com.sun.jersey.samples.helloworld;

import java.io.IOException;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.samples.helloworld.resources.HelloWorldResource;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
    
    public static void main(String[] args) throws IOException {
        
        final ResourceConfig resourceConfig = new PackagesResourceConfig( new String[] { HelloWorldResource.class.getPackage().getName() } );
        final HttpHandler container = ContainerFactory.createContainer( HttpHandler.class, resourceConfig );
        
        final HttpServer server = HttpServerFactory.create( "http://localhost:9998/", container );
        server.start();
        
        System.out.println("Server running");
        System.out.println("Visit: http://localhost:9998/helloworld");
        System.out.println("Hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");   
        server.stop(0);
        System.out.println("Server stopped");
    }    
}
