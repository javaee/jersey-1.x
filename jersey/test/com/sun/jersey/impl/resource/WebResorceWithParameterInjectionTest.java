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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
        
    @Path("/{arg1}/{arg2}")
    public static class TestParameterInjectedUriInfo {
        @GET
        public String doGet(@Context UriInfo uriInfo) {
            URI baseUri = uriInfo.getBaseUri();
            URI uri = uriInfo.getAbsolutePath();
            assertEquals(BASE_URI, baseUri);
            assertEquals(UriBuilder.fromUri(BASE_URI).path("a/b").build(), uri);

            return "GET";
        }        
    }
    
    @Path("/{arg1}/{arg2}")
    public static class TestParameterInjectedHttpHeaders {
        @GET
        public String doGet(@Context HttpHeaders httpHeaders) {
            String value = httpHeaders.getRequestHeaders().getFirst("X-TEST");
            assertEquals("TEST", value);
            return "GET";
        }        
    }
        
    @Path("/{arg1}/{arg2}")
    public static class TestParameterInjectedUriInfoHttpHeaders {
        @GET
        public String doGet(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders) {
            String value = httpHeaders.getRequestHeaders().getFirst("X-TEST");
            assertEquals("TEST", value);
            
            URI baseUri = uriInfo.getBaseUri();
            URI uri = uriInfo.getAbsolutePath();
            assertEquals(BASE_URI, baseUri);
            assertEquals(UriBuilder.fromUri(BASE_URI).path("a/b").build(), uri);
            return "GET";
        }        
    }
    
    public void testParameterInjectedUriInfo() {
        initiateWebApplication(TestParameterInjectedUriInfo.class);
        
        assertEquals("GET", resource("a/b").get(String.class));
    }
    
    public void testParameterInjectedHttpHeaders() {
        initiateWebApplication(TestParameterInjectedHttpHeaders.class);
        
        assertEquals("GET", resource("a/b").
                header("X-TEST", "TEST").get(String.class));
    }
    
    public void testParameterInjectedUriInfoHttpHeaders() {
        initiateWebApplication(TestParameterInjectedUriInfoHttpHeaders.class);
        
        assertEquals("GET", resource("a/b").
                header("X-TEST", "TEST").get(String.class));
    }
}
