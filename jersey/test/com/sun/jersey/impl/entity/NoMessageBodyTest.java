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

package com.sun.jersey.impl.entity;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class NoMessageBodyTest extends AbstractResourceTester {
    public NoMessageBodyTest(String testName) {
        super(testName);
    }
    
    @Path("/")
    public static class NoMessageBodyReaderResource {
        @POST
        public void post(NoMessageBodyReaderResource t) {
        }
    }
    
    public void testNoMessageBodyReaderResource() {
        initiateWebApplication(NoMessageBodyReaderResource.class);        
        
        ClientResponse r = resource("/", false).post(ClientResponse.class);
        assertEquals(415, r.getStatus());
    }
    
    @Path("/")
    public static class NoMessageBodyWriterResource {
        @GET
        public NoMessageBodyWriterResource get() {
            return new NoMessageBodyWriterResource();
        }
    }
    
    public void testNoMessageBodyWriterResource() {
        initiateWebApplication(NoMessageBodyWriterResource.class);        
        
        ClientResponse r = resource("/", false).get(ClientResponse.class);
        assertEquals(406, r.getStatus());
    }
    
}
