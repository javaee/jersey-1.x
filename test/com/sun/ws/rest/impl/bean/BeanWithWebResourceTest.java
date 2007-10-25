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

package com.sun.ws.rest.impl.bean;

import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.WebResource;
import com.sun.ws.rest.impl.client.ResourceProxy;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class BeanWithWebResourceTest extends AbstractResourceTester {
    
    public BeanWithWebResourceTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class BeanWithWebResource implements WebResource {
        @HttpMethod("GET")
        public void doGet(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            
            response.setResponse(Response.Builder.ok("RESPONSE").build());
        }
        
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            String method = request.getHttpMethod();
            
            boolean match = "POST".equals(method) | "DELETE".equals(method) | 
                    "PUT".equals(method);
            assertTrue(match);
            
            response.setResponse(Response.Builder.ok("RESPONSE").build());
        }
    }
        
    @UriTemplate("/{arg1}/{arg2}")
    public static class BeanProduceWithWebResource implements WebResource {
        @HttpMethod("GET")
        @ProduceMime("text/html")
        public void doGet(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Accept"));
            
            response.setResponse(Response.Builder.ok("RESPONSE").build());
        }
        
        @ProduceMime("text/xhtml")
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            String method = request.getHttpMethod();
            boolean match = "POST".equals(method) | "DELETE".equals(method) | 
                    "PUT".equals(method);
            assertTrue(match);
            
            String accept = request.getRequestHeaders().getFirst("Accept");
            match = accept == null | "text/xhtml".equals(accept);
            assertTrue(match);
            
            response.setResponse(Response.Builder.ok("RESPONSE").build());
        }
    }
    
    public void testBeanWithWebResource() {
        initiateWebApplication(BeanWithWebResource.class);
        ResourceProxy r = resourceProxy("/a/b");
        
        r.acceptable("text/html").get(String.class);
        r.acceptable("text/xhtml").post();
        r.acceptable("text/xhtml").put();
        r.delete();
    }    
    
    public void testBeanProduceWithWebResource() {
        initiateWebApplication(BeanProduceWithWebResource.class);
        ResourceProxy r = resourceProxy("/a/b");
        
        r.acceptable("text/html").get(String.class);
        r.acceptable("text/xhtml").post();
        r.acceptable("text/xhtml").put();
        r.delete();        
    }    
}
