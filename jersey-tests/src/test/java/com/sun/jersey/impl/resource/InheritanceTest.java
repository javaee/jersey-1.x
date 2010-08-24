/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
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
        @Produces("application/super")
        public String doGet() {
            assertNotNull(info);
            return "super";
        }
    }
    
    @Path("/")
    static public class SubResource extends SuperResource { 
        @Context Request request;
        
        @GET
        @Produces("application/sub")
        public String doGetSub() {
            assertNotNull(request);
            return "sub";
        }
    }

    @Path("/")
    static public class SubResourceOverride extends SuperResource { 
        @GET
        @Produces("application/sub")
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

    
    @Produces("application/default")
    static public abstract class SuperResourceWithProduce { 
        @GET
        @Produces("application/super")
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