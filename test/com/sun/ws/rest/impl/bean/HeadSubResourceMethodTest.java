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

import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResponseInBound;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HeadSubResourceMethodTest extends AbstractResourceTester {
    
    public HeadSubResourceMethodTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    static public class ResourceGetNoHead {
        @UriTemplate("sub")
        @HttpMethod
        public String get() {
            return "GET";
        }
    }
        
    public void testGetNoHead() {
        initiateWebApplication(ResourceGetNoHead.class);
        
        ResponseInBound response = resourceProxy("/sub", false).
                head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
    }
    
    @UriTemplate("/")
    static public class ResourceGetWithHead { 
        @UriTemplate("sub")
        @HttpMethod
        public Response head() {
            return Response.Builder.ok().header("X-TEST", "HEAD").build();
        }
        
        @UriTemplate("sub")
        @HttpMethod
        public Response get() {
            return Response.Builder.representation("GET").header("X-TEST", "GET").build();
        }
    }
    
    public void testGetWithHead() {
        initiateWebApplication(ResourceGetWithHead.class);
        
        ResponseInBound response = resourceProxy("/sub", false).
                head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals("HEAD", response.getMetadata().getFirst("X-TEST"));
    }
    
    @UriTemplate("/")
    static public class ResourceGetWithProduceNoHead { 
        @UriTemplate("sub")
        @HttpMethod
        @ProduceMime("application/foo")
        public String getFoo() {
            return "FOO";
        }
        
        @UriTemplate("sub")
        @HttpMethod
        @ProduceMime("application/bar")
        public String getBar() {
            return "BAR";
        }
    }
    
    public void testGetWithProduceNoHead() {
        initiateWebApplication(ResourceGetWithProduceNoHead.class);
        ResourceProxy r = resourceProxy("/sub", false);
        
        MediaType foo = new MediaType("application/foo");
        ResponseInBound response = r.acceptable(foo).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getContentType());
        
        MediaType bar = new MediaType("application/bar");
        response = r.acceptable(bar).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getContentType());
    }
    
    @UriTemplate("/")
    static public class ResourceGetWithProduceWithHead { 
        
        @UriTemplate("sub")
        @HttpMethod
        @ProduceMime("application/foo")
        public Response headFoo() {
            return Response.Builder.ok().header("X-TEST", "FOO-HEAD").build();
        }
        
        @UriTemplate("sub")
        @HttpMethod
        @ProduceMime("application/foo")
        public Response getFoo() {
            return Response.Builder.representation("GET","application/foo").header("X-TEST", "FOO-GET").build();
        }
                
        @UriTemplate("sub")
        @HttpMethod
        @ProduceMime("application/bar")
        public Response headBar() {
            return Response.Builder.ok().header("X-TEST", "BAR-HEAD").build();
        }
        
        @UriTemplate("sub")
        @HttpMethod
        @ProduceMime("application/bar")
        public Response getBar() {
            return Response.Builder.representation("GET").header("X-TEST", "BAR-GET").build();
        }
    }
    
    public void testGetWithProduceWithHead() {
        initiateWebApplication(ResourceGetWithProduceWithHead.class);
        ResourceProxy r = resourceProxy("/sub", false);
        
        MediaType foo = new MediaType("application/foo");
        ResponseInBound response = r.acceptable(foo).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getContentType());
        assertEquals("FOO-HEAD", response.getMetadata().getFirst("X-TEST").toString());
        
        MediaType bar = new MediaType("application/bar");
        response = r.acceptable(bar).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getContentType());        
        assertEquals("BAR-HEAD", response.getMetadata().getFirst("X-TEST").toString());
    }
    
    @UriTemplate("/")
    static public class ResourceGetWithProduceNoHeadDifferentSub { 
        @UriTemplate("sub1")
        @HttpMethod
        @ProduceMime("application/foo")
        public String getFoo() {
            return "FOO";
        }
        
        @UriTemplate("sub2")
        @HttpMethod
        @ProduceMime("application/bar")
        public String getBar() {
            return "BAR";
        }
    }
    
    public void testGetWithProduceNoHeadDifferentSub() {
        initiateWebApplication(ResourceGetWithProduceNoHeadDifferentSub.class);
        
        MediaType foo = new MediaType("application/foo");
        ResponseInBound response = resourceProxy("/sub1", false).acceptable(foo).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getContentType());
        
        MediaType bar = new MediaType("application/bar");
        response = resourceProxy("/sub2", false).acceptable(bar).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getContentType());
    }
}
