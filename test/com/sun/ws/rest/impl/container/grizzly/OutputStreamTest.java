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

import javax.ws.rs.Path;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.ProduceMime;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class OutputStreamTest extends AbstractGrizzlyServerTester {
    @Path("/output")
    public static class TestResource { // implements WebResource {

        @ProduceMime("text/plain")
        @GET
        public void get(HttpRequestContext requestContext, 
                HttpResponseContext responseContext) throws IOException {
            assertEquals("GET", requestContext.getHttpMethod());
            
            responseContext.getOutputStream().
                    write("RESOURCE".getBytes());
        }
        
        @ProduceMime("text/plain")
        @POST
        public void post(HttpRequestContext requestContext, 
                HttpResponseContext responseContext) throws IOException {
            assertEquals("POST", requestContext.getHttpMethod());
            
            String s = requestContext.getEntity(String.class);
            assertEquals("RESOURCE", s);
     
            responseContext.getOutputStream().
                    write("RESOURCE".getBytes());
        }
    }
    
    public OutputStreamTest(String testName) {
        super(testName);
    }
    
    public void testGet() {
        startServer(TestResource.class);
                
        WebResource r = Client.create().resource(getUri().path("output").build());
        assertEquals("RESOURCE", r.get(String.class));        
    }
    
    public void testPost() {        
        startServer(TestResource.class);
                
        WebResource r = Client.create().resource(getUri().path("output").build());
        assertEquals("RESOURCE", r.post(String.class, "RESOURCE"));
    }
    
    public void testAll() {
        startServer(TestResource.class);
                
        WebResource r = Client.create().resource(getUri().path("output").build());
        assertEquals("RESOURCE", r.get(String.class));        
        
        r = Client.create().resource(getUri().path("output").build());
        assertEquals("RESOURCE", r.post(String.class, "RESOURCE"));
    }
}