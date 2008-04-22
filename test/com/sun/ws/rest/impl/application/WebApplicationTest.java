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

package com.sun.ws.rest.impl.application;

import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.spi.container.AbstractContainerRequest;
import com.sun.ws.rest.spi.container.AbstractContainerResponse;
import com.sun.ws.rest.impl.TestHttpRequestContext;
import com.sun.ws.rest.impl.TestHttpResponseContext;
import com.sun.ws.rest.api.core.DefaultResourceConfig;
import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WebApplicationTest extends TestCase {
    
    public WebApplicationTest(String testName) {
        super(testName);
    }
    
    @Path("/{arg1}/{arg2}")
    public static class TestOneWebResource {
        @Context UriInfo info;
        
        @GET
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            
            assertEquals("a", info.getPathParameters().getFirst("arg1"));
            assertEquals("b", info.getPathParameters().getFirst("arg2"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-ONE", s);
        }
    }
    
    @Path("/{arg1}")
    public static class TestTwoWebResource {
        @Context UriInfo info;
        
        @GET
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            
            System.out.println(info.getPath());
            String v = info.getPathParameters().getFirst("arg1");
            boolean b = v.equals("a") || v.equals("a.foo");
            assertTrue(b);
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-TWO", s);
        }
    }
    
    @Path("/{arg1}.xml")
    public static class TestThreeWebResource {
        @Context UriInfo info;
        
        @GET
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            
            System.out.println(info.getPath());
            assertEquals("a", info.getPathParameters().getFirst("arg1"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-THREE", s);
        }
    }
        
    public void testOneResource() {
        WebApplicationImpl a = createWebApplication(
                TestOneWebResource.class);
        
        call(a, "GET", "/a/b", "RESOURCE-ONE");
    }
    
    public void testTwoResource() {
        WebApplicationImpl a = createWebApplication(
                TestOneWebResource.class, 
                TestTwoWebResource.class);
        
        call(a, "GET", "/a/b", "RESOURCE-ONE");
        call(a, "GET", "/a", "RESOURCE-TWO");
    }
    
    public void testThreeResource() {
        WebApplicationImpl a = createWebApplication(
                TestOneWebResource.class,
                TestTwoWebResource.class,
                TestThreeWebResource.class);
        
        call(a, "GET", "/a/b", "RESOURCE-ONE");
        call(a, "GET", "/a", "RESOURCE-TWO");
        call(a, "GET", "/a.foo", "RESOURCE-TWO");
        call(a, "GET", "/a.xml", "RESOURCE-THREE");
    }

    public void testResourcesWithOnePath() {
        WebApplicationImpl a = createWebApplication(
            com.sun.ws.rest.impl.application.ResourceOne.class,
            com.sun.ws.rest.impl.application.ResourceTwo.class);
        
        call(a, "GET", "/resource1", "RESOURCE-ONE");
        call(a, "GET", "/resource2", "RESOURCE-TWO");
        call(a, "GET", "/resource2", "RESOURCE-TWO");
        call(a, "GET", "/resource1", "RESOURCE-ONE");
        call(a, "GET", "/resource1", "RESOURCE-ONE");
        call(a, "GET", "/resource1", "RESOURCE-ONE");
        call(a, "GET", "/resource2", "RESOURCE-TWO");
        call(a, "GET", "/resource2", "RESOURCE-TWO");
    }
    
    public WebApplicationImpl createWebApplication(Class... resources) {
        final Set<Class<?>> s = new HashSet<Class<?>>();
        for (Class resource : resources)
            s.add(resource);
        
        WebApplicationImpl a = new WebApplicationImpl();
        ResourceConfig c = new DefaultResourceConfig(s);
        a.initiate(c);
        return a;
    }
    
    public void call(WebApplicationImpl a, String method, String path, String content) {
        // Make the path relative to the base URI
        String relativePath = (path.length() > 0 && path.charAt(0) == '/') 
            ? path.substring(1) : path;

        ByteArrayInputStream e = new ByteArrayInputStream(content.getBytes());
        AbstractContainerRequest request = new TestHttpRequestContext(
                a.getMessageBodyContext(), 
                method, 
                e, 
                path, 
                "/");
        AbstractContainerResponse response = new TestHttpResponseContext(
                a.getMessageBodyContext(), 
                request);

        a.handleRequest(request, response);
        if (response.getEntity() != null) {
            assertEquals(200, response.getStatus());        
        } else {            
            assertEquals(204, response.getStatus());        
        }
    }
}
