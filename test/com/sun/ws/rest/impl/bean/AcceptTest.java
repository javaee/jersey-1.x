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
import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class AcceptTest extends AbstractBeanTester {
    
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
    
    public void testAcceptGet() {
        HttpResponseContext r = callGet(WebResource.class, "/", 
                "application/foo");
        String rep = (String)r.getResponse().getEntity();
        assertEquals("foo", rep);
        
        r = callGet(WebResource.class, "/", 
                "application/foo;q=0.1");
        rep = (String)r.getResponse().getEntity();
        assertEquals("foo", rep);
        
        r = callGet(WebResource.class, "/", 
                "application/foo, application/bar;q=0.4, application/baz;q=0.2");
        rep = (String)r.getResponse().getEntity();
        assertEquals("foo", rep);
        
        r = callGet(WebResource.class, "/", 
                "application/foo;q=0.4, application/bar, application/baz;q=0.2");
        rep = (String)r.getResponse().getEntity();
        assertEquals("bar", rep);
        
        r = callGet(WebResource.class, "/", 
                "application/foo;q=0.4, application/bar;q=0.2, application/baz");
        rep = (String)r.getResponse().getEntity();
        assertEquals("baz", rep);
    }   
    
    public void testAcceptGetWildCard() {
        HttpResponseContext r = callGet(WebResource.class, "/", 
                "application/wildcard, application/foo;q=0.6, application/bar;q=0.4, application/baz;q=0.2");
        String rep = (String)r.getResponse().getEntity();
        assertEquals("wildcard", rep);
    }   
    
    public void testQualityErrorGreaterThanOne() {
        HttpResponseContext response = callNoStatusCheck(
                WebResource.class, "GET", "/", 
                null, "application/foo;q=1.1", "");
        assertEquals(400, response.getResponse().getStatus());        
    }
    
    public void testQualityErrorMoreThanThreeDigits() {
        HttpResponseContext response = callNoStatusCheck(
                WebResource.class, "GET", "/", 
                null, "application/foo;q=0.1234", "");
        assertEquals(400, response.getResponse().getStatus());        
    }
}
