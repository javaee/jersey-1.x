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

import com.sun.ws.rest.impl.RequestHttpHeadersImpl;
import java.net.URI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WebResorceWithParameterInjectionTest extends AbstractResourceTester {
    
    public WebResorceWithParameterInjectionTest(String testName) {
        super(testName);
    }
        
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestParameterInjectedUriInfo {
        @HttpMethod("GET")
        public String doGet(@HttpContext UriInfo uriInfo) {
            URI baseUri = uriInfo.getBase();
            URI uri = uriInfo.getAbsolute();
            assertEquals(BASE_URI, baseUri);
            assertEquals(UriBuilder.fromUri(BASE_URI).path("a/b").build(), uri);

            return "GET";
        }        
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestParameterInjectedHttpHeaders {
        @HttpMethod("GET")
        public String doGet(@HttpContext HttpHeaders httpHeaders) {
            String value = httpHeaders.getRequestHeaders().getFirst("X-TEST");
            assertEquals("TEST", value);
            return "GET";
        }        
    }
        
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestParameterInjectedUriInfoHttpHeaders {
        @HttpMethod("GET")
        public String doGet(@HttpContext UriInfo uriInfo, @HttpContext HttpHeaders httpHeaders) {
            String value = httpHeaders.getRequestHeaders().getFirst("X-TEST");
            assertEquals("TEST", value);
            
            URI baseUri = uriInfo.getBase();
            URI uri = uriInfo.getAbsolute();
            assertEquals(BASE_URI, baseUri);
            assertEquals(UriBuilder.fromUri(BASE_URI).path("a/b").build(), uri);
            return "GET";
        }        
    }
    
    public void testParameterInjectedUriInfo() {
        initiateWebApplication(TestParameterInjectedUriInfo.class);
        
        assertEquals("GET", resourceProxy("a/b").get(String.class));
    }
    
    public void testParameterInjectedHttpHeaders() {
        initiateWebApplication(TestParameterInjectedHttpHeaders.class);
        
        assertEquals("GET", resourceProxy("a/b").
                request("X-TEST", "TEST").get(String.class));
    }
    
    public void testParameterInjectedUriInfoHttpHeaders() {
        initiateWebApplication(TestParameterInjectedUriInfoHttpHeaders.class);
        
        assertEquals("GET", resourceProxy("a/b").
                request("X-TEST", "TEST").get(String.class));
    }
}
