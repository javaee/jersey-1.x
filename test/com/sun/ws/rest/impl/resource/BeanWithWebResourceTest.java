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

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import com.sun.ws.rest.api.client.WebResource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class BeanWithWebResourceTest extends AbstractResourceTester {
    
    public BeanWithWebResourceTest(String testName) {
        super(testName);
    }
    
    @Path("/{arg1}/{arg2}")
    public static class BeanWithWebResource{
        @GET
        public void doGet(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            
            response.setResponse(Response.ok("RESPONSE").build());
        }
        
        @PUT
        public void putReqWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        @POST
        public void postReqWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        @DELETE
        public void deleteReqWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            String method = request.getHttpMethod();
            
            boolean match = "POST".equals(method) | "DELETE".equals(method) |
                    "PUT".equals(method);
            assertTrue(match);
            
            response.setResponse(Response.ok("RESPONSE").build());
        }
    }
    
    @Path("/{arg1}/{arg2}")
    public static class BeanProduceWithWebResource {
        @GET
        @ProduceMime("text/html")
        public void doGet(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Accept"));
            
            response.setResponse(Response.ok("RESPONSE").build());
        }
        
        //TODO: reunify the following 3 methods once PUT, POST, DELETE annotations are available
        @ProduceMime("text/xhtml")
        @PUT
        public void putRequestWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        @ProduceMime("text/xhtml")
        @POST
        public void postRequestWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        @ProduceMime("text/xhtml")
        @DELETE
        public void deleteRequestWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            String method = request.getHttpMethod();
            boolean match = "POST".equals(method) | "DELETE".equals(method) |
                    "PUT".equals(method);
            assertTrue(match);
            
            String accept = request.getRequestHeaders().getFirst("Accept");
            match = accept == null | "text/xhtml".equals(accept);
            assertTrue(match);
            
            response.setResponse(Response.ok("RESPONSE").build());
        }
    }
    
    public void testBeanWithWebResource() {
        initiateWebApplication(BeanWithWebResource.class);
        WebResource r = resource("/a/b");
        
        r.accept("text/html").get(String.class);
        r.accept("text/xhtml").post();
        r.accept("text/xhtml").put();
        r.delete();
    }
    
    public void testBeanProduceWithWebResource() {
        initiateWebApplication(BeanProduceWithWebResource.class);
        WebResource r = resource("/a/b");
        
        r.accept("text/html").get(String.class);
        r.accept("text/xhtml").post();
        r.accept("text/xhtml").put();
        r.delete();
    }
}
