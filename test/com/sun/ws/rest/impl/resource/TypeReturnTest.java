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

import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class TypeReturnTest extends AbstractResourceTester {
    
    public TypeReturnTest(String testName) {
        super(testName);
    }
    
    @Path("/")
    static public class Resource { 
        @GET
        public String doGet() {
            return "CONTENT";
        }
    }
    
    @Path("/")
    static public class ResourceWithSingleProduceMime { 
        @GET
        @ProduceMime("text/plain")
        public String doGet() {
            return "CONTENT";
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testReturnType() {
        initiateWebApplication(Resource.class);
        
        ClientResponse response = resource("/", false).get(ClientResponse.class);                
        assertEquals("CONTENT", response.getEntity(String.class));
        assertEquals(MediaType.parse("application/octet-stream"), 
                response.getType());
    }
    
    @SuppressWarnings("unchecked")
    public void testReturnHttpTypeWithSingleProduceMime() {
        initiateWebApplication(ResourceWithSingleProduceMime.class);
        
        ClientResponse response = resource("/", false).get(ClientResponse.class);                
        assertEquals("CONTENT", response.getEntity(String.class));
        assertEquals(MediaType.parse("text/plain"), 
                response.getType());
    }
}
