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

import com.sun.ws.rest.api.core.HttpContextAccess;
import com.sun.ws.rest.impl.client.ResourceProxy;
import java.net.URI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WebResorceWithFieldInjectionTest extends AbstractResourceTester {
    
    public WebResorceWithFieldInjectionTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestFieldInjectedHttpContextAccess {
        private @HttpContext HttpContextAccess context;
        
        @HttpMethod("POST")
        public String doPost(String in) {
            assertEquals("BEAN-ONE", in);
            String method = context.getHttpRequestContext().getHttpMethod();
            assertEquals("POST", method);
            return "POST";
        }
        
        @HttpMethod("GET")
        public String doGet() {
            String method = context.getHttpRequestContext().getHttpMethod();
            assertEquals("GET", method);
            return "GET";
        }
        
        @HttpMethod("PUT")
        public String doPut(String in) {
            assertEquals("BEAN-ONE", in);
            String method = context.getHttpRequestContext().getHttpMethod();
            assertEquals("PUT", method);
            return "PUT";
        }
        
        @HttpMethod("DELETE")
        public String doDelete() {
            String method = context.getHttpRequestContext().getHttpMethod();
            assertEquals("DELETE", method);
            return "DELETE";
        }
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestFieldInjectedUriInfo {
        private @HttpContext UriInfo uriInfo;
        
        @HttpMethod("GET")
        public String doGet() {
            URI baseUri = uriInfo.getBase();
            URI uri = uriInfo.getAbsolute();
            assertEquals(BASE_URI, baseUri);
            assertEquals(UriBuilder.fromUri(BASE_URI).path("a/b").build(), uri);
            return "GET";
        }        
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestFieldInjectedHttpHeaders {
        private @HttpContext HttpHeaders httpHeaders;
        
        @HttpMethod("GET")
        public String doGet() {
            String value = httpHeaders.getRequestHeaders().getFirst("X-TEST");
            assertEquals("TEST", value);
            return "GET";
        }        
    }
    
    public void testFieldInjectedHttpContextAccess() {
        initiateWebApplication(TestFieldInjectedHttpContextAccess.class);
        
        ResourceProxy r = resourceProxy("a/b");
        
        assertEquals("POST", r.post(String.class, "BEAN-ONE"));
        assertEquals("GET", r.get(String.class));
        assertEquals("PUT", r.put(String.class, "BEAN-ONE"));
        assertEquals("DELETE", r.delete(String.class, "BEAN-ONE"));
    }
    
    public void testFieldInjectedUriInfo() {
        initiateWebApplication(TestFieldInjectedUriInfo.class);
        
        assertEquals("GET", resourceProxy("a/b").get(String.class));
    }
    
    public void testFieldInjectedHttpHeaders() {
        initiateWebApplication(TestFieldInjectedHttpHeaders.class);
        
        assertEquals("GET", resourceProxy("a/b").
                request("X-TEST", "TEST").get(String.class));
    }
}
