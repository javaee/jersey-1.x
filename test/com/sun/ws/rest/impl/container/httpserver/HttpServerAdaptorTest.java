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
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.container.ContainerFactory;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.core.WebResource;
import com.sun.ws.rest.impl.client.ResourceProxy;
import java.io.IOException;
import java.net.InetSocketAddress;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpServerAdaptorTest extends TestCase {
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestOneWebResource implements WebResource {
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            
            assertEquals("a", request.getTemplateParameters().getFirst("arg1"));
            assertEquals("b/c", request.getTemplateParameters().getFirst("arg2"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-ONE", s);
            
            response.setResponse(Response.Builder.representation("RESOURCE-ONE").build());
        }
    }
    
    @UriTemplate("/{arg1}")
    public static class TestTwoWebResource implements WebResource {
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            
            assertEquals("a", request.getTemplateParameters().getFirst("arg1"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-TWO", s);
            
            response.setResponse(Response.Builder.representation("RESOURCE-TWO").build());
        }
    }
    
    public HttpServerAdaptorTest(String testName) {
        super(testName);
    }
    
    public void testExpliciWebResourceReference() throws IOException {
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, 
                TestOneWebResource.class, TestTwoWebResource.class);
        
        HttpServer server = HttpServer.create(new InetSocketAddress(9998), 0);
        server.createContext("/context", handler);
        server.start();
        
        ResourceProxy r = ResourceProxy.create("http://localhost:9998/context/a");
        assertEquals("RESOURCE-TWO", r.post(String.class, "RESOURCE-TWO"));

        r = ResourceProxy.create(UriBuilder.fromUri(r.getURI()).path("b/c").build());
        assertEquals("RESOURCE-ONE", r.post(String.class, "RESOURCE-ONE"));
        
        server.stop(0);
    }
    
    public void testPackageReference() throws IOException {
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, 
                this.getClass().getPackage().getName());
        
        HttpServer server = HttpServer.create(new InetSocketAddress(9999), 0);
        server.createContext("/context", handler);
        server.start();
        
        ResourceProxy r = ResourceProxy.create("http://localhost:9999/context/a");
        assertEquals("RESOURCE-TWO", r.post(String.class, "RESOURCE-TWO"));

        r = ResourceProxy.create(UriBuilder.fromUri(r.getURI()).path("b/c").build());
        assertEquals("RESOURCE-ONE", r.post(String.class, "RESOURCE-ONE"));
        
        server.stop(0);
    }
}
