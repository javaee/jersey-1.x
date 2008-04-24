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

package com.sun.ws.rest.impl.container.grizzly.web;

import com.sun.ws.rest.api.client.Client;
import com.sun.ws.rest.api.client.ClientResponse;
import com.sun.ws.rest.api.client.WebResource;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ExceptionTest extends AbstractGrizzlyWebContainerTester {
    @Path("{status}")
    public static class ExceptionResource {
        @GET
        public String get(@PathParam("status") int status) {
            throw new WebApplicationException(status);
        }
               
    }
    
    public ExceptionTest(String testName) {
        super(testName);
    }
    
    public void test400StatusCode() {
        startServer(ExceptionResource.class);

        WebResource r = Client.create().resource(getUri().path("400").build());
        assertEquals(400, r.get(ClientResponse.class).getStatus());
    }
    
    public void test500StatusCode() {
        startServer(ExceptionResource.class);

        WebResource r = Client.create().resource(getUri().path("500").build());
        assertEquals(500, r.get(ClientResponse.class).getStatus());
    }
}
