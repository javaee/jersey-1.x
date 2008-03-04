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
import com.sun.ws.rest.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ProduceMimeAcceptableTest extends AbstractResourceTester {
    
    public ProduceMimeAcceptableTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class WebResource {
        @ProduceMime("application/foo")
        @GET
        public Response doGet() {
            return Response.ok("content", "application/bar").build();
        }
    }
        
    public void testAcceptable() {
        initiateWebApplication(WebResource.class);
        
        resource("/").accept("application/foo", "application/bar").get(String.class);
    }
    
    public void testNotAcceptable() {
        initiateWebApplication(WebResource.class);
        
        ClientResponse response = resource("/", false).
                accept("application/foo").get(ClientResponse.class);
        assertEquals(500, response.getStatus());
    }

    
    @Path("/")
    public static class WebResourceProduceGeneric {
        @ProduceMime("*/*")
        @GET
        public Response doGet() {
            return Response.ok("content", "application/bar").build();
        }
    }
    
    public void testProduceGeneric() {
        initiateWebApplication(WebResourceProduceGeneric.class);
        
        resource("/").accept("application/bar").get(String.class);
    }
    
}
