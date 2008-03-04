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

package com.sun.ws.rest.impl.container.grizzly;

import com.sun.ws.rest.api.client.Client;
import javax.ws.rs.Path;
import com.sun.ws.rest.api.client.WebResource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpMethodTest extends AbstractGrizzlyServerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get() {
            return "GET";
        }
               
        @POST
        public String post(String entity) {
            return entity;
        }
        
        @PUT
        public String put(String entity) {
            return entity;
        }
        
        @DELETE
        public String delete() {
            return "DELETE";
        }    
    }
        
    public HttpMethodTest(String testName) {
        super(testName);
    }
    
    public void testGet() {
        startServer(HttpMethodResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }
    
    public void testPost() {
        startServer(HttpMethodResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));
    }    
    
    public void testPut() {
        startServer(HttpMethodResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("PUT", r.post(String.class, "PUT"));
    }
    
    public void testDelete() {
        startServer(HttpMethodResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class));
    }
    
    public void testAll() {
        startServer(HttpMethodResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));

        r = Client.create().resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));
        
        r = Client.create().resource(getUri().path("test").build());
        assertEquals("PUT", r.post(String.class, "PUT"));
        
        r = Client.create().resource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class));
    }
}
