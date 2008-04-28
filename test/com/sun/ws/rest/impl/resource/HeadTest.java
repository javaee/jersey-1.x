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
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HeadTest extends AbstractResourceTester {
    
    public HeadTest(String testName) {
        super(testName);
    }

    @Path("/")
    static public class ResourceGetNoHead { 
        @GET
        public String get() {
            return "GET";
        }
    }
        
    public void testGetNoHead() {
        initiateWebApplication(ResourceGetNoHead.class);
        
        ClientResponse response = resource("/", false).
                head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
    }
    
    @Path("/")
    static public class ResourceGetWithHead { 
        @HEAD
        public Response head() {
            return Response.ok().header("X-TEST", "HEAD").build();
        }
        
        @GET
        public Response get() {
            return Response.ok("GET").header("X-TEST", "GET").build();
        }
    }
    
    public void testGetWithHead() {
        initiateWebApplication(ResourceGetWithHead.class);
        
        ClientResponse response = resource("/", false).
                head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals("HEAD", response.getMetadata().getFirst("X-TEST"));
    }
    
    @Path("/")
    static public class ResourceGetWithProduceNoHead { 
        @GET
        @ProduceMime("application/foo")
        public String getFoo() {
            return "FOO";
        }
        
        @GET
        @ProduceMime("application/bar")
        public String getBar() {
            return "BAR";
        }
    }
    
    public void testGetWithProduceNoHead() {
        initiateWebApplication(ResourceGetWithProduceNoHead.class);
        WebResource r = resource("/", false);
        
        MediaType foo = MediaType.valueOf("application/foo");
        ClientResponse response = r.accept(foo).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getType());
        
        MediaType bar = MediaType.valueOf("application/bar");
        response = r.accept(bar).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getType());
    }
    
    @Path("/")
    static public class ResourceGetWithProduceWithHead { 
        
        @HEAD
        @ProduceMime("application/foo")
        public Response headFoo() {
            return Response.ok().header("X-TEST", "FOO-HEAD").build();
        }
        
        @GET
        @ProduceMime("application/foo")
        public Response getFoo() {
            return Response.ok("GET","application/foo").header("X-TEST", "FOO-GET").build();
        }
                
        @HEAD
        @ProduceMime("application/bar")
        public Response headBar() {
            return Response.ok().header("X-TEST", "BAR-HEAD").build();
        }
        
        @GET
        @ProduceMime("application/bar")
        public Response getBar() {
            return Response.ok("GET").header("X-TEST", "BAR-GET").build();
        }
    }
    
    public void testGetWithProduceWithHead() {
        initiateWebApplication(ResourceGetWithProduceWithHead.class);
        WebResource r = resource("/", false);
        
        MediaType foo = MediaType.valueOf("application/foo");
        ClientResponse response = r.accept(foo).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getType());
        assertEquals("FOO-HEAD", response.getMetadata().getFirst("X-TEST").toString());
        
        MediaType bar = MediaType.valueOf("application/bar");
        response = r.accept(bar).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getType());        
        assertEquals("BAR-HEAD", response.getMetadata().getFirst("X-TEST").toString());
    }
}
