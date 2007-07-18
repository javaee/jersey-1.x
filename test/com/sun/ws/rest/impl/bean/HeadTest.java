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

import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HeadTest extends AbstractBeanTester {
    
    public HeadTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    static public class ResourceGetNoHead { 
        @HttpMethod
        public String get() {
            return "GET";
        }
    }
        
    public void testGetNoHead() {
        HttpResponseContext r = callNoStatusCheck(ResourceGetNoHead.class, "HEAD", "/", null, null, "");
        assertEquals(200, r.getStatus());
        assertEquals(null, r.getEntity());
    }
    
    @UriTemplate("/")
    static public class ResourceGetWithHead { 
        @HttpMethod
        public Response head() {
            return Response.Builder.ok().header("X-TEST", "HEAD").build();
        }
        
        @HttpMethod
        public Response get() {
            return Response.Builder.representation("GET").header("X-TEST", "GET").build();
        }
    }
    
    public void testGetWithHead() {
        HttpResponseContext r = callNoStatusCheck(ResourceGetWithHead.class, "HEAD", "/", null, null, "");
        assertEquals(200, r.getStatus());
        assertEquals(null, r.getEntity());
        assertEquals("HEAD", r.getHttpHeaders().getFirst("X-TEST").toString());
    }
    
    @UriTemplate("/")
    static public class ResourceGetWithProduceNoHead { 
        @HttpMethod
        @ProduceMime("application/foo")
        public String getFoo() {
            return "FOO";
        }
        
        @HttpMethod
        @ProduceMime("application/bar")
        public String getBar() {
            return "BAR";
        }
    }
    
    public void testGetWithProduceNoHead() {
        HttpResponseContext r = callNoStatusCheck(ResourceGetWithProduceNoHead.class, "HEAD", "/", null, "application/foo", "");
        assertEquals(200, r.getStatus());
        assertEquals(null, r.getEntity());
        assertEquals("application/foo", r.getHttpHeaders().getFirst("Content-Type").toString());
        
        r = callNoStatusCheck(ResourceGetWithProduceNoHead.class, "HEAD", "/", null, "application/bar", "");
        assertEquals(200, r.getStatus());
        assertEquals(null, r.getEntity());
        assertEquals("application/bar", r.getHttpHeaders().getFirst("Content-Type").toString());
    }
    
    @UriTemplate("/")
    static public class ResourceGetWithProduceWithHead { 
        
        @HttpMethod
        @ProduceMime("application/foo")
        public Response headFoo() {
            return Response.Builder.ok().header("X-TEST", "FOO-HEAD").build();
        }
        
        @HttpMethod
        @ProduceMime("application/foo")
        public Response getFoo() {
            return Response.Builder.representation("GET","application/foo").header("X-TEST", "FOO-GET").build();
        }
                
        @HttpMethod
        @ProduceMime("application/bar")
        public Response headBar() {
            return Response.Builder.ok().header("X-TEST", "BAR-HEAD").build();
        }
        
        @HttpMethod
        @ProduceMime("application/bar")
        public Response getBar() {
            return Response.Builder.representation("GET").header("X-TEST", "BAR-GET").build();
        }
    }
    
    public void testGetWithProduceWithHead() {
        HttpResponseContext r = callNoStatusCheck(ResourceGetWithProduceWithHead.class, "HEAD", "/", null, "application/foo", "");
        assertEquals(200, r.getStatus());
        assertEquals(null, r.getEntity());
        assertEquals("application/foo", r.getHttpHeaders().getFirst("Content-Type").toString());
        assertEquals("FOO-HEAD", r.getHttpHeaders().getFirst("X-TEST").toString());
        
        r = callNoStatusCheck(ResourceGetWithProduceWithHead.class, "HEAD", "/", null, "application/bar", "");
        assertEquals(200, r.getStatus());
        assertEquals(null, r.getEntity());
        assertEquals("application/bar", r.getHttpHeaders().getFirst("Content-Type").toString());
        assertEquals("BAR-HEAD", r.getHttpHeaders().getFirst("X-TEST").toString());
    }
}
