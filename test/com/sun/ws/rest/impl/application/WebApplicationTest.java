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
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.core.WebResource;
import com.sun.ws.rest.impl.HttpRequestContextImpl;
import com.sun.ws.rest.impl.HttpResponseContextImpl;
import com.sun.ws.rest.impl.TestHttpRequestContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WebApplicationTest extends TestCase {
    
    public WebApplicationTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestOneWebResource implements WebResource {
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            
            assertEquals("a", request.getURIParameters().getFirst("arg1"));
            assertEquals("b/c", request.getURIParameters().getFirst("arg2"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-ONE", s);
        }
    }
    
    @UriTemplate("/{arg1}")
    public static class TestTwoWebResource implements WebResource {
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            
            System.out.println(request.getURIPath());
            String v = request.getURIParameters().getFirst("arg1");
            boolean b = v.equals("a") || v.equals("a.foo");
            assertTrue(b);
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-TWO", s);
        }
    }
    
    @UriTemplate("/{arg1}.xml")
    public static class TestThreeWebResource implements WebResource {
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            
            System.out.println(request.getURIPath());
            assertEquals("a", request.getURIParameters().getFirst("arg1"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-THREE", s);
        }
    }
        
    public void testOneResource() {
        WebApplicationImpl a = createWebApplication(
                TestOneWebResource.class);
        
        call(a, "GET", "/a/b/c", "RESOURCE-ONE");
    }
    
    public void testTwoResource() {
        WebApplicationImpl a = createWebApplication(
                TestOneWebResource.class, 
                TestTwoWebResource.class);
        
        call(a, "GET", "/a/b/c", "RESOURCE-ONE");
        call(a, "GET", "/a", "RESOURCE-TWO");
    }
    
    public void testThreeResource() {
        WebApplicationImpl a = createWebApplication(
                TestOneWebResource.class,
                TestTwoWebResource.class,
                TestThreeWebResource.class);
        
        call(a, "GET", "/a/b/c", "RESOURCE-ONE");
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
        final Set<Class> s = new HashSet<Class>();
        for (Class resource : resources)
            s.add(resource);
        
        WebApplicationImpl a = new WebApplicationImpl();
        ResourceConfig c = new ResourceConfig() {
            public Set<Class> getResourceClasses() {
                return s;
            }

            public boolean isIgnoreMatrixParams() {
                return true;
            }

            public boolean isRedirectToNormalizedURI() {
                return true;
            }
        };
        a.initiate(null, c, null);
        return a;
    }
    
    public void call(WebApplicationImpl a, String method, String path, String content) {
        // Make the path relative to the base URI
        String relativePath = (path.length() > 0 && path.charAt(0) == '/') 
            ? path.substring(1) : path;

        ByteArrayInputStream e = new ByteArrayInputStream(content.getBytes());
        HttpRequestContextImpl request = new TestHttpRequestContext(method, e, 
                path, "/", relativePath);
        HttpResponseContextImpl response = new HttpResponseContextImpl(request) {
            public OutputStream getOutputStream() throws IOException {
                throw new UnsupportedOperationException();
            }
        };

        a.handleRequest(request, response);
        if (response.getEntity() != null) {
            assertEquals(200, response.getStatus());        
        } else {            
            assertEquals(204, response.getStatus());        
        }
    }
}
