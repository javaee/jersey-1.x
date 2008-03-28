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

package com.sun.ws.rest.impl.container.httpserver;

import javax.ws.rs.Path;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.client.Client;
import com.sun.ws.rest.api.client.WebResource;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpServerAdaptorTest extends AbstractHttpServerTester {
    @Path("/{arg1}/{arg2}")
    public static class TestOneWebResource {
        @Context UriInfo info;
        
        @POST
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            
            assertEquals("a", info.getTemplateParameters().getFirst("arg1"));
            assertEquals("b", info.getTemplateParameters().getFirst("arg2"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-ONE", s);
            
            response.setResponse(Response.ok("RESOURCE-ONE").build());
        }
    }
    
    @Path("/{arg1}")
    public static class TestTwoWebResource {
        @Context UriInfo info;
        
        @POST
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            
            assertEquals("a", info.getTemplateParameters().getFirst("arg1"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-TWO", s);
            
            response.setResponse(Response.ok("RESOURCE-TWO").build());
        }
    }
    
    public HttpServerAdaptorTest(String testName) {
        super(testName);
    }
    
    public void testExplicitWebResourceReference() {
        startServer(TestOneWebResource.class, TestTwoWebResource.class);
        
        WebResource r = Client.create().resource(getUri().path("a").build());
        assertEquals("RESOURCE-TWO", r.post(String.class, "RESOURCE-TWO"));

        r = Client.create().resource(UriBuilder.fromUri(r.getURI()).path("b").build());
        assertEquals("RESOURCE-ONE", r.post(String.class, "RESOURCE-ONE"));
    }
    
    public void testPackageReference() {
        startServer(this.getClass().getPackage().getName());
        
        WebResource r = Client.create().resource(getUri().path("a").build());
        assertEquals("RESOURCE-TWO", r.post(String.class, "RESOURCE-TWO"));

        r = Client.create().resource(UriBuilder.fromUri(r.getURI()).path("b").build());
        assertEquals("RESOURCE-ONE", r.post(String.class, "RESOURCE-ONE"));
    }
}
