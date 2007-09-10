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
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.core.WebResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Response;
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
        System.out.println("testExpliciWebResourceReference");
        final Set<Class> resources = new HashSet<Class>();
        resources.add(TestOneWebResource.class);
        resources.add(TestTwoWebResource.class);
        
        ResourceConfig config = new ResourceConfig() {
            public Set<Class> getResourceClasses() {
                return resources;
            }

            public boolean isIgnoreMatrixParams() {
                return true;
            }

            public boolean isRedirectToNormalizedURI() {
                return true;
            }
        };
        
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, config);
        
        HttpServer server = startServerInNewThread("/context", handler, 9998);
        
        post("http://localhost:9998/context/a/b/c", "RESOURCE-ONE");
        post("http://localhost:9998/context/a", "RESOURCE-TWO");
        
        server.stop(1);
        server.removeContext("/context");
    }
    
    public void testPackageReference() throws IOException {
        System.out.println("testPackageReference");
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, this.getClass().getPackage().getName());
        
        HttpServer server = startServerInNewThread("/context", handler, 9999);
        
        post("http://localhost:9999/context/a/b/c", "RESOURCE-ONE");
        post("http://localhost:9999/context/a", "RESOURCE-TWO");
        
        server.stop(1);
        server.removeContext("/context");
    }
    
    private HttpServer startServerInNewThread(String context, HttpHandler handler, int port) throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
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
    
    private void post(String uri, String contents) throws IOException {
        URL u = new URL(uri);
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        uc.setRequestMethod("POST");
        uc.setDoInput(true);
        uc.setDoOutput(true);
        OutputStream out = uc.getOutputStream();
        out.write(contents.getBytes());
        out.close();
        
        assertEquals(200, uc.getResponseCode());
        
        InputStream in = uc.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int r;
        while ((r = in.read(buffer)) != -1) {
            baos.write(buffer, 0, r);
        }
        String s = new String(baos.toByteArray());
        assertEquals(contents, s);
    }    
}
