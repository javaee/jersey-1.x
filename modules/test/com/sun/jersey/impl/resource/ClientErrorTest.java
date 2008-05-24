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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ClientErrorTest extends AbstractResourceTester {
    
    public ClientErrorTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class WebResourceNotFoundMethodNotAllowed {
        @ProduceMime("application/foo")
        @GET
        public String doGet() {
            return "content";
        }
    }
        
    @Path("/")
    public static class WebResourceUnsupportedMediaType {
        @ConsumeMime("application/bar")
        @ProduceMime("application/foo")
        @POST
        public String doPost(String entity) {
            return "content";
        }
    }
    
    public void testNotFound() {
        initiateWebApplication(WebResourceNotFoundMethodNotAllowed.class);
        WebResource r = resource("/foo", false);

        ClientResponse response = r.accept("application/foo").get(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }
    
    public void testMethodNotAllowed() {
        initiateWebApplication(WebResourceNotFoundMethodNotAllowed.class);
        WebResource r = resource("/", false);
        
        ClientResponse response = r.entity("content", "application/foo").
                accept("application/foo").post(ClientResponse.class);
        assertEquals(405, response.getStatus());
        String allow = response.getMetadata().getFirst("Allow").toString();
        assertTrue(allow.contains("GET"));
    }    
    
    public void testUnsupportedMediaType() {
        initiateWebApplication(WebResourceUnsupportedMediaType.class);
        WebResource r = resource("/", false);
        
        ClientResponse response = r.entity("content", "application/foo").
                accept("application/foo").post(ClientResponse.class);
        assertEquals(415, response.getStatus());
    }
    
    public void testNotAcceptable() {
        initiateWebApplication(WebResourceUnsupportedMediaType.class);
        WebResource r = resource("/", false);
        
        ClientResponse response = r.entity("content", "application/bar").
                accept("application/bar").post(ClientResponse.class);
        assertEquals(406, response.getStatus());
    }    
}
