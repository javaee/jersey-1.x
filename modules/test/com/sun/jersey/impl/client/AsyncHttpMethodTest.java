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

package com.sun.jersey.impl.client;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.impl.container.grizzly.*;
import com.sun.jersey.api.client.Client;
import javax.ws.rs.Path;
import java.util.concurrent.Future;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AsyncHttpMethodTest extends AbstractGrizzlyServerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get() {
            sleep();
            return "GET";
        }
               
        @POST
        public String post(String entity) {
            sleep();
            return entity;
        }
        
        @PUT
        public String put(String entity) {
            sleep();
            return entity;
        }
        
        @DELETE
        public String delete() {
            sleep();
            return "DELETE";
        }    
        
        private void sleep() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }
        
    public AsyncHttpMethodTest(String testName) {
        super(testName);
    }
    
    public void testGet() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class).get());
    }
    
    public void testPost() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST").get());
    }    
    
    public void testPut() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT").get());
    }
    
    public void testDelete() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class).get());
    }
    
    public void testAllSequential() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class).get());

        r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST").get());
        
        r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT").get());
        
        r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class).get());
    }    
    
    public void testAllParallel() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        
        Future<String> get = r.get(String.class);
        Future<String> post = r.post(String.class, "POST");
        Future<String> put = r.put(String.class, "PUT");
        Future<String> delete =  r.delete(String.class);
        
        assertEquals("GET", get.get());
        assertEquals("POST", post.get());
        assertEquals("PUT", put.get());        
        assertEquals("DELETE", delete.get());
    }    
}