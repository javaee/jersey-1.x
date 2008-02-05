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
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ClientResponse;
import javax.ws.rs.Path;
import java.io.IOException;
import javax.ws.rs.GET;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class RootResourceTest extends AbstractResourceTester {
    
    public RootResourceTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static abstract class AbstractResource {
        @GET
        public String get() {
            return "foo";
        }
        
    }
    
    public void testAbstractResource() throws IOException {
        initiateWebApplication(AbstractResource.class);
        ResourceProxy r = resourceProxy("/", false);
        
        ClientResponse res = r.get(ClientResponse.class);
        assertEquals(404, res.getStatus());
    }   
    
    @Path("/")
    public static interface InterfaceResource {
        @GET
        public String get();        
    }
    
    public void testInterfaceResource() throws IOException {
        initiateWebApplication(InterfaceResource.class);
        ResourceProxy r = resourceProxy("/", false);
        
        ClientResponse res = r.get(ClientResponse.class);
        assertEquals(404, res.getStatus());
    }   
}
