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

import com.sun.ws.rest.api.Entity;
import com.sun.ws.rest.api.core.HttpContextAccess;
import com.sun.ws.rest.impl.RequestHttpHeadersImpl;
import java.net.URI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WebResorceWithFieldInjectionTest extends AbstractBeanTester {
    
    public WebResorceWithFieldInjectionTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestFieldInjectedHttpContextAccess {
        private @HttpContext HttpContextAccess context;
        
        @HttpMethod("POST")
        public String doPost(Entity<String> in) {
            assertEquals("BEAN-ONE", in.getContent());
            String method = context.getHttpRequestContext().getHttpMethod();
            assertEquals("POST", method);
            return "RETURN";
        }
        
        @HttpMethod("GET")
        public String doGet() {
            String method = context.getHttpRequestContext().getHttpMethod();
            assertEquals("GET", method);
            return "RETURN";
        }
        
        @HttpMethod("PUT")
        public String doPut(Entity<String> in) {
            assertEquals("BEAN-ONE", in.getContent());
            String method = context.getHttpRequestContext().getHttpMethod();
            assertEquals("PUT", method);
            return "RETURN";
        }
        
        @HttpMethod("DELETE")
        public String doDelete() {
            String method = context.getHttpRequestContext().getHttpMethod();
            assertEquals("DELETE", method);
            return "RETURN";
        }
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestFieldInjectedUriInfo {
        private @HttpContext UriInfo uriInfo;
        
        @HttpMethod("GET")
        public String doGet() {
            URI baseUri = uriInfo.getBaseURI();
            URI uri = uriInfo.getURI();
            assertEquals("/base/a/b", uri.toString());
            return "RETURN";
        }        
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestFieldInjectedHttpHeaders {
        private @HttpContext HttpHeaders httpHeaders;
        
        @HttpMethod("GET")
        public String doGet() {
            String value = httpHeaders.getRequestHeaders().getFirst("X-TEST");
            assertEquals("TEST", value);
            return "RETURN";
        }        
    }
    
    public void testFieldInjectedHttpContextAccess() {
        Class r = TestFieldInjectedHttpContextAccess.class;
        call(r, "POST", "/a/b", null, null, "BEAN-ONE");
        call(r, "GET", "/a/b", null, null, "BEAN-ONE");
        call(r, "PUT", "/a/b", null, null, "BEAN-ONE");
        call(r, "DELETE", "/a/b", null, null, "BEAN-ONE");
    }
    
    public void testFieldInjectedUriInfo() {
        Class r = TestFieldInjectedUriInfo.class;
        call(r, "GET", "/a/b", null, null, "BEAN-ONE");
    }
    
    public void testFieldInjectedHttpHeaders() {
        Class r = TestFieldInjectedHttpHeaders.class;
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("X-TEST", "TEST");
        call(r, "GET", "/a/b", headers, "BEAN-ONE");
    }
}
