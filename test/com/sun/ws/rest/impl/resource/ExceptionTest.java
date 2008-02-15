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
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ExceptionTest extends AbstractResourceTester {
    
    public ExceptionTest(String testName) {
        super(testName);
    }

    static public class CheckedException extends Exception {
        public CheckedException() {
            super();
        }
    }
    
    @Path("/exception/checked")
    static public class ExceptionCheckedResource { 
        @GET
        public String get() throws CheckedException {
            throw new CheckedException();
        }
    }

    public void testExceptionChecked() {
        initiateWebApplication(ExceptionCheckedResource.class);
        
        boolean caught = false;
        try {
            resourceProxy("/exception/checked").get(ClientResponse.class);
        } catch (ContainerException e) {
            caught = true;
            assertEquals(CheckedException.class, e.getCause().getClass());
        }
    }
    
    @Path("/exception/runtime")
    static public class ExceptionRutimeResource { 
        @GET
        public String get() {
            throw new UnsupportedOperationException();
        }
    }
    
    public void testExceptionRuntime() {
        initiateWebApplication(ExceptionRutimeResource.class);
        
        boolean caught = false;
        try {
            resourceProxy("/exception/runtime").get(ClientResponse.class);
        } catch (UnsupportedOperationException e) {
            caught = true;
        }
        assertEquals(true, caught);
    }
    
    @Path("/exception/webapplication/{status}")
    static public class ExceptionWebApplicationResource { 
        @GET
        public String get(@PathParam("status") int status) {
            throw new WebApplicationException(status);
        }
    }
    
    public void test400StatusCode() {
        initiateWebApplication(ExceptionWebApplicationResource.class);

        ClientResponse cr = resourceProxy("/exception/webapplication/400", false).
                get(ClientResponse.class);        
        assertEquals(400, cr.getStatus());
    }
    
    public void test500StatusCode() {
        initiateWebApplication(ExceptionWebApplicationResource.class);

        ClientResponse cr = resourceProxy("/exception/webapplication/500", false).
                get(ClientResponse.class);        
        assertEquals(500, cr.getStatus());
    }   
}