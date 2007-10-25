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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResponseInBound;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class AcceptTest extends AbstractResourceTester {
    
    public AcceptTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    public static class WebResource {
        @ProduceMime("application/foo")
        @HttpMethod("GET")
        public String doGetFoo() {
            return "foo";
        }
        
        @ProduceMime("application/bar")
        @HttpMethod("GET")
        public String doGetBar() {
            return "bar";
        }
        
        @ProduceMime("application/baz")
        @HttpMethod("GET")
        public String doGetBaz() {
            return "baz";
        }
        
        @ProduceMime("*/*")
        @HttpMethod("GET")
        public Response doGetWildCard() {
            return Response.Builder.representation("wildcard", "application/wildcard").build();
        }
    }
    
    public void testAcceptGet() throws IOException {
        initiateWebApplication(WebResource.class);
        ResourceProxy r = resourceProxy("/");
        
        String s = r.acceptable("application/foo").get(String.class);
        assertEquals("foo", s);
        
        s = r.acceptable("application/foo;q=0.1").get(String.class);
        assertEquals("foo", s);

        s = r.acceptable("application/foo", "application/bar;q=0.4", "application/baz;q=0.2").
                get(String.class);
        assertEquals("foo", s);
        
        s = r.acceptable("application/foo;q=0.4", "application/bar", "application/baz;q=0.2").
                get(String.class);
        assertEquals("bar", s);
        
        s = r.acceptable("application/foo;q=0.4", "application/bar;q=0.2", "application/baz").
                get(String.class);
        assertEquals("baz", s);
    }   
    
    public void testAcceptGetWildCard() {
        initiateWebApplication(WebResource.class);
        ResourceProxy r = resourceProxy("/");
        
        String s = r.acceptable("application/wildcard", "application/foo;q=0.6", 
                "application/bar;q=0.4", "application/baz;q=0.2").
                get(String.class);
        assertEquals("wildcard", s);
    }   
    
    public void testQualityErrorGreaterThanOne() {
        initiateWebApplication(WebResource.class);
        ResourceProxy r = resourceProxy("/", false);

        ResponseInBound response = r.acceptable("application/foo;q=1.1").get(ResponseInBound.class);
        assertEquals(400, response.getStatus());        
    }
    
    public void testQualityErrorMoreThanThreeDigits() {
        initiateWebApplication(WebResource.class);
        ResourceProxy r = resourceProxy("/", false);
        
        ResponseInBound response = r.acceptable("application/foo;q=0.1234").get(ResponseInBound.class);
        assertEquals(400, response.getStatus());
    }
    
    @UriTemplate("/")
    public static class MultipleResource {
        @ProduceMime({"application/foo", "application/bar"})
        @HttpMethod
        public String get() {
            return "GET";
        }        
    }
    
    public void testAcceptMultiple() {
        initiateWebApplication(MultipleResource.class);
        ResourceProxy r = resourceProxy("/");

        MediaType foo = new MediaType("application/foo");
        MediaType bar = new MediaType("application/bar");
        
        ResponseInBound response = r.acceptable(foo).get(ResponseInBound.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getContentType());
        
        response = r.acceptable(bar).get(ResponseInBound.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(bar, response.getContentType());

        response = r.acceptable("*/*").get(ResponseInBound.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getContentType());

        response = r.acceptable("application/*").get(ResponseInBound.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getContentType());
    }   
}
