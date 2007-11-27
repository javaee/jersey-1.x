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
import com.sun.ws.rest.impl.client.ResponseInBound;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;

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
        @HttpMethod("GET")
        @ProduceMime("application/super")
        public String doGet() {
            return "super";
        }
    }
    
    @Path("/")
    static public class SubResource extends SuperResource { 
        @HttpMethod("GET")
        @ProduceMime("application/sub")
        public String doGetSub() {
            return "sub";
        }
    }

    @Path("/")
    static public class SubResourceOverride extends SuperResource { 
        @HttpMethod("GET")
        @ProduceMime("application/sub")
        public String doGet() {
            return "suboverride";
        }
    }

    public void testSuperResourceGet() {
        initiateWebApplication(SubResource.class);
        String s = resourceProxy("/").acceptable("application/super").get(String.class);
        assertEquals("super", s);
    }
    
    public void testSubResourceGet() {
        initiateWebApplication(SubResource.class);
        String s = resourceProxy("/").acceptable("application/sub").get(String.class);
        assertEquals("sub", s);
    }
    
    public void testSuperResourceOverrideGet() {
        initiateWebApplication(SubResourceOverride.class);
        ResponseInBound response = resourceProxy("/", false).acceptable("application/super").
                get(ResponseInBound.class);
        assertEquals(406, response.getStatus());
    }
    
    public void testSubResourceOverrideGet() {
        initiateWebApplication(SubResourceOverride.class);
        String s = resourceProxy("/").acceptable("application/sub").get(String.class);
        assertEquals("suboverride", s);
    }

    
    @ProduceMime("application/default")
    static public abstract class SuperResourceWithProduce { 
        @HttpMethod("GET")
        @ProduceMime("application/super")
        public String doGet() {
            return "super";
        }
    }
    
    @Path("/")
    static public class SubResourceWithProduce extends SuperResourceWithProduce { 
        @HttpMethod("GET")
        public String doGetSub() {
            return "sub";
        }
    }
        
    @Path("/")
    static public class SubResourceWithProduceOverride extends SuperResourceWithProduce { 
        @HttpMethod("GET")
        public String doGet() {
            return "suboverride";
        }
    }
    
    public void testSuperResourceWithProduceGet() {
        initiateWebApplication(SubResourceWithProduce.class);
        String s = resourceProxy("/").acceptable("application/super").get(String.class);
        assertEquals("super", s);
    }
    
    public void testSubResourceWithProduceGet() {
        initiateWebApplication(SubResourceWithProduce.class);
        String s = resourceProxy("/").acceptable("application/default").get(String.class);
        assertEquals("sub", s);
    }
    
    public void testSuperResourceOverrideWithProduceGet() {
        initiateWebApplication(SubResourceWithProduceOverride.class);
        ResponseInBound response = resourceProxy("/", false).acceptable("application/super").
                get(ResponseInBound.class);
        assertEquals(406, response.getStatus());
    }
    
    public void testSubResourceOverrideWithProduceGet() {
        initiateWebApplication(SubResourceWithProduceOverride.class);
        String s = resourceProxy("/").acceptable("application/default").get(String.class);
        assertEquals("suboverride", s);
    }        
}