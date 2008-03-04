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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class InheritanceTest extends AbstractResourceTester {
    
    public InheritanceTest(String testName) {
        super(testName);
    }

    static public abstract class SuperResource {
        @Context UriInfo info;
        
        @GET
        @ProduceMime("application/super")
        public String doGet() {
            assertNotNull(info);
            return "super";
        }
    }
    
    @Path("/")
    static public class SubResource extends SuperResource { 
        @Context Request request;
        
        @GET
        @ProduceMime("application/sub")
        public String doGetSub() {
            assertNotNull(request);
            return "sub";
        }
    }

    @Path("/")
    static public class SubResourceOverride extends SuperResource { 
        @GET
        @ProduceMime("application/sub")
        public String doGet() {
            return "suboverride";
        }
    }

    public void testSuperResourceGet() {
        initiateWebApplication(SubResource.class);
        String s = resource("/").accept("application/super").get(String.class);
        assertEquals("super", s);
    }
    
    public void testSubResourceGet() {
        initiateWebApplication(SubResource.class);
        String s = resource("/").accept("application/sub").get(String.class);
        assertEquals("sub", s);
    }
    
    public void testSuperResourceOverrideGet() {
        initiateWebApplication(SubResourceOverride.class);
        ClientResponse response = resource("/", false).accept("application/super").
                get(ClientResponse.class);
        assertEquals(406, response.getStatus());
    }
    
    public void testSubResourceOverrideGet() {
        initiateWebApplication(SubResourceOverride.class);
        String s = resource("/").accept("application/sub").get(String.class);
        assertEquals("suboverride", s);
    }

    
    @ProduceMime("application/default")
    static public abstract class SuperResourceWithProduce { 
        @GET
        @ProduceMime("application/super")
        public String doGet() {
            return "super";
        }
    }
    
    @Path("/")
    static public class SubResourceWithProduce extends SuperResourceWithProduce { 
        @GET
        public String doGetSub() {
            return "sub";
        }
    }
        
    @Path("/")
    static public class SubResourceWithProduceOverride extends SuperResourceWithProduce { 
        @GET
        public String doGet() {
            return "suboverride";
        }
    }
    
    public void testSuperResourceWithProduceGet() {
        initiateWebApplication(SubResourceWithProduce.class);
        String s = resource("/").accept("application/super").get(String.class);
        assertEquals("super", s);
    }
    
    public void testSubResourceWithProduceGet() {
        initiateWebApplication(SubResourceWithProduce.class);
        String s = resource("/").accept("application/default").get(String.class);
        assertEquals("sub", s);
    }
    
    public void testSuperResourceOverrideWithProduceGet() {
        initiateWebApplication(SubResourceWithProduceOverride.class);
        ClientResponse response = resource("/", false).accept("application/super").
                get(ClientResponse.class);
        assertEquals(406, response.getStatus());
    }
    
    public void testSubResourceOverrideWithProduceGet() {
        initiateWebApplication(SubResourceWithProduceOverride.class);
        String s = resource("/").accept("application/default").get(String.class);
        assertEquals("suboverride", s);
    }        
}